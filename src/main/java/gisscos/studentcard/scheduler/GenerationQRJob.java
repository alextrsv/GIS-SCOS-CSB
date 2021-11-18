package gisscos.studentcard.scheduler;

import gisscos.studentcard.clients.GisScosApiRestClient;
import gisscos.studentcard.clients.VamRestClient;
import gisscos.studentcard.entities.DynamicQRUser;
import gisscos.studentcard.entities.dto.OrganizationDTO;
import gisscos.studentcard.repositories.IDynamicQRRepository;
import gisscos.studentcard.repositories.IDynamicQRUserRepository;
import gisscos.studentcard.services.IDynamicQRUserService;
import gisscos.studentcard.services.IStudentService;
import gisscos.studentcard.services.IUserService;
import lombok.NonNull;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
public class GenerationQRJob extends QuartzJobBean {

    private final IDynamicQRRepository dynamicQRRepository;

    private final VamRestClient vamRestClient;

    private final GisScosApiRestClient gisScosApiRestClient;

    private final IUserService IUserService;

    private final IStudentService IStudentService;

    private final IDynamicQRUserRepository dynamicQRUserRepository;

    private final IDynamicQRUserService dynamicQRUserService;


    @Autowired
    public GenerationQRJob(IDynamicQRRepository dynamicQRRepository, VamRestClient vamRestClient, GisScosApiRestClient gisScosApiRestClient, IStudentService IStudentService, IUserService IUserService, IDynamicQRUserRepository dynamicQRUserRepository, IDynamicQRUserService dynamicQRUserService) {
        this.dynamicQRRepository = dynamicQRRepository;
        this.vamRestClient = vamRestClient;
        this.gisScosApiRestClient = gisScosApiRestClient;
        this.IStudentService = IStudentService;
        this.IUserService = IUserService;
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

        threadAmount = allStudents.size()/itemsPerThread;


        List<Checker> checkerList = new ArrayList<>();

        for (int i = 0; i < threadAmount; i++){
            Checker checker = new Checker(dynamicQRRepository, dynamicQRUserService);
            checker.setThreadNumber(i);
            checker.setAllUsers(allStudents);
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