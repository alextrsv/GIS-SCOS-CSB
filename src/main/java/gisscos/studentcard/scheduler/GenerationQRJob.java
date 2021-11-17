package gisscos.studentcard.scheduler;

import gisscos.studentcard.clients.GisScosApiRestClient;
import gisscos.studentcard.clients.VamRestClient;
import gisscos.studentcard.entities.DynamicQR;
import gisscos.studentcard.entities.dto.OrganizationDTO;
import gisscos.studentcard.entities.dto.StudentDTO;
import gisscos.studentcard.entities.enums.QRStatus;
import gisscos.studentcard.repositories.IDynamicQRRepository;
import gisscos.studentcard.services.OrganizationService;
import gisscos.studentcard.utils.HashingUtil;
import lombok.NonNull;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class GenerationQRJob extends QuartzJobBean {

    private final IDynamicQRRepository dynamicQRRepository;

    private final VamRestClient vamRestClient;

    private final GisScosApiRestClient gisScosApiRestClient;

    private final OrganizationService organizationService;

    @Autowired
    public GenerationQRJob(IDynamicQRRepository dynamicQRRepository, VamRestClient vamRestClient, OrganizationService organizationService, GisScosApiRestClient gisScosApiRestClient) {
        this.dynamicQRRepository = dynamicQRRepository;
        this.vamRestClient = vamRestClient;
        this.organizationService = organizationService;
        this.gisScosApiRestClient = gisScosApiRestClient;
    }

    @Override
    protected void executeInternal(@NonNull JobExecutionContext jobExecutionContext) {
        System.out.println("Hi! ---" + new Date());
        System.out.println("new QR generating");
        generateQR();
    }

    /**
     * 1. Получить всех студентов
     * 2. У для каждого получить список разрешений на проход
     * 3. Для каждой организации в разрешении сгенерировать код, старый, если был, сделать истекшим*/
    private void generateQR(){
        List<StudentDTO> allStudents = vamRestClient.makeGetStudentsRequest();

        allStudents.forEach(studentDTO -> {
            List<UUID> permittedOrgsUUID = organizationService.getPermittedOrganizations(studentDTO);

            permittedOrgsUUID.forEach(organizationUUID -> {
                Optional<DynamicQR> dynamicQR =
                        dynamicQRRepository.getByUserIdAndUniversityId(studentDTO.getId(), organizationUUID)
                                .stream()
                                .filter(qr -> qr.getStatus() != QRStatus.EXPIRED && qr.getStatus() != QRStatus.DELETED)
                                .findAny();
                if (dynamicQR.isPresent())
                    updateQR(dynamicQR.get());
                else addNewQR(studentDTO, organizationUUID);
            });

            dynamicQRRepository.getByUserId(studentDTO.getId())
                    .forEach(dynamicQR -> {
                        if (!permittedOrgsUUID.contains(dynamicQR.getUniversityId()))
                            expireQR(dynamicQR);
                    });
        });

    }

    private void addNewQR(StudentDTO studentDTO, UUID organizationUUID) {
        DynamicQR newQR = new DynamicQR();
        newQR.setCreationDate(LocalDate.now());
        newQR.setEndDate(newQR.getCreationDate().plusDays(1));
        newQR.setUniversityId(organizationUUID);
        newQR.setUserId(studentDTO.getId());
        newQR.setContent(makeNewContent(organizationUUID));
        newQR.setStatus(QRStatus.NEW);

        dynamicQRRepository.save(newQR);
    }

    private void expireQR(DynamicQR dynamicQR) {
        dynamicQR.setStatus(QRStatus.EXPIRED);
        dynamicQRRepository.save(dynamicQR);
    }

    private void updateQR(DynamicQR dynamicQR)  {
        dynamicQR.setContent(makeNewContent(dynamicQR.getUniversityId()));
        dynamicQR.setStatus(QRStatus.UPDATED);
        dynamicQRRepository.save(dynamicQR);
    }

    private String makeNewContent(UUID organizationId)  {
        OrganizationDTO organization = gisScosApiRestClient.makeGetOrganizationRequest(organizationId);
        switch (organization.getQRInterfaceType()){
            case "wiegand-34":
                try {
                    return makeWiegand34QR();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
        }
        return "";
    }

    private String makeWiegand34QR() throws NoSuchAlgorithmException {
        String randomUUID = String.valueOf(UUID.randomUUID());
        return HashingUtil.getHash(randomUUID);
    }
}