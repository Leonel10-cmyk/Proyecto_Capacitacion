package com.capacitacion.ebdn.repository;

import com.capacitacion.ebdn.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findByParticipanteId(Long participanteId);
    Optional<Pago> findByCodigoOperacion(String codigoOperacion);
    boolean existsByCodigoOperacion(String codigoOperacion);
}
