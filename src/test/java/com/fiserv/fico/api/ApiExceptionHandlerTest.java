package com.fiserv.fico.api;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @SuppressWarnings("unused")
    private void dummy(String arg) {}

    @Test
    void handleMethodArgumentNotValid_shouldReturnBadRequestWithErrors() throws NoSuchMethodException {
        BeanPropertyBindingResult br = new BeanPropertyBindingResult(new Object(), "obj");
        br.addError(new FieldError("obj", "f1", "m1"));
        br.addError(new FieldError("obj", "f2", "m2"));
        Method m = ApiExceptionHandlerTest.class.getDeclaredMethod("dummy", String.class);
        MethodParameter mp = new MethodParameter(m, 0);

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(mp, br);

        ResponseEntity<ApiErrorResponse> resp = handler.handleMethodArgumentNotValid(ex);
        assertThat(resp.getStatusCode().value()).isEqualTo(400);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getMessage()).isEqualTo("Erro de validação");
        Map<String, Object> details = new LinkedHashMap<>(resp.getBody().getDetails());
        assertThat(details.get("f1")).isEqualTo("m1");
        assertThat(details.get("f2")).isEqualTo("m2");
    }

    @Test
    void handleConstraintViolation_shouldReturnBadRequestWithErrors() {
        ConstraintViolation<?> v1 = Mockito.mock(ConstraintViolation.class);
        Path p1 = Mockito.mock(Path.class);
        Mockito.when(p1.toString()).thenReturn("p1");
        Mockito.when(v1.getPropertyPath()).thenReturn(p1);
        Mockito.when(v1.getMessage()).thenReturn("m1");

        ConstraintViolation<?> v2 = Mockito.mock(ConstraintViolation.class);
        Path p2 = Mockito.mock(Path.class);
        Mockito.when(p2.toString()).thenReturn("p2");
        Mockito.when(v2.getPropertyPath()).thenReturn(p2);
        Mockito.when(v2.getMessage()).thenReturn("m2");

        Set<ConstraintViolation<?>> set = new LinkedHashSet<>();
        set.add(v1);
        set.add(v2);

        ResponseEntity<ApiErrorResponse> resp = handler.handleConstraintViolation(new ConstraintViolationException(set));
        assertThat(resp.getStatusCode().value()).isEqualTo(400);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getMessage()).isEqualTo("Erro de validação");
        Map<String, Object> details = new LinkedHashMap<>(resp.getBody().getDetails());
        assertThat(details.get("p1")).isEqualTo("m1");
        assertThat(details.get("p2")).isEqualTo("m2");
    }

    @Test
    void handleInfrastructureExceptions_shouldReturn500() {
        ResponseEntity<ApiErrorResponse> resp = handler.handleInfrastructureExceptions(new java.sql.SQLException("boom"));
        assertThat(resp.getStatusCode().value()).isEqualTo(500);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getMessage()).contains("Erro de infraestrutura");
        assertThat(resp.getBody().getDetails()).containsKey("causa");
    }

    @Test
    void handleUnexpected_shouldReturn500() {
        ResponseEntity<ApiErrorResponse> resp = handler.handleUnexpected(new RuntimeException("x"));
        assertThat(resp.getStatusCode().value()).isEqualTo(500);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getMessage()).isEqualTo("Erro inesperado ao processar a requisição");
    }
}
