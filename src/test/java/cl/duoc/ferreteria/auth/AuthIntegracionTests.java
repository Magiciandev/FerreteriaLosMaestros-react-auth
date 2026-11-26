package cl.duoc.ferreteria.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.duoc.ferreteria.auth.repository.UsuarioRepository;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

// Pruebas de la API completa (login, registro y usuarios) con una base de datos en memoria.
// Cada prueba se revierte al terminar (@Transactional), así que parten siempre con los usuarios iniciales.
@SpringBootTest
@Transactional
class AuthIntegracionTests {

  @Autowired WebApplicationContext contexto;
  @Autowired UsuarioRepository usuarios;
  MockMvc mvc;

  @BeforeEach
  void preparar() {
    mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
  }

  // ---------- Ayudas ----------

  private static String json(String... pares) {
    StringBuilder sb = new StringBuilder("{");
    for (int i = 0; i < pares.length; i += 2) {
      if (i > 0) sb.append(",");
      sb.append("\"").append(pares[i]).append("\":").append(pares[i + 1]);
    }
    return sb.append("}").toString();
  }

  private static String txt(String valor) {
    return "\"" + valor + "\"";
  }

  private String respuesta(MockHttpServletRequestBuilder peticion, int estado) throws Exception {
    return mvc.perform(peticion).andExpect(status().is(estado)).andReturn().getResponse().getContentAsString();
  }

  private MockHttpServletRequestBuilder enviar(MockHttpServletRequestBuilder base, String cuerpo) {
    return base.contentType(MediaType.APPLICATION_JSON).content(cuerpo);
  }

  private String login(String correo, String password) throws Exception {
    String cuerpo = json("correo", txt(correo), "password", txt(password));
    String texto = respuesta(enviar(post("/api/v1/auth/login"), cuerpo), 200);
    Matcher m = Pattern.compile("\"token\":\"([^\"]+)\"").matcher(texto);
    assertTrue(m.find(), "La respuesta no trae token: " + texto);
    return m.group(1);
  }

  private String tokenAdmin() throws Exception {
    return login("admin@profesor.duoc.cl", "Cambiar123!");
  }

  private static String bearer(String token) {
    return "Bearer " + token;
  }

  private static String cuerpoVendedor(String correo) {
    return json("rol", txt("VENDEDOR"), "nombre", txt("Luis"), "apellidos", txt("Rojas"),
        "correo", txt(correo), "password", txt("clave123"));
  }

  private static String cuerpoRegistro(String correo, String run) {
    return json("run", txt(run), "nombre", txt("Ana"), "apellidos", txt("Pérez"),
        "correo", txt(correo), "tipoCliente", txt("particular"), "region", txt("Metropolitana de Santiago"),
        "comuna", txt("Santiago"), "direccion", txt("Av. Siempre Viva 123"), "password", txt("clave123"));
  }

  // ---------- Login ----------

  @Test
  void loginCorrectoEntregaTokenCorreoYRol() throws Exception {
    String cuerpo = json("correo", txt("admin@profesor.duoc.cl"), "password", txt("Cambiar123!"));
    String texto = respuesta(enviar(post("/api/v1/auth/login"), cuerpo), 200);

    assertTrue(texto.contains("\"correo\":\"admin@profesor.duoc.cl\""));
    assertTrue(texto.contains("\"rol\":\"ADMIN\""));
    // El payload del JWT trae rol, emisor y vencimiento (el front lee "exp").
    String payload = new String(Base64.getUrlDecoder().decode(login("admin@profesor.duoc.cl", "Cambiar123!").split("\\.")[1]));
    assertTrue(payload.contains("\"rol\":\"ADMIN\""));
    assertTrue(payload.contains("\"iss\":\"ferreteria-auth\""));
    assertTrue(payload.contains("\"exp\":"));
  }

  @Test
  void loginConClaveIncorrectaOCorreoInexistenteResponde401() throws Exception {
    String claveMala = json("correo", txt("admin@profesor.duoc.cl"), "password", txt("mala"));
    String sinCuenta = json("correo", txt("nadie@gmail.com"), "password", txt("Cambiar123!"));

    assertTrue(respuesta(enviar(post("/api/v1/auth/login"), claveMala), 401).contains("Credenciales inválidas"));
    assertTrue(respuesta(enviar(post("/api/v1/auth/login"), sinCuenta), 401).contains("Credenciales inválidas"));
  }

