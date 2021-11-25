package ru.edu.online.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.edu.online.entities.dto.OrganizationDTO;
import ru.edu.online.entities.dto.UserDTO;

import java.util.Collections;
import java.util.Optional;

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



    /** Получение списка организаций */
    public Optional<OrganizationDTO[]> makeGetOrganizationsRequest(){

        String urlTemplate = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host(host)
                .path(gisScosPrefix)
                .pathSegment(GET_ORGANIZATION_ENDPOINT)
                .encode()
                .toUriString();

        ResponseEntity<OrganizationDTO[]> response;
        try {
            response = restTemplate.exchange(
                    urlTemplate,
                    HttpMethod.GET,
                    buildGisScosRequest(),
                    OrganizationDTO[].class
            );
        }catch (HttpClientErrorException.NotFound notFoundEx){
            return Optional.empty();
        }
        return Optional.ofNullable(response.getBody());
    }

    /** 4.1.10.3 Получение объекта записи об организации по global_id организации */
    public Optional<OrganizationDTO> makeGetOrganizationRequest(String organizationId){

        String urlTemplate = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host(host)
                .path(gisScosPrefix)
                .pathSegment(GET_ORGANIZATION_ENDPOINT)
                .queryParam("global_id", organizationId)
                .encode()
                .toUriString();

        ResponseEntity<OrganizationDTO> response;
        try {
            response = restTemplate.exchange(
                    urlTemplate,
                    HttpMethod.GET,
                    buildGisScosRequest(),
                    OrganizationDTO.class
            );
        }catch (HttpClientErrorException.NotFound notFoundEx){
            return Optional.empty();
        }
        return Optional.ofNullable(response.getBody());
    }

    public Optional<OrganizationDTO> makeGetOrganizationByOrgnRequest(String orgn){

        String urlTemplate = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host(host)
                .path(gisScosPrefix)
                .pathSegment(GET_ORGANIZATION_ENDPOINT)
                .pathSegment(orgn)
                .encode()
                .toUriString();

        ResponseEntity<OrganizationDTO> response;
        try {
            response = restTemplate.exchange(
                    urlTemplate,
                    HttpMethod.GET,
                    buildGisScosRequest(),
                    OrganizationDTO.class
            );
        }catch (HttpClientErrorException.NotFound notFoundEx){
            return Optional.empty();
        }
        return Optional.ofNullable(response.getBody());
    }


    /** НОВЫЙ Получение Получение сотрудника Организации */
    public synchronized Optional<UserDTO> makeGetUserRequest(String userId){

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
