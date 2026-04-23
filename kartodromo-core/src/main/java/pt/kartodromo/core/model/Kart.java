package pt.kartodromo.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import pt.kartodromo.core.model.enums.KartEstado;

@Entity
@Table(name = "karts")
public class Kart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer numero;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private KartEstado estado;

    @Column(nullable = false)
    private boolean disponivel;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaKart categoria;

    public Kart() {
    }

    public Kart(Integer numero, KartEstado estado, boolean disponivel, CategoriaKart categoria) {
        this.numero = numero;
        this.estado = estado;
        this.disponivel = disponivel;
        this.categoria = categoria;
    }

    public Long getId() {
        return id;
    }

    public Integer getNumero() {
        return numero;
    }

    public KartEstado getEstado() {
        return estado;
    }

    public boolean isDisponivel() {
        return disponivel;
    }

    public CategoriaKart getCategoria() {
        return categoria;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public void setEstado(KartEstado estado) {
        this.estado = estado;
    }

    public void setDisponivel(boolean disponivel) {
        this.disponivel = disponivel;
    }

    public void setCategoria(CategoriaKart categoria) {
        this.categoria = categoria;
    }

    @Override
    public String toString() {
        return "Kart{"
                + "id=" + id
                + ", numero=" + numero
                + ", estado=" + estado
                + ", disponivel=" + disponivel
                + ", categoria=" + (categoria != null ? categoria.getDescricao() : "null")
                + '}';
    }
}
