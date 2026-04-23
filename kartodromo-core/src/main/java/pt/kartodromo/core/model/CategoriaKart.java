package pt.kartodromo.core.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "categorias_kart")
public class CategoriaKart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer cilindrada;

    @Column(nullable = false, length = 120)
    private String descricao;

    @Column(name = "idade_minima", nullable = false)
    private Integer idadeMinima;

    @Column(name = "experiencia_minima", nullable = false)
    private Integer experienciaMinima;

    @Column(name = "preco_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoBase;

    public CategoriaKart() {
    }

    public CategoriaKart(Integer cilindrada,
            String descricao,
            Integer idadeMinima,
            Integer experienciaMinima,
            BigDecimal precoBase) {

        this.cilindrada = cilindrada;
        this.descricao = descricao;
        this.idadeMinima = idadeMinima;
        this.experienciaMinima = experienciaMinima;
        this.precoBase = precoBase;
    }

    public Long getId() {
        return id;
    }

    public Integer getCilindrada() {
        return cilindrada;
    }

    public void setCilindrada(Integer cilindrada) {
        this.cilindrada = cilindrada;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Integer getIdadeMinima() {
        return idadeMinima;
    }

    public void setIdadeMinima(Integer idadeMinima) {
        this.idadeMinima = idadeMinima;
    }

    public Integer getExperienciaMinima() {
        return experienciaMinima;
    }

    public void setExperienciaMinima(Integer experienciaMinima) {
        this.experienciaMinima = experienciaMinima;
    }

    public BigDecimal getPrecoBase() {
        return precoBase;
    }

    public void setPrecoBase(BigDecimal precoBase) {
        this.precoBase = precoBase;
    }

    @Override
    public String toString() {
        return "CategoriaKart{"
                + "id=" + id
                + ", cilindrada=" + cilindrada
                + ", descricao='" + descricao + '\''
                + ", idadeMinima=" + idadeMinima
                + ", experienciaMinima=" + experienciaMinima
                + ", precoBase=" + precoBase
                + '}';
    }
}
