package com.infoworks.lab.webapp.config;

import com.infoworks.connect.JDBCDriverClass;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    public static final String[] URL_WHITELIST = {
            "/v2/api-docs"
            , "/swagger-ui.html"
            , "/swagger-ui.html/**"
            , "/webjars/springfox-swagger-ui/**"
            , "/swagger-resources/**"
            , "/swagger-resources/configuration/**"
            , "/actuator/health"
            , "/actuator/prometheus"
            , "/h2-console/**"
            , "/v3/api-docs/**"
            , "/swagger-ui/**"
    };

    @Value("${spring.datasource.driver-class-name}") String activeDriverClass;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .csrf().disable()
                .authorizeRequests().antMatchers(URL_WHITELIST).permitAll()
                .and()
                //.authorizeRequests().anyRequest().authenticated() //enable to restrict all
                .authorizeRequests().antMatchers("/**").permitAll(); //enable to open all
                /*.authorizeRequests()
                .antMatchers("/auth/v1/login"
                        , "/auth/v1/forget"
                        , "/auth/v1/reset")
                        .permitAll()
                .antMatchers("/auth/v1/new/account"
                        , "/account/v1/new/account")
                        .hasAnyRole("ROLE_ADMIN", "ADMIN", "BANK_ADMIN")
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(new AuthorizationFilter(), BasicAuthenticationFilter.class);*/
        //Disable for H2 DB:
        if (activeDriverClass.equalsIgnoreCase(JDBCDriverClass.H2_EMBEDDED.toString())){
            http.headers().frameOptions().disable();
        }
        return http.build();
    }

}
