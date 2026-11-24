package cl.duoc.ferreteria.auth.service;

import cl.duoc.ferreteria.auth.dto.LoginRequest;
import cl.duoc.ferreteria.auth.dto.LoginResponse;
import cl.duoc.ferreteria.auth.dto.RegistroRequest;
import cl.duoc.ferreteria.auth.dto.UsuarioRequest;
import cl.duoc.ferreteria.auth.dto.UsuarioResponse;
import cl.duoc.ferreteria.auth.model.Usuario;
import cl.duoc.ferreteria.auth.repository.UsuarioRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private final UsuarioRepository usuarios;
  private final PasswordEncoder encoder;
  private final JwtService jwtService;
  private final UsuarioService usuarioService;

  public AuthService(UsuarioRepository usuarios, PasswordEncoder encoder, JwtService jwtService,
      UsuarioService usuarioService) {
    this.usuarios = usuarios;
    this.encoder = encoder;
    this.jwtService = jwtService;
    this.usuarioService = usuarioService;
  }

  public LoginResponse login(LoginRequest request) {
    Usuario usuario = usuarios.findByCorreoIgnoreCase(request.correo().trim())
        .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));
    if (!encoder.matches(request.password(), usuario.getPassword())) {
      throw new BadCredentialsException("Credenciales inválidas");
    }
    return new LoginResponse(jwtService.generar(usuario), usuario.getCorreo(), usuario.getRol());
  }

  // Un cliente crea su propia cuenta: el rol siempre es CLIENTE, sin importar lo que se envíe.
  public UsuarioResponse registrar(RegistroRequest r) {
    return usuarioService.crear(new UsuarioRequest("CLIENTE", r.run(), r.nombre(), r.apellidos(),
        r.correo(), r.fechaNacimiento(), r.tipoCliente(), r.region(), r.comuna(), r.direccion(),
        r.password()));
  }
}
