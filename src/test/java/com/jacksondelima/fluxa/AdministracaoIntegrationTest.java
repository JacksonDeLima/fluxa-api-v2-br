package com.jacksondelima.fluxa;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jacksondelima.fluxa.observabilidade.RateLimitingService;
import com.jacksondelima.fluxa.tarefa.StatusTarefa;
import com.jacksondelima.fluxa.tarefa.Tarefa;
import com.jacksondelima.fluxa.tarefa.TarefaRepository;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdministracaoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TarefaRepository tarefaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RateLimitingService rateLimitingService;

    @BeforeEach
    void setUp() {
        tarefaRepository.deleteAll();
        usuarioRepository.deleteAll();
        rateLimitingService.clear();
    }

    @Test
    void deveBloquearEndpointAdministrativoParaUsuarioComum() throws Exception {
        Usuario usuario = usuarioRepository.save(
                Usuario.builder()
                        .nome("Usuario")
                        .email("usuario@fluxa.com")
                        .senha(passwordEncoder.encode("123456"))
                        .perfil(Perfil.USUARIO)
                        .build()
        );

        String token = autenticar(usuario.getEmail(), "123456");

        mockMvc.perform(get("/administracao/usuarios")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("acesso_negado"));
    }

    @Test
    void devePaginarEFiltrarEndpointsAdministrativos() throws Exception {
        Usuario admin = usuarioRepository.save(
                Usuario.builder()
                        .nome("Admin Fluxa")
                        .email("admin@fluxa.com")
                        .senha(passwordEncoder.encode("123456"))
                        .perfil(Perfil.ADMINISTRADOR)
                        .build()
        );

        Usuario analista = usuarioRepository.save(
                Usuario.builder()
                        .nome("Ana Analista")
                        .email("ana@fluxa.com")
                        .senha(passwordEncoder.encode("123456"))
                        .perfil(Perfil.USUARIO)
                        .build()
        );

        Usuario bruno = usuarioRepository.save(
                Usuario.builder()
                        .nome("Bruno QA")
                        .email("bruno@fluxa.com")
                        .senha(passwordEncoder.encode("123456"))
                        .perfil(Perfil.USUARIO)
                        .build()
        );

        tarefaRepository.save(Tarefa.builder()
                .titulo("Organizar backlog")
                .descricao("Revisar cards")
                .status(StatusTarefa.PENDENTE)
                .usuario(analista)
                .build());

        tarefaRepository.save(Tarefa.builder()
                .titulo("Deploy homologacao")
                .descricao("Executar deploy")
                .status(StatusTarefa.CONCLUIDA)
                .usuario(bruno)
                .build());

        String token = autenticar(admin.getEmail(), "123456");

        mockMvc.perform(get("/administracao/usuarios")
                        .param("perfil", "USUARIO")
                        .param("termo", "ana")
                        .param("page", "0")
                        .param("size", "5")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].email").value("ana@fluxa.com"))
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(get("/administracao/tarefas")
                        .param("status", "CONCLUIDA")
                        .param("emailUsuario", "bruno@fluxa.com")
                        .param("page", "0")
                        .param("size", "5")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].titulo").value("Deploy homologacao"))
                .andExpect(jsonPath("$.content[0].status").value("CONCLUIDA"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    private String autenticar(String email, String senha) throws Exception {
        String resposta = mockMvc.perform(post("/autenticacao/login")
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
