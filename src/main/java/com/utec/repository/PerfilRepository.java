package com.utec.repository;

import com.utec.model.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PerfilRepository extends JpaRepository <Perfil, Integer> {
    Optional<Perfil> findByNombre(String administrador);

    @Query("SELECT COUNT(f) > 0 FROM Perfil p JOIN Funcionalidad f " +
            "WHERE p.idPerfil = :perfilId AND f.nombre = :funcionalidad")
    boolean existeFuncionalidadParaPerfil(@Param("perfilId") Integer perfilId,
                                          @Param("funcionalidad") String funcionalidad);
}
