package com.recipebox.App.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;

import com.recipebox.App.component.CustomLoginFailureHandler;
import com.recipebox.App.component.LoginSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private LoginSuccessHandler successHandler;

	@Autowired
	private CustomLoginFailureHandler customLoginFailureHandler;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/", "/home", "/login", "/signup","/user/bmi/bmiForm","/user/bmi/calculate", "/forgot-password", "/css/**",
								"/uploads/**")
						.permitAll()
						.requestMatchers("/admin/**").hasAnyRole("ADMIN", "SUPERADMIN")
						.requestMatchers("/user/**").hasRole("USER")
						.anyRequest().authenticated())
				.formLogin(form -> form
						.loginPage("/login")
						.usernameParameter("email") 
						.passwordParameter("password")
						.successHandler(successHandler)
						.failureHandler(customLoginFailureHandler) 
						.permitAll())

				.logout(logout -> logout
						.logoutUrl("/logout")
						.logoutSuccessUrl("/login?logout=true")
						.invalidateHttpSession(true)
						.clearAuthentication(true)
						.deleteCookies("JSESSIONID", "remember-me")
						.permitAll())
				.rememberMe(remember -> remember
						.key("FlavorFoldSecretKey")
						.tokenValiditySeconds(86400 * 30)
						.rememberMeParameter("remember-me")
						.userDetailsService(userDetailsService));

		return http.build();
	}

	@Bean
	public HttpSessionEventPublisher httpSessionEventPublisher() {
		return new HttpSessionEventPublisher();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SpringSecurityDialect springSecurityDialect() {
		return new SpringSecurityDialect();
	}
}