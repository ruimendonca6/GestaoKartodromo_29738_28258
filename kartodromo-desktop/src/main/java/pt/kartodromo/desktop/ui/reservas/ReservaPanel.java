package pt.kartodromo.desktop.ui.reservas;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import pt.kartodromo.core.bll.CategoriaKartService;
import pt.kartodromo.core.bll.ClienteService;
import pt.kartodromo.core.bll.CorridaService;
import pt.kartodromo.core.bll.KartService;
import pt.kartodromo.core.bll.ReservaService;
import pt.kartodromo.core.model.Cliente;
import pt.kartodromo.core.model.Corrida;
import pt.kartodromo.core.model.Reserva;

public class ReservaPanel extends JPanel {

    private final ClienteService clienteService = new ClienteService();
    private final CategoriaKartService categoriaService = new CategoriaKartService();
    private final CorridaService corridaService = new CorridaService();
    private final KartService kartService = new KartService();
    private final ReservaService reservaService = new ReservaService();

    private final DefaultComboBoxModel<Cliente> clienteComboModel = new DefaultComboBoxModel<>();
    private final DefaultComboBoxModel<Corrida> corridaComboModel = new DefaultComboBoxModel<>();

    private final DefaultListModel<Reserva> reservasAtivasModel = new DefaultListModel<>();
    private final DefaultListModel<String> resumoModel = new DefaultListModel<>();

    private final JList<Reserva> reservasAtivasList = new JList<>(reservasAtivasModel);

    public ReservaPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(buildForm(), BorderLayout.NORTH);
        add(buildReservasList(), BorderLayout.CENTER);
        add(buildResumoList(), BorderLayout.SOUTH);

        configureReservaRenderer();

        refreshData();
    }

    private JPanel buildForm() {
        JComboBox<Cliente> clienteCombo = new JComboBox<>(clienteComboModel);
        JComboBox<Corrida> corridaCombo = new JComboBox<>(corridaComboModel);

        JButton reservarButton = new JButton("Reservar corrida");
        JButton listarAtivasButton = new JButton("Listar reservas ativas");
        JButton cancelarButton = new JButton("Cancelar reserva selecionada");

        reservarButton.addActionListener(e -> {
            try {
                Cliente cliente = (Cliente) clienteCombo.getSelectedItem();
                Corrida corrida = (Corrida) corridaCombo.getSelectedItem();

                if (cliente == null || corrida == null) {
                    showError("Selecione cliente e corrida para reservar.");
                    return;
                }

                Reserva reserva = reservaService.reservarCorrida(cliente.getId(), corrida.getId());

                showInfo("Reserva criada com sucesso: " + reserva.getId());

                refreshData();
                loadReservasAtivasCliente(cliente.getId());

            } catch (RuntimeException ex) {
                showError(ex.getMessage());
            }
        });

        listarAtivasButton.addActionListener(e -> {
            Cliente cliente = (Cliente) clienteCombo.getSelectedItem();

            if (cliente == null) {
                showError("Selecione um cliente.");
                return;
            }

            loadReservasAtivasCliente(cliente.getId());
        });

        cancelarButton.addActionListener(e -> {
            Reserva reservaSelecionada = reservasAtivasList.getSelectedValue();

            if (reservaSelecionada == null) {
                showError("Selecione uma reserva ativa para cancelar.");
                return;
            }

            try {
                reservaService.cancelarReserva(reservaSelecionada.getId());

                showInfo("Reserva cancelada com sucesso: " + reservaSelecionada.getId());

                Cliente cliente = (Cliente) clienteCombo.getSelectedItem();

                if (cliente != null) {
                    loadReservasAtivasCliente(cliente.getId());
                }

                refreshData();

            } catch (RuntimeException ex) {
                showError(ex.getMessage());
            }
        });

        JPanel form = new JPanel(new GridLayout(3, 3, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Gestão de reservas"));

        form.add(new JLabel("Cliente"));
        form.add(clienteCombo);
        form.add(reservarButton);

        form.add(new JLabel("Corrida"));
        form.add(corridaCombo);
        form.add(listarAtivasButton);

        form.add(new JLabel());
        form.add(new JLabel());
        form.add(cancelarButton);

        return form;
    }

    private JScrollPane buildReservasList() {
        JScrollPane listPane = new JScrollPane(reservasAtivasList);
        listPane.setBorder(BorderFactory.createTitledBorder("Reservas ativas do cliente selecionado"));
        return listPane;
    }

    private JScrollPane buildResumoList() {
        JList<String> resumoList = new JList<>(resumoModel);
        JScrollPane infoPane = new JScrollPane(resumoList);
        infoPane.setBorder(BorderFactory.createTitledBorder("Resumo"));
        return infoPane;
    }

    private void configureReservaRenderer() {
        reservasAtivasList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            String text = "Reserva #" + value.getId()
                    + " | Corrida #" + value.getCorrida().getId()
                    + " | " + value.getDataReserva()
                    + " | " + value.getEstado();

            return new DefaultListCellRenderer()
                    .getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
        });
    }

    public void refreshData() {
        refreshClientes();
        refreshCorridas();
        refreshResumo();
    }

    private void refreshClientes() {
        clienteComboModel.removeAllElements();

        for (Cliente cliente : clienteService.listarClientes()) {
            clienteComboModel.addElement(cliente);
        }
    }

    private void refreshCorridas() {
        corridaComboModel.removeAllElements();

        for (Corrida corrida : corridaService.listarCorridas()) {
            corridaComboModel.addElement(corrida);
        }
    }

    private void refreshResumo() {
        resumoModel.clear();

        resumoModel.addElement("Clientes registados: " + clienteService.listarClientes().size());
        resumoModel.addElement("Categorias disponíveis: " + categoriaService.listarCategorias().size());
        resumoModel.addElement("Karts registados: " + kartService.listarKarts().size());
        resumoModel.addElement("Corridas registadas: " + corridaService.listarCorridas().size());
    }

    private void loadReservasAtivasCliente(Long clienteId) {
        reservasAtivasModel.clear();

        for (Reserva reserva : reservaService.listarReservasAtivasCliente(clienteId)) {
            reservasAtivasModel.addElement(reserva);
        }
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erro", JOptionPane.ERROR_MESSAGE);
    }
}