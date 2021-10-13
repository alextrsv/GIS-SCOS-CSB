package gisscos.studentcard.scheduler;

import gisscos.studentcard.entities.DynamicQR;
import gisscos.studentcard.entities.enums.QRStatus;
import gisscos.studentcard.repositories.DynamicQRRepository;
import lombok.NonNull;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class GenerationQRJob extends QuartzJobBean {

    private final DynamicQRRepository dynamicQRRepository;

    @Autowired
    public GenerationQRJob(DynamicQRRepository dynamicQRRepository) {
        this.dynamicQRRepository = dynamicQRRepository;
    }

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println("Hi! ---" + new Date());
        System.out.println("new QR generating");
        reGenerateAllQRs();

    }

    private void reGenerateAllQRs() {
        ArrayList<DynamicQR> allDynamicQRList = (ArrayList<DynamicQR>) dynamicQRRepository.findAll();

        //todo логика изменения контента
        for (DynamicQR qr: allDynamicQRList) {
            qr.setContent("новый контент: " + UUID.randomUUID());
            qr.setStatus(QRStatus.UPDATED);
            dynamicQRRepository.save(qr);
        }
    }
}