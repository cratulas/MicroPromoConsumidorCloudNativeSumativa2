// src/main/java/com/example/micropromoconsumidor/controller/PromocionReaderController.java
package com.example.micropromoconsumidor.controller;

import com.example.micropromoconsumidor.model.Promocion;
import com.example.micropromoconsumidor.repository.PromocionRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/promos")
@CrossOrigin
public class PromocionReaderController {

    private final PromocionRepository promocionRepository;

    public PromocionReaderController(PromocionRepository promocionRepository) {
        this.promocionRepository = promocionRepository;
    }

    @GetMapping
    public List<Promocion> listar() {
        return promocionRepository.findAll();
    }

    @GetMapping("/{id}")
    public Promocion porId(@PathVariable Long id) {
        return promocionRepository.findById(id).orElse(null);
    }

    @GetMapping("/producto/{productoId}")
    public List<Promocion> porProducto(@PathVariable Long productoId) {
        return promocionRepository.findByProductoId(productoId);
    }
}
