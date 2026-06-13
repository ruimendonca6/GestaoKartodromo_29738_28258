package pt.kartodromo.core.bll;

import java.time.LocalDateTime;
import java.util.List;

import pt.kartodromo.core.dal.ResultadoDao;
import pt.kartodromo.core.model.Resultado;

public class ResultadoService {

    private final ResultadoDao resultadoDao;

    public ResultadoService() {
        this(new ResultadoDao());
    }

    public ResultadoService(ResultadoDao resultadoDao) {
        this.resultadoDao = resultadoDao;
    }

    public Resultado registar(String nomePiloto, String nomeCorrida, long tempoMs,
                              int posicao, LocalDateTime dataCorrida) {
        if (nomePiloto == null || nomePiloto.isBlank()) {
            throw new BusinessException("O nome do piloto é obrigatório.");
        }
        if (nomeCorrida == null || nomeCorrida.isBlank()) {
            throw new BusinessException("O nome da corrida é obrigatório.");
        }
        if (tempoMs <= 0) {
            throw new BusinessException("O tempo deve ser maior que zero.");
        }
        if (posicao <= 0) {
            throw new BusinessException("A posição deve ser maior que zero.");
        }

        return resultadoDao.save(new Resultado(
                nomePiloto.trim(), nomeCorrida.trim(), tempoMs, posicao,
                dataCorrida != null ? dataCorrida : LocalDateTime.now()
        ));
    }

    public void eliminar(Long id) {
        resultadoDao.deleteById(id);
    }

    public List<Resultado> listarTodos() {
        return resultadoDao.findAll();
    }

    public List<Resultado> listarPorCorrida(String nomeCorrida) {
        return resultadoDao.findByCorrida(nomeCorrida);
    }

    public List<Resultado> listarMelhorVoltaPorPiloto() {
        return resultadoDao.findMelhorVoltaPorPiloto();
    }

    public List<Resultado> listarClassificacaoGeral() {
        return resultadoDao.findClassificacaoGeral();
    }
}
