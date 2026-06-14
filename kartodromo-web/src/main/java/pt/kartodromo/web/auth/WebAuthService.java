package pt.kartodromo.web.auth;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class WebAuthService {

    private static final String DEFAULT_ADMIN = "admin";
    private static final String DEFAULT_PASS  = "admin";
    private static final String DEFAULT_EMAIL = "admin@kartodromo.pt";

    private final File usersFile;

    public WebAuthService() {
        File dir = new File(System.getProperty("user.home"), ".kartodromo-desktop");
        if (!dir.exists()) dir.mkdirs();

        usersFile = new File(dir, "users.properties");
        ensureDefaultAdmin();
    }

    public Optional<WebAuthUser> login(String username, String password) {
        if (username == null || password == null) return Optional.empty();

        String u = username.trim().toLowerCase();
        if (u.isBlank() || password.isBlank()) return Optional.empty();

        Properties users = load();
        String stored = users.getProperty(u);

        if (stored == null) return Optional.empty();

        String[] parts = stored.split(";", 3);
        String hash  = parts.length > 0 ? parts[0] : "";
        String role  = parts.length > 1 ? parts[1] : "CLIENTE";
        String email = parts.length > 2 ? parts[2] : "";

        if (!hash(password).equals(hash)) return Optional.empty();

        return Optional.of(new WebAuthUser(u, email, role));
    }

    public void criar(String username, String email, String password, String role) {
        if (username == null || username.isBlank()) {
            throw new RuntimeException("Username obrigatório.");
        }

        if (password == null || password.isBlank()) {
            throw new RuntimeException("Password obrigatória.");
        }

        if (role == null || role.isBlank()) {
            role = "CLIENTE";
        }

        String u = username.trim().toLowerCase();

        Properties users = load();

        if (users.containsKey(u)) {
            throw new RuntimeException("Utilizador '" + u + "' já existe.");
        }

        users.setProperty(
                u,
                hash(password) + ";" + role.trim().toUpperCase() + ";" + (email == null ? "" : email.trim())
        );

        save(users);
    }

    public void criarContaPublica(String username, String email, String password, String confirmPassword, String role) {
        if (username == null || username.isBlank()) {
            throw new RuntimeException("O nome de utilizador é obrigatório.");
        }

        if (email == null || email.isBlank()) {
            throw new RuntimeException("O email é obrigatório.");
        }

        if (password == null || password.isBlank()) {
            throw new RuntimeException("A password é obrigatória.");
        }

        if (!password.equals(confirmPassword)) {
            throw new RuntimeException("As passwords não coincidem.");
        }

        if (role == null || role.isBlank()) {
            role = "CLIENTE";
        }

        role = role.trim().toUpperCase();

        if (!role.equals("CLIENTE") && !role.equals("FUNCIONARIO")) {
            role = "CLIENTE";
        }

        criar(username, email, password, role);
    }

    public List<WebAuthUser> listarTodos() {
        Properties users = load();
        List<WebAuthUser> lista = new ArrayList<>();

        for (String u : users.stringPropertyNames()) {
            String[] parts = users.getProperty(u).split(";", 3);
            String role  = parts.length > 1 ? parts[1] : "CLIENTE";
            String email = parts.length > 2 ? parts[2] : "";

            lista.add(new WebAuthUser(u, email, role));
        }

        lista.sort((a, b) -> a.getUsername().compareToIgnoreCase(b.getUsername()));
        return lista;
    }

    public void atualizar(String username, String email, String role) {
        String u = username.trim().toLowerCase();

        Properties users = load();
        String stored = users.getProperty(u);

        if (stored == null) {
            throw new RuntimeException("Utilizador não encontrado.");
        }

        String[] parts = stored.split(";", 3);
        String hash = parts[0];

        users.setProperty(
                u,
                hash + ";" + role.trim().toUpperCase() + ";" + (email == null ? "" : email.trim())
        );

        save(users);
    }

    public void alterarPassword(String username, String novaPassword) {
        String u = username.trim().toLowerCase();

        Properties users = load();
        String stored = users.getProperty(u);

        if (stored == null) {
            throw new RuntimeException("Utilizador não encontrado.");
        }

        String[] parts = stored.split(";", 3);
        String role  = parts.length > 1 ? parts[1] : "CLIENTE";
        String email = parts.length > 2 ? parts[2] : "";

        users.setProperty(u, hash(novaPassword) + ";" + role + ";" + email);
        save(users);
    }

    public void remover(String username) {
        String u = username.trim().toLowerCase();

        if (u.equals(DEFAULT_ADMIN)) {
            throw new RuntimeException("Não é possível remover o admin padrão.");
        }

        Properties users = load();

        if (!users.containsKey(u)) {
            throw new RuntimeException("Utilizador não encontrado.");
        }

        users.remove(u);
        save(users);
    }

    private void ensureDefaultAdmin() {
        Properties users = load();

        if (!users.containsKey(DEFAULT_ADMIN)) {
            users.setProperty(DEFAULT_ADMIN, hash(DEFAULT_PASS) + ";ADMIN;" + DEFAULT_EMAIL);
            save(users);
        }
    }

    private Properties load() {
        Properties p = new Properties();

        if (!usersFile.exists()) {
            return p;
        }

        try (FileInputStream in = new FileInputStream(usersFile)) {
            p.load(in);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar utilizadores.", e);
        }

        return p;
    }

    private void save(Properties p) {
        try (FileOutputStream out = new FileOutputStream(usersFile)) {
            p.store(out, "Kartodromo Users");
        } catch (Exception e) {
            throw new RuntimeException("Erro ao guardar utilizadores.", e);
        }
    }

    private String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(password.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();

            for (byte b : bytes) {
                String hex = Integer.toHexString(0xff & b);

                if (hex.length() == 1) {
                    sb.append('0');
                }

                sb.append(hex);
            }

            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}