package com.jacksondelima.fluxa.excecao;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(criarCorpoErro(
                        "autenticacao",
                        "Credenciais invalidas.",
                        HttpStatus.UNAUTHORIZED,
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(criarCorpoErro(
                        "acesso_negado",
                        "Acesso negado para este recurso.",
                        HttpStatus.FORBIDDEN,
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.badRequest().body(
                criarCorpoErro("negocio", ex.getMessage(), HttpStatus.BAD_REQUEST, request.getRequestURI())
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String mensagem = ex.getBindingResult().getFieldError() != null
                ? ex.getBindingResult().getFieldError().getDefaultMessage()
                : "Falha de validacao.";

        return ResponseEntity.badRequest().body(
                criarCorpoErro("validacao", mensagem, HttpStatus.BAD_REQUEST, request.getRequestURI())
        );
    }

    private Map<String, Object> criarCorpoErro(
            String erro,
            String mensagem,
            HttpStatus status,
            String caminho
    ) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("erro", erro);
        body.put("mensagem", mensagem);
        body.put("status", status.value());
        body.put("caminho", caminho);
        body.put("timestamp", Instant.now().toString());
        return body;
    }
}
