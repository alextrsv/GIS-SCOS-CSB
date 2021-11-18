package gisscos.studentcard.scheduler;

import gisscos.studentcard.entities.ScanHistory;
import gisscos.studentcard.repositories.IScanHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ScanHistorySchedulerService {

    private final static long DAY_TIME = 24*60*60*1000L;
    private final static long MONTH_TIME = 12*DAY_TIME;

    @Autowired
    private IScanHistoryRepository scanHistoryRepository;

    @Scheduled(fixedDelay = DAY_TIME)
    public void deleteScanHistoryAfter1Month() {
        System.out.println("CALL");
        List<ScanHistory> scanHistoryList = scanHistoryRepository.findAll();
        long currentTime = new Date().getTime();

        if (!scanHistoryList.isEmpty()) {
            scanHistoryList.forEach(scanHistory -> {
                if (scanHistory.getCreationDate() != null
                        && (currentTime - scanHistory.getCreationDate().getTime()) >= MONTH_TIME) {
                    scanHistoryRepository.delete(scanHistory);
                }
            });
        }
    }

}
