# Data Connector

A simplified Java-based data connector that syncs data from JSONPlaceholder API to PostgreSQL database, similar to Fivetran's approach.

## Features

- **Scheduled Sync**: Automatically syncs data at configurable intervals
- **Dependency-Ordered Syncing**: Syncs users → posts → comments to maintain referential integrity
- **Upsert Operations**: Uses ON CONFLICT to handle updates and inserts
- **Sync Logging**: Tracks sync history with timestamps and status
- **Error Handling**: Comprehensive error handling and logging

## Project Structure

```
fivetran-connector/
├── pom.xml                    # Maven configuration
├── setup.sql                 # Database schema setup
├── README.md                 # This file
└── src/app/java/com/connector/
    └── DataConnector.java     # Main connector class
```

## Prerequisites

- Java 11 or higher
- PostgreSQL database
- Maven 3.6 or higher

## Setup

### 1. Database Setup

1. Create a PostgreSQL database:
```sql
CREATE DATABASE connector_db;
```

2. Run the setup script:
```bash
psql -U postgres -d connector_db -f setup.sql
```

### 2. Build the Project

```bash
mvn clean compile
```

### 3. Run the Connector

```bash
mvn exec:java -Dexec.mainClass="com.connector.DataConnector"
```

Or create a JAR file:
```bash
mvn clean package
java -jar target/fivetran-connector-1.0.0.jar
```

## Configuration

Update the database connection details in `DataConnector.java`:

```java
DataConnector connector = new DataConnector(
    "https://jsonplaceholder.typicode.com",  // API URL
    "jdbc:postgresql://localhost:5432/connector_db",  // Database URL
    "postgres",    // Database user
    "password",    // Database password
    5             // Sync interval in minutes
);
```

## Database Schema

The connector creates and manages these tables:

### Data Tables
- **users**: User information from API
- **posts**: Posts with foreign key to users
- **comments**: Comments with foreign key to posts

### Sync Table
- **sync_log**: Simple sync tracking with 4 columns:
  - `id`: Auto-increment primary key
  - `sync_time`: When sync occurred
  - `status`: STARTED, SUCCESS, or FAILED
  - `record_count`: Number of records processed
  - `error_message`: Error details if failed

## Key Simplifications

Compared to the original complex version, this simplified connector:

1. **Reduced sync table** from 9 columns to 4 columns
2. **Eliminated POJOs** - works directly with JSON and SQL
3. **Simplified sync logic** - one method per table
4. **Reduced code** from 1400+ lines to under 400 lines
5. **Removed complex features** like incremental sync, checksums, and detailed monitoring

## Usage Examples

### Check Sync History
The connector automatically displays sync history during startup:

```
=== SYNC HISTORY ===
Time                 Status     Records    Error
----------------------------------------------------------------------
2024-01-15T10:30:15  SUCCESS    600        
2024-01-15T10:25:15  SUCCESS    600        
2024-01-15T10:20:15  FAILED     0          Connection timeout
```

### Monitor Sync Status
Check the sync_log table directly:

```sql
SELECT * FROM sync_log ORDER BY sync_time DESC LIMIT 5;
```

### Verify Data Integrity
```sql
-- Check record counts
SELECT 
    (SELECT COUNT(*) FROM users) as users,
    (SELECT COUNT(*) FROM posts) as posts,
    (SELECT COUNT(*) FROM comments) as comments;

-- Check for orphaned records
SELECT COUNT(*) as orphaned_posts 
FROM posts p 
LEFT JOIN users u ON p.user_id = u.id 
WHERE u.id IS NULL;
```

## Production Considerations

For production use, consider adding:

1. **Configuration file** instead of hardcoded values
2. **Connection pooling** for better performance
3. **Retry logic** for failed API calls
4. **Incremental sync** based on timestamps
5. **Metrics and monitoring** integration
6. **Data validation** and quality checks

## Dependencies

- **Jackson**: JSON processing
- **PostgreSQL JDBC**: Database connectivity
- **SLF4J**: Logging framework
- **JUnit**: Testing (optional)

## License

This project is for educational purposes and demonstrates a simplified data connector pattern.
