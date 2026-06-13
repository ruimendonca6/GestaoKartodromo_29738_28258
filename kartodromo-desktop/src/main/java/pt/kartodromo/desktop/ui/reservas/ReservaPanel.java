package pt.kartodromo.desktop.ui.reservas;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import pt.kartodromo.core.bll.ClienteService;
import pt.kartodromo.core.bll.KartService;
import pt.kartodromo.core.bll.ReservaService;
import pt.kartodromo.core.model.Cliente;
import pt.kartodromo.core.model.Kart;
import pt.kartodromo.core.model.Reserva;
import pt.kartodromo.core.model.enums.ReservaEstado;
import pt.kartodromo.desktop.ui.UiStyle;

public class ReservaPanel extends JPanel {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ClienteService clienteService = new ClienteService();
    private final KartService kartService = new KartService();
    private final ReservaService reservaService = new ReservaService();

    private final Runnable abrirPistasAction;

    private Long reservaSelecionadaId = null;

    private String pistaSelecionada = "Pista Completa";

    private final JLabel pistaSelecionadaLabel =
            new JLabel(pistaSelecionada);

    private final JTextField inicioField =
            new JTextField(LocalDateTime.now().format(DATE_TIME_FORMAT));

    private final JTextField fimField =
            new JTextField(LocalDateTime.now().plusMinutes(30).format(DATE_TIME_FORMAT));

    private final JComboBox<Cliente> clienteCombo = new JComboBox<>();
    private final JComboBox<Kart> kartCombo = new JComboBox<>();

    private final JComboBox<ReservaEstado> estadoCombo =
            new JComboBox<>(ReservaEstado.values());

    private final DefaultTableModel reservasTableModel =
            new DefaultTableModel(
                    new Object[]{
                            "ID",
                            "Cliente",
                            "Kart",
                            "Pista",
                            "Início",
                            "Fim",
                            "Estado"
                    },
                    0
            ) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

    private final JTable reservasTable =
            new JTable(reservasTableModel);

    public ReservaPanel() {
        this(null);
    }

    public ReservaPanel(Runnable abrirPistasAction) {
        this.abrirPistasAction = abrirPistasAction;

        setLayout(new BorderLayout(20, 20));
        setBackground(UiStyle.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));

        configureComboRenderers();

        JPanel topPanel = new JPanel(new BorderLayout(0, 10));
        topPanel.setOpaque(false);

        topPanel.add(
                UiStyle.createPageTitle("Reservas"),
                BorderLayout.NORTH
        );

        topPanel.add(
                UiStyle.createCard(buildForm()),
                BorderLayout.CENTER
        );

        add(topPanel, BorderLayout.NORTH);
        add(UiStyle.createCard(buildTable()), BorderLayout.CENTER);

