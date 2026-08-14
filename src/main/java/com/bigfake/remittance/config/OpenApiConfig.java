package com.bigfake.remittance.config;
import io.swagger.v3.oas.models.OpenAPI; import io.swagger.v3.oas.models.info.Info; import org.springframework.context.annotation.*;
@Configuration public class OpenApiConfig { @Bean public OpenAPI remittanceApi(){return new OpenAPI().info(new Info().title("Legacy Remittance API").version("v1"));} }
