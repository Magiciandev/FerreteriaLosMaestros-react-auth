package cl.duoc.ferreteria.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import cl.duoc.ferreteria.auth.service.Run;
import org.junit.jupiter.api.Test;

class RunTest {

  @Test
  void normalizaPuntosGuionYMinusculas() {
    assertEquals("12345678-5", Run.normalizar("12345678-5"));
    assertEquals("12345678-5", Run.normalizar("12.345.678-5"));
    assertEquals("12345678-5", Run.normalizar("123456785"));
    assertEquals("10000013-K", Run.normalizar("10.000.013-k"));
    assertEquals("10000004-0", Run.normalizar("10000004-0"));
  }

  @Test
  void rechazaDigitoVerificadorIncorrectoOFormatoInvalido() {
    assertNull(Run.normalizar("12345678-9"));
    assertNull(Run.normalizar("abc"));
    assertNull(Run.normalizar("1234"));
    assertNull(Run.normalizar(null));
  }
}
