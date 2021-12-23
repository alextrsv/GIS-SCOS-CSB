package ru.edu.online.entities.dto;

import lombok.Data;

@Data
public class ContingentFlowDTO {
    private String id;
    private String student_id;
    private String organization_id;
    private String contingent_flow;
    private String flow_type;
    private String date;
    private String faculty;
    private String education_form;
    private String form_fin;
    private String details;
    private String external_id;
}
