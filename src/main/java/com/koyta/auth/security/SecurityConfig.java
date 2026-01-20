package com.koyta.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.koyta.auth.dtos.ApiError;
import com.koyta.auth.util.AppConstants;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final AuthenticationSuccessHandler authenticationSuccessHandler;
//    @Bean
//    public UserDetailsService users(){
//
//        User.UserBuilder userBuilder = User.withDefaultPasswordEncoder();
//
//        UserDetails user = userBuilder.username("abhi")
//                .password("abhi123")
//                .roles("ADMIN")
//                .build();
//        UserDetails user1 = userBuilder.username("mayur")
//                .password("mayur123")
//                .roles("ADMIN")
//                .build();
//        UserDetails user2 = userBuilder.username("aditya")
//                .password("aditya123")
//                .roles("ADMIN")
//                .build();
//
//        return new InMemoryUserDetailsManager(user,user2,user2);
//    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // Disable CSRF because this is a stateless REST API using JWT for auth
        http.csrf(csrf -> csrf.disable());
        http.cors(Customizer.withDefaults());

        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.headers(headers ->
                headers.frameOptions(frame -> frame.disable())

        );

        http.authorizeHttpRequests(authorize -> {
            authorize.requestMatchers("/api/v1/auth/**",
                    "/v3/api-docs",
                    "/v3/api-docs/**",
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/h2-console/**").permitAll();

            authorize.anyRequest().authenticated();
        });

        http.oauth2Login(oauth2 ->
                          oauth2.successHandler(authenticationSuccessHandler)
                        . failureHandler(null)
        ).logout(AbstractHttpConfigurer::disable);


        http.exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, ex2) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");

//                    Map<String, Object> error = Map.of(
//                            "status", 401,
//                            "message", ex2.getMessage()
//                    );

                    ApiError apiError = ApiError.of(HttpStatus.UNAUTHORIZED.value(),
                            "Unauthorized Access !!", ex2.getMessage(), request.getRequestURI(),true);
                    ObjectMapper mapper = new ObjectMapper();
                    response.getWriter().write(mapper.writeValueAsString(apiError));
                })
        );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
     //   return new BCryptPasswordEncoder();
	  return NoOpPasswordEncoder.getInstance();

    }




}
