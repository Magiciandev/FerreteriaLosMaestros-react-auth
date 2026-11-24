package cl.duoc.ferreteria.auth.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Entity @Table(name = "usuarios")
@Getter @Setter
public class Usuario {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false, unique = true)
  private String correo;
  @Column(nullable = false)
  private String password;
  // ADMIN, VENDEDOR o CLIENTE
  @Column(nullable = false)
  private String rol;
  // Solo los clientes necesitan RUN y datos de contacto; para el personal van null.
  @Column(unique = true)
  private String run;
  @Column(nullable = false)
  private String nombre;
  @Column(nullable = false)
  private String apellidos;
  private LocalDate fechaNacimiento;
  private String tipoCliente;
  private String region;
  private String comuna;
  private String direccion;
}
