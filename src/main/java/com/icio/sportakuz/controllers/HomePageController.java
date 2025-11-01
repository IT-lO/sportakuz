package com.icio.sportakuz.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomePageController {

	@GetMapping("/")
	public String index() {
		return "index"; // /resources/templates/index.html
	}

	@GetMapping("/hello")
	public String hello(Model model) {
		model.addAttribute("message", "messagemessagemessagemessagemessage");
		return "HomePageView"; // /resources/templates/HomePageView.html
	}
}