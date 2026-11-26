package ru.ccfit.nsu.navigator.dataservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import ru.ccfit.nsu.navigator.dataservice.entity.work.WorkFiles;
import ru.ccfit.nsu.navigator.dataservice.exception.parsing.CrmParsingException;
import ru.ccfit.nsu.navigator.dataservice.exception.parsing.PrmParsingException;
import ru.ccfit.nsu.navigator.dataservice.parser.crm.model.CrmCorrection;
import ru.ccfit.nsu.navigator.dataservice.parser.prm.model.PrmCalculation;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.SrvSurvey;
import ru.ccfit.nsu.navigator.dataservice.repository.work.WorkFilesRepository;
import ru.ccfit.nsu.navigator.dataservice.service.persistence.CalculationPersistenceService;
import ru.ccfit.nsu.navigator.dataservice.service.persistence.CorrectionPersistenceService;
import ru.ccfit.nsu.navigator.dataservice.service.persistence.SurveyPersistenceService;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkFilesServiceTest {

	@Mock
	private WorkFilesRepository filesRepository;
	@Mock
	private SurveyPersistenceService surveyPersistenceService;
	@Mock
	private CalculationPersistenceService calculationPersistenceService;
	@Mock
	private CorrectionPersistenceService correctionPersistenceService;

	private WorkFilesService service;

	@BeforeEach
	void setUp() {
		service = new WorkFilesService(
				filesRepository,
				surveyPersistenceService,
				calculationPersistenceService,
				correctionPersistenceService
		);
	}

	@Test
	void shouldParseAndPersistAllThreeFiles() throws Exception {
		MockMultipartFile srvFile = loadMultipart("surveyFile", "srv2.srv", "/srv/srv2.srv");
		MockMultipartFile prmFile = loadMultipart("calculationFile", "prm2.prm", "/prm/prm2.prm");
		MockMultipartFile crmFile = loadMultipart("workFile", "crm2.crm", "/crm/crm2.crm");

		when(surveyPersistenceService.save(any(SrvSurvey.class))).thenReturn(77L);

		Long workId = service.saveFiles(srvFile, prmFile, crmFile);

		assertEquals(77L, workId);
		verify(surveyPersistenceService).save(any(SrvSurvey.class));
		verify(calculationPersistenceService).save(eq(77L), any(SrvSurvey.class), any(PrmCalculation.class));
		verify(correctionPersistenceService).save(eq(77L), any(SrvSurvey.class), any(CrmCorrection.class));
		verify(filesRepository).save(any(WorkFiles.class));
	}

	@Test
	void shouldFailBeforeAnyPersistenceWhenCrmIsInvalid() throws Exception {
		MockMultipartFile srvFile = loadMultipart("surveyFile", "srv2.srv", "/srv/srv2.srv");
		MockMultipartFile prmFile = loadMultipart("calculationFile", "prm2.prm", "/prm/prm2.prm");
		MockMultipartFile brokenCrm = new MockMultipartFile(
				"workFile", "broken.crm", "application/octet-stream", new byte[]{1, 2, 3}
		);

		assertThrows(CrmParsingException.class, () -> service.saveFiles(srvFile, prmFile, brokenCrm));
		verifyNoInteractions(
				surveyPersistenceService,
				calculationPersistenceService,
				correctionPersistenceService,
				filesRepository
		);
	}

	@Test
	void shouldNotSaveWorkFilesWhenCalculationValidationFails() throws Exception {
		MockMultipartFile srvFile = loadMultipart("surveyFile", "srv.srv", "/srv/srv.srv");
		MockMultipartFile prmFile = loadMultipart("calculationFile", "prm.prm", "/prm/prm.prm");
		MockMultipartFile crmFile = loadMultipart("workFile", "crm.crm", "/crm/crm.crm");

		when(surveyPersistenceService.save(any(SrvSurvey.class))).thenReturn(88L);
		doThrow(new PrmParsingException("mismatch"))
				.when(calculationPersistenceService)
				.save(eq(88L), any(SrvSurvey.class), any(PrmCalculation.class));

		assertThrows(PrmParsingException.class, () -> service.saveFiles(srvFile, prmFile, crmFile));
		verifyNoInteractions(correctionPersistenceService, filesRepository);
	}

	private MockMultipartFile loadMultipart(String partName, String filename, String resourcePath) throws Exception {
		try (InputStream input = getClass().getResourceAsStream(resourcePath)) {
			if (input == null) {
				throw new IllegalStateException("Test file not found: " + resourcePath);
			}
			return new MockMultipartFile(partName, filename, "application/octet-stream", input.readAllBytes());
		}
	}
}