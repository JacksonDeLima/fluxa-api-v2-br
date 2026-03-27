package com.jacksondelima.fluxa.tarefa;

import com.jacksondelima.fluxa.tarefa.dto.TarefaResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TarefaRepository extends JpaRepository<Tarefa, Long> {

    @EntityGraph(attributePaths = "usuario")
    List<Tarefa> findByUsuarioIdOrderByCriadaEmDesc(Long usuarioId);

    @EntityGraph(attributePaths = "usuario")
    Optional<Tarefa> findByIdAndUsuarioId(Long id, Long usuarioId);

    @Query("""
            select new com.jacksondelima.fluxa.tarefa.dto.TarefaResponseDTO(
                t.id,
                t.titulo,
                t.descricao,
                t.status,
                u.email
            )
            from Tarefa t
            join t.usuario u
            where (
                :termo is null
                or lower(t.titulo) like lower(concat('%', :termo, '%'))
                or lower(coalesce(t.descricao, '')) like lower(concat('%', :termo, '%'))
            )
            and (:status is null or t.status = :status)
            and (:emailUsuario is null or lower(u.email) = lower(:emailUsuario))
            """)
    Page<TarefaResponseDTO> buscarParaAdministracao(
            @Param("termo") String termo,
            @Param("status") StatusTarefa status,
            @Param("emailUsuario") String emailUsuario,
            Pageable pageable
    );
}
