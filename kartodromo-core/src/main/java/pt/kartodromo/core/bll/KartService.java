package pt.kartodromo.core.bll;

import java.util.List;
import pt.kartodromo.core.dal.CategoriaKartDao;
import pt.kartodromo.core.dal.KartDao;
import pt.kartodromo.core.model.CategoriaKart;
import pt.kartodromo.core.model.Kart;
import pt.kartodromo.core.model.enums.KartEstado;

public class KartService {

    private final KartDao kartDao;
    private final CategoriaKartDao categoriaKartDao;

    public KartService() {
        this(new KartDao(), new CategoriaKartDao());
    }

    public KartService(KartDao kartDao, CategoriaKartDao categoriaKartDao) {
        this.kartDao = kartDao;
        this.categoriaKartDao = categoriaKartDao;
    }

    public Kart criarKart(int numero, KartEstado estado, boolean disponivel, Long categoriaId) {

        if (numero <= 0) {
            throw new BusinessException("Numero do kart deve ser maior que zero.");
        }

        if (estado == null) {
            throw new BusinessException("Estado do kart e obrigatorio.");
        }

        if (disponivel && estado != KartEstado.OPERACIONAL) {
            throw new BusinessException("Apenas karts operacionais podem estar disponiveis.");
        }

        CategoriaKart categoria = categoriaKartDao.findById(categoriaId)
                .orElseThrow(() -> new BusinessException("Categoria nao encontrada: " + categoriaId));

        if (kartDao.findByNumero(numero).isPresent()) {
            throw new BusinessException("Ja existe kart com o numero indicado.");
        }

        Kart kart = new Kart(numero, estado, disponivel, categoria);
        return kartDao.save(kart);
    }

    public Kart atualizarEstado(Long kartId, KartEstado novoEstado) {

        if (novoEstado == null) {
            throw new BusinessException("Novo estado do kart e obrigatorio.");
        }

        Kart kart = obterKartPorId(kartId);
        kart.setEstado(novoEstado);

        if (novoEstado != KartEstado.OPERACIONAL) {
            kart.setDisponivel(false);
        }

        return kartDao.update(kart);
    }

    public Kart definirDisponibilidade(Long kartId, boolean disponivel) {

        Kart kart = obterKartPorId(kartId);

        if (disponivel && kart.getEstado() != KartEstado.OPERACIONAL) {
            throw new BusinessException("Kart nao operacional nao pode ficar disponivel.");
        }

        kart.setDisponivel(disponivel);
        return kartDao.update(kart);
    }

    public Kart obterKartPorId(Long kartId) {
        return kartDao.findById(kartId)
                .orElseThrow(() -> new BusinessException("Kart nao encontrado: " + kartId));
    }

    public List<Kart> listarKarts() {
        return kartDao.findAll();
    }

    public List<Kart> listarKartsDisponiveis() {
        return kartDao.findDisponiveis();
    }
}
