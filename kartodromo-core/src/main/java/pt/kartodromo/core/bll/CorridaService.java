package pt.kartodromo.core.bll;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import pt.kartodromo.core.dal.CategoriaKartDao;
import pt.kartodromo.core.dal.ClienteDao;
import pt.kartodromo.core.dal.CorridaDao;
import pt.kartodromo.core.model.CategoriaKart;
import pt.kartodromo.core.model.Cliente;
import pt.kartodromo.core.model.Corrida;

public class CorridaService {

    private static final LocalTime HORA_ABERTURA = LocalTime.of(9, 0);
    private static final LocalTime HORA_FECHO = LocalTime.of(22, 0);

    private final CorridaDao corridaDao;
    private final CategoriaKartDao categoriaKartDao;
    private final ClienteDao clienteDao;

    public CorridaService() {
        this(new CorridaDao(), new CategoriaKartDao(), new ClienteDao());
    }

    public CorridaService(
            CorridaDao corridaDao,
            CategoriaKartDao categoriaKartDao,
            ClienteDao clienteDao
    ) {
        this.corridaDao = corridaDao;
        this.categoriaKartDao = categoriaKartDao;
        this.clienteDao = clienteDao;
    }

    public Corrida criarCorrida(
            LocalDateTime dataHoraInicio,
            int duracaoMinutos,
            int vagasMaximas,
            Long categoriaId,
            Long clienteId,
            String layoutNome
    ) {
        CategoriaKart categoria = obterCategoria(categoriaId);
        Cliente cliente = obterCliente(clienteId);

        validarRegrasOperacionais(
                dataHoraInicio,
                duracaoMinutos,
                vagasMaximas,
                layoutNome,
                categoria,
                cliente,
                null
        );

        return corridaDao.save(new Corrida(
                dataHoraInicio,
                duracaoMinutos,
                vagasMaximas,
                layoutNome,
                cliente,
                categoria
        ));
    }

    public Corrida atualizarCorrida(
            Long corridaId,
            LocalDateTime dataHoraInicio,
            int duracaoMinutos,
            int vagasMaximas,
            Long categoriaId,
            Long clienteId,
            String layoutNome
    ) {
        Corrida corrida = obterCorridaPorId(corridaId);
        CategoriaKart categoria = obterCategoria(categoriaId);
        Cliente cliente = obterCliente(clienteId);

        validarRegrasOperacionais(
                dataHoraInicio,
                duracaoMinutos,
                vagasMaximas,
                layoutNome,
                categoria,
                cliente,
                corridaId
        );

        corrida.setDataHoraInicio(dataHoraInicio);
        corrida.setDuracaoMinutos(duracaoMinutos);
        corrida.setVagasMaximas(vagasMaximas);
        corrida.setLayoutNome(layoutNome.trim());
        corrida.setCliente(cliente);
        corrida.setCategoria(categoria);

        return corridaDao.update(corrida);
    }

    public void eliminarCorrida(Long corridaId) {
        if (corridaId == null) {
            throw new BusinessException("Corrida invalida.");
        }

        if (corridaDao.findById(corridaId).isEmpty()) {
            throw new BusinessException("Corrida nao encontrada: " + corridaId);
        }

        corridaDao.deleteById(corridaId);
    }

    public Corrida obterCorridaPorId(Long corridaId) {
        if (corridaId == null) {
            throw new BusinessException("Corrida invalida.");
        }

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

    private CategoriaKart obterCategoria(Long categoriaId) {
        if (categoriaId == null) {
            throw new BusinessException("Categoria da corrida e obrigatoria.");
        }

        return categoriaKartDao.findById(categoriaId)
                .orElseThrow(() -> new BusinessException("Categoria nao encontrada: " + categoriaId));
    }

    private Cliente obterCliente(Long clienteId) {
        if (clienteId == null) {
            throw new BusinessException("Cliente da corrida e obrigatorio.");
        }

        return clienteDao.findById(clienteId)
                .orElseThrow(() -> new BusinessException("Cliente nao encontrado: " + clienteId));
    }

    private void validarRegrasOperacionais(
            LocalDateTime dataHoraInicio,
            int duracaoMinutos,
            int vagasMaximas,
            String layoutNome,
            CategoriaKart categoria,
            Cliente cliente,
            Long corridaIgnorarId
    ) {
        if (dataHoraInicio == null || dataHoraInicio.isBefore(LocalDateTime.now())) {
            throw new BusinessException("Data/hora da corrida deve estar no futuro.");
        }

        if (duracaoMinutos < 5 || duracaoMinutos > 60) {
            throw new BusinessException("Duracao da corrida deve estar entre 5 e 60 minutos.");
        }

        if (vagasMaximas <= 0 || vagasMaximas > 20) {
            throw new BusinessException("Numero maximo de participantes invalido.");
        }

        if (layoutNome == null || layoutNome.isBlank()) {
            throw new BusinessException("Layout da pista e obrigatorio.");
        }

        if (cliente.getIdadeAtual() < categoria.getIdadeMinima()) {
            throw new BusinessException(
                    "Cliente nao cumpre idade minima para esta categoria. Minimo: "
                    + categoria.getIdadeMinima()
            );
        }

        if (cliente.getNivelExperiencia() < categoria.getExperienciaMinima()) {
            throw new BusinessException(
                    "Cliente nao cumpre experiencia minima para esta categoria. Minimo: "
                    + categoria.getExperienciaMinima()
            );
        }

        LocalDateTime dataHoraFim = dataHoraInicio.plusMinutes(duracaoMinutos);

        if (!dataHoraInicio.toLocalDate().equals(dataHoraFim.toLocalDate())) {
            throw new BusinessException("Corrida deve iniciar e terminar no mesmo dia.");
        }

        if (dataHoraInicio.toLocalTime().isBefore(HORA_ABERTURA)
                || dataHoraFim.toLocalTime().isAfter(HORA_FECHO)) {
            throw new BusinessException(
                    "Horario invalido. Funcionamento do kartodromo: "
                    + HORA_ABERTURA + " - " + HORA_FECHO + "."
            );
        }

        validarDisponibilidadePista(layoutNome.trim(), dataHoraInicio, dataHoraFim, corridaIgnorarId);
    }

    private void validarDisponibilidadePista(
            String layoutNome,
            LocalDateTime dataHoraInicio,
            LocalDateTime dataHoraFim,
            Long corridaIgnorarId
    ) {
        List<Corrida> corridasNoLayout = corridaDao.findByLayoutNome(layoutNome);

        for (Corrida corridaExistente : corridasNoLayout) {
            if (corridaIgnorarId != null && corridaIgnorarId.equals(corridaExistente.getId())) {
                continue;
            }

            LocalDateTime inicioExistente = corridaExistente.getDataHoraInicio();
            LocalDateTime fimExistente = corridaExistente.getDataHoraFim();

            boolean sobreposicao = dataHoraInicio.isBefore(fimExistente)
                    && dataHoraFim.isAfter(inicioExistente);

            if (sobreposicao) {
                throw new BusinessException("Pista indisponivel no horario pretendido.");
            }
        }
    }
}
