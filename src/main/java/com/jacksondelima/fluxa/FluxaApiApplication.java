package com.jacksondelima.fluxa;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "Fluxa API",
                description = "API REST para gestao de tarefas e usuarios com autenticacao JWT",
                version = "v1"
        )
)
public class FluxaApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(FluxaApiApplication.class, args);
    }
}
