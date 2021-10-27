package gisscos.studentcard.clients;

import gisscos.studentcard.entities.dto.StudentDTO;
import gisscos.studentcard.entities.dto.StudentsDTO;
import gisscos.studentcard.entities.dto.StudyPlanDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


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

    public StudyPlanDTO makeGetStudyPlanRequest(UUID studyPlanId){

        String urlTemplate = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host(host)
                .path(vamPrefix)
                .pathSegment(STUDY_PLAN_ENDPOINT)
                .pathSegment(studyPlanId.toString())
                .encode()
                .toUriString();

        ResponseEntity<StudyPlanDTO> response = restTemplate.exchange(
                urlTemplate,
                HttpMethod.GET,
                buildVamRequest(),
                StudyPlanDTO.class
        );
        return response.getBody();
    }

    public List<StudentDTO> makeGetStudentsRequest(){

        String urlTemplate = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host(host)
                .path(vamPrefix)
                .pathSegment(STUDENTS_ENDPOINT)
                .encode()
                .toUriString();

        ResponseEntity<StudentsDTO> response = restTemplate.exchange(
                urlTemplate,
                HttpMethod.GET,
                buildVamRequest(),
                StudentsDTO.class
        );
        return Objects.requireNonNull(response.getBody(), "students list is Empty").getResults();
    }

    public StudentDTO makeGetStudentRequest(String id){

        String urlTemplate = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host(host)
                .path(vamPrefix)
                .pathSegment(STUDENTS_ENDPOINT)
                .pathSegment(id)
                .encode()
                .toUriString();

        ResponseEntity<StudentDTO> response = restTemplate.exchange(
                urlTemplate,
                HttpMethod.GET,
                buildVamRequest(),
                StudentDTO.class
        );

        return Objects.requireNonNull(response.getBody(), "student isn't present");
    }

    private HttpEntity buildVamRequest() {
        HttpHeaders headers = new HttpHeaders();

        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("X-CN-UUID", vamSecret);

        return new HttpEntity(headers);
    }
}
