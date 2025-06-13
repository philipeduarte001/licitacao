package com.api.licitacao.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(SwaggerConfig.class);

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Servidor de Desenvolvimento")
                ))
                .info(new Info()
                        .title("API de Licitação")
                        .description("API para processamento de documentos PDF de licitação, " +
                                    "busca de fornecedores, cotação do dólar e geração de planilhas Excel.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipe de Desenvolvimento")
                                .email("dev@licitacao.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html"))
                );
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String swaggerUrl = "http://localhost:" + serverPort + "/swagger";
        
        logger.info("");
        logger.info("╔══════════════════════════════════════════════════════════════╗");
        logger.info("║                    🚀 APLICAÇÃO INICIADA 🚀                  ║");
        logger.info("╠══════════════════════════════════════════════════════════════╣");
        logger.info("║  📋 Swagger UI disponível em:                                ║");
        logger.info("║  🌐 {}                           ║", swaggerUrl);
        logger.info("║                                                              ║");
        logger.info("║  📚 API Documentation:                                       ║");
        logger.info("║  🔗 http://localhost:{}/api-docs                        ║", serverPort);
        logger.info("║                                                              ║");
        logger.info("║  💡 Use o Swagger para testar os endpoints da API!           ║");
        logger.info("╚══════════════════════════════════════════════════════════════╝");
        logger.info("");
    }
} 