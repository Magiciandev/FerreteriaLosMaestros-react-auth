package cl.duoc.ferreteria.auth.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

// Datos con los que un cliente crea su cuenta. No trae "rol": el servidor siempre crea un CLIENTE.
public record RegistroRequest(
  @NotBlank(message = "es obligatorio") String run,
  @NotBlank(message = "es obligatorio") @Size(max = 50, message = "máximo 50 caracteres") String nombre,
  @NotBlank(message = "es obligatorio") @Size(max = 100, message = "máximo 100 caracteres") String apellidos,
  @NotBlank(message = "es obligatorio") @Email(message = "debe ser un correo válido")
    @Size(max = 100, message = "máximo 100 caracteres") String correo,
  @Past(message = "debe ser una fecha pasada") LocalDate fechaNacimiento,
  @NotBlank(message = "es obligatorio")
    @Pattern(regexp = "particular|contratista", message = "debe ser particular o contratista") String tipoCliente,
  @NotBlank(message = "es obligatoria") String region,
  @NotBlank(message = "es obligatoria") String comuna,
  @NotBlank(message = "es obligatoria") @Size(max = 300, message = "máximo 300 caracteres") String direccion,
  @NotBlank(message = "es obligatoria") @Size(min = 4, max = 72, message = "debe tener entre 4 y 72 caracteres") String password
) {}
