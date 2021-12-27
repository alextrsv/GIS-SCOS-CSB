package ru.edu.online.services.Impl;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.edu.online.clients.GisScosApiRestClient;
import ru.edu.online.clients.VamRestClient;
import ru.edu.online.entities.DynamicQRUser;
import ru.edu.online.entities.QRUser;
import ru.edu.online.entities.dto.*;
import ru.edu.online.repositories.IValidateStudentCacheRepository;
import ru.edu.online.services.IDynamicQRUserService;
import ru.edu.online.services.IQRUserService;
import ru.edu.online.services.IVamAPIService;
import ru.edu.online.utils.HashingUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class StudentServiceImpl implements IQRUserService {

    private final GisScosApiRestClient gisScosApiRestClient;

    private final VamRestClient vamRestClient;
    /** Сервис для работы с АПИ ВАМа */
    private final IVamAPIService vamAPIService;

    private final IDynamicQRUserService dynamicQRUserService;

    private final IValidateStudentCacheRepository studentCacheRepository;

    @Autowired
    public StudentServiceImpl(GisScosApiRestClient gisScosApiRestClient,
                              VamRestClient vamRestClient,
                              IVamAPIService vamAPIService,
                              IDynamicQRUserService dynamicQRUserService,
                              IValidateStudentCacheRepository studentCacheRepository) {
        this.gisScosApiRestClient = gisScosApiRestClient;
        this.vamRestClient = vamRestClient;
        this.vamAPIService = vamAPIService;
        this.dynamicQRUserService = dynamicQRUserService;
        this.studentCacheRepository = studentCacheRepository;
    }



    @SneakyThrows
    @Override
    public String getContentWithHash(QRUser qrUser){
        String finalContent = getFullStaticQRPayload(qrUser).toString();
        String hash = HashingUtil.getHash(finalContent);
        finalContent = finalContent.substring(0, finalContent.length()-1);
        finalContent += ", \"hash\": \"" + hash + "\"}";
        return finalContent;
    }

    @Override
    public PermanentUserQRDTO getFullStaticQRPayload(QRUser qrUser) {
        StudentDTO studentDTO = (StudentDTO) qrUser;
        PermanentStudentQRDTO permanentStudentQRDTO = new PermanentStudentQRDTO();

        permanentStudentQRDTO.setUserId(String.valueOf(studentDTO.getScos_id()));   // id из SCOS
        permanentStudentQRDTO.setSurname(studentDTO.getSurname());
        permanentStudentQRDTO.setName(studentDTO.getName());
        permanentStudentQRDTO.setMiddle_name( studentDTO.getMiddle_name());
        permanentStudentQRDTO.setOrganization(getOrganizationsName(studentDTO));    // by organizationId
        permanentStudentQRDTO.setStatus(getStudentFlowStatus(studentDTO.getId()));  // Получаем текущий статус студента
        permanentStudentQRDTO.setRole("student");
        permanentStudentQRDTO.setStud_bilet(
                studentCacheRepository.findAllByEmailAndScosId(
                        studentDTO.getEmail(),
                        studentDTO.getScos_id()).stream().findFirst().orElseThrow().getStudNumber()
        );
        permanentStudentQRDTO.setEducation_form(getEducationForm(studentDTO.getStudy_plans()));
        permanentStudentQRDTO.setStart_year(getStartYear(studentDTO));
        permanentStudentQRDTO.setStud_bilet_duration(getEndYear(studentDTO));
        permanentStudentQRDTO.setPhoto_url(studentDTO.getPhoto_url());
        permanentStudentQRDTO.setAccessed_organizations(getDPermittedOrgs(studentDTO));

        return permanentStudentQRDTO;
    }

    @Override
    public PermanentUserQRDTO getAbbreviatedStaticQRPayload(QRUser qrUser) {
        StudentDTO studentDTO = (StudentDTO) qrUser;
        PermanentStudentQRDTO permanentStudentQRDTO = new PermanentStudentQRDTO();

        permanentStudentQRDTO.setUserId(String.valueOf(studentDTO.getScos_id())); // id из SCOS
        permanentStudentQRDTO.setSurname(studentDTO.getSurname());
        permanentStudentQRDTO.setName(studentDTO.getName());
        permanentStudentQRDTO.setMiddle_name( studentDTO.getMiddle_name());
        permanentStudentQRDTO.setOrganization(getOrganizationsName(studentDTO));
        permanentStudentQRDTO.setRole("student");
        permanentStudentQRDTO.setStud_bilet_duration(getEndYear(studentDTO));

        return permanentStudentQRDTO;
    }

    @SneakyThrows
    @Override
    public String getHash(QRUser qrUser) {
        return HashingUtil.getHash(getFullStaticQRPayload(qrUser).toString());
    }

    /**
     * Получить статус студента по его академической мобильности
     * @param studentId идентификатор студента в ВАМ
     * @return статус flow студента.
     *
     * Возможные значения:
     * ENROLLMENT           - зачисление
     * DEDUCTION            - отчисление
     * SUBBATICAL_TAKING    - академ
     * UNDEFINED            - не определено
     */
    private String getStudentFlowStatus(String studentId) {
        Optional<StudentFlowsDTO> studentFlows = vamAPIService.getStudentFlows(studentId);

        Optional<ContingentFlowDTO> flow =
                studentFlows.orElseThrow()
                        .getResults()
                        .stream()
                        .filter(f -> f.getFlow_type().equals("DEDUCTION")).findAny();
        if (flow.isPresent()) {
            return "DEDUCTION";
        }

        flow = studentFlows.orElseThrow()
                .getResults()
                .stream()
                .filter(f -> f.getFlow_type().equals("SUBBATICAL_TAKING")).findAny(); // Академ
        if (flow.isPresent()) {
            return "SUBBATICAL_TAKING";
        }

        flow = studentFlows.orElseThrow()
                .getResults()
                .stream()
                .filter(f -> f.getFlow_type().equals("ENROLLMENT")).findAny();
        return flow.map(contingentFlowDTO -> "ENROLLMENT").orElse("UNDEFINED");
    }

    private String getOrganizationsName(StudentDTO studentDTO) {
        return gisScosApiRestClient.makeGetOrganizationRequest(studentDTO.getOrganization_id()).get().getShort_name();
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

    private List<OrganizationInQRDTO> getDPermittedOrgs(StudentDTO studentDTO){

        List<OrganizationInQRDTO> orgs = new ArrayList<>();

        dynamicQRUserService.getAcceptedPassRequests(new DynamicQRUser(studentDTO))
                .forEach(passRequest -> {
                    orgs.add(new OrganizationInQRDTO(passRequest.getTargetUniversityId(), passRequest.getTargetUniversityName(), "",
                            passRequest.getStartDate().toString() + " - " + passRequest.getEndDate().toString()));
                });
        return orgs;
    }
}
