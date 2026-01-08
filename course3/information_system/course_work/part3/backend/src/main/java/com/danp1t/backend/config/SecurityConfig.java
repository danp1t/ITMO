package com.danp1t.backend.config;

import com.danp1t.backend.service.CustomUserDetailsService;
import com.danp1t.backend.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtService, userDetailsService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/", "/index.html", "/error", "/favicon.ico").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                        // Для AU01 - гостевой режим, разрешаем просмотр постов/комментариев
                        .requestMatchers("/api/posts/**", "/api/comments/**").permitAll() // Разрешаем GET и другие методы для публичного доступа
                        .requestMatchers(HttpMethod.GET, "/api/tournaments/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/tournaments").hasAuthority("OAPI:ROLE:PublishTournament") // TT02
                        .requestMatchers(HttpMethod.PUT, "/api/tournaments/**").hasAuthority("OAPI:ROLE:EditTournament") // TT03
                        .requestMatchers(HttpMethod.DELETE, "/api/tournaments/**").hasAuthority("OAPI:ROLE:DeleteTournament")
                        .requestMatchers(HttpMethod.POST, "/api/posts/**").hasAuthority("OAPI:ROLE:PublishPost")
                        .requestMatchers(HttpMethod.PUT, "/api/posts/**").hasAuthority("OAPI:ROLE:EditPost")
                        .requestMatchers(HttpMethod.DELETE, "/api/posts/**").hasAuthority("OAPI:ROLE:DeletePost")
                        .requestMatchers(HttpMethod.POST, "/api/attachments/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/attachments/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll() // IS01, IS02
                        .requestMatchers(HttpMethod.GET, "/api/products/filtered").permitAll() // IS03, IS04
                        .requestMatchers(HttpMethod.GET, "/api/products/*/availability").permitAll() // IS12
                        .requestMatchers(HttpMethod.GET, "/api/products/*/available").permitAll() // IS12
                        .requestMatchers(HttpMethod.GET, "/api/order-statuses").permitAll()

                                // Защищенные endpoints для товаров
                        .requestMatchers(HttpMethod.POST, "/api/products/**").hasAuthority("OAPI:ROLE:PublishProduct") // IS05
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasAuthority("OAPI:ROLE:EditProduct") // IS07
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasAuthority("OAPI:ROLE:DeleteProduct") // IS06

                                // Защищенные endpoints для информации о товарах
                        .requestMatchers(HttpMethod.GET, "/api/products/stock").hasAuthority("OAPI:ROLE:GetProductInfo") // IS14
                        .requestMatchers(HttpMethod.POST, "/api/product-infos").hasAuthority("OAPI:ROLE:UpdateProductInfo") // IS11
                        .requestMatchers(HttpMethod.PUT, "/api/product-infos/**").hasAuthority("OAPI:ROLE:UpdateProductInfo") // IS11

                                // Защищенные endpoints для заказов (корзины)
                        .requestMatchers(HttpMethod.POST, "/api/orders").authenticated() // IS08, IS15
                        .requestMatchers(HttpMethod.DELETE, "/api/orders/**").authenticated() // IS09

                                // Остальные настройки
                        .anyRequest().authenticated()

                );

        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:5173",
                "http://localhost:8080",
                "http://127.0.0.1:5173",
                "http://127.0.0.1:8080"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}