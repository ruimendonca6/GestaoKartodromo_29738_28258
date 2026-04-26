package pt.kartodromo.core.demo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import pt.kartodromo.core.bll.BusinessException;
import pt.kartodromo.core.bll.CategoriaKartService;
import pt.kartodromo.core.bll.ClienteService;
import pt.kartodromo.core.bll.KartService;
import pt.kartodromo.core.bll.ReservaService;
import pt.kartodromo.core.config.HibernateUtil;
import pt.kartodromo.core.model.CategoriaKart;
import pt.kartodromo.core.model.Cliente;
import pt.kartodromo.core.model.Kart;
import pt.kartodromo.core.model.Reserva;
import pt.kartodromo.core.model.enums.KartEstado;
import pt.kartodromo.core.model.enums.ReservaEstado;

public class CoreDemoApplication {

    public static void main(String[] args) {

        CategoriaKartService categoriaService = new CategoriaKartService();
        ClienteService clienteService = new ClienteService();
        KartService kartService = new KartService();
        ReservaService reservaService = new ReservaService();

        try {

            System.out.println("===== DEMO SISTEMA KARTODROMO =====");

            System.out.println("\nCriar categoria...");

            CategoriaKart categoria = categoriaService.criarCategoria(
                    390,
                    "Categoria Avançada",
                    18,
                    3,
                    new BigDecimal("55.00")
            );

            System.out.println("Categoria criada: " + categoria);

            System.out.println("\nCriar clientes...");

            Cliente ana = clienteService.criarCliente(
                    "Ana Silva",
                    LocalDate.of(2002, 5, 8),
                    "ana@kartodromo.pt",
                    4
            );

            Cliente joao = clienteService.criarCliente(
                    "João Costa",
                    LocalDate.of(2011, 9, 10),
                    "joao@kartodromo.pt",
                    1
            );

            System.out.println("Cliente criado: " + ana);
            System.out.println("Cliente criado: " + joao);

            System.out.println("\nCriar kart...");

            Kart kart = kartService.criarKart(
                    8,
                    KartEstado.OPERACIONAL,
                    true,
                    categoria.getId()
            );

            System.out.println("Kart criado: " + kart);

            System.out.println("\nCriar reserva para Ana...");

            Reserva reservaAna = reservaService.criarReserva(
                    ana.getId(),
                    kart.getId(),
                    "Pista Completa",
                    LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0),
                    LocalDateTime.now().plusDays(1).withHour(10).withMinute(30).withSecond(0).withNano(0),
                    ReservaEstado.PENDENTE
            );

            System.out.println("Reserva criada: " + reservaAna);

            System.out.println("\nTentar criar reserva sobreposta...");

            try {
                reservaService.criarReserva(
                        joao.getId(),
                        kart.getId(),
                        "Pista Completa",
                        LocalDateTime.now().plusDays(1).withHour(10).withMinute(15).withSecond(0).withNano(0),
                        LocalDateTime.now().plusDays(1).withHour(10).withMinute(45).withSecond(0).withNano(0),
                        ReservaEstado.CONFIRMADA
                );
            } catch (BusinessException e) {
                System.out.println("Erro esperado: " + e.getMessage());
            }

            System.out.println("\nCriar cliente Pedro...");

            Cliente pedro = clienteService.criarCliente(
                    "Pedro Martins",
                    LocalDate.of(1999, 2, 20),
                    "pedro@kartodromo.pt",
                    4
            );

            System.out.println("Cliente criado: " + pedro);

            System.out.println("\nCriar reserva para Pedro...");

            Reserva reservaPedro = reservaService.criarReserva(
                    pedro.getId(),
                    kart.getId(),
                    "Pista Completa",
                    LocalDateTime.now().plusDays(1).withHour(11).withMinute(0).withSecond(0).withNano(0),
                    LocalDateTime.now().plusDays(1).withHour(11).withMinute(30).withSecond(0).withNano(0),
                    ReservaEstado.CONFIRMADA
            );

            System.out.println("Reserva criada: " + reservaPedro);

            System.out.println("\nAtualizar reserva da Ana...");

            Reserva reservaAnaAtualizada = reservaService.atualizarReserva(
                    reservaAna.getId(),
                    ana.getId(),
                    kart.getId(),
                    "Pista Completa",
                    reservaAna.getDataHoraInicio(),
                    reservaAna.getDataHoraFim(),
                    ReservaEstado.CONFIRMADA
            );

            System.out.println("Reserva atualizada: " + reservaAnaAtualizada);

            System.out.println("\nListar reservas...");

            List<Reserva> reservas = reservaService.listarReservas();
            reservas.forEach(System.out::println);

            System.out.println("\nEliminar reserva do Pedro...");

            reservaService.eliminarReserva(reservaPedro.getId());

            System.out.println("Reserva eliminada.");

            System.out.println("\nReservas finais:");
            reservaService.listarReservas().forEach(System.out::println);

            System.out.println("\n===== DEMO CONCLUÍDA =====");

        } finally {
            HibernateUtil.shutdown();
        }
    }
}