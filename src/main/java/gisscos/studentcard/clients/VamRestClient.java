package gisscos.studentcard.clients;

import gisscos.studentcard.entities.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;


@Slf4j
@Component
public class VamRestClient {

    RestTemplate restTemplate = new RestTemplate();

    private final static String STUDENTS_ENDPOINT = "students";
    private final static String STUDY_PLAN_ENDPOINT = "study_plans";

    @Value("${gis-scos.host}")
    String host;

    @Value("${vam.prefix}")
    private String vamPrefix;

    @Value("${vam.X-CN-UUID}")
    private String vamSecret;


    /** 5.1.4 Получение учебного плана */
    public StudyPlanDTO makeGetStudyPlanRequest(UUID studyPlanId){

        String urlTemplate = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host(host)
                .path(vamPrefix)
                .pathSegment(STUDY_PLAN_ENDPOINT)
                .pathSegment(studyPlanId.toString())
                .encode()
                .toUriString();

        System.out.println(urlTemplate);
        log.info(urlTemplate);

        ResponseEntity<StudyPlanDTO> response;
//        try {
            response = restTemplate.exchange(
                    urlTemplate,
                    HttpMethod.GET,
                    buildVamRequest(),
                    StudyPlanDTO.class
            );
//        }catch (HttpClientErrorException.NotFound notFoundEx){
//            return Optional.empty();
//        }
        return response.getBody();
    }

    /** 5.1.5 Получение списка студентов */
    public StudentsDTO makeGetStudentsRequest(int pageSize, int pageNumber, String organizationId){

        String urlTemplate = "";
        if (organizationId != null) {
            urlTemplate = UriComponentsBuilder.newInstance()
                    .scheme("https")
                    .host(host)
                    .path(vamPrefix)
                    .pathSegment(STUDENTS_ENDPOINT)
                    .queryParam("page_size", pageSize)
                    .queryParam("page", pageNumber)
                    .queryParam("organization_id", organizationId)
                    .encode()
                    .toUriString();
        } else
            urlTemplate = UriComponentsBuilder.newInstance()
                    .scheme("https")
                    .host(host)
                    .path(vamPrefix)
                    .pathSegment(STUDENTS_ENDPOINT)
                    .queryParam("page_size", pageSize)
                    .queryParam("page", pageNumber)
                    .encode()
                    .toUriString();

        ResponseEntity<StudentsDTO> response = restTemplate.exchange(
                urlTemplate,
                HttpMethod.GET,
                buildVamRequest(),
                StudentsDTO.class
        );

        return response.getBody();
    }

    /** 5.1.6 Получение студента */
    public Optional<StudentDTO> makeGetStudentRequest(String id){

        String urlTemplate = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host(host)
                .path(vamPrefix)
                .pathSegment(STUDENTS_ENDPOINT)
                .pathSegment(id.toString())
                .encode()
                .toUriString();

        ResponseEntity<StudentDTO> response;
        try {
            response = restTemplate.exchange(
                    urlTemplate,
                    HttpMethod.GET,
                    buildVamRequest(),
                    StudentDTO.class
            );
        }catch (HttpClientErrorException.NotFound notFoundEx){
            return Optional.empty();
        }
        return Optional.ofNullable(response.getBody());
    }


    private HttpEntity buildVamRequest() {
        HttpHeaders headers = new HttpHeaders();

        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("X-CN-UUID", vamSecret);

        return new HttpEntity(headers);
    }
}
