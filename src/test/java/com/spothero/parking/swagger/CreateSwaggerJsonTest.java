package com.spothero.parking.swagger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.spothero.parking.AbstractTest;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springdoc.core.Constants.DEFAULT_API_DOCS_URL;
import static org.springdoc.core.Constants.DEFAULT_API_DOCS_URL_YAML;
import org.springdoc.webmvc.api.OpenApiResource;
import org.springframework.beans.factory.annotation.Autowired;

// This is a small utility to create a json version of the swagger for consulting
public class CreateSwaggerJsonTest extends AbstractTest {

    @Autowired
    private OpenApiResource openApiResource;

    private HttpServletRequest request;

    @Test
    public void createJsonFile() throws IOException {
        // Mock up HttpSession and insert it into mocked up HttpServletRequest
        HttpSession session = mock(HttpSession.class);
        given(session.getId()).willReturn("sessionid");

        // Mock up HttpServletRequest
        request = mock(HttpServletRequest.class);
        given(request.getRequestURL()).willReturn(new StringBuffer().append("http://localhost:8080/v3/api-docs"));
        given(request.getSession()).willReturn(session);
        given(request.getSession(true)).willReturn(session);
        HashMap<String, String[]> params = new HashMap<>();
        given(request.getParameterMap()).willReturn(params);

        // Mock up HttpServletResponse
        HttpServletResponse response = mock(HttpServletResponse.class);
        PrintWriter writer = mock(PrintWriter.class);
        given(response.getWriter()).willReturn(writer);

        String userDir = System.getProperty("user.dir");
        String pathString = userDir + "/docs/";
        Path dirPath = Paths.get(pathString);

        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        Path path = Paths.get(pathString + "/swagger.json");
        Files.write(path, getSwaggerJson().getBytes());

        assertTrue(Files.exists(path));

    }

    @SuppressWarnings("unchecked")
    public String getSwaggerJson() throws JsonProcessingException {
        String json = openApiResource.openapiJson(request, DEFAULT_API_DOCS_URL);
        ObjectMapper om = new ObjectMapper();
        om.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        Map<String, Object> map = om.readValue(json, HashMap.class);
        return om.writerWithDefaultPrettyPrinter().writeValueAsString(map);
    }

    public String getSwaggerYaml() throws JsonProcessingException {
        return openApiResource.openapiJson(request, DEFAULT_API_DOCS_URL_YAML);
    }

}
