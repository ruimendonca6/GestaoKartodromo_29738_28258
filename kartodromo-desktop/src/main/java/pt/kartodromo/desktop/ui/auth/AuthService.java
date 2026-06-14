package pt.kartodromo.desktop.ui.auth;

import java.util.List;
import java.util.Optional;

import pt.kartodromo.core.bll.UtilizadorService;
import pt.kartodromo.core.model.Utilizador;
import pt.kartodromo.core.model.enums.UtilizadorPerfil;

public class AuthService {

    private final UtilizadorService utilizadorService =
            new UtilizadorService();

    public Optional<AuthUser> login(String username, String password) {
        return utilizadorService
                .login(username, password)
                .map(this::toAuthUser);
    }

    public List<AuthUser> listarUtilizadores() {
        return utilizadorService
                .listarUtilizadores()
                .stream()
                .map(this::toAuthUser)
                .toList();
    }

    public void eliminarUtilizador(String username) {
        utilizadorService.eliminarUtilizador(username);
    }

    public AuthUser criarUtilizadorAdmin(
            String username,
            String email,
            String password,
            AuthRole role) {

        Utilizador utilizador =
                utilizadorService.criarUtilizador(
                        username,
                        email,
                        password,
                        toPerfil(role)
                );

        return toAuthUser(utilizador);
    }

    public AuthUser atualizarUtilizadorAdmin(
            String username,
            String email,
            AuthRole role,
            String newPassword) {

        Utilizador utilizador =
                utilizadorService.atualizarUtilizador(
                        username,
                        email,
                        toPerfil(role),
                        newPassword
                );

        return toAuthUser(utilizador);
    }

    public void register(
            String username,
            String email,
            String password,
            String confirmPassword,
            AuthRole role) {

        if (password == null || !password.equals(confirmPassword)) {
            throw new IllegalArgumentException("As passwords não coincidem.");
        }

        if (role == AuthRole.ADMIN) {
            throw new IllegalArgumentException("Não é possível criar contas ADMIN pelo registo.");
        }

        utilizadorService.criarUtilizador(
                username,
                email,
                password,
                toPerfil(role)
        );
    }

    public AuthUser updateProfile(
            AuthUser currentUser,
            String newEmail,
            String currentPassword,
            String newPassword,
            String confirmNewPassword) {

        if (currentUser == null) {
            throw new IllegalArgumentException("Sessão inválida.");
        }

        Utilizador utilizador =
                utilizadorService.atualizarPerfil(
                        currentUser.getUsername(),
                        newEmail,
                        currentPassword,
                        newPassword,
                        confirmNewPassword
                );

        return toAuthUser(utilizador);
    }

    private AuthUser toAuthUser(Utilizador utilizador) {
        return new AuthUser(
                utilizador.getUsername(),
                utilizador.getEmail(),
                toAuthRole(utilizador.getPerfil())
        );
    }

    private UtilizadorPerfil toPerfil(AuthRole role) {
        return switch (role) {
            case CLIENTE -> UtilizadorPerfil.CLIENTE;
            case FUNCIONARIO -> UtilizadorPerfil.FUNCIONARIO;
            case ADMIN -> UtilizadorPerfil.ADMIN;
        };
    }

    private AuthRole toAuthRole(UtilizadorPerfil perfil) {
        return switch (perfil) {
            case CLIENTE -> AuthRole.CLIENTE;
            case FUNCIONARIO -> AuthRole.FUNCIONARIO;
            case ADMIN -> AuthRole.ADMIN;
        };
    }
}