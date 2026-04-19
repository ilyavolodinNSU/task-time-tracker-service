package ru.ilyavolodin.tasktimetrackerservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import ru.ilyavolodin.tasktimetrackerservice.dto.ErrorResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        given(request.getRequestURI()).willReturn("/api/test");
    }

    @Nested
    @DisplayName("handleBusinessException")
    class HandleBusinessExceptionTests {

        @Test
        @DisplayName("должен вернуть статус из исключения")
        void shouldReturnStatusFromException() {
            var ex = new ResourceNotFoundException("Task", 1L);
            ResponseEntity<ErrorResponse> response = handler.handleBusinessException(ex, request);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(404);
            assertThat(response.getBody().message()).contains("not found");
        }
    }

    @Nested
    @DisplayName("handleIllegalArgument")
    class HandleIllegalArgumentTests {
        @Test
        @DisplayName("должен вернуть 404 для 'not found' сообщений")
        void shouldReturn404ForNotFoundMessage() {
            var ex = new IllegalArgumentException("Task not found with id: 1");
            ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(ex, request);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody().status()).isEqualTo(404);
        }

        @Test
        @DisplayName("должен вернуть 400 для остальных аргументов")
        void shouldReturn400ForOtherArguments() {
            var ex = new IllegalArgumentException("Invalid input");
            ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(ex, request);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().status()).isEqualTo(400);
        }
    }

    // @Nested
    // @DisplayName("handleValidationExceptions")
    // class HandleValidationExceptionsTests {

    //     @Test
    //     @DisplayName("должен вернуть мапу с ошибками полей")
    //     void shouldReturnFieldErrorsMap() {
    //         var bindingResult = new org.springframework.validation.BeanPropertyBindingResult(
    //                 new Object(), "target");
    //         bindingResult.rejectValue("title", "NotBlank", "Название обязательно");
    //         var ex = new MethodArgumentNotValidException(null, bindingResult);
    //         ResponseEntity<Map<String, String>> response = handler.handleValidationExceptions(ex);
    //         assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    //         assertThat(response.getBody()).containsKey("title");
    //         assertThat(response.getBody().get("title")).contains("обязательно");
    //     }
    // }

    @Nested
    @DisplayName("handleTypeMismatch")
    class HandleTypeMismatchTests {

        @Test
        @DisplayName("должен вернуть 400 при ошибке типа")
        void shouldReturn400ForTypeMismatch() {
            var ex = new MethodArgumentTypeMismatchException("abc", Long.class, "id", null, null);
            ResponseEntity<ErrorResponse> response = handler.handleTypeMismatch(ex, request);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().message()).contains("Invalid value for parameter 'id'");
        }
    }

    @Nested
    @DisplayName("handleDataIntegrityViolation")
    class HandleDataIntegrityViolationTests {
        @Test
        @DisplayName("должен вернуть 409 Conflict")
        void shouldReturn409ForDataIntegrity() {
            var cause = new org.postgresql.util.PSQLException("duplicate key", null);
            var ex = new DataIntegrityViolationException("FK violation", cause);
            ResponseEntity<ErrorResponse> response = handler.handleDataIntegrityViolation(ex, request);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody().status()).isEqualTo(409);
        }
    }

    @Nested
    @DisplayName("handleGenericException")
    class HandleGenericExceptionTests {
        @Test
        @DisplayName("должен вернуть 500 для необработанных исключений")
        void shouldReturn500ForUnhandledException() {
            var ex = new RuntimeException("Unexpected error");
            ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex, request);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody().status()).isEqualTo(500);
            assertThat(response.getBody().message()).isEqualTo("Internal server error");
        }
    }
}
