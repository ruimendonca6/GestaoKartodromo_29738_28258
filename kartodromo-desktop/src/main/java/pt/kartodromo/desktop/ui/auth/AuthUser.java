package pt.kartodromo.desktop.ui.auth;

public class AuthUser {

    private final String username;
    private final String email;
    private final AuthRole role;

    public AuthUser(String username, String email, AuthRole role) {
        this.username = username;
        this.email = email;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public AuthRole getRole() {
        return role;
    }

    public boolean isCliente() {
        return role == AuthRole.CLIENTE;
    }

    public boolean isFuncionario() {
        return role == AuthRole.FUNCIONARIO;
    }

    public boolean isAdmin() {
        return role == AuthRole.ADMIN;
    }
}