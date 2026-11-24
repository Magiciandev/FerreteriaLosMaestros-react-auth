package cl.duoc.ferreteria.auth.dto;

import cl.duoc.ferreteria.auth.model.Usuario;
import java.time.LocalDate;

// Un usuario tal como lo ve el front: nunca incluye la contraseña.
public record UsuarioResponse(
    Long id,
    String rol,
    String run,
    String nombre,
    String apellidos,
    String correo,
    LocalDate fechaNacimiento,
    String tipoCliente,
    String region,
    String comuna,
    String direccion)
{
  public static UsuarioResponse desde(Usuario u) {
    return new UsuarioResponse(u.getId(), u.getRol(), u.getRun(), u.getNombre(), u.getApellidos(),
      u.getCorreo(), u.getFechaNacimiento(), u.getTipoCliente(), u.getRegion(), u.getComuna(),
      u.getDireccion());
  }
}