  @Test
  void loginConDatosInvalidosResponde400ConElCampo() throws Exception {
    String texto = respuesta(enviar(post("/api/v1/auth/login"), json("correo", txt("sin-arroba"), "password", txt("x"))), 400);

    assertTrue(texto.startsWith("{\"error\":\"correo: "), texto);
  }

  @Test
  void unTokenInvalidoEnUnaRutaPublicaNoImpideElLogin() throws Exception {
    String cuerpo = json("correo", txt("admin@profesor.duoc.cl"), "password", txt("Cambiar123!"));

    respuesta(enviar(post("/api/v1/auth/login"), cuerpo).header("Authorization", "Bearer token-basura"), 200);
  }

  // ---------- Registro ----------

  @Test
  void registroCreaUnClienteSinDevolverLaClaveYPuedeIniciarSesion() throws Exception {
    String texto = respuesta(enviar(post("/api/v1/auth/registro"), cuerpoRegistro("Ana.Nueva@Gmail.com", "12.345.678-5")), 201);

    assertTrue(texto.contains("\"rol\":\"CLIENTE\""));
    assertTrue(texto.contains("\"run\":\"12345678-5\""));
    assertTrue(texto.contains("\"correo\":\"ana.nueva@gmail.com\""));
    assertFalse(texto.contains("password"));
    login("ana.nueva@gmail.com", "clave123");
  }

  @Test
  void registroRechazaUnCorreoOUnRunRepetidos() throws Exception {
    respuesta(enviar(post("/api/v1/auth/registro"), cuerpoRegistro("ana@gmail.com", "12345678-5")), 201);

    String correoRepetido = respuesta(enviar(post("/api/v1/auth/registro"), cuerpoRegistro("ana@gmail.com", "10000013-K")), 409);
    String runRepetido = respuesta(enviar(post("/api/v1/auth/registro"), cuerpoRegistro("otra@gmail.com", "12345678-5")), 409);

    assertTrue(correoRepetido.contains("Ya existe una cuenta con el correo ana@gmail.com"));
    assertTrue(runRepetido.contains("Ya existe una cuenta con el RUN 12345678-5"));
  }

  @Test
  void registroRechazaUnRunInvalidoOCamposFaltantes() throws Exception {
    String runMalo = respuesta(enviar(post("/api/v1/auth/registro"), cuerpoRegistro("ana@gmail.com", "12345678-9")), 400);
    String vacio = respuesta(enviar(post("/api/v1/auth/registro"), "{}"), 400);

    assertTrue(runMalo.contains("run: no es un RUN válido"));
    assertTrue(vacio.startsWith("{\"error\":\""));
  }

  // ---------- Usuarios (solo ADMIN) ----------

  @Test
  void usuariosSinTokenResponde401() throws Exception {
    respuesta(get("/api/v1/usuarios"), 401);
  }

  @Test
  void usuariosConUnTokenQueNoEsDeAdminResponde403() throws Exception {
    String token = login("vendedor@duoc.cl", "Cambiar123!");

    String texto = respuesta(get("/api/v1/usuarios").header("Authorization", bearer(token)), 403);

    assertTrue(texto.contains("Acceso denegado"));
  }

  @Test
  void elAdminListaLosUsuariosSinLasClaves() throws Exception {
    String texto = respuesta(get("/api/v1/usuarios").header("Authorization", bearer(tokenAdmin())), 200);

    assertTrue(texto.contains("admin@profesor.duoc.cl"));
    assertTrue(texto.contains("vendedor@duoc.cl"));
    assertFalse(texto.contains("password"));
  }

  @Test
  void elAdminCreaUnUsuarioDelPersonalYNoSePuedeRepetirElCorreo() throws Exception {
    String token = tokenAdmin();

    String creado = respuesta(enviar(post("/api/v1/usuarios"), cuerpoVendedor("luis@gmail.com")).header("Authorization", bearer(token)), 201);
    String repetido = respuesta(enviar(post("/api/v1/usuarios"), cuerpoVendedor("LUIS@gmail.com")).header("Authorization", bearer(token)), 409);

    assertTrue(creado.contains("\"rol\":\"VENDEDOR\""));
    assertTrue(creado.contains("\"run\":null"));
    assertTrue(repetido.contains("Ya existe una cuenta con el correo luis@gmail.com"));
    login("luis@gmail.com", "clave123");
  }

