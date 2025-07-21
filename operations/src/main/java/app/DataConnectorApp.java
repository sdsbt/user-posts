package app;

import impl.DataConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/*
@author Sambhav D Sethia
 */
public class DataConnectorApp {
     private static final String apiUrl = "https://jsonplaceholder.typicode.com";
     private static final String dbURL = "jdbc:postgresql://localhost:5432/practise";
     private static final String dbUser = "postgres";
     private static final String dbPassword = "password";
     private static final int syncIntervalMinutes = 5;
     private static final DataConnector connector = new DataConnector(apiUrl,dbURL,dbUser,dbPassword,syncIntervalMinutes);
     private static final Logger logger = LoggerFactory.getLogger(DataConnector.class);
    public static void main(String[] args) throws InterruptedException {
        try {
            System.out.println("Source: https://jsonplaceholder.typicode.com");
            System.out.println("Destination: PostgreSQL");
            System.out.println("Sync Interval: 5 minutes\n");


            connector.startSync();
            Thread.sleep(10000);
        }
        catch (InterruptedException e) {
            logger.info("Connector interruper");
        }
        catch (Exception e) {
            logger.error("Connector failed: {}", e.getMessage(), e);
        }
        finally {
            connector.stopSync();
        }
    }
}
