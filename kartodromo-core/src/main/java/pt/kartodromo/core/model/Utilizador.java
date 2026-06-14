package pt.kartodromo.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import pt.kartodromo.core.model.enums.UtilizadorPerfil;

@Entity
@Table(name = "utilizadores")
public class Utilizador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String username;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 128)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UtilizadorPerfil perfil;

    protected Utilizador() {
    }

    public Utilizador(
            String username,
            String email,
            String passwordHash,
            UtilizadorPerfil perfil) {

        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.perfil = perfil;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UtilizadorPerfil getPerfil() {
        return perfil;
    }

    public void setPerfil(UtilizadorPerfil perfil) {
        this.perfil = perfil;
    }
}