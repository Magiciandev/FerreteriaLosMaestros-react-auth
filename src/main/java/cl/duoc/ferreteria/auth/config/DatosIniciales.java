package cl.duoc.ferreteria.auth.config;

import cl.duoc.ferreteria.auth.model.Usuario;
import cl.duoc.ferreteria.auth.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

// Usuarios con los que parte el sistema (se crean solo si no existen).
// La contraseña inicial es Cambiar123!; conviene cambiarla fuera del entorno de desarrollo.
@Configuration
public class DatosIniciales {
  @Bean CommandLineRunner usuariosIniciales(UsuarioRepository repo,
      PasswordEncoder encoder) {
    return args -> {
      crear(repo, encoder, "admin@profesor.duoc.cl", "ADMIN", "Administrador");
      crear(repo, encoder, "vendedor@duoc.cl", "VENDEDOR", "Vendedor");
    };
  }

  private static void crear(UsuarioRepository repo, PasswordEncoder encoder,
      String correo, String rol, String nombre) {
    if (repo.findByCorreoIgnoreCase(correo).isEmpty()) {
      Usuario u = new Usuario();
      u.setCorreo(correo);
      u.setPassword(encoder.encode("Cambiar123!"));
      u.setRol(rol);
      u.setNombre(nombre);
      u.setApellidos("Los Maestros");
      repo.save(u);
    }
  }
}
