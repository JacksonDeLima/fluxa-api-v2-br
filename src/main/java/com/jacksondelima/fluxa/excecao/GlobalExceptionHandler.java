package com.jacksondelima.fluxa.excecao;

import com.jacksondelima.fluxa.observabilidade.FluxaMetricsService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final FluxaMetricsService fluxaMetricsService;

    public GlobalExceptionHandler(FluxaMetricsService fluxaMetricsService) {
        this.fluxaMetricsService = fluxaMetricsService;
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErroResponseDTO> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request
    ) {
        fluxaMetricsService.registrarFalhaDeLogin();
        log.warn("auth_failed path={} message={}", request.getRequestURI(), ex.getMessage());
        return responder("autenticacao", "Credenciais invalidas.", HttpStatus.UNAUTHORIZED, request);
    }

    @ExceptionHandler({InsufficientAuthenticationException.class, UsernameNotFoundException.class})
    public ResponseEntity<ErroResponseDTO> handleUnauthorized(
            Exception ex,
            HttpServletRequest request
    ) {
        return responder("autenticacao", "Usuario nao autenticado.", HttpStatus.UNAUTHORIZED, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroResponseDTO> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        log.warn("access_denied path={} message={}", request.getRequestURI(), ex.getMessage());
        return responder("acesso_negado", "Acesso negado para este recurso.", HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ErroResponseDTO> handleBusinessException(
            RegraDeNegocioException ex,
            HttpServletRequest request
    ) {
        log.info("business_rule_violation path={} message={}", request.getRequestURI(), ex.getMessage());
        return responder("negocio", ex.getMessage(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponseDTO> handleNotFound(
            RecursoNaoEncontradoException ex,
            HttpServletRequest request
    ) {
        log.info("resource_not_found path={} message={}", request.getRequestURI(), ex.getMessage());
        return responder("nao_encontrado", ex.getMessage(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponseDTO> handleConflict(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        log.warn("data_integrity_violation path={} message={}", request.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return responder(
                "conflito",
                "Violacao de integridade de dados. Revise os dados enviados e tente novamente.",
                HttpStatus.CONFLICT,
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponseDTO> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String mensagem = ex.getBindingResult().getFieldError() != null
                ? ex.getBindingResult().getFieldError().getDefaultMessage()
                : "Falha de validacao.";

        log.info("validation_failed path={} message={}", request.getRequestURI(), mensagem);
        return responder("validacao", mensagem, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponseDTO> handleGeneric(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("unexpected_error path={}", request.getRequestURI(), ex);
        return responder("erro_interno", "Ocorreu um erro interno no servidor.", HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    private ResponseEntity<ErroResponseDTO> responder(
            String erro,
            String mensagem,
            HttpStatus status,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(status).body(
                new ErroResponseDTO(
                        erro,
                        mensagem,
                        status.value(),
                        request.getRequestURI(),
                        Instant.now()
                )
        );
    }
}
