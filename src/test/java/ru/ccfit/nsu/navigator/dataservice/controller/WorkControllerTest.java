package ru.ccfit.nsu.navigator.dataservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.ccfit.nsu.navigator.dataservice.config.TraceIdFilter;
import ru.ccfit.nsu.navigator.dataservice.dto.WorkFilesResponse;
import ru.ccfit.nsu.navigator.dataservice.exception.WorkNotFoundException;
import ru.ccfit.nsu.navigator.dataservice.service.WorkFilesService;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static ru.ccfit.nsu.navigator.dataservice.controller.EndpointDocumentation.endpoint;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.multipart;


@WebMvcTest(WorkController.class)
@AutoConfigureRestDocs
@Import(TraceIdFilter.class)
class WorkControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private WorkFilesService service;

	// ТЕСТЫ ДЛЯ КОНТРАКТА API (генерируют документацию target/generated-docs/index.html)

	@Test
	void getWorkFilesInfo_shouldReturnWorkFiles() throws Exception {
		// TODO заменить пример на реальный с реальными расширениями
		WorkFilesResponse response = new WorkFilesResponse(
				1L,
				"0914_0812.srv",
				"0914_0812.dat",
				"0914_0812.bin"
		);
		when(service.getWorkFilesInfo(eq(1L))).thenReturn(response);

		mockMvc.perform(get("/api/works/{id}", 1L))
				.andExpect(status().isOk())
				.andDo(document("work-files/get-info",
						preprocessResponse(prettyPrint()),
						endpoint(),
						pathParameters(
								parameterWithName("id").description("Уникальный ID работы")
						),
						responseFields(
								fieldWithPath("id").description("Уникальный ID работы"),
								fieldWithPath("surveyFilename").description("Имя файла съёмки"),
								fieldWithPath("calculationFilename").description("Имя файла проекта"),
								fieldWithPath("workFilename").description("Имя файла выправки")
						)
				));
	}

	@Test
	void getWorkInfo_whenNotFound_shouldReturn404() throws Exception {
		when(service.getWorkFilesInfo(eq(999L)))
				.thenThrow(new WorkNotFoundException(999L));

		mockMvc.perform(get("/api/works/{id}", 999L))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.code").value("WORK_NOT_FOUND"))
				.andDo(document("work-files/get-info-not-found",
						preprocessResponse(prettyPrint())
						)
				);
	}

	@Test
	void uploadFiles_shouldSaveFilesAndReturnId() throws Exception {
		// TODO заменить пример на реальный с реальными расширениями
		MockMultipartFile surveyFile = new MockMultipartFile(
				"surveyFile",
				"0914_0812.srv",
				"application/octet-stream",
				"survey content".getBytes()
		);
		MockMultipartFile calculationFile = new MockMultipartFile(
				"calculationFile",
				"0914_0812.dat",
				"application/octet-stream",
				"calculation content".getBytes()
		);
		MockMultipartFile workFile = new MockMultipartFile(
				"workFile",
				"0914_0812.bin",
				"application/octet-stream",
				"work content".getBytes()
		);

		when(service.saveFiles(any(), any(), any())).thenReturn(42L);

		mockMvc.perform(multipart("/api/works")
						.file(surveyFile)
						.file(calculationFile)
						.file(workFile))
				.andExpect(status().isOk())
				.andDo(document("work-files/upload",
						preprocessResponse(prettyPrint()),
						endpoint(),
						requestParts(
								partWithName("surveyFile").description("Файл съёмки"),
								partWithName("calculationFile").description("Файл проекта"),
								partWithName("workFile").description("Файл выправки")
						),
						responseFields(
								fieldWithPath("id").description("Уникальный ID созданной записи с файлами")
						)
				));
	}

}