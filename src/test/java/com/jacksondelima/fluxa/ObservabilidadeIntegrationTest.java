package com.jacksondelima.fluxa;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jacksondelima.fluxa.observabilidade.RateLimitingService;
import com.jacksondelima.fluxa.usuario.Perfil;
import com.jacksondelima.fluxa.usuario.Usuario;
import com.jacksondelima.fluxa.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ObservabilidadeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RateLimitingService rateLimitingService;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
        rateLimitingService.clear();
    }

    @Test
    void deveExporHealthPublicamenteComRequestId() throws Exception {
        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Request-Id"))
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void deveRestringirInfoDoActuatorParaAdministrador() throws Exception {
        Usuario admin = usuarioRepository.save(
                Usuario.builder()
                        .nome("Admin Observabilidade")
                        .email("admin-observabilidade@fluxa.com")
                        .senha(passwordEncoder.encode("123456"))
                        .perfil(Perfil.ADMINISTRADOR)
                        .build()
        );

        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isUnauthorized());

        String token = autenticar(admin.getEmail(), "123456");

        mockMvc.perform(get("/actuator/info")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.app.name").value("fluxa-api"));
    }

    @Test
    void deveExporPrometheusParaAdministrador() throws Exception {
        Usuario admin = usuarioRepository.save(
                Usuario.builder()
                        .nome("Admin Metrics")
                        .email("admin-metrics@fluxa.com")
                        .senha(passwordEncoder.encode("123456"))
                        .perfil(Perfil.ADMINISTRADOR)
                        .build()
        );

        String token = autenticar(admin.getEmail(), "123456");

        mockMvc.perform(get("/actuator/prometheus")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("jvm_memory_used_bytes")));
    }

    private String autenticar(String email, String senha) throws Exception {
        String resposta = mockMvc.perform(post("/autenticacao/login")
                        .header("X-Forwarded-For", "10.0.0.99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "senha": "%s"
                                }
                                """.formatted(email, senha)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode payload = objectMapper.readTree(resposta);
        return payload.get("token").asText();
    }
}
