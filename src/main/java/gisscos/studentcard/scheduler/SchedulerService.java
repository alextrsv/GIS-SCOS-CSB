package gisscos.studentcard.scheduler;

import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SchedulerService {


    @Autowired
    private Scheduler scheduler;


    public void startGeneratingQRJob(){
        JobDetail jobDetail = buildJobDetail();
        Trigger trigger = buildTrigger(jobDetail);
        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }


    private JobDetail buildJobDetail(){
        return JobBuilder.newJob(GenerationQRJob.class)
                .withIdentity(UUID.randomUUID().toString())
                .withDescription("generation dynamics QR-codes")
                .storeDurably()
                .build();
    }

    private CronTrigger buildTrigger(JobDetail jobDetail){
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(), "email-triggers")
                .withDescription("generate new one-day dynamic QR-code")
                .withSchedule(CronScheduleBuilder.cronSchedule("0/10 * * * * ?"))
                .build();
    }
}
