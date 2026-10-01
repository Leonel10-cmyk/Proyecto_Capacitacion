package com.capacitacion.ebdn.repository;

import com.capacitacion.ebdn.entity.Participante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipanteRepository extends JpaRepository<Participante, Long> {

    Optional<Participante> findByDni(String dni);

    boolean existsByDni(String dni);

    List<Participante> findAllByOrderByFechaRegistroDesc();

    @Query("SELECT p FROM Participante p WHERE " +
           "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.apellidos) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "p.dni LIKE CONCAT('%', :query, '%') OR " +
           "LOWER(p.iglesia) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY p.fechaRegistro DESC")
    List<Participante> buscarPorTexto(@Param("query") String query);

    @Query("SELECT p FROM Participante p WHERE " +
           "(:query IS NULL OR :query = '' OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.apellidos) LIKE LOWER(CONCAT('%', :query, '%')) OR p.dni LIKE CONCAT('%', :query, '%')) AND " +
           "(:modalidad IS NULL OR :modalidad = '' OR p.modalidad = :modalidad) AND " +
           "(:estadoPago IS NULL OR :estadoPago = '' OR p.estadoPago LIKE CONCAT('%', :estadoPago, '%')) AND " +
           "(:estadoRegistro IS NULL OR :estadoRegistro = '' OR p.estadoRegistro = :estadoRegistro) " +
           "ORDER BY p.fechaRegistro DESC")
    List<Participante> filtrarParticipantes(@Param("query") String query,
                                           @Param("modalidad") String modalidad,
                                           @Param("estadoPago") String estadoPago,
                                           @Param("estadoRegistro") String estadoRegistro);
}
