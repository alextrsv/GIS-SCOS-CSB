package gisscos.studentcard.components;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class FrontendHealthIndicator implements HealthIndicator {
    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public Health health() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(frontendUrl))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 200){
                return Health.up().build();
            }
        } catch (IOException | InterruptedException ignored) {
        }

        return Health.down().build();
    }

}
