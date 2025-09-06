package com.example.AppSaumerios.repository;

import com.example.AppSaumerios.entity.Pedidos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedidos, Long> {

    // Devuelve Optional<Pedidos>
    Optional<Pedidos> findByIdAndUsuarioId(Long id, Long usuarioId);

    List<Pedidos> findByUsuarioId(Long usuarioId);
    List<Pedidos> findAllByOrderByFechaDesc();
}