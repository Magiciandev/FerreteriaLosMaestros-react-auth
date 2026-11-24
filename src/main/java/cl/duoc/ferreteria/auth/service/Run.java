package cl.duoc.ferreteria.auth.service;

// Utilidad para el RUN chileno: lo normaliza al formato 12345678-5 y revisa su dígito verificador.
public final class Run {

  private Run() {}

  // Devuelve el RUN como "cuerpo-DV" (por ejemplo 12345678-5) o null si no es válido.
  // Acepta puntos, guion y la K en minúscula.
  public static String normalizar(String texto) {
    if (texto == null) return null;
    String limpio = texto.replaceAll("[^0-9kK]", "").toUpperCase();
    if (limpio.length() < 8 || limpio.length() > 9) return null;
    String cuerpo = limpio.substring(0, limpio.length() - 1);
    String dv = limpio.substring(limpio.length() - 1);
    if (!cuerpo.matches("[0-9]+")) return null;
    return digitoVerificador(cuerpo).equals(dv) ? cuerpo + "-" + dv : null;
  }

  private static String digitoVerificador(String cuerpo) {
    int suma = 0;
    int factor = 2;
    for (int i = cuerpo.length() - 1; i >= 0; i--) {
      suma += (cuerpo.charAt(i) - '0') * factor;
      factor = factor == 7 ? 2 : factor + 1;
    }
    int resto = 11 - (suma % 11);
    if (resto == 11) return "0";
    if (resto == 10) return "K";
    return String.valueOf(resto);
  }
}
