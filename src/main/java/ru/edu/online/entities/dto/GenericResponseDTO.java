package ru.edu.online.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class GenericResponseDTO<T> {
    private Long currentPage;
    private Long pageSize;
    private Long pagesCount;
    private Long resultsTotal;
    private List<T> results;
}
