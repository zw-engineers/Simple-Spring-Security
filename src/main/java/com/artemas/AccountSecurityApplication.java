package com.artemas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class AccountSecurityApplication {

	public static void main(String[] args) {
		SpringApplication.run(AccountSecurityApplication.class, args);
	}

}

@EnableWebSecurity
class SecurityConfig extends WebSecurityConfigurerAdapter {
	// Since passwords won't work in plain text... we have to use an encoder for passwords to work through login
	private final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

	// AUTHENTICATION - Defining you are WHO you say you are.
	@Bean
	UserDetailsService authentication() {
		UserDetails paul = User.builder()
				.username("paul")
				.password(passwordEncoder.encode("password"))
				.roles("USER")
				.build();

		UserDetails artemas = User.builder()
				.username("artemas")
				.password(passwordEncoder.encode("StrongPassword!"))
				.roles("USER", "ADMIN")
				.build();

		System.out.println("	Paul's password: " + paul.getPassword());
		System.out.println("	Artemas's password: " + artemas.getPassword());

		return new InMemoryUserDetailsManager(paul, artemas);
	}

	@Override // AUTHORISATION - Are you authorised to access this resource?
	protected void configure(HttpSecurity http) throws Exception {
		http
				.authorizeRequests()
				.mvcMatchers("/admin/**").hasRole("ADMIN")
				.and()
				.authorizeRequests().anyRequest().authenticated()
				.and()
				.formLogin()
				.and()
				.httpBasic();
	}
}

@RestController
class AccountController {
	@GetMapping("/everyone")
	String getEveryone() {
		return "Hello Everyone";
	}

	@GetMapping("/admin")
	String getAdmin() {
		return "<h1>Administrator Page</h1> Greetings Admin!";
	}
}