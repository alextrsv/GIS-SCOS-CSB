package ru.edu.online.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.edu.online.entities.ScanHistory;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScanHistoriesWithPayloadDTO {
    private List<ScanHistory> scanHistories;
    /**кол-во страниц в пагинации*/
    private int totalPages;
    /**кол-во записей в пагинации*/
    private long totalItems;

}
