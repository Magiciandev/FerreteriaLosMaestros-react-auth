package cl.duoc.ferreteria.auth.repository;

import cl.duoc.ferreteria.auth.model.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
  Optional<Usuario> findByCorreoIgnoreCase(String correo);
  boolean existsByCorreoIgnoreCase(String correo);
  boolean existsByCorreoIgnoreCaseAndIdNot(String correo, Long id);
  boolean existsByRun(String run);
  boolean existsByRunAndIdNot(String run, Long id);
  long countByRol(String rol);
}
