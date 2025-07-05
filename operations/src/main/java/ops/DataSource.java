package ops;

import com.fasterxml.jackson.databind.JsonNode;
/**
 * Interface for data source operations
 */
public interface DataSource {
    JsonNode fetchData(String endpoint) throws Exception;
}
