package gisscos.studentcard.controllers;

import gisscos.studentcard.entities.ScanHistory;
import gisscos.studentcard.entities.dto.ScanHistoriesWithPayloadDTO;
import gisscos.studentcard.entities.enums.UserRole;
import gisscos.studentcard.services.IScanHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("scan-history")
public class ScanHistoryController {

    private final IScanHistoryService scanHistoryService;

    @Autowired
    public ScanHistoryController(IScanHistoryService scanHistoryService){
        this.scanHistoryService = scanHistoryService;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    private void /*ResponseEntity<String>*/ handleConstraintViolationException(IllegalArgumentException e) {
//        return new ResponseEntity<>("Not valid request parameter: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/add/{userId}")
    private ResponseEntity<ScanHistory> addNewScanInHistory(
            @PathVariable UUID userId,
            @RequestParam(name = "securityId") UUID securityId,
            @RequestParam(name = "role") UserRole role){

        if(scanHistoryService.saveNewScanInHistory(userId, securityId, role).isPresent()) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/view/{securityId}/{page}/{size}")
    private ResponseEntity<ScanHistoriesWithPayloadDTO> getScanHistoryBySecurityId(
            @PathVariable("securityId") UUID securityId,
            @PathVariable("page") int page,
            @PathVariable("size") int size){

        return scanHistoryService
                .getScanHistoriesBySecurityId(
                        securityId, PageRequest.of(page - 1, size)
                )
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
