package pt.kartodromo.core.bll;

import java.time.LocalDate;
import java.util.List;

import pt.kartodromo.core.dal.ClienteDao;
import pt.kartodromo.core.model.Cliente;

public class ClienteService {

    private final ClienteDao clienteDao;

    public ClienteService() {
        this(new ClienteDao());
    }

    public ClienteService(ClienteDao clienteDao) {
        this.clienteDao = clienteDao;
    }

    public Cliente criarCliente(
            String nome,
            LocalDate dataNascimento,
            String email,
            int nivelExperiencia) {

        validarDadosCliente(
                nome,
                dataNascimento,
                email,
                nivelExperiencia
        );

        email = email.toLowerCase().trim();

        if (clienteDao.findByEmail(email).isPresent()) {
            throw new BusinessException(
                    "Já existe cliente com o email indicado."
            );
        }

        Cliente cliente = new Cliente(
                nome.trim(),
                dataNascimento,
                email,
                nivelExperiencia
        );

        return clienteDao.save(cliente);
    }

    public Cliente atualizarCliente(
            Long clienteId,
            String nome,
            LocalDate dataNascimento,
            String email,
            int nivelExperiencia) {

        Cliente cliente = obterClientePorId(clienteId);

        validarDadosCliente(
                nome,
                dataNascimento,
                email,
                nivelExperiencia
        );

        email = email.toLowerCase().trim();

        clienteDao.findByEmail(email).ifPresent(existente -> {
            if (!existente.getId().equals(clienteId)) {
                throw new BusinessException(
                        "Já existe outro cliente com esse email."
                );
            }
        });

        cliente.setNome(nome.trim());
        cliente.setDataNascimento(dataNascimento);
        cliente.setEmail(email);
        cliente.setNivelExperiencia(nivelExperiencia);

        return clienteDao.update(cliente);
    }

    public Cliente atualizarNivelExperiencia(
            Long clienteId,
            int novoNivel) {

        if (novoNivel < 0 || novoNivel > 5) {
            throw new BusinessException(
                    "Nível de experiência deve estar entre 0 e 5."
            );
        }

        Cliente cliente = obterClientePorId(clienteId);

        cliente.setNivelExperiencia(novoNivel);

        return clienteDao.update(cliente);
    }

    public void removerCliente(Long clienteId) {

        Cliente cliente = obterClientePorId(clienteId);

        clienteDao.delete(cliente);
    }

    public Cliente obterClientePorId(Long clienteId) {

        return clienteDao.findById(clienteId)
                .orElseThrow(() ->
                        new BusinessException(
                                "Cliente não encontrado: "
                                        + clienteId
                        )
                );
    }

    public List<Cliente> listarClientes() {
        return clienteDao.findAll();
    }

    private void validarDadosCliente(
            String nome,
            LocalDate dataNascimento,
            String email,
            int nivelExperiencia) {

        if (nome == null || nome.isBlank()) {
            throw new BusinessException(
                    "Nome do cliente é obrigatório."
            );
        }

        if (dataNascimento == null
                || dataNascimento.isAfter(LocalDate.now())) {

            throw new BusinessException(
                    "Data de nascimento inválida."
            );
        }

        int idade =
                LocalDate.now().getYear()
                        - dataNascimento.getYear();

        if (idade < 6) {
            throw new BusinessException(
                    "Cliente demasiado novo para participar."
            );
        }

        if (email == null
                || email.isBlank()
                || !email.contains("@")) {

            throw new BusinessException(
                    "Email inválido."
            );
        }

        if (nivelExperiencia < 0
                || nivelExperiencia > 5) {

            throw new BusinessException(
                    "Nível experiência deve estar entre 0 e 5."
            );
        }
    }
}