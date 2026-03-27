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

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AutenticacaoIntegrationTest {

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
    void deveCadastrarComEmailNormalizado() throws Exception {
        mockMvc.perform(post("/autenticacao/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Maria Silva",
                                  "email": "MARIA@fluxa.com",
                                  "senha": "123456"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mensagem").value("Usuario cadastrado com sucesso."));

        Usuario usuario = usuarioRepository.findByEmail("maria@fluxa.com").orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals("Maria Silva", usuario.getNome());
    }

    @Test
    void deveAutenticarERetornarUsuarioAtual() throws Exception {
        usuarioRepository.save(
                Usuario.builder()
                        .nome("Admin")
                        .email("admin@fluxa.com")
                        .senha(passwordEncoder.encode("123456"))
                        .perfil(Perfil.ADMINISTRADOR)
                        .build()
        );

        String resposta = mockMvc.perform(post("/autenticacao/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "admin@fluxa.com",
                                  "senha": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(not(emptyOrNullString())))
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode payload = objectMapper.readTree(resposta);
        String token = payload.get("token").asText();

        mockMvc.perform(get("/usuarios/eu")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@fluxa.com"))
                .andExpect(jsonPath("$.perfil").value("ADMINISTRADOR"));
    }

    @Test
    void deveNegarAcessoSemToken() throws Exception {
        mockMvc.perform(get("/usuarios/eu"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("autenticacao"));
    }

    @Test
    void deveAplicarRateLimitNoLogin() throws Exception {
        usuarioRepository.save(
                Usuario.builder()
                        .nome("Usuario Rate Limit")
                        .email("rate@fluxa.com")
                        .senha(passwordEncoder.encode("123456"))
                        .perfil(Perfil.USUARIO)
                        .build()
        );

        String body = """
                {
                  "email": "rate@fluxa.com",
                  "senha": "123456"
                }
                """;

        mockMvc.perform(post("/autenticacao/login")
                        .header("X-Forwarded-For", "10.0.0.1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(post("/autenticacao/login")
                        .header("X-Forwarded-For", "10.0.0.1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(post("/autenticacao/login")
                        .header("X-Forwarded-For", "10.0.0.1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.erro").value("rate_limit"));
    }
}
