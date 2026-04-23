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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "corrida_id", nullable = false)
    private Corrida corrida;

    @Column(name = "data_reserva", nullable = false)
    private LocalDateTime dataReserva;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservaEstado estado;

    public Reserva() {
    }

    public Reserva(Cliente cliente, Corrida corrida,
            LocalDateTime dataReserva, ReservaEstado estado) {
        this.cliente = cliente;
        this.corrida = corrida;
        this.dataReserva = dataReserva;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Corrida getCorrida() {
        return corrida;
    }

    public LocalDateTime getDataReserva() {
        return dataReserva;
    }

    public ReservaEstado getEstado() {
        return estado;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public void setCorrida(Corrida corrida) {
        this.corrida = corrida;
    }

    public void setDataReserva(LocalDateTime dataReserva) {
        this.dataReserva = dataReserva;
    }

    public void setEstado(ReservaEstado estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Reserva{"
                + "id=" + id
                + ", cliente=" + cliente.getNome()
                + ", corrida=" + corrida.getId()
                + ", dataReserva=" + dataReserva
                + ", estado=" + estado
                + '}';
    }
}
