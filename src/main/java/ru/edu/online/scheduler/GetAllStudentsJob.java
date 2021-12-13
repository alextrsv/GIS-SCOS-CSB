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
import ru.edu.online.entities.enums.PRStatus;
import ru.edu.online.entities.enums.PRType;
import ru.edu.online.repositories.IPRRepository;
import ru.edu.online.services.IDynamicQRUserService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class GetAllStudentsJob extends QuartzJobBean {

    private final VamRestClient vamRestClient;

    final GisScosApiRestClient gisScosApiRestClient;

    private final IDynamicQRUserService dynamicQRUserService;

    private final IPRRepository passRequestRepository;

    @Autowired
    public GetAllStudentsJob(VamRestClient vamRestClient, IDynamicQRUserService dynamicQRUserService, IPRRepository passRequestRepository, GisScosApiRestClient gisScosApiRestClient) {
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
                studentsOnPage = getStudentsByOrganizationResponse.getResults().stream()
                        .filter(student -> student.getEmail() != null)
                        .collect(Collectors.toList());
            else return;
            List<PassRequest> autoRequestsList = new ArrayList<>();
            List<StudentDTO> studentsToSave = new ArrayList<>();
            //для каждого студента организации создается подтвержденная заявка на проход в свой университет
            studentsOnPage.forEach(studentDTO -> {
                if (studentDTO.getEmail() == null) return;
                Optional<UserDTO> userDTO = gisScosApiRestClient.makeGetUserByEmailRequest(studentDTO.getEmail()); // поменял на получение по email
                if (userDTO.isEmpty()) return;
                studentDTO.setScos_id(userDTO.get().getUser_id());

                if (dynamicQRUserService.isExistsByUserIdAndOrgId(studentDTO.getScos_id(), studentDTO.getOrganization_id())) return;
                studentsToSave.add(studentDTO);

                autoRequestsList.add(new PassRequest(
                        userDTO.get().getUser_id(),
                        userDTO.get().getFirst_name(),
                        userDTO.get().getLast_name(),
                        userDTO.get().getPatronymic_name(),
                        orgId.get(),
                        organizationDTO.getShort_name(),
                        LocalDate.of(LocalDate.now().getYear(), 8, 1),
                        LocalDate.of(LocalDate.now().getYear() + 1, 7, 1),
                        PRStatus.ACCEPTED,
                        PRType.SINGLE,
                        "organizationAddress",
                        organizationDTO.getShort_name(),
                        orgId.get(),
                        passRequestRepository.countAllByNumberGreaterThan(0L) + 1,
                        "")
                );
            });

            dynamicQRUserService.addAll(studentsToSave);
            passRequestRepository.saveAll(autoRequestsList);
        }while (pageNumber < pagesAmount);
    }
}
