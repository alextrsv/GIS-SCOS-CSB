package gisscos.studentcard.services.Impl;

import gisscos.studentcard.clients.GisScosApiRestClient;
import gisscos.studentcard.clients.VamRestClient;
import gisscos.studentcard.entities.dto.UserDTO;
import gisscos.studentcard.entities.dto.StudentDTO;
import gisscos.studentcard.entities.dto.StudyPlanDTO;
import gisscos.studentcard.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * сервис для работы со статическими QR
 */
@Service
public class PermanentQRServiceImpl implements IPermanentQRService {

    private final VamRestClient vamRestClient;

    private final GisScosApiRestClient gisScosApiRestClient;

    private final UserService userService;

    private final StudentService studentService;


    @Autowired
    public PermanentQRServiceImpl(VamRestClient vamRestClient, GisScosApiRestClient gisScosApiRestClient, UserService userService, StudentService studentService) {
        this.vamRestClient = vamRestClient;
        this.gisScosApiRestClient = gisScosApiRestClient;
        this.userService = userService;
        this.studentService = studentService;
    }

    @Override
    public Optional<Resource> downloadQRAsFile(UUID userId) {
        String content = null;
          /*нужно понять, пользователь с id - студент или обычный пользователь?
        1. делаем запрос на получение студента - если ответ не пустой, то это студент, работаем с ним
        2. иначе, если ответ пустой - это не студент. Делаем запрос к гис сцосу.
        2.1. Если ответ пустой, то это выдаем ошибку - пользователя нет
        2.2. Иначе, если ответ не пустой - работаем с пользователем.
        */
        Optional<StudentDTO> studentDTOWrapper = vamRestClient.makeGetStudentRequest(userId);
        if (studentDTOWrapper.isPresent())
            content = makeStudentContent(studentDTOWrapper.get());
        else{
            Optional<UserDTO> userWrapper = gisScosApiRestClient.makeGetUserRequest(userId);
            if (userWrapper.isPresent()){
                content = makeStaffContent(userWrapper.get());
            }else return Optional.empty();
        }

        BufferedImage qrCodeImage = QrGenerator.generateQRCodeImage(content);
        return Converter.getResource(qrCodeImage);
    }

    private String makeStaffContent(UserDTO user) {
        String content =  ("surname: " + user.getLast_name() +
                "\nname: " + user.getFirst_name() +
                "\nmiddle-name: " + user.getPatronymic_name() +
                "\norganization: " + userService.getOrganizationsNamesAsString(user) +
                "\nstatus: " + "status" +
                "\nrole: " + userService.getUserRolesAsString(user) +
                "\naccessed organizations: " + userService.getPermittedOrganizationsNamesAsString(user));
        System.out.println(content);
        return content;
    }

    private String makeStudentContent(StudentDTO studentDTO) {
        String content = ("surname: " + studentDTO.getSurname() +
                "\nname: " + studentDTO.getName() +
                "\nmiddle-name: " + studentDTO.getMiddle_name() +
                "\norganization: " + studentService.getOrganizationsName(studentDTO) +
                "\nstatus: " + "status" +
                "\nrole: " + "student" +
                "\nstud-bilet: " + "scos" + studentDTO.getId().toString().substring(0, 6) +
                "\neducation_form: " +  getEducationForm(studentDTO.getStudy_plans()) +
                "\nstart_year: " + getStartYear(studentDTO) +
                "\nstud-bilet-duration: " +  getEndYear(studentDTO) +
                "\naccessed organizations: " + studentService.getPermittedOrganizationsNamesAsString(studentDTO));
        System.out.println(content);
        return content;
    }


    private String getStartYear(StudentDTO studentDTO) {
        if(studentDTO.getStudy_plans().size() != 0)
            return getCurrentStudyPlan(studentDTO.getStudy_plans()).get().getStart_year();
        else return "";
    }

    private String getEndYear(StudentDTO studentDTO) {
        if(studentDTO.getStudy_plans().size() != 0)
            return  getCurrentStudyPlan(studentDTO.getStudy_plans()).get().getEnd_year();
        else return "";
    }

    private String getEducationForm(List<StudyPlanDTO> studyPlanDTOS) {
        Optional<StudyPlanDTO> studyPlanDTO = getCurrentStudyPlan(studyPlanDTOS);
        if (studyPlanDTO.isPresent())
            return vamRestClient.makeGetStudyPlanRequest(studyPlanDTO.get().getId()).getEducation_form();
        else return "";
    }

    private Optional<StudyPlanDTO> getCurrentStudyPlan(List<StudyPlanDTO> studyPlanDTOS) {

        for (StudyPlanDTO plan : studyPlanDTOS) {
            int currentYear = 2019; //LocalDateTime.now().getYear();
            if (Integer.parseInt(plan.getStart_year()) <= currentYear &&
                    Integer.parseInt(plan.getEnd_year()) >= currentYear)
                return Optional.of(plan);
        }
        return Optional.empty();
    }

}
