package com.ibm.astra.demo.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Tag(name = "Home", description = "Home and utility endpoints")
public class HomeController {

    @GetMapping("/")
    public String redirectToSwaggerUi() {
        return "redirect:/swagger-ui/index.html";
    }

}
