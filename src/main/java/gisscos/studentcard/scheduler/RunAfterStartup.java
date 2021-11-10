package gisscos.studentcard.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class RunAfterStartup {

    final
    SchedulerService schedulerService;

    public RunAfterStartup(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

//    @Autowired
//    GenerationQRJob generationQRJob;

    @EventListener(ApplicationReadyEvent.class)
    public void runAfterStartup() {
        schedulerService.startGeneratingQRJob();
//        generationQRJob.generateQR();
    }
}