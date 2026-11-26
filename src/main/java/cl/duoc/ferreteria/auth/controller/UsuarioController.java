package cl.duoc.ferreteria.auth.controller;

import cl.duoc.ferreteria.auth.dto.UsuarioRequest;
import cl.duoc.ferreteria.auth.dto.UsuarioResponse;
import cl.duoc.ferreteria.auth.service.UsuarioService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// Gestión de usuarios: solo el rol ADMIN (lo exige SecurityConfig).
@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {
  private final UsuarioService usuarios;
  public UsuarioController(UsuarioService usuarios) { this.usuarios = usuarios; }

  @GetMapping
  public List<UsuarioResponse> listar() {
    return usuarios.listar();
  }

  @GetMapping("/{id}")
  public UsuarioResponse buscar(@PathVariable Long id) {
    return usuarios.buscar(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public UsuarioResponse crear(@Valid @RequestBody UsuarioRequest request) {
    return usuarios.crear(request);
  }

  @PutMapping("/{id}")
  public UsuarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest request) {
    return usuarios.actualizar(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void eliminar(@PathVariable Long id) {
    usuarios.eliminar(id);
  }
}
