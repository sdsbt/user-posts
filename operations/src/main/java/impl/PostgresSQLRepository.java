package impl;

import com.fasterxml.jackson.databind.JsonNode;
import jdk.security.jarsigner.JarSigner;
import ops.DatabaseRepository;
import ops.SyncLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;

public class PostgresSQLRepository implements DatabaseRepository, SyncLogger {
    private static final Logger logger = LoggerFactory.getLogger(PostgresSQLRepository.class);

    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    public PostgresSQLRepository(String dbUrl, String dbUser, String dbPassword) {

        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl,dbUser,dbPassword);
    }

    @Override
    public int syncUsers(JsonNode users) throws SQLException {
        logger.info("Syncing users...",users.size());

        String sql = """
                   insert into users (id, name, username, email, phone, website, last_synced)
                   values (?,?,?,?,?,?,?)
                   ON CONFLICT (id) DO UPDATE SET
                   name = excluded.name,
                   username = excluded.username,
                   email = excluded.email,
                   phone = excluded.phone,
                   website = excluded.website,
                   last_synced = excluded.website
                """;
        try(Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for(JsonNode user : users) {
                pstmt.setInt(1, user.get("id").asInt());
                pstmt.setString(2, user.get("name").asText());
                pstmt.setString(3, user.get("username").asText());
                pstmt.setString(4, user.get("email").asText());
                pstmt.setString(5, getJsonText(user, "phone"));
                pstmt.setString(6, getJsonText(user, "website"));
                pstmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            }
            pstmt.executeBatch();
            return users.size();
        }
    }

    @Override
    public int syncPosts(JsonNode posts) throws Exception {
        logger.info("Syncing posts...",posts.size());

        String sql = """
                insert into posts(id, user_id, title, body, last_synced)
                values(?,?,?,?,?)
                on conflict (id) DO UPDATE SET
                user_id = excluded.user_id,
                title = excluded.title,
                body = excluded.body,
                last_synced = excluded.last_synced
                """;
        try(Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for(JsonNode post : posts) {
                pstmt.setInt(1, post.get("id").asInt());
                pstmt.setInt(2, post.get("userId").asInt());
                pstmt.setString(3, post.get("title").asText());
                pstmt.setString(4, post.get("body").asText());
                pstmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            return  posts.size();
        }
    }
    @Override
    public int syncComments(JsonNode comments) throws Exception {
        logger.info("Syncing comments...",comments.size());

        String sql = """
                insert into comments(id, postId, name, email, body, last_synced)
                values(?,?,?,?,?,?)
                on conflict (id) do update set
                post_id = excluded.post_id,
                name = excluded.name,
                email = excluded.email,
                body = excluded.body,
                last_synced = excluded.last_synced
                """;
            try(Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (JsonNode comment : comments) {
                    pstmt.setInt(1, comment.get("id").asInt());
                    pstmt.setInt(2, comment.get("postId").asInt());
                    pstmt.setString(3, comment.get("name").asText());
                    pstmt.setString(4, comment.get("email").asText());
                    pstmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
                return comments.size();
            }
    }
    @Override
    public void logSyncStart() throws SQLException {
        String sql = "insert into sync_log (sync_time, status) values (?, 'STARTED')";
        try(Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.executeUpdate();
        }
    }
    @Override
    public void logSyncComplete(int recordCount) throws SQLException {
        String sql = """
                        insert into sync_log (sync_time, status, record_count)
                        values (?, 'SUCCESS', ?)
                     """;
        try(Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(2, recordCount);
            pstmt.executeUpdate();
        }
    }

    @Override
    public void logSyncError(String error) throws SQLException {
        String sql = "INSERT INTO sync_log (sync_time, status, error_message) VALUES (?, 'FAILED', ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(2, error);
            pstmt.executeUpdate();
        }
    }

    @Override
    public void showSyncHistory() {

    }

    private String getJsonText(JsonNode node, String field) {
        JsonNode filedNode = node.get(field);
        return (filedNode != null && !filedNode.isNull()) ? filedNode.asText() : null;
    }
}
