package pt.kartodromo.desktop.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import pt.kartodromo.desktop.ui.categorias.CategoriaPanel;
import pt.kartodromo.desktop.ui.clientes.ClientePanel;
import pt.kartodromo.desktop.ui.corridas.CorridaPanel;
import pt.kartodromo.desktop.ui.dashboard.DashboardPanel;
import pt.kartodromo.desktop.ui.karts.KartPanel;
import pt.kartodromo.desktop.ui.reservas.ReservaPanel;

public class KartodromoDesktopFrame extends JFrame {

    private final DashboardPanel dashboardPanel;
    private final ClientePanel clientePanel;
    private final CategoriaPanel categoriaPanel;
    private final KartPanel kartPanel;
    private final CorridaPanel corridaPanel;
    private final ReservaPanel reservaPanel;

    public KartodromoDesktopFrame() {
        setTitle("🏁 Kartódromo - Desktop");
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

        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab(UiLabels.DASHBOARD, dashboardPanel);
        tabs.addTab(UiLabels.CLIENTES, clientePanel);
        tabs.addTab(UiLabels.CATEGORIAS, categoriaPanel);
        tabs.addTab(UiLabels.KARTS, kartPanel);
        tabs.addTab(UiLabels.CORRIDAS, corridaPanel);
        tabs.addTab(UiLabels.RESERVAS, reservaPanel);

        tabs.addChangeListener(e -> refreshAllPanels());

        add(tabs, BorderLayout.CENTER);
    }

    private void refreshAllPanels() {
        dashboardPanel.refreshData();
        clientePanel.refreshData();
        categoriaPanel.refreshData();
        kartPanel.refreshData();
        corridaPanel.refreshData();
        reservaPanel.refreshData();
    }
}