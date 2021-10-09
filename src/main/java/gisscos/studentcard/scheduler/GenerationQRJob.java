package gisscos.studentcard.scheduler;

import gisscos.studentcard.entities.DynamicQR;
import gisscos.studentcard.entities.enums.QRStatus;
import gisscos.studentcard.repositories.DynamicQRRepository;
import gisscos.studentcard.services.Impl.QrGenerator;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class GenerationQRJob extends QuartzJobBean {

    @Autowired
    DynamicQRRepository dynamicQRRepository;

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