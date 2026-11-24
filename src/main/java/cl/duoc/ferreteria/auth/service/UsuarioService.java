package cl.duoc.ferreteria.auth.service;

import cl.duoc.ferreteria.auth.dto.UsuarioRequest;
import cl.duoc.ferreteria.auth.dto.UsuarioResponse;
import cl.duoc.ferreteria.auth.exception.ConflictoException;
import cl.duoc.ferreteria.auth.exception.RecursoNoEncontradoException;
import cl.duoc.ferreteria.auth.exception.SolicitudInvalidaException;
import cl.duoc.ferreteria.auth.model.Usuario;
import cl.duoc.ferreteria.auth.repository.UsuarioRepository;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

  private final UsuarioRepository usuarios;
  private final PasswordEncoder encoder;

  public UsuarioService(UsuarioRepository usuarios, PasswordEncoder encoder) {
    this.usuarios = usuarios;
    this.encoder = encoder;
  }

  public List<UsuarioResponse> listar() {
    return usuarios.findAll().stream().map(UsuarioResponse::desde).toList();
  }

  public UsuarioResponse buscar(Long id) {
    return UsuarioResponse.desde(obtener(id));
  }

  @Transactional
  public UsuarioResponse crear(UsuarioRequest datos) {
    if (datos.password() == null) {
      throw new SolicitudInvalidaException("password: es obligatoria");
    }
    String run = validarDatos(datos);
    String correo = datos.correo().trim().toLowerCase();
    if (usuarios.existsByCorreoIgnoreCase(correo)) {
      throw new ConflictoException("Ya existe una cuenta con el correo " + correo);
    }
    if (run != null && usuarios.existsByRun(run)) {
      throw new ConflictoException("Ya existe una cuenta con el RUN " + run);
    }
    Usuario usuario = new Usuario();
    aplicar(usuario, datos, run);
    usuario.setPassword(encoder.encode(datos.password()));
    return UsuarioResponse.desde(usuarios.save(usuario));
  }

  @Transactional
  public UsuarioResponse actualizar(Long id, UsuarioRequest datos) {
    Usuario usuario = obtener(id);
    String run = validarDatos(datos);
    String correo = datos.correo().trim().toLowerCase();
    if (usuarios.existsByCorreoIgnoreCaseAndIdNot(correo, id)) {
      throw new ConflictoException("Ya existe una cuenta con el correo " + correo);
    }
    if (run != null && usuarios.existsByRunAndIdNot(run, id)) {
      throw new ConflictoException("Ya existe una cuenta con el RUN " + run);
    }
    if (esUltimoAdmin(usuario) && !"ADMIN".equals(datos.rol())) {
      throw new ConflictoException("No se puede quitar el rol al último administrador");
    }
    aplicar(usuario, datos, run);
    if (datos.password() != null) {
      usuario.setPassword(encoder.encode(datos.password()));
    }
    return UsuarioResponse.desde(usuarios.save(usuario));
  }

  @Transactional
  public void eliminar(Long id) {
    Usuario usuario = obtener(id);
    if (esUltimoAdmin(usuario)) {
      throw new ConflictoException("No se puede eliminar al último administrador");
    }
    usuarios.delete(usuario);
  }

  private Usuario obtener(Long id) {
    return usuarios.findById(id)
        .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + id));
  }

  private boolean esUltimoAdmin(Usuario usuario) {
    return "ADMIN".equals(usuario.getRol()) && usuarios.countByRol("ADMIN") <= 1;
  }

  // Revisa las reglas que dependen del rol y devuelve el RUN normalizado (o null si no hay).
  // Un cliente debe traer RUN válido, tipo de cliente, región, comuna y dirección.
  private String validarDatos(UsuarioRequest datos) {
    String run = null;
    if (datos.run() != null && !datos.run().isBlank()) {
      run = Run.normalizar(datos.run());
      if (run == null) {
        throw new SolicitudInvalidaException("run: no es un RUN válido");
      }
    }
    if ("CLIENTE".equals(datos.rol())) {
      if (run == null) {
        throw new SolicitudInvalidaException("run: es obligatorio para un cliente");
      }
      exigir(datos.tipoCliente(), "tipoCliente");
      exigir(datos.region(), "region");
      exigir(datos.comuna(), "comuna");
      exigir(datos.direccion(), "direccion");
    }
    return run;
  }

  private static void exigir(String valor, String campo) {
    if (valor == null || valor.isBlank()) {
      throw new SolicitudInvalidaException(campo + ": es obligatorio para un cliente");
    }
  }

  // Copia los datos al usuario. Los datos de cliente solo se guardan si el rol es CLIENTE;
  // en otro caso quedan null, así no queda información vieja al cambiar de rol.
  private static void aplicar(Usuario usuario, UsuarioRequest datos, String run) {
    boolean cliente = "CLIENTE".equals(datos.rol());
    usuario.setRol(datos.rol());
    usuario.setRun(run);
    usuario.setNombre(datos.nombre().trim());
    usuario.setApellidos(datos.apellidos().trim());
    usuario.setCorreo(datos.correo().trim().toLowerCase());
    usuario.setFechaNacimiento(cliente ? datos.fechaNacimiento() : null);
    usuario.setTipoCliente(cliente ? datos.tipoCliente() : null);
    usuario.setRegion(cliente ? datos.region() : null);
    usuario.setComuna(cliente ? datos.comuna() : null);
    usuario.setDireccion(cliente ? datos.direccion().trim() : null);
  }
}
