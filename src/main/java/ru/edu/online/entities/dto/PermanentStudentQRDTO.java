package ru.edu.online.entities.dto;

import com.google.gson.Gson;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class PermanentStudentQRDTO extends PermanentUserQRDTO {
    String stud_bilet;
    String education_form;
    String start_year;
    String stud_bilet_duration;

    @Override
    public String toString() {
        Gson p = new Gson();
        return p.toJson(this);
    }
}

