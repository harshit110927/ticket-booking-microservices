package com.booking.adminservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Define the names for our queue, exchange, and routing key as constants
    public static final String QUEUE_NAME = "ticket-queue";
    public static final String EXCHANGE_NAME = "ticket-exchange";
    public static final String ROUTING_KEY = "tickets.new";

    // Spring Bean for the RabbitMQ queue
    @Bean
    public Queue queue() {
        return new Queue(QUEUE_NAME);
    }

    // Spring Bean for the RabbitMQ exchange
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    // Spring Bean for the binding between the queue and exchange
    // This tells the exchange to send messages with the specific routing key to our queue
    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    // We need a message converter to serialize our DTOs to JSON
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // Configure the RabbitTemplate to use our JSON message converter
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}

