package com.example.AppSaumerios.repository;
import com.example.AppSaumerios.entity.LogNotificacionPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface LogNotificacionPagoRepository extends JpaRepository<LogNotificacionPago, Long> {
}