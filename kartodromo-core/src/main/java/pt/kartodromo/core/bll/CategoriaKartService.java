package pt.kartodromo.core.bll;

import java.math.BigDecimal;
import java.util.List;

import pt.kartodromo.core.dal.CategoriaKartDao;
import pt.kartodromo.core.model.CategoriaKart;

public class CategoriaKartService {

    private final CategoriaKartDao categoriaKartDao;

    public CategoriaKartService() {
        this(new CategoriaKartDao());
    }

    public CategoriaKartService(CategoriaKartDao categoriaKartDao) {
        this.categoriaKartDao = categoriaKartDao;
    }

    public CategoriaKart criarCategoria(
            int cilindrada,
            String descricao,
            int idadeMinima,
            int experienciaMinima,
            BigDecimal precoBase
    ) {

        if (cilindrada <= 0) {
            throw new BusinessException("Cilindrada deve ser maior que zero.");
        }

        if (descricao == null || descricao.isBlank()) {
            throw new BusinessException("Descricao da categoria e obrigatoria.");
        }

        if (idadeMinima < 0) {
            throw new BusinessException("Idade minima invalida.");
        }

        if (experienciaMinima < 0 || experienciaMinima > 5) {
            throw new BusinessException("Experiencia minima deve estar entre 0 e 5.");
        }

        if (precoBase == null || precoBase.signum() <= 0) {
            throw new BusinessException("Preco base invalido.");
        }

        CategoriaKart categoria = new CategoriaKart(
                cilindrada,
                descricao,
                idadeMinima,
                experienciaMinima,
                precoBase
        );

        return categoriaKartDao.save(categoria);
    }

    public CategoriaKart obterCategoriaPorId(Long categoriaId) {

        return categoriaKartDao.findById(categoriaId)
                .orElseThrow(() -> new BusinessException(
                "Categoria nao encontrada: " + categoriaId
        ));
    }

    public List<CategoriaKart> listarCategorias() {
        return categoriaKartDao.findAll();
    }
}
