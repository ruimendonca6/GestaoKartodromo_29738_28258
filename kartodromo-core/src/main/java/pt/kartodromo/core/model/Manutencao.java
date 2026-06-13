package pt.kartodromo.core.model;

import java.time.LocalDate;

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
import pt.kartodromo.core.model.enums.TipoManutencao;

@Entity
@Table(name = "manutencoes")
public class Manutencao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "kart_id", nullable = false)
    private Kart kart;

    @Column(name = "data_entrada", nullable = false)
    private LocalDate dataEntrada;

    @Column(name = "data_saida")
    private LocalDate dataSaida;

    @Column(nullable = false, length = 500)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoManutencao tipo;

    @Column(name = "proxima_revisao")
    private LocalDate proximaRevisao;

    @Column(nullable = false)
    private boolean concluida;

    public Manutencao() {
    }

    public Manutencao(Kart kart, LocalDate dataEntrada, String descricao,
                      TipoManutencao tipo, LocalDate proximaRevisao) {
        this.kart = kart;
        this.dataEntrada = dataEntrada;
        this.descricao = descricao;
        this.tipo = tipo;
        this.proximaRevisao = proximaRevisao;
        this.concluida = false;
    }

    public Long getId() { return id; }
    public Kart getKart() { return kart; }
    public LocalDate getDataEntrada() { return dataEntrada; }
    public LocalDate getDataSaida() { return dataSaida; }
    public String getDescricao() { return descricao; }
    public TipoManutencao getTipo() { return tipo; }
    public LocalDate getProximaRevisao() { return proximaRevisao; }
    public boolean isConcluida() { return concluida; }

    public void setKart(Kart kart) { this.kart = kart; }
    public void setDataEntrada(LocalDate dataEntrada) { this.dataEntrada = dataEntrada; }
    public void setDataSaida(LocalDate dataSaida) { this.dataSaida = dataSaida; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setTipo(TipoManutencao tipo) { this.tipo = tipo; }
    public void setProximaRevisao(LocalDate proximaRevisao) { this.proximaRevisao = proximaRevisao; }
    public void setConcluida(boolean concluida) { this.concluida = concluida; }

    @Override
    public String toString() {
        return "Manutencao{id=" + id + ", kart=" + (kart != null ? kart.getNumero() : "null")
                + ", tipo=" + tipo + ", concluida=" + concluida + '}';
    }
}
