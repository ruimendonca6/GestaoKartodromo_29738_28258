package pt.kartodromo.core.demo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import pt.kartodromo.core.bll.BusinessException;
import pt.kartodromo.core.bll.CategoriaKartService;
import pt.kartodromo.core.bll.ClienteService;
import pt.kartodromo.core.bll.CorridaService;
import pt.kartodromo.core.bll.ReservaService;
import pt.kartodromo.core.config.HibernateUtil;
import pt.kartodromo.core.model.CategoriaKart;
import pt.kartodromo.core.model.Cliente;
import pt.kartodromo.core.model.Corrida;
import pt.kartodromo.core.model.Reserva;

public class CoreDemoApplication {

    public static void main(String[] args) {

        CategoriaKartService categoriaService = new CategoriaKartService();
        ClienteService clienteService = new ClienteService();
        CorridaService corridaService = new CorridaService();
        ReservaService reservaService = new ReservaService();

        try {

            System.out.println("===== DEMO SISTEMA KARTODROMO =====");

            /*
             * 1 - Criar categoria de kart
             */
            System.out.println("\nCriar categoria...");

            CategoriaKart categoria = categoriaService.criarCategoria(
                    390,
                    "Categoria Avancada",
                    18,
                    3,
                    new BigDecimal("55.00")
            );

            System.out.println("Categoria criada: " + categoria);


            /*
             * 2 - Criar clientes
             */
            System.out.println("\nCriar clientes...");

            Cliente ana = clienteService.criarCliente(
                    "Ana Silva",
                    LocalDate.of(2002, 5, 8),
                    "ana@kartodromo.pt",
                    4
            );

            Cliente joao = clienteService.criarCliente(
                    "Joao Costa",
                    LocalDate.of(2011, 9, 10),
                    "joao@kartodromo.pt",
                    1
            );

            System.out.println("Cliente criado: " + ana);
            System.out.println("Cliente criado: " + joao);


            /*
             * 3 - Criar corrida
             */
            System.out.println("\nCriar corrida...");

            Corrida corrida = corridaService.criarCorrida(
                    LocalDateTime.now().plusDays(1),
                    20,
                    1,
                    categoria.getId(),
                    ana.getId(),
                    "Pista Completa"
            );

            System.out.println("Corrida criada: " + corrida);


            /*
             * 4 - Criar reserva válida
             */
            System.out.println("\nCriar reserva para Ana...");

            Reserva reservaAna = reservaService.reservarCorrida(
                    ana.getId(),
                    corrida.getId()
            );

            System.out.println("Reserva criada: " + reservaAna);


            /*
             * 5 - Tentar reserva inválida (cliente não elegível)
             */
            System.out.println("\nTentar reserva para Joao (esperado erro)...");

            try {
                reservaService.reservarCorrida(
                        joao.getId(),
                        corrida.getId()
                );
            } catch (BusinessException e) {
                System.out.println("Erro esperado: " + e.getMessage());
            }


            /*
             * 6 - Criar cliente elegível
             */
            System.out.println("\nCriar cliente Pedro...");

            Cliente pedro = clienteService.criarCliente(
                    "Pedro Martins",
                    LocalDate.of(1999, 2, 20),
                    "pedro@kartodromo.pt",
                    4
            );

            /*
             * 7 - Tentar reservar corrida cheia
             */
            System.out.println("\nTentar reservar corrida cheia...");

            try {
                reservaService.reservarCorrida(
                        pedro.getId(),
                        corrida.getId()
                );
            } catch (BusinessException e) {
                System.out.println("Erro esperado: " + e.getMessage());
            }


            /*
             * 8 - Cancelar reserva
             */
            System.out.println("\nCancelar reserva da Ana...");

            reservaService.cancelarReserva(reservaAna.getId());

            System.out.println("Reserva cancelada.");


            /*
             * 9 - Nova reserva após cancelamento
             */
            System.out.println("\nCriar reserva para Pedro...");

            Reserva reservaPedro = reservaService.reservarCorrida(
                    pedro.getId(),
                    corrida.getId()
            );

            System.out.println("Reserva criada: " + reservaPedro);


            /*
             * 10 - Listar reservas do cliente
             */
            System.out.println("\nListar reservas ativas do Pedro...");

            List<Reserva> reservasPedro
                    = reservaService.listarReservasAtivasCliente(pedro.getId());

            reservasPedro.forEach(System.out::println);


            /*
             * 11 - Listar corridas do dia
             */
            System.out.println("\nListar corridas do dia...");

            List<Corrida> corridas
                    = corridaService.listarCorridasPorDia(
                            corrida.getDataHoraInicio().toLocalDate()
                    );

            corridas.forEach(System.out::println);

            System.out.println("\n===== DEMO CONCLUIDA =====");

        } finally {

            HibernateUtil.shutdown();

        }
    }
}
