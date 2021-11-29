package ru.edu.online.scheduler;

import lombok.NonNull;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import ru.edu.online.clients.GisScosApiRestClient;
import ru.edu.online.clients.VamRestClient;
import ru.edu.online.entities.DynamicQRUser;
import ru.edu.online.entities.dto.OrganizationDTO;
import ru.edu.online.repositories.IDynamicQRRepository;
import ru.edu.online.repositories.IDynamicQRUserRepository;
import ru.edu.online.services.IDynamicQRUserService;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
public class GenerationQRJob extends QuartzJobBean {

    private final IDynamicQRRepository dynamicQRRepository;

    private final VamRestClient vamRestClient;

    private final GisScosApiRestClient gisScosApiRestClient;

//    private final IUserService IUserService;
//
//    private final IStudentService IStudentService;

    private final IDynamicQRUserRepository dynamicQRUserRepository;

    private final IDynamicQRUserService dynamicQRUserService;


    @Autowired
    public GenerationQRJob(IDynamicQRRepository dynamicQRRepository, VamRestClient vamRestClient, GisScosApiRestClient gisScosApiRestClient, IDynamicQRUserRepository dynamicQRUserRepository, IDynamicQRUserService dynamicQRUserService) {
        this.dynamicQRRepository = dynamicQRRepository;
        this.vamRestClient = vamRestClient;
        this.gisScosApiRestClient = gisScosApiRestClient;
//        this.IStudentService = IStudentService;
//        this.IUserService = IUserService;
        this.dynamicQRUserRepository = dynamicQRUserRepository;
        this.dynamicQRUserService = dynamicQRUserService;
    }

    @Override
    protected void executeInternal(@NonNull JobExecutionContext jobExecutionContext) {
        System.out.println("Hi! ---" + new Date());
        System.out.println("new QR generating");
        generateQR();
    }


    /** Генерация QR-ов
     * 1. Получить список организаций
     * 2. Для каждой организации из БД получить пользователей
     * 3. Проверить разрешения каждого
     * 4. Сгенерировать код
     * */
    public void generateQR(){

        List<OrganizationDTO> organizationDTOList = List.of(gisScosApiRestClient.makeGetOrganizationsRequest().get());

        organizationDTOList.forEach(this::handleUsersOfOrganization);

    }

    private void handleUsersOfOrganization(OrganizationDTO organizationDTO) {

        long start = System.currentTimeMillis();
//        System.out.println(start);

        int itemsPerThread = 300;

        Optional<String> organizationId = organizationDTO.getOrganizationId();
        if (organizationId.isEmpty()) return;

        List<DynamicQRUser> allStudents = dynamicQRUserRepository.getByOrganizationId(organizationDTO.getOrganizationId().get());
        if (allStudents.isEmpty()) return;

        int threadAmount;

        if (itemsPerThread > allStudents.size())
            itemsPerThread = allStudents.size();

        threadAmount = (int) Math.ceil(allStudents.size()/itemsPerThread);
        System.out.println("thread amount: " + threadAmount);


        List<Checker> checkerList = new ArrayList<>();

        for (int i = 0; i < threadAmount; i++){
            Checker checker = new Checker(dynamicQRRepository, dynamicQRUserService);
            checker.setThreadNumber(i);
            checker.setAllUsers(allStudents);
            checker.setItemsPerThread(itemsPerThread);
            checker.setStartIndx(itemsPerThread * i);
            if (i == threadAmount - 1)
                checker.setEndIndex(allStudents.size());
            else
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