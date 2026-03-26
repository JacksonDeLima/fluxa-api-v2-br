package com.jacksondelima.fluxa;

import com.jacksondelima.fluxa.tarefa.StatusTarefa;
import com.jacksondelima.fluxa.tarefa.Tarefa;
import com.jacksondelima.fluxa.tarefa.TarefaRepository;
import com.jacksondelima.fluxa.tarefa.TarefaService;
import com.jacksondelima.fluxa.tarefa.dto.TarefaRequestDTO;
import com.jacksondelima.fluxa.tarefa.dto.TarefaResponseDTO;
import com.jacksondelima.fluxa.usuario.Perfil;
import com.jacksondelima.fluxa.usuario.Usuario;
import com.jacksondelima.fluxa.usuario.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TarefaServiceTest {

    @Mock
    private TarefaRepository tarefaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private TarefaService tarefaService;

    @Test
    @DisplayName("Deve lançar exceção quando o usuário autenticado não for encontrado")
    void deveLancarExcecaoUsuarioNaoEncontrado() {
        // Arrange
        configurarContextoDeSeguranca("teste@fluxa.com");
        when(usuarioRepository.findByEmail("teste@fluxa.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> tarefaService.listarMinhas());
    }

    @Test
    @DisplayName("Deve criar uma nova tarefa com sucesso para o usuário autenticado")
    void deveCriarTarefaComSucesso() {
        // Arrange
        var usuarioMock = Usuario.builder()
                .id(1L)
                .email("teste@fluxa.com")
                .nome("Usuário Teste")
                .perfil(Perfil.USUARIO)
                .build();

        var requestDTO = new TarefaRequestDTO("Nova Tarefa", "Descrição da tarefa");

        var tarefaSalva = Tarefa.builder()
                .id(100L)
                .titulo(requestDTO.titulo())
                .descricao(requestDTO.descricao())
                .status(StatusTarefa.PENDENTE)
                .usuario(usuarioMock)
                .build();

        configurarContextoDeSeguranca(usuarioMock.getEmail());
        when(usuarioRepository.findByEmail(usuarioMock.getEmail())).thenReturn(Optional.of(usuarioMock));
        when(tarefaRepository.save(any(Tarefa.class))).thenReturn(tarefaSalva);

        // Act
        TarefaResponseDTO responseDTO = tarefaService.criar(requestDTO);

        // Assert
        assertNotNull(responseDTO);
        assertEquals(tarefaSalva.getId(), responseDTO.id());
        assertEquals(requestDTO.titulo(), responseDTO.titulo());
        assertEquals(usuarioMock.getEmail(), responseDTO.emailUsuario());

        verify(tarefaRepository).save(any(Tarefa.class));
    }

    private void configurarContextoDeSeguranca(String email) {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn(email);
    }
}