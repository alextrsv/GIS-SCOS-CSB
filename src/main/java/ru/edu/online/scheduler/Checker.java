package ru.edu.online.scheduler;

import ru.edu.online.entities.DynamicQR;
import ru.edu.online.entities.DynamicQRUser;
import ru.edu.online.entities.dto.OrganizationDTO;
import ru.edu.online.entities.enums.QRStatus;
import ru.edu.online.repositories.IDynamicQRRepository;
import ru.edu.online.services.IDynamicQRUserService;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class Checker extends Thread {

    private final IDynamicQRRepository dynamicQRRepository;

    private final IDynamicQRUserService dynamicQRUserService;

    String[] codesArray;

    int lastCodeIndx = 0;

    List<DynamicQRUser> allUsers;
    int itemsPerThread;
    int threadNumber;
    int startIndx;
    int endIndex;

    public Checker(IDynamicQRRepository dynamicQRRepository, IDynamicQRUserService dynamicQRUserService) {
        this.dynamicQRRepository = dynamicQRRepository;
        this.dynamicQRUserService = dynamicQRUserService;
    }

    @Override
    public void run() {
        System.out.println("THREAD: " + threadNumber +
                "\n---startIndex: " + startIndx +
                "\n---endIndex: " + endIndex +
                "\n---itemsPerThread: " + itemsPerThread);


        for (int i = startIndx; i < endIndex; i++) {
            DynamicQRUser user = allUsers.get(i);

            System.out.println("thr: " + threadNumber + "  currentIndx: " + i + "  user: " + user.getId() + "");

            Set<String> permittedOrgsID = dynamicQRUserService.getPermittedOrganizations(user);

            if (user.getUserId() != null)
                permittedOrgsID.forEach(System.out::println);


            List<DynamicQR> usersQRs = dynamicQRRepository.getByUserId(user.getUserId());

            //для разрешенных организаций
            permittedOrgsID.forEach(organizationID -> {
                Optional<DynamicQR> dynamicQR =
                        usersQRs.stream()
                                .filter(qr -> qr.getUniversityId().equals(organizationID))
                                .filter(qr -> qr.getStatus() != QRStatus.EXPIRED && qr.getStatus() != QRStatus.DELETED)
                                .findAny();
                if (dynamicQR.isPresent())
                    updateQR(dynamicQR.get());
                else addNewQR(user, organizationID);
            });

            //проверка уже хранящихся QR-ов на актуальность (не истек ли срок действия заявки на прозод)
            dynamicQRRepository.getByUserId(user.getUserId())
                    .forEach(dynamicQR -> {
                        if (!permittedOrgsID.contains(dynamicQR.getUniversityId()))
                            expireQR(dynamicQR);
                    });
        }

    }

    private void addNewQR(DynamicQRUser dynamicQRUser, String organizationUUID) {
        DynamicQR newQR = new DynamicQR();
        newQR.setCreationDate(LocalDate.now());
        newQR.setEndDate(newQR.getCreationDate().plusDays(1));
        newQR.setUniversityId(organizationUUID);
        newQR.setUserId(dynamicQRUser.getUserId());
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

    private String makeNewContent(String organizationId)  {
        OrganizationDTO organization = new OrganizationDTO();
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
        String code = codesArray[lastCodeIndx];
        lastCodeIndx++;
        return code;
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

    public List<DynamicQRUser> getAllUsers() {
        return allUsers;
    }

    public void setAllUsers(List<DynamicQRUser> allUsers) {
        this.allUsers = allUsers;
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
        codesArray = CodesGenerator.getWiegand34CodesFromInteger(itemsPerThread* 10);
    }
}
