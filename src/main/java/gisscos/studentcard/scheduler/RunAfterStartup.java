package gisscos.studentcard.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class RunAfterStartup {

    @Autowired
    SchedulerService schedulerService;

    @EventListener(ApplicationReadyEvent.class)
    public void runAfterStartup() {
        schedulerService.startGeneratingQRJob();
    }
}