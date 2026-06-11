package pt.kartodromo.desktop.ui.auth;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class AuthService {

    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin";
    private static final String DEFAULT_ADMIN_EMAIL = "admin@kartodromo.pt";

    private final File usersFile;

    public AuthService() {
        File appDirectory = new File(
                System.getProperty("user.home"),
                ".kartodromo-desktop"
        );

        if (!appDirectory.exists()) {
            appDirectory.mkdirs();
        }

        usersFile = new File(appDirectory, "users.properties");

        ensureDefaultAdmin();
    }

    public Optional<AuthUser> login(String username, String password) {
        String normalizedUsername = normalizeUsername(username);

        if (normalizedUsername.isBlank() || password == null || password.isBlank()) {
            return Optional.empty();
        }

        Properties users = loadUsers();
        String storedValue = users.getProperty(normalizedUsername);

        if (storedValue == null) {
            return Optional.empty();
        }

        UserRecord record = parseUserRecord(storedValue);

        if (!record.passwordHash.equals(hashPassword(password))) {
            return Optional.empty();
        }

        return Optional.of(
                new AuthUser(
                        normalizedUsername,
                        record.email,
                        record.role
                )
        );
    }

    public List<AuthUser> listarUtilizadores() {
        Properties users = loadUsers();
        List<AuthUser> result = new ArrayList<>();
        for (String username : users.stringPropertyNames()) {
            UserRecord record = parseUserRecord(users.getProperty(username));
            result.add(new AuthUser(username, record.email, record.role));
        }
        result.sort((a, b) -> a.getUsername().compareTo(b.getUsername()));
        return result;
    }

    public void eliminarUtilizador(String username) {
        String normalized = normalizeUsername(username);
        if (normalized.equals(DEFAULT_ADMIN_USERNAME)) {
            throw new IllegalArgumentException("Não é possível eliminar o administrador padrão.");
        }
        Properties users = loadUsers();
        if (!users.containsKey(normalized)) {
            throw new IllegalArgumentException("Utilizador não encontrado.");
        }
        users.remove(normalized);
        saveUsers(users);
    }

    public AuthUser criarUtilizadorAdmin(
            String username,
            String email,
            String password,
            AuthRole role) {

        String normalizedUsername = normalizeUsername(username);
        String normalizedEmail = normalizeEmail(email);

        if (normalizedUsername.isBlank() || normalizedUsername.length() < 3) {
            throw new IllegalArgumentException("O utilizador deve ter pelo menos 3 caracteres.");
        }
        if (normalizedEmail.isBlank() || !normalizedEmail.contains("@")) {
            throw new IllegalArgumentException("Email inválido.");
        }
        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException("A password deve ter pelo menos 4 caracteres.");
        }
        if (role == null) {
            throw new IllegalArgumentException("Selecione o perfil.");
        }

        Properties users = loadUsers();
        if (users.containsKey(normalizedUsername)) {
            throw new IllegalArgumentException("Já existe uma conta com esse utilizador.");
        }

        users.setProperty(
                normalizedUsername,
                buildUserValue(hashPassword(password), role, normalizedEmail)
        );
        saveUsers(users);

        return new AuthUser(normalizedUsername, normalizedEmail, role);
    }

    public AuthUser atualizarUtilizadorAdmin(
            String username,
            String email,
            AuthRole role,
            String newPassword) {

        String normalized = normalizeUsername(username);
        String normalizedEmail = normalizeEmail(email);

        if (normalizedEmail.isBlank() || !normalizedEmail.contains("@")) {
            throw new IllegalArgumentException("Email inválido.");
        }
        if (role == null) {
            throw new IllegalArgumentException("Perfil obrigatório.");
        }
        if (normalized.equals(DEFAULT_ADMIN_USERNAME) && role != AuthRole.ADMIN) {
            throw new IllegalArgumentException("Não é possível alterar o perfil do administrador padrão.");
        }

        Properties users = loadUsers();
        if (!users.containsKey(normalized)) {
            throw new IllegalArgumentException("Utilizador não encontrado.");
        }

        UserRecord current = parseUserRecord(users.getProperty(normalized));
        String passwordHash = (newPassword != null && !newPassword.isBlank())
                ? hashPassword(newPassword)
                : current.passwordHash;

        users.setProperty(normalized, buildUserValue(passwordHash, role, normalizedEmail));
        saveUsers(users);

        return new AuthUser(normalized, normalizedEmail, role);
    }

    public void register(
            String username,
            String email,
            String password,
            String confirmPassword,
            AuthRole role) {

        String normalizedUsername = normalizeUsername(username);
        String normalizedEmail = normalizeEmail(email);

        validateUserData(
                normalizedUsername,
                normalizedEmail,
                password,
                confirmPassword,
                role
        );

        if (role == AuthRole.ADMIN) {
            throw new IllegalArgumentException("Não é possível criar contas ADMIN pelo registo.");
        }

        Properties users = loadUsers();

        if (users.containsKey(normalizedUsername)) {
            throw new IllegalArgumentException("Já existe uma conta com esse utilizador.");
        }

        users.setProperty(
                normalizedUsername,
                buildUserValue(
                        hashPassword(password),
                        role,
                        normalizedEmail
                )
        );

        saveUsers(users);
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

        String normalizedEmail = normalizeEmail(newEmail);

        if (normalizedEmail.isBlank() || !normalizedEmail.contains("@")) {
            throw new IllegalArgumentException("Email inválido.");
        }

        Properties users = loadUsers();
        String storedValue = users.getProperty(currentUser.getUsername());

        if (storedValue == null) {
            throw new IllegalArgumentException("Utilizador não encontrado.");
        }

        UserRecord record = parseUserRecord(storedValue);

        String passwordHash = record.passwordHash;

        boolean wantsToChangePassword =
                newPassword != null && !newPassword.isBlank();

        if (wantsToChangePassword) {
            if (currentPassword == null || currentPassword.isBlank()) {
                throw new IllegalArgumentException("Indique a password atual.");
            }

            if (!passwordHash.equals(hashPassword(currentPassword))) {
                throw new IllegalArgumentException("Password atual incorreta.");
            }

            if (newPassword.length() < 4) {
                throw new IllegalArgumentException("A nova password deve ter pelo menos 4 caracteres.");
            }

            if (!newPassword.equals(confirmNewPassword)) {
                throw new IllegalArgumentException("As novas passwords não coincidem.");
            }

            passwordHash = hashPassword(newPassword);
        }

        users.setProperty(
                currentUser.getUsername(),
                buildUserValue(
                        passwordHash,
                        record.role,
                        normalizedEmail
                )
        );

        saveUsers(users);

        return new AuthUser(
                currentUser.getUsername(),
                normalizedEmail,
                record.role
        );
    }

    private void validateUserData(
            String username,
            String email,
            String password,
            String confirmPassword,
            AuthRole role) {

        if (username.isBlank()) {
            throw new IllegalArgumentException("O utilizador é obrigatório.");
        }

        if (username.length() < 3) {
            throw new IllegalArgumentException("O utilizador deve ter pelo menos 3 caracteres.");
        }

        if (email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException("Email inválido.");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("A password é obrigatória.");
        }

        if (password.length() < 4) {
            throw new IllegalArgumentException("A password deve ter pelo menos 4 caracteres.");
        }

        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("As passwords não coincidem.");
        }

        if (role == null) {
            throw new IllegalArgumentException("Selecione o tipo de conta.");
        }
    }

    private void ensureDefaultAdmin() {
        Properties users = loadUsers();

        if (!users.containsKey(DEFAULT_ADMIN_USERNAME)) {
            users.setProperty(
                    DEFAULT_ADMIN_USERNAME,
                    buildUserValue(
                            hashPassword(DEFAULT_ADMIN_PASSWORD),
                            AuthRole.ADMIN,
                            DEFAULT_ADMIN_EMAIL
                    )
            );

            saveUsers(users);
        }
    }

    private Properties loadUsers() {
        Properties properties = new Properties();

        if (!usersFile.exists()) {
            return properties;
        }

        try (FileInputStream inputStream = new FileInputStream(usersFile)) {
            properties.load(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar utilizadores.", e);
        }

        return properties;
    }

    private void saveUsers(Properties users) {
        try (FileOutputStream outputStream = new FileOutputStream(usersFile)) {
            users.store(outputStream, "Kartodromo Desktop Users");
        } catch (Exception e) {
            throw new RuntimeException("Erro ao guardar utilizadores.", e);
        }
    }

    private UserRecord parseUserRecord(String value) {
        String[] parts = value.split(";", 3);

        String passwordHash = parts.length > 0 ? parts[0] : "";
        AuthRole role = parts.length > 1 ? AuthRole.valueOf(parts[1]) : AuthRole.FUNCIONARIO;
        String email = parts.length > 2 ? parts[2] : "";

        return new UserRecord(passwordHash, role, email);
    }

    private String buildUserValue(String passwordHash, AuthRole role, String email) {
        return passwordHash + ";" + role.name() + ";" + email;
    }

    private String normalizeUsername(String username) {
        if (username == null) {
            return "";
        }

        return username.trim().toLowerCase();
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }

        return email.trim().toLowerCase();
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] encodedHash = digest.digest(
                    password.getBytes(StandardCharsets.UTF_8)
            );

            StringBuilder hexString = new StringBuilder();

            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);

                if (hex.length() == 1) {
                    hexString.append('0');
                }

                hexString.append(hex);
            }

            return hexString.toString();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao encriptar password.", e);
        }
    }

    private static class UserRecord {

        private final String passwordHash;
        private final AuthRole role;
        private final String email;

        private UserRecord(String passwordHash, AuthRole role, String email) {
            this.passwordHash = passwordHash;
            this.role = role;
            this.email = email;
        }
    }
}