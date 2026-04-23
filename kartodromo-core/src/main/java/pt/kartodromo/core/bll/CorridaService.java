package pt.kartodromo.core.bll;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import pt.kartodromo.core.dal.CategoriaKartDao;
import pt.kartodromo.core.dal.CorridaDao;
import pt.kartodromo.core.model.CategoriaKart;
import pt.kartodromo.core.model.Corrida;

public class CorridaService {

    private final CorridaDao corridaDao;
    private final CategoriaKartDao categoriaKartDao;

    public CorridaService() {
        this(new CorridaDao(), new CategoriaKartDao());
    }

    public CorridaService(CorridaDao corridaDao, CategoriaKartDao categoriaKartDao) {
        this.corridaDao = corridaDao;
        this.categoriaKartDao = categoriaKartDao;
    }

    public Corrida criarCorrida(
            LocalDateTime dataHoraInicio,
            int duracaoMinutos,
            int vagasMaximas,
            Long categoriaId,
            String layoutNome
    ) {

        if (dataHoraInicio == null || dataHoraInicio.isBefore(LocalDateTime.now())) {
            throw new BusinessException("Data/hora da corrida deve estar no futuro.");
        }

        if (duracaoMinutos < 5) {
            throw new BusinessException("Duracao minima da corrida e 5 minutos.");
        }

        if (vagasMaximas <= 0 || vagasMaximas > 20) {
            throw new BusinessException("Numero de vagas invalido.");
        }

        if (layoutNome == null || layoutNome.isBlank()) {
            throw new BusinessException("Layout da pista e obrigatorio.");
        }

        CategoriaKart categoria = categoriaKartDao.findById(categoriaId)
                .orElseThrow(() -> new BusinessException("Categoria nao encontrada: " + categoriaId));

        Corrida corrida = new Corrida(
                dataHoraInicio,
                duracaoMinutos,
                vagasMaximas,
                layoutNome,
                categoria
        );

        return corridaDao.save(corrida);
    }

    public Corrida obterCorridaPorId(Long corridaId) {
        return corridaDao.findById(corridaId)
                .orElseThrow(() -> new BusinessException("Corrida nao encontrada: " + corridaId));
    }

    public List<Corrida> listarCorridasPorDia(LocalDate dia) {

        if (dia == null) {
            throw new BusinessException("Dia de consulta e obrigatorio.");
        }

        return corridaDao.findByDia(dia);
    }

    public List<Corrida> listarCorridas() {
        return corridaDao.findAll();
    }
}
