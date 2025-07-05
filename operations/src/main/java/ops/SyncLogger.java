package ops;

import java.sql.SQLException;

public interface SyncLogger {
    void logSyncStart() throws SQLException;
    void logSyncComplete(int recordCount) throws SQLException;
    void logSyncError(String error) throws SQLException;
    void showSyncHistory();
}
