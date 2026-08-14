package com.bigfake.remittance.config;
import org.springframework.beans.factory.annotation.Value; import org.springframework.context.annotation.*; import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.*; import org.springframework.security.core.userdetails.*; import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.security.provisioning.InMemoryUserDetailsManager;
@Configuration @EnableWebSecurity @SuppressWarnings("deprecation")
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Value("${remittance.security.username:admin}") private String username; @Value("${remittance.security.password:changeit}") private String password;
    @Override protected void configure(HttpSecurity http)throws Exception{http.csrf().disable().authorizeRequests().antMatchers("/webhooks/**","/actuator/health","/swagger-ui/**","/swagger-ui.html","/v3/api-docs/**","/h2-console/**").permitAll().anyRequest().authenticated().and().httpBasic();http.headers().frameOptions().disable();}
    @Bean public PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
    @Bean @Override public UserDetailsService userDetailsService(){return new InMemoryUserDetailsManager(User.withUsername(username).password(passwordEncoder().encode(password)).roles("REMITTANCE").build());}
}
