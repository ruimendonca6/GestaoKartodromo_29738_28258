package pt.kartodromo.desktop.ui;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import pt.kartodromo.desktop.ui.categorias.CategoriaPanel;
import pt.kartodromo.desktop.ui.clientes.ClientePanel;
import pt.kartodromo.desktop.ui.corridas.CorridaPanel;
import pt.kartodromo.desktop.ui.karts.KartPanel;
import pt.kartodromo.desktop.ui.reservas.ReservaPanel;

public class KartodromoDesktopFrame extends JFrame {

    private final ClientePanel clientePanel;
    private final CategoriaPanel categoriaPanel;
    private final KartPanel kartPanel;
    private final CorridaPanel corridaPanel;
    private final ReservaPanel reservaPanel;

    public KartodromoDesktopFrame() {
        setTitle("Kartodromo - Desktop");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        clientePanel = new ClientePanel();
        categoriaPanel = new CategoriaPanel();
        kartPanel = new KartPanel();
        corridaPanel = new CorridaPanel();
        reservaPanel = new ReservaPanel();

        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("Clientes", clientePanel);
        tabs.addTab("Categorias", categoriaPanel);
        tabs.addTab("Karts", kartPanel);
        tabs.addTab("Corridas", corridaPanel);
        tabs.addTab("Reservas", reservaPanel);

        tabs.addChangeListener(e -> refreshAllPanels());

        add(tabs, BorderLayout.CENTER);
    }

    private void refreshAllPanels() {
        clientePanel.refreshData();
        categoriaPanel.refreshData();
        kartPanel.refreshData();
        corridaPanel.refreshData();
        reservaPanel.refreshData();
    }
}