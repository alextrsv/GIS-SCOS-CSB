package gisscos.studentcard.scheduler;

import gisscos.studentcard.clients.GisScosApiRestClient;
import gisscos.studentcard.clients.VamRestClient;
import gisscos.studentcard.entities.dto.StudentDTO;
import gisscos.studentcard.entities.dto.StudentsDTO;
import gisscos.studentcard.repositories.IDynamicQRRepository;
import gisscos.studentcard.services.OrganizationService;
import gisscos.studentcard.services.StudentService;
import gisscos.studentcard.services.UserService;
import lombok.NonNull;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class GenerationQRJob extends QuartzJobBean {

    private final IDynamicQRRepository dynamicQRRepository;

    private final VamRestClient vamRestClient;

    private final GisScosApiRestClient gisScosApiRestClient;

    private final UserService userService;

    private final StudentService studentService;


    @Autowired
    public GenerationQRJob(IDynamicQRRepository dynamicQRRepository, VamRestClient vamRestClient, GisScosApiRestClient gisScosApiRestClient, StudentService studentService, UserService userService) {
        this.dynamicQRRepository = dynamicQRRepository;
        this.vamRestClient = vamRestClient;
        this.gisScosApiRestClient = gisScosApiRestClient;
        this.studentService = studentService;
        this.userService = userService;
    }

    @Override
    protected void executeInternal(@NonNull JobExecutionContext jobExecutionContext) {
        System.out.println("Hi! ---" + new Date());
        System.out.println("new QR generating");
        generateQR();
    }


    public void generateQR(){

        long start = System.currentTimeMillis();
        System.out.println(start);

        int itemsPerThread = 300;
        StudentsDTO getStudentsResponse = vamRestClient.makeGetStudentsRequest(itemsPerThread);
        List<StudentDTO> allStudents = getStudentsResponse.getResults();

        int threadAmount = getStudentsResponse.getLast_page();

        List<Checker> checkerList = new ArrayList<>();

        for (int i = 0; i < threadAmount; i++){
            Checker checker = new Checker(dynamicQRRepository, gisScosApiRestClient, userService, studentService);
            checker.setThreadNumber(i);
            checker.setAllStudents(allStudents);
            checker.setItemsPerThread(itemsPerThread);
            checker.setStartIndx(itemsPerThread * i);
            checker.setEndIndex(itemsPerThread * i + itemsPerThread);
            checkerList.add(checker);
        }

        for (Checker c: checkerList) {
            c.start();
        }

        for (Checker c: checkerList) {
            try {
                c.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        long finish = System.currentTimeMillis();
        long elapsed = finish - start;
        System.out.println("Прошло времени, мс: " + elapsed);
    }
}