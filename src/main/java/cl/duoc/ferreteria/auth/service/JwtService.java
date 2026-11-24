package cl.duoc.ferreteria.auth.service;

import cl.duoc.ferreteria.auth.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  @Value("${app.jwt.secret}")
  private String secret;

  @Value("${app.jwt.expiration-ms}")
  private long expirationMs;

  @Value("${app.jwt.issuer}")
  private String issuer;

  private SecretKey clave() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
  }

  // Token firmado (HS256): "sub" = correo, "rol" = rol del usuario, "exp" = vencimiento.
  // El servicio de la tienda (ferreteria-back) y el front leen estos mismos datos.
  public String generar(Usuario usuario) {
    Date ahora = new Date();
    return Jwts.builder()
        .issuer(issuer)
        .subject(usuario.getCorreo())
        .claim("rol", usuario.getRol())
        .issuedAt(ahora)
        .expiration(new Date(ahora.getTime() + expirationMs))
        .signWith(clave())
        .compact();
  }

  public Claims validar(String token) {
    return Jwts.parser()
        .verifyWith(clave())
        .requireIssuer(issuer)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}
