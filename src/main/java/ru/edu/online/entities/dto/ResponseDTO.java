package ru.edu.online.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.edu.online.entities.PassRequest;

import java.util.List;

@Data
@AllArgsConstructor
public class ResponseDTO {
    private Long currentPage;
    private Long pageSize;
    private Long pagesCount;
    private Long resultsTotal;
    private List<PassRequest> results;
}
