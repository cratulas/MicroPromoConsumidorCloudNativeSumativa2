package com.example.micropromoconsumidor.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String PROMOS_QUEUE = "cola_promos";
    public static final String DLX_EXCHANGE = "dlx-exchange";
    public static final String DLX_QUEUE = "cola_promos_dlx";
    public static final String DLX_ROUTING_KEY = "promos.dlx";

    @Bean
    public Queue colaPromos() {
        return QueueBuilder.durable(PROMOS_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DLX_ROUTING_KEY)
                .build();
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE);
    }

    @Bean
    public Queue colaPromosDLX() {
        return QueueBuilder.durable(DLX_QUEUE).build();
    }

    @Bean
    public Binding bindingDLX() {
        return BindingBuilder.bind(colaPromosDLX())
                .to(dlxExchange())
                .with(DLX_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(mapper);
    }
}
