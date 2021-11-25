package gisscos.studentcard.entities.dto;

import gisscos.studentcard.entities.ScanHistory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScanHistoriesWithPayloadDTO {
    private List<ScanHistory> scanHistories;
    /**кол-во страниц в пагинации*/
    private int totalPages;
    /**кол-во элементов в пагинации*/
    private long totalItems;
}
