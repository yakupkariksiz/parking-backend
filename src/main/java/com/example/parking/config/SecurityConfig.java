package com.example.parking.config;

import com.example.parking.model.AppUser;
import com.example.parking.repository.AppUserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // iç tool, basit tutuyoruz
                .authorizeHttpRequests(auth -> auth
                        // login sayfasi, static dosyalar serbest
                        .requestMatchers(
                                "/login", "/login.html",
                                "/css/**", "/js/**"
                        ).permitAll()

                        // sadece ADMIN görebilsin:
                        .requestMatchers(
                                "/residents.html",
                                "/stats.html",
                                "/residents/**",
                                "/stats/**",
                                "/locations/**",
                                "/plates/**"
                        ).hasRole("ADMIN")

                        .requestMatchers("/admin/**").hasRole("ADMIN")


                        // sadece login olmus herkes (ADMIN + USER):
                        .requestMatchers(
                                "/",              // index
                                "/index.html",
                                "/scan-entries/**"
                        ).authenticated()

                        // kalan her şey default: authenticated
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login.html")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login.html?logout")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(AppUserRepository userRepository) {
        return username -> {
            AppUser appUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

            return User.withUsername(appUser.getUsername())
                    .password(appUser.getPassword())
                    // stored "ROLE_ADMIN" veya "ROLE_USER"; .roles yerine .authorities kullanıyoruz
                    .authorities(appUser.getRole())
                    .disabled(!appUser.isEnabled())
                    .build();
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
