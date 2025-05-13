package backend.academy.scrapper.repository;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkUpdate;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@SuppressFBWarnings("OBL_UNSATISFIED_OBLIGATION_EXCEPTION_EDGE")
public class JdbcLinkUpdateRepository {

    private final JdbcTemplate jdbcTemplate;
    private final JdbcLinkRepository linkRepository;

    private static final String SQL_INSERT =
            "INSERT INTO link_update (link_id, update_id, update_type, title, username, created_at, description_preview, processed) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_SELECT_BY_ID =
            "SELECT id, link_id, update_id, update_type, title, username, created_at, description_preview, processed "
                    + "FROM link_update WHERE id = ?";
    private static final String SQL_SELECT_BY_LINK_AND_UPDATE_ID =
            "SELECT id, link_id, update_id, update_type, title, username, created_at, description_preview, processed "
                    + "FROM link_update WHERE link_id = ? AND update_id = ?";
    private static final String SQL_SELECT_UNPROCESSED_BY_LINK =
            "SELECT id, link_id, update_id, update_type, title, username, created_at, description_preview, processed "
                    + "FROM link_update WHERE link_id = ? AND processed = false ORDER BY created_at LIMIT ?";
    private static final String SQL_MARK_AS_PROCESSED = "UPDATE link_update SET processed = true WHERE id = ?";

    public JdbcLinkUpdateRepository(JdbcTemplate jdbcTemplate, JdbcLinkRepository linkRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.linkRepository = linkRepository;
    }

    @Transactional
    public void save(LinkUpdate linkUpdate) {
        if (linkUpdate.getId() != null) {
            updateLinkUpdate(linkUpdate);
        } else {
            insertLinkUpdate(linkUpdate);
        }
    }

    @Transactional
    public void insertLinkUpdate(LinkUpdate linkUpdate) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                conn -> {
                    var ps = conn.prepareStatement(SQL_INSERT, new String[] {"id"});
                    ps.setLong(1, linkUpdate.getLink().getId());
                    ps.setString(2, linkUpdate.getUpdateId());
                    ps.setString(3, linkUpdate.getUpdateType());
                    ps.setString(4, linkUpdate.getTitle());
                    ps.setString(5, linkUpdate.getUsername());
                    ps.setTimestamp(6, Timestamp.from(linkUpdate.getCreatedAt().toInstant()));
                    ps.setString(7, linkUpdate.getDescriptionPreview());
                    ps.setBoolean(8, linkUpdate.isProcessed());
                    return ps;
                },
                keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            linkUpdate.setId(key.longValue());
        }
    }

    @Transactional
    public void updateLinkUpdate(LinkUpdate linkUpdate) {
        jdbcTemplate.update(
                "UPDATE link_update SET update_type = ?, title = ?, username = ?, "
                        + "created_at = ?, description_preview = ?, processed = ? WHERE id = ?",
                linkUpdate.getUpdateType(),
                linkUpdate.getTitle(),
                linkUpdate.getUsername(),
                Timestamp.from(linkUpdate.getCreatedAt().toInstant()),
                linkUpdate.getDescriptionPreview(),
                linkUpdate.isProcessed(),
                linkUpdate.getId());
    }

    public Optional<LinkUpdate> findById(Long id) {
        List<LinkUpdate> updates = jdbcTemplate.query(SQL_SELECT_BY_ID, linkUpdateRowMapper(), id);
        return updates.isEmpty() ? Optional.empty() : Optional.of(updates.get(0));
    }

    public Optional<LinkUpdate> findByLinkAndUpdateId(Link link, String updateId) {
        List<LinkUpdate> updates =
                jdbcTemplate.query(SQL_SELECT_BY_LINK_AND_UPDATE_ID, linkUpdateRowMapper(), link.getId(), updateId);
        return updates.isEmpty() ? Optional.empty() : Optional.of(updates.get(0));
    }

    public List<LinkUpdate> findUnprocessedByLink(Link link, int limit) {
        return jdbcTemplate.query(SQL_SELECT_UNPROCESSED_BY_LINK, linkUpdateRowMapper(), link.getId(), limit);
    }

    @Transactional
    public void markAsProcessed(Long id) {
        jdbcTemplate.update(SQL_MARK_AS_PROCESSED, id);
    }

    private RowMapper<LinkUpdate> linkUpdateRowMapper() {
        return (rs, rowNum) -> mapLinkUpdate(rs);
    }

    private LinkUpdate mapLinkUpdate(ResultSet rs) throws SQLException {
        Long linkId = rs.getLong("link_id");
        Link link = linkRepository
                .findById(linkId)
                .orElseThrow(() -> new IllegalStateException("Link not found with ID: " + linkId));

        return LinkUpdate.builder()
                .id(rs.getLong("id"))
                .link(link)
                .updateId(rs.getString("update_id"))
                .updateType(rs.getString("update_type"))
                .title(rs.getString("title"))
                .username(rs.getString("username"))
                .createdAt(rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC))
                .descriptionPreview(rs.getString("description_preview"))
                .processed(rs.getBoolean("processed"))
                .build();
    }
}
