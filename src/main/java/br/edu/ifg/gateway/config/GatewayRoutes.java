package br.edu.ifg.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutes {

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("usuario", r -> r.path("/usuario/**")
                        .uri("http://localhost:8081"))
                .route("learning", r -> r.path("/progresso/**")
                        .uri("http://localhost:8082"))
                .route("trilha", r -> r.path("/trilha/**")
                        .uri("http://localhost:8083"))
                .route("autenticacao", r -> r.path("/auth/**")
                        .uri("http://localhost:8084"))
                .build();
    }
}
