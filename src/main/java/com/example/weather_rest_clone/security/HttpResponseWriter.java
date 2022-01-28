package com.example.weather_rest_clone.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.Getter;
import lombok.ToString;
import org.apache.http.entity.ContentType;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class HttpResponseWriter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final ObjectWriter OBJECT_WRITER = OBJECT_MAPPER.writer();

    private HttpResponseWriter() {}

    public static void updateHttpServletResponseToShowError(HttpServletResponse response,
                                                            HttpStatus httpStatus, String errorDescription) throws IOException {
        response.setStatus(httpStatus.value());
        response.setContentType(ContentType.APPLICATION_JSON.getMimeType());

        var errorResponse = new FilterErrorResponse(errorDescription);
        OBJECT_WRITER.writeValue(response.getWriter(), errorResponse);
        response.flushBuffer();
    }

    @Getter
    @ToString
    static class FilterErrorResponse {
        private final String description;

        FilterErrorResponse(String description) {
            this.description = description;
        }
    }
}
