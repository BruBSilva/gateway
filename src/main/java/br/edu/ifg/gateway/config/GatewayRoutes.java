package br.edu.ifg.gateway.config;

import br.edu.ifg.gateway.filter.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutes {

    private final JwtAuthFilter jwtFilter;

    @Value("${services.user-url}")
    private String userUrl;

    @Value("${services.learning-url}")
    private String learningUrl;

    @Value("${services.trilha-url}")
    private String trilhaUrl;

    @Value("${services.auth-url}")
    private String authUrl;

    public GatewayRoutes(JwtAuthFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("usuario", r -> r.path("/usuario/**")
                        .filters(f -> f.filter(jwtFilter).addRequestHeader("X-Gateway-Key", "trilhadeaprendizadoapims-gateway"))
                        .uri(userUrl))
                .route("learning", r -> r.path("/progresso/**")
                        .filters(f -> f.filter(jwtFilter).addRequestHeader("X-Gateway-Key", "trilhadeaprendizadoapims-gateway"))
                        .uri(learningUrl))
                .route("trilha", r -> r.path("/trilha/**")
                        .filters(f -> f.filter(jwtFilter).addRequestHeader("X-Gateway-Key", "trilhadeaprendizadoapims-gateway"))
                        .uri(trilhaUrl))
                .route("autenticacao", r -> r.path("/auth/**")
                        .filters(f -> f.addRequestHeader("X-Gateway-Key", "trilhadeaprendizadoapims-gateway"))
                        .uri(authUrl))
                .build();
    }
}
