package pt.kartodromo.web.auth;

import java.io.Serializable;

public class WebAuthUser implements Serializable {

    private final String username;
    private final String email;
    private final String role;

    public WebAuthUser(String username, String email, String role) {
        this.username = username;
        this.email    = email;
        this.role     = role;
    }

    public String getUsername() { return username; }
    public String getEmail()    { return email; }
    public String getRole()     { return role; }

    public boolean isAdmin()       { return "ADMIN".equalsIgnoreCase(role); }
    public boolean isFuncionario() { return "FUNCIONARIO".equalsIgnoreCase(role); }
    public boolean isCliente()     { return "CLIENTE".equalsIgnoreCase(role); }
}
