package com.qualitysports.backend.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AsesorVentasRepository extends JpaRepository<AsesorVentas, Long> {

    List<AsesorVentas> findAllByActivoTrue();
}
