package com.jacksondelima.fluxa.configuracao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jacksondelima.fluxa.excecao.ErroResponseDTO;
import com.jacksondelima.fluxa.observabilidade.RateLimitingFilter;
import com.jacksondelima.fluxa.observabilidade.RequestLoggingFilter;
import com.jacksondelima.fluxa.seguranca.CustomUserDetailsService;
import com.jacksondelima.fluxa.seguranca.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.time.Instant;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitingFilter rateLimitingFilter;
    private final RequestLoggingFilter requestLoggingFilter;
    private final CustomUserDetailsService customUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/assets/**",
                                "/actuator/health",
                                "/actuator/health/**",
                                "/error",
                                "/favicon.ico",
                                "/autenticacao/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/administracao/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/tarefas/**").hasAnyRole("USUARIO", "ADMINISTRADOR")
                        .requestMatchers("/usuarios/**").authenticated()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) ->
                                escreverErro(
                                        response,
                                        HttpServletResponse.SC_UNAUTHORIZED,
                                        "autenticacao",
                                        "Usuario nao autenticado.",
                                        request.getRequestURI()
                                )
                        )
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                escreverErro(
                                        response,
                                        HttpServletResponse.SC_FORBIDDEN,
                                        "acesso_negado",
                                        "Acesso negado para este recurso.",
                                        request.getRequestURI()
                                )
                        )
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(requestLoggingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(rateLimitingFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    private void escreverErro(
            HttpServletResponse response,
            int status,
            String erro,
            String mensagem,
            String caminho
    ) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(
                response.getWriter(),
                new ErroResponseDTO(erro, mensagem, status, caminho, Instant.now())
        );
    }
}
