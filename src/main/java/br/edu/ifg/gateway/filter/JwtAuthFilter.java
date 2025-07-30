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

                String serviceOrigin = claims.get("service_origin", String.class);

                if (isAuthorized(role, path, method, serviceOrigin)) {
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
        // Autenticação, é necessário para o login
        if (path.startsWith("/auth") && method == HttpMethod.POST) {
            return true;
        }
        
        // Registro de usuários, necessário pra criar conta
        if (path.equals("/usuario/aluno") && method == HttpMethod.POST) {
            return true;
        }
        
        if (path.equals("/usuario/admin") && method == HttpMethod.POST) {
            return true;
        }
        
        // Verificação de email, necessário pra login, o usuário ainda não vai ter JWT
        if (path.matches("/usuario/aluno/email/.+") && method == HttpMethod.GET) {
            return true;
        }
        
        if (path.matches("/usuario/admin/email/.+") && method == HttpMethod.GET) {
            return true;
        }
        
        return false;
    }

    private boolean isAuthorized(String role, String path, HttpMethod method, String serviceOrigin) {

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

            if (path.matches("/usuario/aluno/\\d+/add-xp") && method == HttpMethod.PUT) {
                return true;
            }

            return false;
        }

        return false;
    }
}