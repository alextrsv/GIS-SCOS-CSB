package gisscos.studentcard.controllers;

import gisscos.studentcard.clients.GisScosApiRestClient;
import gisscos.studentcard.clients.VamRestClient;
import gisscos.studentcard.entities.dto.OrganizationDTO;
import gisscos.studentcard.entities.dto.StudentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TestController {

    @Autowired
    VamRestClient restClient;

    @Autowired
    GisScosApiRestClient gisScosApiRestClient;

    @GetMapping("gis-students")
    public ResponseEntity<List<StudentDTO>> getGisStudents(){
        return new ResponseEntity<>(restClient.makeGetStudentsRequest(), HttpStatus.OK);
    }

    @GetMapping("gis-student/{id}")
    public ResponseEntity<StudentDTO> getGisStudent(@PathVariable String id){
        return new ResponseEntity<>(restClient.makeGetStudentRequest(id), HttpStatus.OK);
    }

    @GetMapping("organization")
    public ResponseEntity<OrganizationDTO> getGisOrganization(@RequestParam String global_id){
        return new ResponseEntity<>(gisScosApiRestClient.makeGetOrganizationRequest(global_id), HttpStatus.OK);
    }
}
