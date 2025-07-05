package impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ops.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HTTPDataSource implements DataSource {
    private static final Logger logger = LoggerFactory.getLogger(HTTPDataSource.class);

    private final String apiUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public HTTPDataSource(String apiUrl) {
        this.apiUrl = apiUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public JsonNode fetchData(String endpoint) throws Exception {
        logger.debug("Fethcing data from: {}{}",apiUrl, endpoint);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl+endpoint))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if(response.statusCode() != 200) {
            throw  new RuntimeException("API request failed with status: "+response.statusCode());
        }
        return objectMapper.readTree(response.body());
    }
}
