package cl.duoc.ferreteria.auth.dto;

import jakarta.validation.constraints.*;

public record LoginRequest(
  @NotBlank(message = "es obligatorio") @Email(message = "debe ser un correo válido") String correo,
  @NotBlank(message = "es obligatoria") String password
) {}
