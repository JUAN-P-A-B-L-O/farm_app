package com.jpsoftware.farmapp.shared.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.servlet.RequestDispatcher;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

class LocalizedErrorAttributesTest {

    @Test
    void shouldTranslateErrorAttributeMessage() {
        LocalizedErrorAttributes localizedErrorAttributes = new LocalizedErrorAttributes();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/animals");
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 403);
        ServletWebRequest webRequest = new ServletWebRequest(request);

        Map<String, Object> attributes = localizedErrorAttributes.getErrorAttributes(
                webRequest,
                ErrorAttributeOptions.defaults());

        assertEquals("Acesso negado.", attributes.get("error"));
    }
}
