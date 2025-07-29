package br.edu.ifg.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthFilter implements GatewayFilter {

    private final SecretKey secret;

    public JwtAuthFilter(@Value("${jwt.secret}") String secret) {
        this.secret = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();
        HttpMethod method = exchange.getRequest().getMethod();
        
        if (isPublicEndpoint(path, method)) {
            return chain.filter(exchange);
        }
        
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);

            try {
                Claims claims = Jwts.parser()
                        .setSigningKey(secret)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                String role = claims.get("role", String.class);

                if (isAuthorized(role, path, method)) {
                    return chain.filter(exchange);
                } else {
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }

            } catch (ExpiredJwtException e) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            } catch (Exception e) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        } else {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private boolean isPublicEndpoint(String path, HttpMethod method) {
        if (path.startsWith("/auth") && method == HttpMethod.POST) {
            return true;
        }
        
        if (path.equals("/usuario/aluno") && method == HttpMethod.POST) {
            return true;
        }
        
        if (path.equals("/usuario/admin") && method == HttpMethod.POST) {
            return true;
        }
        
        if (path.matches("/trilha/\\d+/modulos-ids") && method == HttpMethod.GET) {
            return true;
        }
        
        if (path.matches("/trilha/\\d+/trilha-conquista-detalhada") && method == HttpMethod.GET) {
            return true;
        }
        
        if (path.matches("/trilha/modulo-conquista-detalhada/\\d+") && method == HttpMethod.GET) {
            return true;
        }
        
        if (path.matches("/usuario/aluno/\\d+/add-xp") && method == HttpMethod.PUT) {
            return true;
        }
        
        return false;
    }

    private boolean isAuthorized(String role, String path, HttpMethod method) {

        if ("temporario".equalsIgnoreCase(role) && method == HttpMethod.GET) {
            return true;
        }

        if (path.startsWith("/auth") && method != HttpMethod.POST) {
            return false;
        }

        if ("admin".equalsIgnoreCase(role)) {
            return true;
        }

        if ("aluno".equalsIgnoreCase(role)) {
            if (path.startsWith("/trilha") && method == HttpMethod.GET) {
                return true;
            }

            if (path.startsWith("/progresso") &&
                    (method == HttpMethod.GET || method == HttpMethod.POST || method == HttpMethod.PUT)) {
                return true;
            }
            
            if (path.startsWith("/usuario/aluno") && method == HttpMethod.GET) {
                return true;
            }

            return false;
        }

        return false;
    }
}