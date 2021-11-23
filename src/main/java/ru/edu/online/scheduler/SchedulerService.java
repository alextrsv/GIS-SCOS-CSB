package ru.edu.online.scheduler;

import org.quartz.*;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SchedulerService {


    private final Scheduler scheduler;

    public SchedulerService(Scheduler scheduler) {
        this.scheduler = scheduler;
    }


    public void startGeneratingQRJob(){
        JobDetail jobDetail = buildJobDetail();
        Trigger trigger = buildTrigger(jobDetail);
        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public void startGettingStudentsQRJob(){
        JobDetail jobDetail = buildGetStudentsJobDetail();
        Trigger trigger = buildGetStudentsTrigger(jobDetail);
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
                .withSchedule(CronScheduleBuilder.cronSchedule("0 30 21 ? * * *"))
                .build();
    }

    private JobDetail buildGetStudentsJobDetail(){
        return JobBuilder.newJob(GetAllStudentsJob.class)
                .withIdentity(UUID.randomUUID().toString())
                .withDescription("downloading all students from VAM")
                .storeDurably()
                .build();
    }

    private CronTrigger buildGetStudentsTrigger(JobDetail jobDetail){
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(), "downloading students trigger")
                .withDescription("download all students from VAM")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 0 28 AUG ? *"))
                .build();
    }
}