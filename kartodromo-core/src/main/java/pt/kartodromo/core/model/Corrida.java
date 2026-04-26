package pt.kartodromo.core.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "corridas")
public class Corrida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_hora_inicio", nullable = false)
    private LocalDateTime dataHoraInicio;

    @Column(name = "duracao_minutos", nullable = false)
    private Integer duracaoMinutos;

    @Column(name = "vagas_maximas", nullable = false)
    private Integer vagasMaximas;

    @Column(name = "layout_nome", nullable = false, length = 80)
    private String layoutNome;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaKart categoria;

    public Corrida() {
    }

    public Corrida(
            LocalDateTime dataHoraInicio,
            Integer duracaoMinutos,
            Integer vagasMaximas,
            String layoutNome,
            Cliente cliente,
            CategoriaKart categoria
    ) {
        this.dataHoraInicio = dataHoraInicio;
        this.duracaoMinutos = duracaoMinutos;
        this.vagasMaximas = vagasMaximas;
        this.layoutNome = layoutNome;
        this.cliente = cliente;
        this.categoria = categoria;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getDataHoraInicio() {
        return dataHoraInicio;
    }

    public Integer getDuracaoMinutos() {
        return duracaoMinutos;
    }

    public Integer getVagasMaximas() {
        return vagasMaximas;
    }

    public String getLayoutNome() {
        return layoutNome;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public CategoriaKart getCategoria() {
        return categoria;
    }

    public void setDataHoraInicio(LocalDateTime dataHoraInicio) {
        this.dataHoraInicio = dataHoraInicio;
    }

    public void setDuracaoMinutos(Integer duracaoMinutos) {
        this.duracaoMinutos = duracaoMinutos;
    }

    public void setVagasMaximas(Integer vagasMaximas) {
        this.vagasMaximas = vagasMaximas;
    }

    public void setLayoutNome(String layoutNome) {
        this.layoutNome = layoutNome;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public void setCategoria(CategoriaKart categoria) {
        this.categoria = categoria;
    }

    public LocalDateTime getDataHoraFim() {
        return dataHoraInicio.plusMinutes(duracaoMinutos);
    }

    @Override
    public String toString() {
        return "Corrida{"
                + "id=" + id
                + ", dataHoraInicio=" + dataHoraInicio
                + ", duracaoMinutos=" + duracaoMinutos
                + ", vagasMaximas=" + vagasMaximas
                + ", layoutNome='" + layoutNome + '\''
                + ", cliente=" + (cliente != null ? cliente.getNome() : "null")
                + ", categoria=" + (categoria != null ? categoria.getDescricao() : "null")
                + '}';
    }
}
