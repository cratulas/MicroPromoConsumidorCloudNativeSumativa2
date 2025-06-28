package com.example.micropromoconsumidor.listener;

import com.example.micropromoconsumidor.config.RabbitMQConfig;
import com.example.micropromoconsumidor.model.Promocion;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class PromocionListener {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final JdbcTemplate jdbcTemplate;

    public PromocionListener(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.PROMOS_QUEUE)
    public void recibirPromocion(Promocion promocion) {
        try {

            if (promocion.getDescuento() == null || promocion.getDescuento().compareTo(BigDecimal.ZERO) <= 0) {
                throw new AmqpRejectAndDontRequeueException("❌ Descuento inválido.");
            }
            if (promocion.getProductoId() == null) {
                throw new AmqpRejectAndDontRequeueException("❌ ProductoId es requerido.");
            }


            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM productos WHERE id = ?", Integer.class, promocion.getProductoId()
            );

            if (count == null || count == 0) {
                throw new AmqpRejectAndDontRequeueException("❌ Producto no existe en Oracle.");
            }


            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String nombreArchivo = "promocion_" + promocion.getId() + "_" + timestamp + "_" + UUID.randomUUID() + ".json";

            File carpeta = Paths.get("archivos_promos").toFile();
            if (!carpeta.exists()) carpeta.mkdirs();

            File archivo = new File(carpeta, nombreArchivo);
            try (FileWriter writer = new FileWriter(archivo)) {
                mapper.writeValue(writer, promocion);
                System.out.println("✅ Promoción guardada en archivo: " + archivo.getAbsolutePath());
            }

            // Oracle
            jdbcTemplate.update(
                "INSERT INTO promocion (descripcion, descuento, fecha_inicio, fecha_fin, producto_id) VALUES (?, ?, ?, ?, ?)",
                promocion.getDescripcion(),
                promocion.getDescuento(),
                promocion.getFechaInicio(),
                promocion.getFechaFin(),
                promocion.getProductoId()
            );

            System.out.println("✅ Promoción insertada en Oracle");

        } catch (AmqpRejectAndDontRequeueException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("❌ Error inesperado: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
