package com.abdr.employee.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {
    @GetMapping("/home")
    public String showHome() {
        return "home";
    }

    @GetMapping("/error/unauthorized")
    public String showUnauthorizedPage(@RequestParam(name = "message", required = false) String message, Model model) {
        // Pass the error message to the model
        model.addAttribute("error", message != null ? message : "Authentication is required to access this resource.");
        return "error";  // This maps to "error.html" in the resources/templates directory
    }
}
