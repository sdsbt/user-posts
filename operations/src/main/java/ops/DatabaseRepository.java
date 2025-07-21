package ops;

import com.fasterxml.jackson.databind.JsonNode;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Interface for database operations
 */
/*
@author Sambhav D Sethia
 */
public interface DatabaseRepository {
    Connection getConnection() throws SQLException;
    int syncUsers(JsonNode users) throws Exception;
    int syncPosts(JsonNode posts) throws Exception;
    int syncComments(JsonNode comments) throws Exception;
}
