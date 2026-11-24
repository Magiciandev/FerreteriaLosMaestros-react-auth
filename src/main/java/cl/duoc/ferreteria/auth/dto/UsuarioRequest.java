package cl.duoc.ferreteria.auth.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

// Datos con los que el administrador crea o edita un usuario. Los datos de cliente (RUN, tipo,
// región, comuna y dirección) solo se exigen cuando el rol es CLIENTE; eso lo revisa UsuarioService.
// Al editar, si "password" viene null se conserva la contraseña actual.
public record UsuarioRequest(
  @NotBlank(message = "es obligatorio")
    @Pattern(regexp = "ADMIN|VENDEDOR|CLIENTE", message = "debe ser ADMIN, VENDEDOR o CLIENTE") String rol,
  String run,
  @NotBlank(message = "es obligatorio") @Size(max = 50, message = "máximo 50 caracteres") String nombre,
  @NotBlank(message = "es obligatorio") @Size(max = 100, message = "máximo 100 caracteres") String apellidos,
  @NotBlank(message = "es obligatorio") @Email(message = "debe ser un correo válido")
    @Size(max = 100, message = "máximo 100 caracteres") String correo,
  @Past(message = "debe ser una fecha pasada") LocalDate fechaNacimiento,
  @Pattern(regexp = "particular|contratista", message = "debe ser particular o contratista") String tipoCliente,
  String region,
  String comuna,
  @Size(max = 300, message = "máximo 300 caracteres") String direccion,
  @Size(min = 4, max = 72, message = "debe tener entre 4 y 72 caracteres") String password
) {}
