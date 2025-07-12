package br.edu.ifg.gateway.config;

import br.edu.ifg.gateway.filter.JwtAuthFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutes {

    private final JwtAuthFilter jwtFilter;

    public GatewayRoutes(JwtAuthFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("usuario", r -> r.path("/usuario/**")
                        .filters(f -> f.filter(jwtFilter))
                        .uri("http://localhost:8081"))
                .route("learning", r -> r.path("/progresso/**")
                        .filters(f -> f.filter(jwtFilter))
                        .uri("http://localhost:8082"))
                .route("trilha", r -> r.path("/trilha/**")
                        .filters(f -> f.filter(jwtFilter))
                        .uri("http://localhost:8083"))
                .route("autenticacao", r -> r.path("/auth/**")
                        .uri("http://localhost:8084"))
                .build();
    }
}
