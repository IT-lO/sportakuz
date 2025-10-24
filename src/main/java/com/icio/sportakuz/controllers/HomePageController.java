package com.icio.sportakuz.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomePageController {

    @GetMapping("/hello")
    public String hello(Model model) {
        model.addAttribute("message", "Hello from JSP view!");
        return "HomePageView"; // resolves to /WEB-INF/views/HomePageView.jsp
    }
}