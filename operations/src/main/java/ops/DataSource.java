package ops;

import com.fasterxml.jackson.databind.JsonNode;
/**
 * Interface for data source operations
 */
/*
@author Sambhav D Sethia
 */
public interface DataSource {
    JsonNode fetchData(String endpoint) throws Exception;
}
