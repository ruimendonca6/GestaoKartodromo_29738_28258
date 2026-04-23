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

    public Cliente criarCliente(String nome, LocalDate dataNascimento, String email, int nivelExperiencia) {

        if (nome == null || nome.isBlank()) {
            throw new BusinessException("Nome do cliente e obrigatorio.");
        }

        if (dataNascimento == null || dataNascimento.isAfter(LocalDate.now())) {
            throw new BusinessException("Data de nascimento invalida.");
        }

        int idade = LocalDate.now().getYear() - dataNascimento.getYear();
        if (idade < 6) {
            throw new BusinessException("Cliente demasiado novo para participar.");
        }

        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new BusinessException("Email invalido.");
        }

        email = email.toLowerCase().trim();

        if (nivelExperiencia < 0 || nivelExperiencia > 5) {
            throw new BusinessException("Nivel de experiencia deve estar entre 0 e 5.");
        }

        if (clienteDao.findByEmail(email).isPresent()) {
            throw new BusinessException("Ja existe cliente com o email indicado.");
        }

        Cliente cliente = new Cliente(nome, dataNascimento, email, nivelExperiencia);

        return clienteDao.save(cliente);
    }

    public Cliente atualizarNivelExperiencia(Long clienteId, int novoNivel) {

        if (novoNivel < 0 || novoNivel > 5) {
            throw new BusinessException("Nivel de experiencia deve estar entre 0 e 5.");
        }

        Cliente cliente = obterClientePorId(clienteId);
        cliente.setNivelExperiencia(novoNivel);

        return clienteDao.update(cliente);
    }

    public Cliente obterClientePorId(Long clienteId) {
        return clienteDao.findById(clienteId)
                .orElseThrow(() -> new BusinessException("Cliente nao encontrado: " + clienteId));
    }

    public List<Cliente> listarClientes() {
        return clienteDao.findAll();
    }
}
