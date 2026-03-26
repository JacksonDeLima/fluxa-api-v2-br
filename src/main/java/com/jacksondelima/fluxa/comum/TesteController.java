package com.jacksondelima.fluxa.comum;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TesteController {

    @GetMapping("/teste")
    public String test() {
        return "Rota protegida acessada com sucesso.";
    }
}
