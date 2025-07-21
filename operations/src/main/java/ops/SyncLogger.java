package ops;

import java.sql.SQLException;

/*
@author Sambhav D Sethia
 */
public interface SyncLogger {
    void logSyncStart() throws SQLException;
    void logSyncComplete(int recordCount) throws SQLException;
    void logSyncError(String error) throws SQLException;
    void showSyncHistory();
}
