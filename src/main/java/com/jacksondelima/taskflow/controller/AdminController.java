package com.jacksondelima.taskflow.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController {

    @GetMapping("/admin/test")
    public String adminTest() {
        return "Rota ADMIN acessada com sucesso!";
    }
}