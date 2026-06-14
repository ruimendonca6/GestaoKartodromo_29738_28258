package pt.kartodromo.core.bll;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Optional;

import pt.kartodromo.core.dal.UtilizadorDao;
import pt.kartodromo.core.model.Utilizador;
import pt.kartodromo.core.model.enums.UtilizadorPerfil;

public class UtilizadorService {

    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin";
    private static final String DEFAULT_ADMIN_EMAIL = "admin@kartodromo.pt";

    private final UtilizadorDao utilizadorDao;

    public UtilizadorService() {
        this(new UtilizadorDao());
    }

    public UtilizadorService(UtilizadorDao utilizadorDao) {
        this.utilizadorDao = utilizadorDao;
        garantirAdminPadrao();
    }

    public Optional<Utilizador> login(String username, String password) {
        String normalizedUsername = normalizeUsername(username);

        if (normalizedUsername.isBlank()
                || password == null
                || password.isBlank()) {
            return Optional.empty();
        }

        Optional<Utilizador> utilizador =
                utilizadorDao.findByUsername(normalizedUsername);

        if (utilizador.isEmpty()) {
            return Optional.empty();
        }

        String hash =
                hashPassword(password);

        if (!hash.equals(utilizador.get().getPasswordHash())) {
            return Optional.empty();
        }

        return utilizador;
    }

    public Utilizador criarUtilizador(
            String username,
            String email,
            String password,
            UtilizadorPerfil perfil) {

        String normalizedUsername =
                normalizeUsername(username);

        String normalizedEmail =
                normalizeEmail(email);

        validarDados(
                normalizedUsername,
                normalizedEmail,
                password,
                perfil
        );

        if (utilizadorDao.findByUsername(normalizedUsername).isPresent()) {
            throw new BusinessException("Já existe uma conta com esse utilizador.");
        }

        if (utilizadorDao.findByEmail(normalizedEmail).isPresent()) {
            throw new BusinessException("Já existe uma conta com esse email.");
        }

        Utilizador utilizador =
                new Utilizador(
                        normalizedUsername,
                        normalizedEmail,
                        hashPassword(password),
                        perfil
                );

        return utilizadorDao.save(utilizador);
    }

    public Utilizador atualizarUtilizador(
            String username,
            String email,
            UtilizadorPerfil perfil,
            String novaPassword) {

        String normalizedUsername =
                normalizeUsername(username);

        String normalizedEmail =
                normalizeEmail(email);

        Utilizador utilizador =
                utilizadorDao.findByUsername(normalizedUsername)
                        .orElseThrow(() ->
                                new BusinessException("Utilizador não encontrado.")
                        );

        if (normalizedEmail.isBlank() || !normalizedEmail.contains("@")) {
            throw new BusinessException("Email inválido.");
        }

        if (perfil == null) {
            throw new BusinessException("Perfil obrigatório.");
        }

        if (DEFAULT_ADMIN_USERNAME.equals(normalizedUsername)
                && perfil != UtilizadorPerfil.ADMIN) {
            throw new BusinessException("Não é possível alterar o perfil do administrador padrão.");
        }

        utilizadorDao.findByEmail(normalizedEmail).ifPresent(existente -> {
            if (!existente.getId().equals(utilizador.getId())) {
                throw new BusinessException("Já existe outro utilizador com esse email.");
            }
        });

        utilizador.setEmail(normalizedEmail);
        utilizador.setPerfil(perfil);

        if (novaPassword != null && !novaPassword.isBlank()) {
            if (novaPassword.length() < 4) {
                throw new BusinessException("A password deve ter pelo menos 4 caracteres.");
            }

            utilizador.setPasswordHash(
                    hashPassword(novaPassword)
            );
        }

        return utilizadorDao.update(utilizador);
    }

