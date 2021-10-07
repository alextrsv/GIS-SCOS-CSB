package gisscos.studentcard.scheduler;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class RunQRGeneration {

    @EventListener(ApplicationReadyEvent.class)
    public void runAfterStartup() {
        System.out.println("Yaaah, I am running........");
    }
}