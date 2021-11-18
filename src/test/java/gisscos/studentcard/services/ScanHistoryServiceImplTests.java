package gisscos.studentcard.services;

import gisscos.studentcard.entities.ScanHistory;
import gisscos.studentcard.entities.enums.UserRole;
import gisscos.studentcard.repositories.IScanHistoryRepository;
import gisscos.studentcard.services.Impl.ScanHistoryServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ScanHistoryServiceImplTests {

    @Mock
    private IScanHistoryRepository scanHistoryRepository;

    @InjectMocks
    private ScanHistoryServiceImpl scanHistoryService;

    private final UUID USER_ID = UUID.randomUUID();
    private final UUID SECURITY_ID = UUID.randomUUID();
    private final UserRole STUDENT_ROLE = UserRole.STUDENT;
    private final Timestamp TIMESTAMP = new Timestamp(new Date().getTime());

    private ScanHistory sh;

    @Before
    public void checkNulls(){
        sh = new ScanHistory(USER_ID, SECURITY_ID, TIMESTAMP, STUDENT_ROLE);

        assertNotNull(scanHistoryService);
        assertNotNull(scanHistoryRepository);
    }

    @Test
    public void saveNewScanInHistory_WhenScanHistoryWasSaved() {
        when(scanHistoryRepository.save(any(ScanHistory.class))).thenReturn(sh);

        Optional<ScanHistory> temp = scanHistoryService.saveNewScanInHistory(USER_ID, SECURITY_ID, STUDENT_ROLE);

        verify(scanHistoryRepository, times(1)).save(any(ScanHistory.class));

        assertTrue(temp.isPresent());
        assertEquals(temp.get().getCreationDate(), TIMESTAMP);
    }

    @Test
    public void saveNewScanInHistory_WhenScanHistoryWasNotSaved() {
        when(scanHistoryRepository.save(any(ScanHistory.class))).thenReturn(null);

        Optional<ScanHistory> temp = scanHistoryService.saveNewScanInHistory(USER_ID, SECURITY_ID, STUDENT_ROLE);

        verify(scanHistoryRepository, times(1)).save(any(ScanHistory.class));

        assertFalse(temp.isPresent());
    }
}
