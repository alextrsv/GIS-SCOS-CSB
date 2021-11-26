package ru.edu.online.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.edu.online.entities.PassRequest;

import java.util.List;

@Data
@AllArgsConstructor
public class PassRequestsResponseDTO {
    private Long currentPage;
    private Long pageSize;
    private Long pagesCount;
    private Long requestsTotal;
    private List<PassRequest> requests;
}
