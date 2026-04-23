package pt.kartodromo.core.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.Period;

@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(name = "nivel_experiencia", nullable = false)
    private Integer nivelExperiencia;

    public Cliente() {
    }

    public Cliente(String nome, LocalDate dataNascimento, String email, Integer nivelExperiencia) {
        this.nome = nome;
        this.dataNascimento = dataNascimento;
        this.email = email;
        this.nivelExperiencia = nivelExperiencia;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public String getEmail() {
        return email;
    }

    public Integer getNivelExperiencia() {
        return nivelExperiencia;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setNivelExperiencia(Integer nivelExperiencia) {
        this.nivelExperiencia = nivelExperiencia;
    }

    public int getIdadeAtual() {
        return Period.between(this.dataNascimento, LocalDate.now()).getYears();
    }

    @Override
    public String toString() {
        return "Cliente{"
                + "id=" + id
                + ", nome='" + nome + '\''
                + ", email='" + email + '\''
                + ", idadeAtual=" + getIdadeAtual()
                + ", nivelExperiencia=" + nivelExperiencia
                + '}';
    }
}
