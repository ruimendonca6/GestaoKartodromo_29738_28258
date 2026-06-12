package pt.kartodromo.desktop.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import pt.kartodromo.desktop.ui.auth.AuthUser;
import pt.kartodromo.desktop.ui.auth.LoginDialog;
import pt.kartodromo.desktop.ui.categorias.CategoriaPanel;
import pt.kartodromo.desktop.ui.clientes.ClientePanel;
import pt.kartodromo.desktop.ui.corridas.CorridaPanel;
import pt.kartodromo.desktop.ui.dashboard.DashboardPanel;
import pt.kartodromo.desktop.ui.karts.KartPanel;
import pt.kartodromo.desktop.ui.perfil.PerfilPanel;
import pt.kartodromo.desktop.ui.pistas.PistasPanel;
import pt.kartodromo.desktop.ui.reservas.ReservaPanel;
import pt.kartodromo.desktop.ui.calendario.CalendarioPanel;
import pt.kartodromo.desktop.ui.disponibilidade.DisponibilidadePanel;
import pt.kartodromo.desktop.ui.estatisticas.EstatisticasPanel;
import pt.kartodromo.desktop.ui.utilizadores.UtilizadoresPanel;

public class KartodromoDesktopFrame extends JFrame {

    private final AuthUser authenticatedUser;

    private final DashboardPanel dashboardPanel;
    private final ClientePanel clientePanel;
    private final CategoriaPanel categoriaPanel;
    private final KartPanel kartPanel;
    private final CorridaPanel corridaPanel;
    private final ReservaPanel reservaPanel;
    private final PistasPanel pistasPanel;
    private final PerfilPanel perfilPanel;
    private final UtilizadoresPanel utilizadoresPanel;
    private final CalendarioPanel calendarioPanel;
    private final DisponibilidadePanel disponibilidadePanel;
    private final EstatisticasPanel estatisticasPanel;

    public KartodromoDesktopFrame(AuthUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;

        setTitle(
                "🏁 Kartódromo - Desktop | Utilizador: "
                        + authenticatedUser.getUsername()
                        + " | Perfil: "
                        + authenticatedUser.getRole()
        );

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1300, 800);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        dashboardPanel = new DashboardPanel();
        clientePanel = new ClientePanel();
        categoriaPanel = new CategoriaPanel();
        kartPanel = new KartPanel();
        corridaPanel = new CorridaPanel();
        reservaPanel = new ReservaPanel();
        pistasPanel = new PistasPanel();
        utilizadoresPanel = new UtilizadoresPanel();
        calendarioPanel = new CalendarioPanel();
        disponibilidadePanel = new DisponibilidadePanel();
        estatisticasPanel    = new EstatisticasPanel();

        perfilPanel =
                new PerfilPanel(
                        authenticatedUser,
                        this::logout
                );

        JTabbedPane tabs = new JTabbedPane();

        configureTabsByRole(tabs);

        tabs.addChangeListener(e -> refreshAllPanels());

        add(tabs, BorderLayout.CENTER);
    }

    private void configureTabsByRole(JTabbedPane tabs) {
        tabs.addTab(UiLabels.DASHBOARD, dashboardPanel);

        if (authenticatedUser.isCliente()) {
            tabs.addTab(UiLabels.PISTAS, pistasPanel);
            tabs.addTab(UiLabels.RESERVAS, reservaPanel);
            tabs.addTab(UiLabels.CALENDARIO, calendarioPanel);
            tabs.addTab(UiLabels.PERFIL, perfilPanel);
            return;
        }

        if (authenticatedUser.isFuncionario()) {
            tabs.addTab(UiLabels.CLIENTES, clientePanel);
            tabs.addTab(UiLabels.CATEGORIAS, categoriaPanel);
            tabs.addTab(UiLabels.KARTS, kartPanel);
            tabs.addTab(UiLabels.CORRIDAS, corridaPanel);
            tabs.addTab(UiLabels.RESERVAS, reservaPanel);
            tabs.addTab(UiLabels.CALENDARIO, calendarioPanel);
            tabs.addTab(UiLabels.DISPONIBILIDADE, disponibilidadePanel);
            tabs.addTab(UiLabels.ESTATISTICAS, estatisticasPanel);
            tabs.addTab(UiLabels.PERFIL, perfilPanel);
            return;
        }

        if (authenticatedUser.isAdmin()) {
            tabs.addTab(UiLabels.CLIENTES, clientePanel);
            tabs.addTab(UiLabels.CATEGORIAS, categoriaPanel);
            tabs.addTab(UiLabels.KARTS, kartPanel);
            tabs.addTab(UiLabels.CORRIDAS, corridaPanel);
            tabs.addTab(UiLabels.RESERVAS, reservaPanel);
            tabs.addTab(UiLabels.PISTAS, pistasPanel);
            tabs.addTab(UiLabels.CALENDARIO, calendarioPanel);
            tabs.addTab(UiLabels.DISPONIBILIDADE, disponibilidadePanel);
            tabs.addTab(UiLabels.ESTATISTICAS, estatisticasPanel);
            tabs.addTab(UiLabels.UTILIZADORES, utilizadoresPanel);
            tabs.addTab(UiLabels.PERFIL, perfilPanel);
        }
    }

    private void refreshAllPanels() {
        dashboardPanel.refreshData();

        if (!authenticatedUser.isCliente()) {
            clientePanel.refreshData();
            categoriaPanel.refreshData();
            kartPanel.refreshData();
            corridaPanel.refreshData();
        }

        reservaPanel.refreshData();
        pistasPanel.refreshData();
        calendarioPanel.refreshData();
        perfilPanel.refreshData();

        if (!authenticatedUser.isCliente()) {
            disponibilidadePanel.refreshData();
            estatisticasPanel.refreshData();
        }

        if (authenticatedUser.isAdmin()) {
            utilizadoresPanel.refreshData();
        }
    }

    private void logout() {
        dispose();

        LoginDialog loginDialog =
                new LoginDialog(null);

        loginDialog.setVisible(true);

        AuthUser newAuthenticatedUser =
                loginDialog.getAuthenticatedUser();

        if (newAuthenticatedUser == null) {
            System.exit(0);
            return;
        }

        new KartodromoDesktopFrame(newAuthenticatedUser)
                .setVisible(true);
    }

    public AuthUser getAuthenticatedUser() {
        return authenticatedUser;
    }
}