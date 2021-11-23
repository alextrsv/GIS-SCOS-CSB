package ru.edu.online.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlobalIdDTO {

    List<String> partner_id;
    List<String> institution_id;
    List<String> employer_id;

}
