package com.qualitysports.backend.admin.repository;

import com.qualitysports.backend.admin.entity.ReglaPaquete;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReglaPaqueteRepository extends JpaRepository<ReglaPaquete, Long> {

    Optional<ReglaPaquete> findByCantidadParesAndActivoTrue(Integer cantidadPares);

    List<ReglaPaquete> findAllByActivoTrueOrderByCantidadParesAsc();
}
