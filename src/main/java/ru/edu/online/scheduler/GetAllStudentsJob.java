package ru.edu.online.scheduler;

import ru.edu.online.clients.GisScosApiRestClient;
import ru.edu.online.clients.VamRestClient;
import ru.edu.online.entities.PassRequest;
import ru.edu.online.entities.dto.OrganizationDTO;
import ru.edu.online.entities.dto.StudentDTO;
import ru.edu.online.entities.dto.StudentsDTO;
import ru.edu.online.entities.enums.PassRequestStatus;
import ru.edu.online.entities.enums.PassRequestType;
import ru.edu.online.repositories.IPassRequestRepository;
import ru.edu.online.services.IDynamicQRUserService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

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
            StudentsDTO getStudentsByOrganizationResponse = vamRestClient.makeGetStudentsRequest(pageSize, pageNumber, orgId.get());
            pagesAmount = getStudentsByOrganizationResponse.getLast_page();

            List<StudentDTO> studentsOnPage = new ArrayList<>();
            if (getStudentsByOrganizationResponse.getResults().size() != 0)
                studentsOnPage = getStudentsByOrganizationResponse.getResults();
            else return;
            List<PassRequest> autoRequestsList = new ArrayList<>();

            //для каждого студента организации создается подтвержденная заявка на проход в свой университет
            studentsOnPage.forEach(studentDTO -> {
                autoRequestsList.add(new PassRequest(
                        studentDTO.getId(),
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
