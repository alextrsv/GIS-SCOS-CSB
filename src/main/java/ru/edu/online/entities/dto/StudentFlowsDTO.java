package ru.edu.online.entities.dto;

import lombok.Data;

import java.util.List;

@Data
public class StudentFlowsDTO {
    private int page;
    private int last_page;
    List<ContingentFlowDTO> results;
}
