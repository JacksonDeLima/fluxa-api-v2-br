package com.jacksondelima.fluxa.usuario;

import com.jacksondelima.fluxa.usuario.dto.UsuarioResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("""
            select new com.jacksondelima.fluxa.usuario.dto.UsuarioResponseDTO(
                u.id,
                u.nome,
                u.email,
                u.perfil,
                u.criadoEm,
                u.atualizadoEm
            )
            from Usuario u
            where (
                :termo is null
                or lower(u.nome) like lower(concat('%', :termo, '%'))
                or lower(u.email) like lower(concat('%', :termo, '%'))
            )
            and (:perfil is null or u.perfil = :perfil)
            """)
    Page<UsuarioResponseDTO> buscarParaAdministracao(
            @Param("termo") String termo,
            @Param("perfil") Perfil perfil,
            Pageable pageable
    );
}
