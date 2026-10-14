package ru.ccfit.nsu.navigator.dataservice.controller;

import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.snippet.Snippet;
import org.springframework.restdocs.snippet.TemplatedSnippet;

import java.util.HashMap;
import java.util.Map;

public class EndpointDocumentation {

	public static Snippet endpoint() {
		return new TemplatedSnippet("endpoint", null) {
			@Override
			protected Map<String, Object> createModel(Operation operation) {
				String method = operation.getRequest().getMethod().name();
				String urlTemplate = (String) operation.getAttributes()
						.get("org.springframework.restdocs.urlTemplate");

				if (urlTemplate == null) {
					urlTemplate = operation.getRequest().getUri().getPath();
				}

				Map<String, Object> model = new HashMap<>();
				model.put("method", method);
				model.put("urlTemplate", urlTemplate);
				return model;
			}
		};
	}
}