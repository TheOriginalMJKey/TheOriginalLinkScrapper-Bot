package backend.academy.scrapper.repository;

import backend.academy.scrapper.entity.Chat;
import backend.academy.scrapper.entity.Link;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Primary
@Repository
@SuppressFBWarnings("OBL_UNSATISFIED_OBLIGATION_EXCEPTION_EDGE")
public class JdbcChatRepository implements ChatRepository {

    private final JdbcTemplate jdbcTemplate;
    private final JdbcLinkRepository linkRepository;

    private static final String SQL_INSERT = "INSERT INTO chat (chat_id) VALUES (?) ON CONFLICT DO NOTHING";
    private static final String SQL_SELECT_BY_ID = "SELECT id, chat_id FROM chat WHERE id = ?";
    private static final String SQL_SELECT_BY_CHAT_ID = "SELECT id, chat_id FROM chat WHERE chat_id = ?";
    private static final String SQL_SELECT_LINKS_BY_CHAT_ID =
            "SELECT l.* FROM link l JOIN chat_link cl ON l.id = cl.link_id WHERE cl.chat_id = ?";
    private static final String SQL_INSERT_CHAT_LINK =
            "INSERT INTO chat_link (chat_id, link_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
    private static final String SQL_DELETE_CHAT_LINK = "DELETE FROM chat_link WHERE chat_id = ? AND link_id = ?";
    private static final String SQL_EXISTS_BY_CHAT_ID = "SELECT EXISTS(SELECT 1 FROM chat WHERE chat_id = ?)";

    public JdbcChatRepository(JdbcTemplate jdbcTemplate, JdbcLinkRepository linkRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.linkRepository = linkRepository;
    }

    @Override
    @Transactional
    public void add(Chat chat) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                conn -> {
                    var ps = conn.prepareStatement(SQL_INSERT, new String[] {"id"});
                    ps.setLong(1, chat.getChatId());
                    return ps;
                },
                keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            chat.setId(key.longValue());
        }
    }

    @Override
    public List<Chat> findAll() {
        return jdbcTemplate.query("SELECT id, chat_id FROM chat", chatRowMapper());
    }

    @Override
    public Optional<Chat> findById(Long id) {
        List<Chat> chats = jdbcTemplate.query(SQL_SELECT_BY_ID, chatRowMapper(), id);
        if (chats.isEmpty()) {
            return Optional.empty();
        }

        Chat chat = chats.get(0);
        loadLinks(chat);
        return Optional.of(chat);
    }

    @Override
    public Optional<Chat> findByChatId(Long chatId) {
        List<Chat> chats = jdbcTemplate.query(SQL_SELECT_BY_CHAT_ID, chatRowMapper(), chatId);
        if (chats.isEmpty()) {
            return Optional.empty();
        }

        Chat chat = chats.get(0);
        loadLinks(chat);
        return Optional.of(chat);
    }

    @Override
    @Transactional
    public void remove(Long chatId) {
        jdbcTemplate.update("DELETE FROM chat WHERE chat_id = ?", chatId);
    }

    @Override
    @Transactional
    public void addLinkToChat(Chat chat, Link link) {
        jdbcTemplate.update(SQL_INSERT_CHAT_LINK, chat.getId(), link.getId());
    }

    @Override
    @Transactional
    public void removeLinkFromChat(Chat chat, Link link) {
        jdbcTemplate.update(SQL_DELETE_CHAT_LINK, chat.getId(), link.getId());
    }

    @Override
    public boolean exists(Long chatId) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(SQL_EXISTS_BY_CHAT_ID, Boolean.class, chatId));
    }

    private void loadLinks(Chat chat) {
        List<Link> links =
                jdbcTemplate.query(SQL_SELECT_LINKS_BY_CHAT_ID, linkRepository.linkRowMapper(), chat.getId());
        chat.setLinks(Set.copyOf(links));
    }

    private RowMapper<Chat> chatRowMapper() {
        return (rs, rowNum) -> mapChat(rs);
    }

    private Chat mapChat(ResultSet rs) throws SQLException {
        return Chat.builder().id(rs.getLong("id")).chatId(rs.getLong("chat_id")).build();
    }
}
