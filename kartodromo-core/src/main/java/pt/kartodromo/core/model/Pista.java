package pt.kartodromo.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "pistas")
public class Pista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;

    @Column(nullable = false)
    private int comprimento;

    @Column(nullable = false)
    private int capacidade;

    @Column(nullable = false)
    private boolean ativa;

    @Column
    private String imagemPath;

    public Pista() {
    }

    public Pista(String nome, int comprimento, int capacidade, boolean ativa, String imagemPath) {
        this.nome = nome;
        this.comprimento = comprimento;
        this.capacidade = capacidade;
        this.ativa = ativa;
        this.imagemPath = imagemPath;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public int getComprimento() { return comprimento; }
    public int getCapacidade() { return capacidade; }
    public boolean isAtiva() { return ativa; }
    public String getImagemPath() { return imagemPath; }

    public void setNome(String nome) { this.nome = nome; }
    public void setComprimento(int comprimento) { this.comprimento = comprimento; }
    public void setCapacidade(int capacidade) { this.capacidade = capacidade; }
    public void setAtiva(boolean ativa) { this.ativa = ativa; }
    public void setImagemPath(String imagemPath) { this.imagemPath = imagemPath; }

    @Override
    public String toString() {
        return "Pista{id=" + id + ", nome='" + nome + "', comprimento=" + comprimento
                + ", capacidade=" + capacidade + ", ativa=" + ativa + '}';
    }
}
