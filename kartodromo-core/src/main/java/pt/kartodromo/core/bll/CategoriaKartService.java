package pt.kartodromo.core.bll;

import java.math.BigDecimal;
import java.util.List;

import pt.kartodromo.core.dal.CategoriaKartDao;
import pt.kartodromo.core.model.CategoriaKart;

public class CategoriaKartService {

    private final CategoriaKartDao categoriaDao;

    public CategoriaKartService() {
        this(new CategoriaKartDao());
    }

    public CategoriaKartService(CategoriaKartDao categoriaDao) {
        this.categoriaDao = categoriaDao;
    }

    public CategoriaKart criarCategoria(
            int cilindrada,
            String descricao,
            int idadeMinima,
            int experienciaMinima,
            BigDecimal precoBase) {

        validarDadosCategoria(
                cilindrada,
                descricao,
                idadeMinima,
                experienciaMinima,
                precoBase
        );

        CategoriaKart categoria = new CategoriaKart(
                cilindrada,
                descricao.trim(),
                idadeMinima,
                experienciaMinima,
                precoBase
        );

        return categoriaDao.save(categoria);
    }

    public CategoriaKart atualizarCategoria(
            Long categoriaId,
            int cilindrada,
            String descricao,
            int idadeMinima,
            int experienciaMinima,
            BigDecimal precoBase) {

        CategoriaKart categoria = obterCategoriaPorId(categoriaId);

        validarDadosCategoria(
                cilindrada,
                descricao,
                idadeMinima,
                experienciaMinima,
                precoBase
        );

        categoria.setCilindrada(cilindrada);
        categoria.setDescricao(descricao.trim());
        categoria.setIdadeMinima(idadeMinima);
        categoria.setExperienciaMinima(experienciaMinima);
        categoria.setPrecoBase(precoBase);

        return categoriaDao.update(categoria);
    }

    public void removerCategoria(Long categoriaId) {
        CategoriaKart categoria = obterCategoriaPorId(categoriaId);
        categoriaDao.delete(categoria);
    }

    public CategoriaKart obterCategoriaPorId(Long categoriaId) {
        return categoriaDao.findById(categoriaId)
                .orElseThrow(() ->
                        new BusinessException(
                                "Categoria não encontrada: " + categoriaId
                        )
                );
    }

    public List<CategoriaKart> listarCategorias() {
        return categoriaDao.findAll();
    }

    private void validarDadosCategoria(
            int cilindrada,
            String descricao,
            int idadeMinima,
            int experienciaMinima,
            BigDecimal precoBase) {

        if (cilindrada <= 0) {
            throw new BusinessException("A cilindrada deve ser superior a zero.");
        }

        if (descricao == null || descricao.isBlank()) {
            throw new BusinessException("A descrição da categoria é obrigatória.");
        }

        if (idadeMinima < 6) {
            throw new BusinessException("A idade mínima deve ser pelo menos 6 anos.");
        }

        if (experienciaMinima < 0 || experienciaMinima > 5) {
            throw new BusinessException("A experiência mínima deve estar entre 0 e 5.");
        }

        if (precoBase == null || precoBase.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("O preço base deve ser superior a zero.");
        }
    }
}