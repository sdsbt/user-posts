package ops;

/**
 * Interface for sync operations
 */
public interface SyncService {
    void startSync();
    void stopSync();
    void performSync();
}
