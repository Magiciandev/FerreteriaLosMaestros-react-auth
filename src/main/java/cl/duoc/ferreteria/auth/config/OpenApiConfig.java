package cl.duoc.ferreteria.auth.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
  title = "Ferretería Los Maestros: Auth API",
  version = "1.0",
  description = "Inicio de sesión, registro de clientes, gestión de usuarios y emisión de JWT"
))
public class OpenApiConfig {}
