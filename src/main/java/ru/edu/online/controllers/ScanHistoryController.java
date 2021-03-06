package ru.edu.online.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.edu.online.entities.ScanHistory;
import ru.edu.online.entities.dto.ScanHistoriesWithPayloadDTO;
import ru.edu.online.entities.dto.ScanHistoryDTO;
import ru.edu.online.services.IScanHistoryService;

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
            @RequestParam(name = "search", required = false, defaultValue = "") String searchByFullName,
            Principal principal){

        return scanHistoryService
                .getScanHistoriesBySecurityId(
                        UUID.fromString(principal.getName()),
                        PageRequest.of(
                                page - 1,
                                size,
                                Sort.by("creationDate").descending()
                        ),
                        searchByFullName
                )
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
