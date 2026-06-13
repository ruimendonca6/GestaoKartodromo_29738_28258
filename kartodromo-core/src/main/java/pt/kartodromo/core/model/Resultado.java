package pt.kartodromo.core.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "resultados")
public class Resultado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_piloto", nullable = false, length = 100)
    private String nomePiloto;

    @Column(name = "nome_corrida", nullable = false, length = 100)
    private String nomeCorrida;

    @Column(name = "tempo_ms", nullable = false)
    private long tempoMs;

    @Column(nullable = false)
    private int posicao;

    @Column(name = "data_corrida", nullable = false)
    private LocalDateTime dataCorrida;

    public Resultado() {
    }

    public Resultado(String nomePiloto, String nomeCorrida, long tempoMs,
                     int posicao, LocalDateTime dataCorrida) {
        this.nomePiloto = nomePiloto;
        this.nomeCorrida = nomeCorrida;
        this.tempoMs = tempoMs;
        this.posicao = posicao;
        this.dataCorrida = dataCorrida;
    }

    public Long getId() { return id; }
    public String getNomePiloto() { return nomePiloto; }
    public String getNomeCorrida() { return nomeCorrida; }
    public long getTempoMs() { return tempoMs; }
    public int getPosicao() { return posicao; }
    public LocalDateTime getDataCorrida() { return dataCorrida; }

    public void setNomePiloto(String nomePiloto) { this.nomePiloto = nomePiloto; }
    public void setNomeCorrida(String nomeCorrida) { this.nomeCorrida = nomeCorrida; }
    public void setTempoMs(long tempoMs) { this.tempoMs = tempoMs; }
    public void setPosicao(int posicao) { this.posicao = posicao; }
    public void setDataCorrida(LocalDateTime dataCorrida) { this.dataCorrida = dataCorrida; }

    @Override
    public String toString() {
        return "Resultado{id=" + id + ", piloto='" + nomePiloto + "', tempo=" + tempoMs + "ms, posicao=" + posicao + '}';
    }
}
