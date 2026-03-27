package com.jacksondelima.fluxa;

import com.jacksondelima.fluxa.excecao.RecursoNaoEncontradoException;
import com.jacksondelima.fluxa.observabilidade.FluxaMetricsService;
import com.jacksondelima.fluxa.seguranca.UsuarioAutenticadoService;
import com.jacksondelima.fluxa.tarefa.StatusTarefa;
import com.jacksondelima.fluxa.tarefa.Tarefa;
import com.jacksondelima.fluxa.tarefa.TarefaRepository;
import com.jacksondelima.fluxa.tarefa.TarefaService;
import com.jacksondelima.fluxa.tarefa.dto.TarefaRequestDTO;
import com.jacksondelima.fluxa.tarefa.dto.TarefaResponseDTO;
import com.jacksondelima.fluxa.usuario.Perfil;
import com.jacksondelima.fluxa.usuario.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TarefaServiceTest {

    @Mock
    private TarefaRepository tarefaRepository;

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @Mock
    private FluxaMetricsService fluxaMetricsService;

    @InjectMocks
    private TarefaService tarefaService;

    @Test
    @DisplayName("Deve lancar excecao quando a tarefa do usuario nao existir")
    void deveLancarExcecaoQuandoTarefaNaoExistir() {
        var usuario = Usuario.builder()
                .id(1L)
                .email("teste@fluxa.com")
                .nome("Usuario Teste")
                .perfil(Perfil.USUARIO)
                .build();

        when(usuarioAutenticadoService.buscar()).thenReturn(usuario);
        when(tarefaRepository.findByIdAndUsuarioId(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> tarefaService.buscarPorId(99L));
    }

    @Test
    @DisplayName("Deve criar uma nova tarefa com sucesso para o usuario autenticado")
    void deveCriarTarefaComSucesso() {
        var usuarioMock = Usuario.builder()
                .id(1L)
                .email("teste@fluxa.com")
                .nome("Usuario Teste")
                .perfil(Perfil.USUARIO)
                .build();

        var requestDTO = new TarefaRequestDTO("  Nova Tarefa  ", "  Descricao da tarefa  ");

        var tarefaSalva = Tarefa.builder()
                .id(100L)
                .titulo("Nova Tarefa")
                .descricao("Descricao da tarefa")
                .status(StatusTarefa.PENDENTE)
                .usuario(usuarioMock)
                .build();

        when(usuarioAutenticadoService.buscar()).thenReturn(usuarioMock);
        when(tarefaRepository.save(any(Tarefa.class))).thenReturn(tarefaSalva);

        TarefaResponseDTO responseDTO = tarefaService.criar(requestDTO);

        assertNotNull(responseDTO);
        assertEquals(tarefaSalva.getId(), responseDTO.id());
        assertEquals("Nova Tarefa", responseDTO.titulo());
        assertEquals(usuarioMock.getEmail(), responseDTO.emailUsuario());

        verify(tarefaRepository).save(any(Tarefa.class));
    }
}
