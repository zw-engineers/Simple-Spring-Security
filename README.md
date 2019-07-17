# Account-Security Application

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

Secondly we will create a `UserDetailsService` bean that will contain our two
dummy users we will use in this demo. Both users will have `username` and 
`password` however, notice that both users will have the role `USER`
and one of them will have the role `ADMIN`. This will be crucial when 
we start thinking about `authorisation` later on.

```java
@EnableWebSecurity
class SecurityConfig extends WebSecurityConfigurerAdapter {

	// AUTHENTICATION - Defining you are WHO you say you are.
	@Bean
	UserDetailsService authentication() {
		UserDetails paul = User.builder()
				.username("paul")
				.password("password")
				.roles("USER")
				.build();

		UserDetails artemas = User.builder()
				.username("artemas")
				.password("StrongPassword!")
				.roles("USER", "ADMIN")
				.build();

		System.out.println("	Paul's password: " + paul.getPassword());
		System.out.println("	Artemas's password: " + artemas.getPassword());

		return new InMemoryUserDetailsManager(paul, artemas);
	}
}
```
The code above will work fine. Spring Security will automatically present you 
with a `/login` page which will allow you to login and access the endpoints
we created in our `AccountController`. 

However, when you try to login with our plain text passwords, 
you will notice that you will be able to submit the form however, 
you will not be redirected to access anything within the application. Also you
will be presented with a stack trace:

```java
java.lang.IllegalArgumentException: There is no PasswordEncoder mapped for the id "null"
```

What this means is that the passwords for our users we created are not encoded.
To rectify this we would need to encode our users' passwords which will make
our config look like:

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

Notice that we are using `PasswordEncoderFactories.createDelegatingPasswordEncoder()` which 
by default will encrypt our passwords with `bcrypt` encryption. :smile: