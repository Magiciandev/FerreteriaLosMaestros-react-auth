package cl.duoc.ferreteria.auth.controller;

import cl.duoc.ferreteria.auth.dto.LoginRequest;
import cl.duoc.ferreteria.auth.dto.LoginResponse;
import cl.duoc.ferreteria.auth.dto.RegistroRequest;
import cl.duoc.ferreteria.auth.dto.UsuarioResponse;
import cl.duoc.ferreteria.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
  private final AuthService auth;
  public AuthController(AuthService auth) { this.auth = auth; }

  @PostMapping("/login")
  public LoginResponse login(@Valid @RequestBody LoginRequest request) {
    return auth.login(request);
  }

  @PostMapping("/registro")
  @ResponseStatus(HttpStatus.CREATED)
  public UsuarioResponse registro(@Valid @RequestBody RegistroRequest request) {
    return auth.registrar(request);
  }
}
