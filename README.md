#Account-Security Application

This application will demonstrate Spring Security in a nutshell.

First we create our endpoints `/everyone` and `/admin` in our controller.

```java
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
```

Of course, the endpoint `/everyone` will be accessed by... well.. everyone and the
`/admin` endpoint will be accessed by... well you guessed it, any user with the
role `ADMIN`.

If we tried to access the endpoints at this stage, Spring will not allow use to
access any of the endpoint and it will throw a 401 `Unauthorized`. In this case,
we would need to specify a user with a password to access any of the endpoints.

## Authentication

To create users with passwords and roles, we will create a config class that will
extend the `WebSecurityConfigurerAdapter`.

```java
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
}
```