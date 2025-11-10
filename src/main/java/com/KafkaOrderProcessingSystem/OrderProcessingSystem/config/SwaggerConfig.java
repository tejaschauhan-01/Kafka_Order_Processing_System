package com.KafkaOrderProcessingSystem.OrderProcessingSystem.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI myCustomConfig() {
        return new OpenAPI()
                .info(
                        new Info().title("Kafka Ordering System APIs")
                                .description("Ordering System APIs")
                )
                .servers(Arrays.asList(new Server().url("http://localhost:8080").description("local")))
                .tags(Arrays.asList(new Tag().name("Inventory Endpoints"),
                        new Tag().name("Order Endpoints")
                ));
    }
}
