package backend.academy.scrapper.repository;

import backend.academy.scrapper.entity.Chat;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.enums.LinkStatus;
import backend.academy.scrapper.enums.LinkType;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
public class JdbcLinkRepository implements LinkRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String SQL_INSERT =
            "INSERT INTO link (link_type, url, status, checked_at) VALUES (?, ?, ?, ?)";
    private static final String SQL_SELECT_BY_ID =
            "SELECT id, link_type, url, checked_at, status, last_update_id FROM link WHERE id = ?";
    private static final String SQL_SELECT_BY_URL =
            "SELECT id, link_type, url, checked_at, status, last_update_id FROM link WHERE url = ?";
    private static final String SQL_UPDATE_STATUS = "UPDATE link SET status = ? WHERE id = ?";
    private static final String SQL_UPDATE_CHECKED_AT = "UPDATE link SET checked_at = ? WHERE id = ?";
    private static final String SQL_UPDATE_LAST_UPDATE_ID = "UPDATE link SET last_update_id = ? WHERE id = ?";
    private static final String SQL_FIND_LINKS_TO_UPDATE =
            "SELECT DISTINCT l.id, l.link_type, l.url, l.checked_at, l.status, l.last_update_id " + "FROM link l "
                    + "JOIN chat_link cl ON l.id = cl.link_id "
                    + "WHERE l.checked_at < ? AND l.status = 'ACTIVE' "
                    + "LIMIT ?";

    public JdbcLinkRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void add(Link link) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                conn -> {
                    var ps = conn.prepareStatement(SQL_INSERT, new String[] {"id"});
                    ps.setString(1, link.getLinkType().toString());
                    ps.setString(2, link.getUrl());
                    ps.setString(3, link.getStatus().toString());
                    ps.setTimestamp(4, Timestamp.from(OffsetDateTime.now().toInstant()));
                    return ps;
                },
                keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            link.setId(key.longValue());
        }
    }

    @Override
    public List<Link> findAll() {
        return jdbcTemplate.query(
                "SELECT id, link_type, url, checked_at, status, last_update_id FROM link", linkRowMapper());
    }

    @Override
    public Optional<Link> findById(Long id) {
        List<Link> links = jdbcTemplate.query(SQL_SELECT_BY_ID, linkRowMapper(), id);
        return links.isEmpty() ? Optional.empty() : Optional.of(links.get(0));
    }

    @Override
    public Optional<Link> findByUrl(String url) {
        List<Link> links = jdbcTemplate.query(SQL_SELECT_BY_URL, linkRowMapper(), url);
        return links.isEmpty() ? Optional.empty() : Optional.of(links.get(0));
    }

    @Override
    @Transactional
    public void updateStatus(Long id, LinkStatus status) {
        jdbcTemplate.update(SQL_UPDATE_STATUS, status.toString(), id);
    }

    @Override
    @Transactional
    public void updateCheckedAt(Long id, OffsetDateTime checkedAt) {
        jdbcTemplate.update(SQL_UPDATE_CHECKED_AT, Timestamp.from(checkedAt.toInstant()), id);
    }

    @Override
    @Transactional
    public void updateLastUpdateId(Long id, String lastUpdateId) {
        jdbcTemplate.update(SQL_UPDATE_LAST_UPDATE_ID, lastUpdateId, id);
    }

    @Override
    public List<Link> findLinksToUpdate(OffsetDateTime before, int limit) {
        List<Link> links = jdbcTemplate.query(
                SQL_FIND_LINKS_TO_UPDATE, linkRowMapper(), Timestamp.from(before.toInstant()), limit);

        // Load chats for each link
        links.forEach(link -> {
            List<Chat> chats = jdbcTemplate.query(
                    "SELECT c.id, c.chat_id FROM chat c JOIN chat_link cl ON c.id = cl.chat_id WHERE cl.link_id = ?",
                    (rs, rowNum) -> Chat.builder()
                            .id(rs.getLong("id"))
                            .chatId(rs.getLong("chat_id"))
                            .build(),
                    link.getId());
            link.setChats(Set.copyOf(chats));
        });

        return links;
    }

    public RowMapper<Link> linkRowMapper() {
        return (rs, rowNum) -> mapLink(rs);
    }

    private Link mapLink(ResultSet rs) throws SQLException {
        return Link.builder()
                .id(rs.getLong("id"))
                .linkType(LinkType.valueOf(rs.getString("link_type")))
                .url(rs.getString("url"))
                .checkedAt(rs.getTimestamp("checked_at").toInstant().atOffset(ZoneOffset.UTC))
                .status(LinkStatus.valueOf(rs.getString("status")))
                .lastUpdateId(rs.getString("last_update_id"))
                .build();
    }
}
