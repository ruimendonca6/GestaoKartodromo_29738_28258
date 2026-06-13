package pt.kartodromo.core.bll;

import java.time.LocalDate;
import java.util.List;

import pt.kartodromo.core.dal.KartDao;
import pt.kartodromo.core.dal.ManutencaoDao;
import pt.kartodromo.core.model.Kart;
import pt.kartodromo.core.model.Manutencao;
import pt.kartodromo.core.model.enums.KartEstado;
import pt.kartodromo.core.model.enums.TipoManutencao;

public class ManutencaoService {

    private final ManutencaoDao manutencaoDao;
    private final KartDao kartDao;

    public ManutencaoService() {
        this(new ManutencaoDao(), new KartDao());
    }

    public ManutencaoService(ManutencaoDao manutencaoDao, KartDao kartDao) {
        this.manutencaoDao = manutencaoDao;
        this.kartDao = kartDao;
    }

    public Manutencao registar(Long kartId, String descricao, TipoManutencao tipo,
                               LocalDate proximaRevisao, boolean bloquearKart) {
        if (descricao == null || descricao.isBlank()) {
            throw new BusinessException("A descrição da manutenção é obrigatória.");
        }
        if (tipo == null) {
            throw new BusinessException("O tipo de manutenção é obrigatório.");
        }

        Kart kart = kartDao.findById(kartId)
                .orElseThrow(() -> new BusinessException("Kart não encontrado: " + kartId));

        if (bloquearKart) {
            kart.setEstado(KartEstado.EM_MANUTENCAO);
            kart.setDisponivel(false);
            kartDao.update(kart);
        }

        Manutencao m = new Manutencao(kart, LocalDate.now(), descricao.trim(), tipo, proximaRevisao);
        return manutencaoDao.save(m);
    }

    public Manutencao concluir(Long manutencaoId, boolean libertarKart) {
        Manutencao m = obterPorId(manutencaoId);
        m.setConcluida(true);
        m.setDataSaida(LocalDate.now());

        if (libertarKart) {
            Kart kart = m.getKart();
            kart.setEstado(KartEstado.OPERACIONAL);
            kart.setDisponivel(true);
            kartDao.update(kart);
        }

        return manutencaoDao.update(m);
    }

    public void bloquearKart(Long kartId) {
        Kart kart = kartDao.findById(kartId)
                .orElseThrow(() -> new BusinessException("Kart não encontrado: " + kartId));
        kart.setEstado(KartEstado.EM_MANUTENCAO);
        kart.setDisponivel(false);
        kartDao.update(kart);
    }

    public void libertarKart(Long kartId) {
        Kart kart = kartDao.findById(kartId)
                .orElseThrow(() -> new BusinessException("Kart não encontrado: " + kartId));
        kart.setEstado(KartEstado.OPERACIONAL);
        kart.setDisponivel(true);
        kartDao.update(kart);
    }

    public Manutencao obterPorId(Long id) {
        return manutencaoDao.findById(id)
                .orElseThrow(() -> new BusinessException("Manutenção não encontrada: " + id));
    }

    public List<Manutencao> listarTodas() {
        return manutencaoDao.findAll();
    }

    public List<Manutencao> listarEmCurso() {
        return manutencaoDao.findEmCurso();
    }

    public List<Manutencao> listarProximasRevisoes(int diasAdiante) {
        return manutencaoDao.findProximasRevisoes(LocalDate.now().plusDays(diasAdiante));
    }

    public List<Manutencao> listarPorKart(Long kartId) {
        return manutencaoDao.findByKartId(kartId);
    }

    public List<Kart> listarKarts() {
        return kartDao.findAll();
    }
}
