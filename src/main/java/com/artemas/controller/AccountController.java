package com.artemas.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountController {
	@GetMapping("/everyone")
	String getEveryone() {
		return "Hello Everyone";
	}

	@GetMapping("/admin")
	String getAdmin() {
		return "<h1>Administrator Page</h1> Greetings Admin!";
	}

	@GetMapping("/managers")
	String getManagers() {
		return "<h1>Managers Page</h1> Greetings Manager!";
	}
}
