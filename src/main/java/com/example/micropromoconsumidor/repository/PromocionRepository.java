// src/main/java/com/example/micropromoconsumidor/repository/PromocionRepository.java
package com.example.micropromoconsumidor.repository;

import com.example.micropromoconsumidor.model.Promocion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PromocionRepository extends JpaRepository<Promocion, Long> {
    List<Promocion> findByProductoId(Long productoId);
}