  @Test
  void crearUnUsuarioExigeContrasenaYLosDatosDeClienteSiElRolEsCliente() throws Exception {
    String token = tokenAdmin();
    String sinClave = json("rol", txt("VENDEDOR"), "nombre", txt("Luis"), "apellidos", txt("Rojas"), "correo", txt("luis@gmail.com"));
    String clienteIncompleto = json("rol", txt("CLIENTE"), "run", txt("12345678-5"), "nombre", txt("Ana"),
        "apellidos", txt("Pérez"), "correo", txt("ana@gmail.com"), "password", txt("clave123"));

    assertTrue(respuesta(enviar(post("/api/v1/usuarios"), sinClave).header("Authorization", bearer(token)), 400).contains("password: es obligatoria"));
    assertTrue(respuesta(enviar(post("/api/v1/usuarios"), clienteIncompleto).header("Authorization", bearer(token)), 400).contains("tipoCliente: es obligatorio para un cliente"));
  }

  @Test
  void editarUnUsuarioSinEnviarContrasenaConservaLaAnterior() throws Exception {
    String token = tokenAdmin();
    String creado = respuesta(enviar(post("/api/v1/usuarios"), cuerpoVendedor("luis@gmail.com")).header("Authorization", bearer(token)), 201);
    Matcher m = Pattern.compile("\"id\":(\\d+)").matcher(creado);
    assertTrue(m.find());
    String cambios = json("rol", txt("VENDEDOR"), "nombre", txt("Luis Alberto"), "apellidos", txt("Rojas"), "correo", txt("luis@gmail.com"));

    String editado = respuesta(enviar(put("/api/v1/usuarios/" + m.group(1)), cambios).header("Authorization", bearer(token)), 200);

    assertTrue(editado.contains("\"nombre\":\"Luis Alberto\""));
    login("luis@gmail.com", "clave123");
  }

  @Test
  void elAdminEliminaUnUsuarioYDespuesNoExiste() throws Exception {
    String token = tokenAdmin();
    String creado = respuesta(enviar(post("/api/v1/usuarios"), cuerpoVendedor("luis@gmail.com")).header("Authorization", bearer(token)), 201);
    Matcher m = Pattern.compile("\"id\":(\\d+)").matcher(creado);
    assertTrue(m.find());

    respuesta(delete("/api/v1/usuarios/" + m.group(1)).header("Authorization", bearer(token)), 204);
    String texto = respuesta(get("/api/v1/usuarios/" + m.group(1)).header("Authorization", bearer(token)), 404);

    assertTrue(texto.contains("Usuario no encontrado"));
  }

  @Test
  void noSePuedeEliminarNiQuitarElRolAlUltimoAdministrador() throws Exception {
    String token = tokenAdmin();
    Long idAdmin = usuarios.findByCorreoIgnoreCase("admin@profesor.duoc.cl").orElseThrow().getId();
    String comoVendedor = json("rol", txt("VENDEDOR"), "nombre", txt("Administrador"), "apellidos", txt("Los Maestros"), "correo", txt("admin@profesor.duoc.cl"));

    assertTrue(respuesta(delete("/api/v1/usuarios/" + idAdmin).header("Authorization", bearer(token)), 409).contains("último administrador"));
    assertTrue(respuesta(enviar(put("/api/v1/usuarios/" + idAdmin), comoVendedor).header("Authorization", bearer(token)), 409).contains("último administrador"));
    assertEquals(1, usuarios.countByRol("ADMIN"));
  }

  // ---------- CORS ----------

  @Test
  void elFrontEnLocalhost5173PuedeHacerPeticionesDesdeElNavegador() throws Exception {
    mvc.perform(options("/api/v1/usuarios")
        .header("Origin", "http://localhost:5173")
        .header("Access-Control-Request-Method", "GET")
        .header("Access-Control-Request-Headers", "Authorization"))
      .andExpect(status().isOk())
      .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
  }
}
