package ru.edu.online.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentsDTO {

    Integer  page;
    Integer last_page;
    List<StudentDTO> results;

    public StudentsDTO(List<StudentDTO> allStudents, int page_amount) {
        this.results = allStudents;
        this.last_page = page_amount;
    }

//    public StudentsDTO(){}
}
