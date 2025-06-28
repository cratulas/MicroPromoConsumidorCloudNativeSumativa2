package com.example.micropromoconsumidor.listener;

import com.example.micropromoconsumidor.config.RabbitMQConfig;
import com.example.micropromoconsumidor.model.Promocion;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
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

    @RabbitListener(queues = RabbitMQConfig.PROMOS_QUEUE)
    public void recibirPromocion(Promocion promocion) throws Exception {

        if (promocion.getDescuento() == null || promocion.getDescuento().compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException("❌ Descuento inválido.");
        }
        if (promocion.getProductoId() == null) {
            throw new IllegalArgumentException("❌ ProductoId es requerido.");
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String nombreArchivo = "promocion_" + promocion.getId() + "_" + timestamp + "_" + UUID.randomUUID() + ".json";

        File carpeta = Paths.get("archivos_promos").toFile();
        if (!carpeta.exists()) carpeta.mkdirs();

        File archivo = new File(carpeta, nombreArchivo);
        try (FileWriter writer = new FileWriter(archivo)) {
            mapper.writeValue(writer, promocion);
            System.out.println("✅ Promoción guardada: " + archivo.getAbsolutePath());
        }
    }
}
