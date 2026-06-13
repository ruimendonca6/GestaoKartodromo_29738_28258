package pt.kartodromo.core.bll;

import java.util.List;

import pt.kartodromo.core.dal.PistaDao;
import pt.kartodromo.core.model.Pista;

public class PistaService {

    private final PistaDao pistaDao;

    public PistaService() {
        this(new PistaDao());
    }

    public PistaService(PistaDao pistaDao) {
        this.pistaDao = pistaDao;
    }

    public Pista criarPista(String nome, int comprimento, int capacidade, String imagemPath) {
        validarNome(nome);
        validarComprimento(comprimento);
        validarCapacidade(capacidade);

        if (pistaDao.findByNome(nome.trim()).isPresent()) {
            throw new BusinessException("Já existe uma pista com o nome \"" + nome + "\".");
        }

        return pistaDao.save(new Pista(nome.trim(), comprimento, capacidade, true, imagemPath));
    }

    public Pista atualizarPista(Long id, String nome, int comprimento, int capacidade, String imagemPath) {
        validarNome(nome);
        validarComprimento(comprimento);
        validarCapacidade(capacidade);

        Pista pista = obterPorId(id);

        pistaDao.findByNome(nome.trim()).ifPresent(existente -> {
            if (!existente.getId().equals(id)) {
                throw new BusinessException("Já existe outra pista com o nome \"" + nome + "\".");
            }
        });

        pista.setNome(nome.trim());
        pista.setComprimento(comprimento);
        pista.setCapacidade(capacidade);
        pista.setImagemPath(imagemPath);
        return pistaDao.update(pista);
    }

    public Pista toggleAtiva(Long id) {
        Pista pista = obterPorId(id);
        pista.setAtiva(!pista.isAtiva());
        return pistaDao.update(pista);
    }

    public void eliminarPista(Long id) {
        pistaDao.deleteById(id);
    }

    public Pista obterPorId(Long id) {
        return pistaDao.findById(id)
                .orElseThrow(() -> new BusinessException("Pista não encontrada: " + id));
    }

    public List<Pista> listarPistas() {
        return pistaDao.findAll();
    }

    public List<Pista> listarPistasAtivas() {
        return pistaDao.findAtivas();
    }

    private void validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new BusinessException("O nome da pista é obrigatório.");
        }
    }

    private void validarComprimento(int comprimento) {
        if (comprimento <= 0) {
            throw new BusinessException("O comprimento deve ser maior que zero.");
        }
    }

    private void validarCapacidade(int capacidade) {
        if (capacidade <= 0) {
            throw new BusinessException("A capacidade deve ser maior que zero.");
        }
    }
}
