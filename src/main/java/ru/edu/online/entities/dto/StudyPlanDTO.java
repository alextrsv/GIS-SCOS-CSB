package ru.edu.online.entities.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudyPlanDTO{

    UUID id;
    String title;
    String start_year;
    String end_year;
    String direction;
    String education_form;
    UUID educational_program_id;
}
