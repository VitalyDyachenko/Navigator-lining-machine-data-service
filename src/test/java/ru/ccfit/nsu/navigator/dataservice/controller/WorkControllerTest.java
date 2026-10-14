package ru.ccfit.nsu.navigator.dataservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import ru.ccfit.nsu.navigator.dataservice.config.TraceIdFilter;
import ru.ccfit.nsu.navigator.dataservice.dto.WorkResponse;
import ru.ccfit.nsu.navigator.dataservice.exception.WorkNotFoundException;
import ru.ccfit.nsu.navigator.dataservice.service.WorkFilesService;
import static ru.ccfit.nsu.navigator.dataservice.controller.EndpointDocumentation.endpoint;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkController.class)
@AutoConfigureRestDocs
@Import(TraceIdFilter.class)
class WorkControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private WorkFilesService service;

	@Test
	void getWorkInfo_shouldReturnWork() throws Exception {
		WorkResponse response = new WorkResponse(
				1L,
				"survey.dat",
				"calc.dat",
				"work.dat"
		);
		when(service.getWorkInfo(eq(1L))).thenReturn(response);

		mockMvc.perform(get("/api/works/{id}", 1L))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.surveyFilename").value("survey.dat"))
				.andExpect(jsonPath("$.calculationFilename").value("calc.dat"))
				.andExpect(jsonPath("$.workFilename").value("work.dat"))
				.andDo(document("works/get-info",
						preprocessResponse(prettyPrint()),
						endpoint(),
						pathParameters(
								parameterWithName("id").description("Уникальный идентификатор работы")
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
		when(service.getWorkInfo(eq(999L)))
				.thenThrow(new WorkNotFoundException(999L));

		mockMvc.perform(get("/api/works/{id}", 999L))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.code").value("WORK_NOT_FOUND"))
				.andExpect(jsonPath("$.message").value("Work with id 999 not found"))
				.andExpect(jsonPath("$.path").value("/api/works/999"))
				.andDo(document("works/get-info-not-found",
						preprocessResponse(prettyPrint())
						)
				);
	}
}