    public Utilizador atualizarPerfil(
            String username,
            String novoEmail,
            String passwordAtual,
            String novaPassword,
            String confirmarNovaPassword) {

        String normalizedUsername =
                normalizeUsername(username);

        String normalizedEmail =
                normalizeEmail(novoEmail);

        Utilizador utilizador =
                utilizadorDao.findByUsername(normalizedUsername)
                        .orElseThrow(() ->
                                new BusinessException("Utilizador não encontrado.")
                        );

        if (normalizedEmail.isBlank() || !normalizedEmail.contains("@")) {
            throw new BusinessException("Email inválido.");
        }

        utilizadorDao.findByEmail(normalizedEmail).ifPresent(existente -> {
            if (!existente.getId().equals(utilizador.getId())) {
                throw new BusinessException("Já existe outro utilizador com esse email.");
            }
        });

        boolean alterarPassword =
                novaPassword != null && !novaPassword.isBlank();

        if (alterarPassword) {
            if (passwordAtual == null || passwordAtual.isBlank()) {
                throw new BusinessException("Indique a password atual.");
            }

            if (!utilizador.getPasswordHash().equals(hashPassword(passwordAtual))) {
                throw new BusinessException("Password atual incorreta.");
            }

            if (novaPassword.length() < 4) {
                throw new BusinessException("A nova password deve ter pelo menos 4 caracteres.");
            }

            if (!novaPassword.equals(confirmarNovaPassword)) {
                throw new BusinessException("As novas passwords não coincidem.");
            }

            utilizador.setPasswordHash(
                    hashPassword(novaPassword)
            );
        }

        utilizador.setEmail(normalizedEmail);

        return utilizadorDao.update(utilizador);
    }

    public void eliminarUtilizador(String username) {
        String normalizedUsername =
                normalizeUsername(username);

        if (DEFAULT_ADMIN_USERNAME.equals(normalizedUsername)) {
            throw new BusinessException("Não é possível eliminar o administrador padrão.");
        }

        Utilizador utilizador =
                utilizadorDao.findByUsername(normalizedUsername)
                        .orElseThrow(() ->
                                new BusinessException("Utilizador não encontrado.")
                        );

        utilizadorDao.delete(utilizador);
    }

    public List<Utilizador> listarUtilizadores() {
        return utilizadorDao.findAllOrderByUsername();
    }

    private void garantirAdminPadrao() {
        if (utilizadorDao.findByUsername(DEFAULT_ADMIN_USERNAME).isEmpty()) {
            Utilizador admin =
                    new Utilizador(
                            DEFAULT_ADMIN_USERNAME,
                            DEFAULT_ADMIN_EMAIL,
                            hashPassword(DEFAULT_ADMIN_PASSWORD),
                            UtilizadorPerfil.ADMIN
                    );

            utilizadorDao.save(admin);
        }
    }

    private void validarDados(
            String username,
            String email,
            String password,
            UtilizadorPerfil perfil) {

        if (username == null || username.isBlank()) {
            throw new BusinessException("O utilizador é obrigatório.");
        }

        if (username.length() < 3) {
            throw new BusinessException("O utilizador deve ter pelo menos 3 caracteres.");
        }

        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new BusinessException("Email inválido.");
        }

        if (password == null || password.isBlank()) {
            throw new BusinessException("A password é obrigatória.");
        }

        if (password.length() < 4) {
            throw new BusinessException("A password deve ter pelo menos 4 caracteres.");
        }

        if (perfil == null) {
            throw new BusinessException("Selecione o perfil.");
        }
    }

    private String normalizeUsername(String username) {
        return username == null
                ? ""
                : username.trim().toLowerCase();
    }

    private String normalizeEmail(String email) {
        return email == null
                ? ""
                : email.trim().toLowerCase();
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] encodedHash =
                    digest.digest(
                            password.getBytes(StandardCharsets.UTF_8)
                    );

            StringBuilder hexString =
                    new StringBuilder();

            for (byte b : encodedHash) {
                String hex =
                        Integer.toHexString(0xff & b);

                if (hex.length() == 1) {
                    hexString.append('0');
                }

                hexString.append(hex);
            }

            return hexString.toString();

        } catch (Exception e) {
            throw new BusinessException("Erro ao encriptar password.");
        }
    }
}