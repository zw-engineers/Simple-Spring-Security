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

_Authentication - Defining you are WHO you say you are._

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

## Authorisation

_Authorisation - Are you authorised to access this resource?_

Now that we can login, you will notice that if you login as the user `Paul`
you will have access to both the `/everyone` endpoint and the `/admin`
endpoint. Now remember, `Paul` has a role `USER` so shouldn't be accessing 
the `/admin` endpoint. This is where `authorisation` comes in.

Within the same config `SecurityConfig` class we would need to override 
the `configure(HttpSecurity)` method from our `WebSecurityConfigurerAdapter`
that we are extending from. In this method we will start to perform authorisation
on every request that is being served to our application and we will be able to
_filter_ those requests to where they can and what they have access to.

Within our `configure()` method we will be able to define our authorisation 
filter chain as below:

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
```

Let's pay attention to the chain here:

```java
http
        .authorizeRequests() // Authorise all requests
        .mvcMatchers("/admin/**").hasRole("ADMIN") // That match `/admin` for any user with the role `ADMIN`
        .and() // and 
        .authorizeRequests().anyRequest().authenticated() // allow all authenticated requests
        .and() // and
        .formLogin() // serve this request with a login page
        .and() // and
        .httpBasic(); // configure basic http authentication only for this request.
```