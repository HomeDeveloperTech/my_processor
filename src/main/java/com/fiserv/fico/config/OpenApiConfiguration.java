package com.fiserv.fico.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

  @Bean
  public OpenAPI ficoProcessorOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Fico Processor API")
                .description(
                    "Documentação dos recursos expostos pela aplicação de comparação de FICO.")
                .version("v1"));
  }
}
