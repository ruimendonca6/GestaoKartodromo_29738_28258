package pt.kartodromo.core.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import pt.kartodromo.core.model.enums.ReservaEstado;

@Entity
@Table(name = "reservas")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "kart_id", nullable = false)
    private Kart kart;

    @Column(name = "pista_nome", nullable = false, length = 80)
    private String pistaNome;

    @Column(name = "data_hora_inicio", nullable = false)
    private LocalDateTime dataHoraInicio;

    @Column(name = "data_hora_fim", nullable = false)
    private LocalDateTime dataHoraFim;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservaEstado estado;

    public Reserva() {
    }

    public Reserva(
            Cliente cliente,
            Kart kart,
            String pistaNome,
            LocalDateTime dataHoraInicio,
            LocalDateTime dataHoraFim,
            LocalDateTime dataCriacao,
            ReservaEstado estado
    ) {
        this.cliente = cliente;
        this.kart = kart;
        this.pistaNome = pistaNome;
        this.dataHoraInicio = dataHoraInicio;
        this.dataHoraFim = dataHoraFim;
        this.dataCriacao = dataCriacao;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Kart getKart() {
        return kart;
    }

    public String getPistaNome() {
        return pistaNome;
    }

    public LocalDateTime getDataHoraInicio() {
        return dataHoraInicio;
    }

    public LocalDateTime getDataHoraFim() {
        return dataHoraFim;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public ReservaEstado getEstado() {
        return estado;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public void setKart(Kart kart) {
        this.kart = kart;
    }

    public void setPistaNome(String pistaNome) {
        this.pistaNome = pistaNome;
    }

    public void setDataHoraInicio(LocalDateTime dataHoraInicio) {
        this.dataHoraInicio = dataHoraInicio;
    }

    public void setDataHoraFim(LocalDateTime dataHoraFim) {
        this.dataHoraFim = dataHoraFim;
    }

    public void setEstado(ReservaEstado estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Reserva{"
                + "id=" + id
                + ", cliente=" + cliente.getNome()
                + ", kart=" + kart.getNumero()
                + ", pistaNome='" + pistaNome + '\''
                + ", dataHoraInicio=" + dataHoraInicio
                + ", dataHoraFim=" + dataHoraFim
                + ", estado=" + estado
                + '}';
    }
}
