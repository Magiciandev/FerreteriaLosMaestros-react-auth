package cl.duoc.ferreteria.auth.dto;

public record LoginResponse(
    String token,
    String correo,
    String rol)
{}
