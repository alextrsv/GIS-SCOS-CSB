package gisscos.studentcard.scheduler;

import gisscos.studentcard.clients.GisScosApiRestClient;
import gisscos.studentcard.entities.DynamicQR;
import gisscos.studentcard.entities.dto.OrganizationDTO;
import gisscos.studentcard.entities.dto.StudentDTO;
import gisscos.studentcard.entities.enums.QRStatus;
import gisscos.studentcard.repositories.IDynamicQRRepository;
import gisscos.studentcard.services.OrganizationService;
import gisscos.studentcard.utils.HashingUtil;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Checker extends Thread {

    private final IDynamicQRRepository dynamicQRRepository;

    private final GisScosApiRestClient gisScosApiRestClient;

    private final OrganizationService organizationService;

    List<StudentDTO> allStudents;
    int itemsPerThread;
    int threadNumber;
    int startIndx;
    int endIndex;

    public Checker(IDynamicQRRepository dynamicQRRepository, GisScosApiRestClient gisScosApiRestClient, OrganizationService organizationService) {
        this.dynamicQRRepository = dynamicQRRepository;
        this.gisScosApiRestClient = gisScosApiRestClient;
        this.organizationService = organizationService;
    }

    @Override
    public void run() {
        System.out.println("THREAD: " + threadNumber +
                "\n---startIndex: " + startIndx +
                "\n---endIndex: " + endIndex +
                "\n---itemsPerThread: " + itemsPerThread);


        for (int i = startIndx; i < endIndex; i++) {
            StudentDTO studentDTO = allStudents.get(i);

            System.out.println("thr: " + threadNumber + "  currentIndx: " + i + "  student: " + studentDTO.getId() + "");

            List<UUID> permittedOrgsUUID = organizationService.getPermittedOrganizations(studentDTO);

            List<DynamicQR> usersQRs = dynamicQRRepository.getByUserId(studentDTO.getId());


            //для разрешенных организаций
            permittedOrgsUUID.forEach(organizationUUID -> {
                Optional<DynamicQR> dynamicQR =
                        usersQRs.stream()
                                .filter(qr -> qr.getUniversityId().equals(organizationUUID))
                                .filter(qr -> qr.getStatus() != QRStatus.EXPIRED && qr.getStatus() != QRStatus.DELETED)
                                .findAny();
                if (dynamicQR.isPresent())
                    updateQR(dynamicQR.get());
                else addNewQR(studentDTO, organizationUUID);
            });


            //проверка уже хранящихся QR-ов на актуальность (не истек ли срок действия заявки на прозод)
            dynamicQRRepository.getByUserId(studentDTO.getId())
                    .forEach(dynamicQR -> {
                        if (!permittedOrgsUUID.contains(dynamicQR.getUniversityId()))
                            expireQR(dynamicQR);
                    });
        }

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
        OrganizationDTO organization = new OrganizationDTO();
//        try {
//            organization = gisScosApiRestClient.makeGetOrganizationRequest(organizationId);
//        }catch (org.springframework.web.client.HttpClientErrorException.NotFound notFoundEx){
//            System.err.println("CAN'T FIND SUCH ORGANIZATION");
//            return  "";
//        }
        switch (Objects.requireNonNull(organization).getQRInterfaceType()){
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


    public int getStartIndx() {
        return startIndx;
    }

    public void setStartIndx(int startIndx) {
        this.startIndx = startIndx;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    public List<StudentDTO> getAllStudents() {
        return allStudents;
    }

    public void setAllStudents(List<StudentDTO> allStudents) {
        this.allStudents = allStudents;
    }


    public int getThreadNumber() {
        return threadNumber;
    }

    public void setThreadNumber(int threadNumber) {
        this.threadNumber = threadNumber;
    }

    public int getItemsPerThread() {
        return itemsPerThread;
    }

    public void setItemsPerThread(int itemsPerThread) {
        this.itemsPerThread = itemsPerThread;
    }
}
