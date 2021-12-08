package ru.edu.online.entities.dto;

import lombok.Data;
import ru.edu.online.entities.PassRequestFile;
import ru.edu.online.entities.enums.PRStatus;
import ru.edu.online.entities.enums.PRType;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Pass request Data transfer object
 */
@Data
public class PRDTO {
    private UUID id;
    private String authorId;
    private String universityId;
    private String targetUniversityId;
    private String universityName;
    private String targetUniversityName;
    private String targetUniversityAddress;
    private String comment;
    private LocalDate creationDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private PRType type;
    private PRStatus status;
    private List<PRUserDTO> users;
    private List<PassRequestFile> files;
}