        refreshData();
        }

    private JPanel buildForm() {
        JButton criarButton =
                UiStyle.createActionButton("+ Nova", UiStyle.CREATE_GREEN);

        JButton atualizarButton =
                UiStyle.createActionButton("✎ Atualizar", UiStyle.UPDATE_BLUE);

        JButton removerButton =
                UiStyle.createActionButton("🗑 Remover", UiStyle.DELETE_RED);

        JButton limparButton =
                UiStyle.createActionButton("↺ Limpar", UiStyle.CLEAR_GRAY);

        criarButton.addActionListener(e -> criarReserva());
        atualizarButton.addActionListener(e -> atualizarReserva());
        removerButton.addActionListener(e -> removerReserva());
        limparButton.addActionListener(e -> limparFormulario());

        JPanel fieldsPanel =
                new JPanel(new GridBagLayout());

        fieldsPanel.setOpaque(false);
        fieldsPanel.setBorder(
                BorderFactory.createTitledBorder("📋 Dados da Reserva")
        );

        GridBagConstraints gbc =
                createGbc();

        addFormRow(fieldsPanel, gbc, 0, "Cliente", clienteCombo);
        addFormRow(fieldsPanel, gbc, 1, "Kart", kartCombo);
        addFormRow(fieldsPanel, gbc, 2, "Pista", buildPistaSelector());
        addFormRow(fieldsPanel, gbc, 3, "Início (yyyy-MM-dd HH:mm)", inicioField);
        addFormRow(fieldsPanel, gbc, 4, "Fim (yyyy-MM-dd HH:mm)", fimField);
        addFormRow(fieldsPanel, gbc, 5, "Estado", estadoCombo);

        JPanel actionsPanel =
                new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));

        actionsPanel.setOpaque(false);
        actionsPanel.add(criarButton);
        actionsPanel.add(atualizarButton);
        actionsPanel.add(removerButton);
        actionsPanel.add(limparButton);

        JPanel form =
                new JPanel(new BorderLayout(10, 10));

        form.setOpaque(false);
        form.add(fieldsPanel, BorderLayout.CENTER);
        form.add(actionsPanel, BorderLayout.SOUTH);

        return form;
    }

    private JPanel buildPistaSelector() {
        JButton selecionarPistaButton =
                UiStyle.createActionButton(
                        "Selecionar pista",
                        UiStyle.PRIMARY_RED
                );

        selecionarPistaButton.addActionListener(e -> {
            if (abrirPistasAction != null) {
                abrirPistasAction.run();
            }
        });

        pistaSelecionadaLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 13)
        );

        pistaSelecionadaLabel.setForeground(UiStyle.BLACK);

        JPanel panel =
                new JPanel(new BorderLayout(12, 0));

        panel.setOpaque(false);
        panel.add(selecionarPistaButton, BorderLayout.WEST);
        panel.add(pistaSelecionadaLabel, BorderLayout.CENTER);

        return panel;
    }

    private JScrollPane buildTable() {
        reservasTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UiStyle.styleTable(reservasTable);

        reservasTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                carregarReservaSelecionada();
            }
        });

        JScrollPane scroll =
                new JScrollPane(reservasTable);

        scroll.setBorder(
                BorderFactory.createTitledBorder("Reservas")
        );

        return scroll;
    }

    private void criarReserva() {
        try {
            Cliente cliente =
                    (Cliente) clienteCombo.getSelectedItem();

            Kart kart =
                    (Kart) kartCombo.getSelectedItem();

            ReservaEstado estado =
                    (ReservaEstado) estadoCombo.getSelectedItem();

            if (cliente == null || kart == null || estado == null) {
                showError("Selecione cliente, kart e estado.");
                return;
            }

            Reserva reserva =
                    reservaService.criarReserva(
                            cliente.getId(),
                            kart.getId(),
                            pistaSelecionada,
                            LocalDateTime.parse(
                                    inicioField.getText().trim(),
                                    DATE_TIME_FORMAT
                            ),
                            LocalDateTime.parse(
                                    fimField.getText().trim(),
                                    DATE_TIME_FORMAT
                            ),
                            estado
                    );

            showInfo("Reserva criada com sucesso: " + reserva.getId());

            limparFormulario();
            refreshData();

        } catch (DateTimeParseException ex) {
            showError("Data/hora inválida. Formato: yyyy-MM-dd HH:mm.");
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void atualizarReserva() {
        if (reservaSelecionadaId == null) {
            showError("Selecione uma reserva.");
            return;
        }

        try {
            Cliente cliente =
                    (Cliente) clienteCombo.getSelectedItem();

            Kart kart =
                    (Kart) kartCombo.getSelectedItem();

            ReservaEstado estado =
                    (ReservaEstado) estadoCombo.getSelectedItem();

            if (cliente == null || kart == null || estado == null) {
                showError("Selecione cliente, kart e estado.");
                return;
            }

            reservaService.atualizarReserva(
                    reservaSelecionadaId,
                    cliente.getId(),
                    kart.getId(),
                    pistaSelecionada,
                    LocalDateTime.parse(
                            inicioField.getText().trim(),
                            DATE_TIME_FORMAT
                    ),
                    LocalDateTime.parse(
                            fimField.getText().trim(),
                            DATE_TIME_FORMAT
                    ),
                    estado
            );

            showInfo("Reserva atualizada com sucesso.");

            limparFormulario();
            refreshData();

        } catch (DateTimeParseException ex) {
            showError("Data/hora inválida. Formato: yyyy-MM-dd HH:mm.");
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void removerReserva() {
        if (reservaSelecionadaId == null) {
            showError("Selecione uma reserva.");
            return;
        }

        int confirmacao =
                JOptionPane.showConfirmDialog(
                        this,
                        "Deseja remover a reserva selecionada?",
                        "Confirmar remoção",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

        if (confirmacao != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            reservaService.eliminarReserva(reservaSelecionadaId);

            showInfo("Reserva removida com sucesso.");

            limparFormulario();
            refreshData();

        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void carregarReservaSelecionada() {
        int selectedRow =
                reservasTable.getSelectedRow();

        if (selectedRow == -1) {
            return;
        }

        int modelRow =
                reservasTable.convertRowIndexToModel(selectedRow);

        reservaSelecionadaId =
                (Long) reservasTableModel.getValueAt(modelRow, 0);

        Reserva reserva =
                encontrarReservaPorId(reservaSelecionadaId);

        if (reserva == null) {
            return;
        }

        if (reserva.getCliente() != null) {
            selecionarClientePorId(reserva.getCliente().getId());
        }

        if (reserva.getKart() != null) {
            selecionarKartPorId(reserva.getKart().getId());
        }

        setPistaSelecionada(reserva.getPistaNome());

        inicioField.setText(
                reserva.getDataHoraInicio().format(DATE_TIME_FORMAT)
        );

        fimField.setText(
                reserva.getDataHoraFim().format(DATE_TIME_FORMAT)
        );

        estadoCombo.setSelectedItem(
                reserva.getEstado()
        );
    }

    public void setPistaSelecionada(String pista) {
        if (pista == null || pista.isBlank()) {
            pistaSelecionada = "Pista Completa";
        } else {
            pistaSelecionada = pista;
        }

        pistaSelecionadaLabel.setText(pistaSelecionada);
    }

    private Reserva encontrarReservaPorId(Long id) {
        for (Reserva reserva : reservaService.listarReservas()) {
            if (reserva.getId().equals(id)) {
                return reserva;
            }
        }

        return null;
    }

    private void selecionarClientePorId(Long id) {
        for (int i = 0; i < clienteCombo.getItemCount(); i++) {
            Cliente cliente =
                    clienteCombo.getItemAt(i);

            if (cliente.getId().equals(id)) {
                clienteCombo.setSelectedItem(cliente);
                return;
            }
        }
    }

    private void selecionarKartPorId(Long id) {
        for (int i = 0; i < kartCombo.getItemCount(); i++) {
            Kart kart =
                    kartCombo.getItemAt(i);

            if (kart.getId().equals(id)) {
                kartCombo.setSelectedItem(kart);
                return;
            }
        }
    }

    private void limparFormulario() {
        reservaSelecionadaId = null;

        if (clienteCombo.getItemCount() > 0) {
            clienteCombo.setSelectedIndex(0);
        }

        if (kartCombo.getItemCount() > 0) {
            kartCombo.setSelectedIndex(0);
        }

        setPistaSelecionada("Pista Completa");

        inicioField.setText(
                LocalDateTime.now().format(DATE_TIME_FORMAT)
        );

        fimField.setText(
                LocalDateTime.now().plusMinutes(30).format(DATE_TIME_FORMAT)
        );

        estadoCombo.setSelectedItem(ReservaEstado.PENDENTE);

        reservasTable.clearSelection();
    }

    public void refreshData() {
        refreshClientes();
        refreshKarts();
        refreshReservas();
    }

    private void refreshClientes() {
        clienteCombo.removeAllItems();

        for (Cliente cliente : clienteService.listarClientes()) {
            clienteCombo.addItem(cliente);
        }
    }

    private void refreshKarts() {
        kartCombo.removeAllItems();

        for (Kart kart : kartService.listarKarts()) {
            kartCombo.addItem(kart);
        }
    }

    private void refreshReservas() {
        reservasTableModel.setRowCount(0);

        for (Reserva reserva : reservaService.listarReservas()) {
            reservasTableModel.addRow(
                    new Object[]{
                            reserva.getId(),
                            reserva.getCliente() != null
                                    ? reserva.getCliente().getNome()
                                    : "",
                            reserva.getKart() != null
                                    ? "Kart #" + reserva.getKart().getNumero()
                                    : "",
                            reserva.getPistaNome(),
                            reserva.getDataHoraInicio().format(DATE_TIME_FORMAT),
                            reserva.getDataHoraFim().format(DATE_TIME_FORMAT),
                            reserva.getEstado()
                    }
            );
        }
    }

    private void configureComboRenderers() {
        clienteCombo.setRenderer(
                new DefaultListCellRenderer() {

                    @Override
                    public Component getListCellRendererComponent(
                            JList<?> list,
                            Object value,
                            int index,
                            boolean isSelected,
                            boolean cellHasFocus) {

                        String text = "";

                        if (value instanceof Cliente cliente) {
                            text = cliente.getNome();
                        }

                        return super.getListCellRendererComponent(
                                list,
                                text,
                                index,
                                isSelected,
                                cellHasFocus
                        );
                    }
                }
        );

        kartCombo.setRenderer(
                new DefaultListCellRenderer() {

                    @Override
                    public Component getListCellRendererComponent(
                            JList<?> list,
                            Object value,
                            int index,
                            boolean isSelected,
                            boolean cellHasFocus) {

                        String text = "";

                        if (value instanceof Kart kart) {
                            text = "Kart #" + kart.getNumero();
                        }

                        return super.getListCellRendererComponent(
                                list,
                                text,
                                index,
                                isSelected,
                                cellHasFocus
                        );
                    }
                }
        );
    }

    private GridBagConstraints createGbc() {
        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.insets =
                new Insets(7, 8, 7, 8);

        gbc.fill =
                GridBagConstraints.HORIZONTAL;

        gbc.weightx =
                1;

        return gbc;
    }

    private void addFormRow(
            JPanel panel,
            GridBagConstraints gbc,
            int row,
            String label,
            Component field) {

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.gridwidth = 1;

        JLabel labelComponent =
                new JLabel(label);

        labelComponent.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );

        panel.add(labelComponent, gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 1;
        gbc.gridwidth = 1;

        field.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );

        panel.add(field, gbc);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Sucesso",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Erro",
                JOptionPane.ERROR_MESSAGE
        );
    }
}