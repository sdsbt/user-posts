package impl;

import com.fasterxml.jackson.databind.JsonNode;
import ops.DataSource;
import ops.DatabaseRepository;
import ops.SyncLogger;
import ops.SyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Main data connector implementation
 */
public class DataConnector implements SyncService {
    public static final Logger logger = LoggerFactory.getLogger(DataConnector.class);

    private final int syncIntervalMinutes;
    private final DataSource dataSource;
    private final DatabaseRepository repository;
    private final SyncLogger syncLogger;
    private final ScheduledExecutorService scheduler;

    public DataConnector(String apiUrl, String dbUrl, String dbUser, String dbPassword, int syncIntervalMinutes) {
        this.syncIntervalMinutes = syncIntervalMinutes;
        this.dataSource = new HTTPDataSource(apiUrl);
        PostgresSQLRepository pgRepository = new PostgresSQLRepository(dbUrl, dbUser, dbPassword);
        this.repository = pgRepository;
        this.syncLogger = pgRepository;
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    @Override
    public void startSync() {
        logger.info("Starting sync every {} minutes", syncIntervalMinutes);
        performSync();
        scheduler.scheduleAtFixedRate(this::performSync,
                syncIntervalMinutes, syncIntervalMinutes ,TimeUnit.MINUTES);
    }
    @Override
    public void performSync() {
        logger.info("Starting sync at {}", LocalDateTime.now());
        try{
            syncLogger.logSyncStart();

            int userCount = repository.syncUsers(dataSource.fetchData("/users"));
            int postCount = repository.syncPosts(dataSource.fetchData("/posts"));
            int commentCount = repository.syncComments(dataSource.fetchData("/commens"));

            int totalRecords = userCount + postCount + commentCount;
            syncLogger.logSyncComplete(totalRecords);

            logger.info("Sync comleted - Total records {}",totalRecords);

        }catch (Exception e) {
            logger.error("Sync failed {}",e.getMessage());
            try{
                syncLogger.logSyncError(e.getMessage());
            }catch (SQLException logError){
                logger.error("Failed to sync error: {}",e.getMessage());
            }
        }
    }

    @Override
    public void stopSync() {
        if(scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            logger.info("Sync stopped");
        }
    }
}
