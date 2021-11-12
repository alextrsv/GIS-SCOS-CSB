package gisscos.studentcard.clients;

import gisscos.studentcard.entities.dto.UserDTO;
import gisscos.studentcard.entities.dto.OrganizationDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
public class GisScosApiRestClient {

    private final static String GET_ORGANIZATION_ENDPOINT = "organizations";
    private final static String GET_USER_ENDPOINT = "users";

    RestTemplate restTemplate = new RestTemplate();

    @Value("${gis-scos.host}")
    String host;

    @Value("${gis-scos.prefix}")
    String gisScosPrefix;

    @Value("${vam.X-CN-UUID}")
    private String vamSecret;



    /** 4.1.10.3 Получение объекта записи об организации по global_id организации */
     public synchronized OrganizationDTO makeGetOrganizationRequest(String global_id){

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

    /** НОВЫЙ Получение Получение списка сотрудников Организации */
    public synchronized Optional<UserDTO> makeGetUserRequest(UUID userId){

        String urlTemplate = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host(host)
                .path(gisScosPrefix)
                .pathSegment(GET_USER_ENDPOINT)
                .pathSegment(userId.toString())
                .encode()
                .toUriString();

        ResponseEntity<UserDTO> response;
        try {
            response = restTemplate.exchange(
                    urlTemplate,
                    HttpMethod.GET,
                    buildGisScosRequest(),
                    UserDTO.class
            );
        }catch (HttpClientErrorException.NotFound notFoundEx){
            return Optional.empty();
        }
        return Optional.ofNullable(response.getBody());
    }

    private HttpEntity buildGisScosRequest() {
        HttpHeaders headers = new HttpHeaders();

        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("X-CN-UUID", vamSecret);

        return new HttpEntity(headers);
    }
}
