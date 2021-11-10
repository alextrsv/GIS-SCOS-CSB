package gisscos.studentcard.clients;

import gisscos.studentcard.entities.dto.OrganizationDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

@Component
public class GisScosApiRestClient {

    private final static String GET_ORGANIZATION_ENDPOINT = "organizations";

    RestTemplate restTemplate = new RestTemplate();

    @Value("${gis-scos.host}")
    String host;

    @Value("${gis-scos.prefix}")
    String gisScosPrefix;

    @Value("${vam.X-CN-UUID}")
    private String vamSecret;



    /** 4.1.10.3 Получение объекта записи об организации по global_id организации */
     public synchronized OrganizationDTO makeGetOrganizationRequest(UUID global_id){

        String urlTemplate = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host(host)
                .path(gisScosPrefix)
                .pathSegment(GET_ORGANIZATION_ENDPOINT)
                .queryParam("global_id", global_id)
                .encode()
                .toUriString();

        ResponseEntity<OrganizationDTO> response = restTemplate.exchange(
                urlTemplate,
                HttpMethod.GET,
                buildGisScosRequest(),
                OrganizationDTO.class
        );
        return Objects.requireNonNull(response.getBody(), "organization isn't present");
    }

    private HttpEntity buildGisScosRequest() {
        HttpHeaders headers = new HttpHeaders();

        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("X-CN-UUID", vamSecret);

        return new HttpEntity(headers);
    }
}
