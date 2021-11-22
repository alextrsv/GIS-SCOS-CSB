package gisscos.studentcard.controllers;

import gisscos.studentcard.entities.ScanHistory;
import gisscos.studentcard.entities.dto.ScanHistoriesWithPayloadDTO;
import gisscos.studentcard.entities.dto.ScanHistoryDTO;
import gisscos.studentcard.services.IScanHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
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

    @PostMapping("/add")
    private ResponseEntity<ScanHistory> addNewScanInHistory(
            @RequestBody ScanHistoryDTO scanHistoryDTO,
            Principal principal){

        if(scanHistoryService.saveNewScanInHistory(
                UUID.fromString(principal.getName()),
                scanHistoryDTO).isPresent()) {

            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/view/{page}/{size}")
    private ResponseEntity<ScanHistoriesWithPayloadDTO> getScanHistoryBySecurityId(
            @PathVariable("page") int page,
            @PathVariable("size") int size,
            @RequestParam("search") String searchByFullName,
            Principal principal){

        return scanHistoryService
                .getScanHistoriesBySecurityId(
                        UUID.fromString(principal.getName()),
                        PageRequest.of(page - 1, size),
                        searchByFullName
                )
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
