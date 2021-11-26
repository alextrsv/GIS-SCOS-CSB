package ru.edu.online.scheduler;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import ru.edu.online.clients.GisScosApiRestClient;
import ru.edu.online.clients.VamRestClient;
import ru.edu.online.entities.PassRequest;
import ru.edu.online.entities.dto.OrganizationDTO;
import ru.edu.online.entities.dto.StudentDTO;
import ru.edu.online.entities.dto.StudentsDTO;
import ru.edu.online.entities.dto.UserDTO;
import ru.edu.online.entities.enums.PassRequestStatus;
import ru.edu.online.entities.enums.PassRequestType;
import ru.edu.online.repositories.IPassRequestRepository;
import ru.edu.online.services.IDynamicQRUserService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class GetAllStudentsJob extends QuartzJobBean {

    private final VamRestClient vamRestClient;

    final GisScosApiRestClient gisScosApiRestClient;

    private final IDynamicQRUserService dynamicQRUserService;

    private final IPassRequestRepository passRequestRepository;

    @Autowired
    public GetAllStudentsJob(VamRestClient vamRestClient, IDynamicQRUserService dynamicQRUserService, IPassRequestRepository passRequestRepository, GisScosApiRestClient gisScosApiRestClient) {
        this.vamRestClient = vamRestClient;
        this.dynamicQRUserService = dynamicQRUserService;
        this.passRequestRepository = passRequestRepository;
        this.gisScosApiRestClient = gisScosApiRestClient;
    }

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        doJob();
    }

    public void doJob() {
        System.out.println("качаю студентов...");

        List<OrganizationDTO> organizationDTOList = List.of(gisScosApiRestClient.makeGetOrganizationsRequest().get());

        organizationDTOList.forEach(this::downloadStudentsByOrganization);
    }

    private void downloadStudentsByOrganization(OrganizationDTO organizationDTO) {
        int pageSize = 10;
        int pageNumber = 1;
        int pagesAmount;

        do {
            Optional<String> orgId = organizationDTO.getOrganizationId();
            if (orgId.isEmpty()) return;
            //качаю студентов из текущей организации
            StudentsDTO getStudentsByOrganizationResponse = vamRestClient.makeGetStudentsRequest(pageSize, pageNumber, orgId.get());
            pagesAmount = getStudentsByOrganizationResponse.getLast_page();

            List<StudentDTO> studentsOnPage;
            if (getStudentsByOrganizationResponse.getResults().size() != 0)
                studentsOnPage = getStudentsByOrganizationResponse.getResults();
            else return;
            List<PassRequest> autoRequestsList = new ArrayList<>();

            //для каждого студента организации создается подтвержденная заявка на проход в свой университет
            studentsOnPage.forEach(studentDTO -> {
                if (studentDTO.getEmail() == null) return;
                Optional<UserDTO> userDTO = gisScosApiRestClient.makeGetUserByEmailRequest(studentDTO.getEmail()); // поменял на получение по email
                if (userDTO.isEmpty()) return;
                studentDTO.setScos_id(userDTO.get().getUser_id());

                autoRequestsList.add(new PassRequest(
                        studentDTO.getScos_id(), // поменял на Scos_Id
                        "Нужно починить",
                        "Класс GetAllStudentJob",
                        "Генерация не работает нормально",
                        organizationDTO.getOrganizationId().get(),
                        organizationDTO.getOrganizationId().get(),
                        LocalDate.of(LocalDate.now().getYear(), 8, 1),
                        LocalDate.of(LocalDate.now().getYear() + 1, 7, 1),
                        PassRequestStatus.ACCEPTED,
                        PassRequestType.SINGLE,
                        "organizationAddress",
                        organizationDTO.getShort_name(),
                        organizationDTO.getShort_name(),
                        passRequestRepository.countAllByNumberGreaterThan(0L) + 1
                        ));
            });

            dynamicQRUserService.addAll(studentsOnPage);
            passRequestRepository.saveAll(autoRequestsList);
        }while (pageNumber < pagesAmount);
    }
}
