package pt.kartodromo.desktop.ui.reservas;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

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
import javax.swing.JTextField;

import pt.kartodromo.core.bll.ClienteService;
import pt.kartodromo.core.bll.KartService;
import pt.kartodromo.core.bll.ReservaService;
import pt.kartodromo.core.model.Cliente;
import pt.kartodromo.core.model.Kart;
import pt.kartodromo.core.model.Reserva;
import pt.kartodromo.core.model.enums.ReservaEstado;

public class ReservaPanel extends JPanel {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ClienteService clienteService =
            new ClienteService();

    private final KartService kartService =
            new KartService();

    private final ReservaService reservaService =
            new ReservaService();

    private final DefaultComboBoxModel<Cliente> clienteComboModel =
            new DefaultComboBoxModel<>();

    private final DefaultComboBoxModel<Kart> kartComboModel =
            new DefaultComboBoxModel<>();

    private final DefaultListModel<Reserva> reservasModel =
            new DefaultListModel<>();

    private final JList<Reserva> reservasList =
            new JList<>(reservasModel);

    private final JTextField pistaField =
            new JTextField("Pista Completa");

    private final JTextField inicioField =
            new JTextField("2030-01-01 10:00");

    private final JTextField fimField =
            new JTextField("2030-01-01 10:30");

    private final JComboBox<Cliente> clienteCombo =
            new JComboBox<>(clienteComboModel);

    private final JComboBox<Kart> kartCombo =
            new JComboBox<>(kartComboModel);

    private final JComboBox<ReservaEstado> estadoCombo =
            new JComboBox<>(ReservaEstado.values());

    private Long reservaSelecionadaId = null;

    public ReservaPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        configureComboRenderers();
        configureReservaRenderer();

        add(buildForm(), BorderLayout.NORTH);
        add(buildReservasList(), BorderLayout.CENTER);

        refreshData();
    }

    private JPanel buildForm() {
        JButton criarButton =
                new JButton("Criar reserva");

        JButton atualizarButton =
                new JButton("Atualizar reserva");

        JButton eliminarButton =
                new JButton("Eliminar reserva");

        JButton limparButton =
                new JButton("Limpar seleção");

        criarButton.addActionListener(e -> criarReserva());
        atualizarButton.addActionListener(e -> atualizarReserva());
        eliminarButton.addActionListener(e -> eliminarReserva());
        limparButton.addActionListener(e -> limparFormulario());

        JPanel form =
                new JPanel(
                        new GridLayout(
                                8,
                                2,
                                8,
                                8
                        )
                );

        form.setBorder(
                BorderFactory.createTitledBorder(
                        "Gestão de reservas"
                )
        );

        form.add(new JLabel("Cliente"));
        form.add(clienteCombo);

        form.add(new JLabel("Kart"));
        form.add(kartCombo);

        form.add(new JLabel("Pista"));
        form.add(pistaField);

        form.add(new JLabel("Início (yyyy-MM-dd HH:mm)"));
        form.add(inicioField);

        form.add(new JLabel("Fim (yyyy-MM-dd HH:mm)"));
        form.add(fimField);

        form.add(new JLabel("Estado"));
        form.add(estadoCombo);

        form.add(criarButton);
        form.add(atualizarButton);

        form.add(eliminarButton);
        form.add(limparButton);

        return form;
    }

    private JScrollPane buildReservasList() {
        reservasList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                carregarReservaSelecionada();
            }
        });

        JScrollPane scroll =
                new JScrollPane(
                        reservasList
                );

        scroll.setBorder(
                BorderFactory.createTitledBorder(
                        "Reservas"
                )
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
                            pistaField.getText().trim(),
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

            showInfo(
                    "Reserva criada com sucesso: "
                            + reserva.getId()
            );

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
            showError("Selecione uma reserva para atualizar.");
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
                    pistaField.getText().trim(),
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

    private void eliminarReserva() {
        if (reservaSelecionadaId == null) {
            showError("Selecione uma reserva para eliminar.");
            return;
        }

        int confirmacao =
                JOptionPane.showConfirmDialog(
                        this,
                        "Deseja eliminar a reserva selecionada?",
                        "Confirmar eliminação",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

        if (confirmacao != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            reservaService.eliminarReserva(
                    reservaSelecionadaId
            );

            showInfo("Reserva eliminada com sucesso.");

            limparFormulario();
            refreshData();

        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void carregarReservaSelecionada() {
        Reserva reserva =
                reservasList.getSelectedValue();

        if (reserva == null) {
            return;
        }

        reservaSelecionadaId =
                reserva.getId();

        clienteCombo.setSelectedItem(
                reserva.getCliente()
        );

        kartCombo.setSelectedItem(
                reserva.getKart()
        );

        pistaField.setText(
                reserva.getPistaNome()
        );

        inicioField.setText(
                reserva.getDataHoraInicio()
                        .format(DATE_TIME_FORMAT)
        );

        fimField.setText(
                reserva.getDataHoraFim()
                        .format(DATE_TIME_FORMAT)
        );

        estadoCombo.setSelectedItem(
                reserva.getEstado()
        );
    }

    private void limparFormulario() {
        reservaSelecionadaId = null;

        if (clienteCombo.getItemCount() > 0) {
            clienteCombo.setSelectedIndex(0);
        }

        if (kartCombo.getItemCount() > 0) {
            kartCombo.setSelectedIndex(0);
        }

        pistaField.setText("Pista Completa");
        inicioField.setText("2030-01-01 10:00");
        fimField.setText("2030-01-01 10:30");
        estadoCombo.setSelectedItem(ReservaEstado.PENDENTE);

        reservasList.clearSelection();
    }

    public void refreshData() {
        refreshClientes();
        refreshKarts();
        refreshReservas();
    }

    private void refreshClientes() {
        clienteComboModel.removeAllElements();

        for (Cliente cliente : clienteService.listarClientes()) {
            clienteComboModel.addElement(cliente);
        }
    }

    private void refreshKarts() {
        kartComboModel.removeAllElements();

        for (Kart kart : kartService.listarKarts()) {
            kartComboModel.addElement(kart);
        }
    }

    private void refreshReservas() {
        reservasModel.clear();

        for (Reserva reserva : reservaService.listarReservas()) {
            reservasModel.addElement(reserva);
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

                        String texto = "";

                        if (value instanceof Cliente cliente) {
                            texto = cliente.getNome();
                        }

                        return super.getListCellRendererComponent(
                                list,
                                texto,
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

                        String texto = "";

                        if (value instanceof Kart kart) {
                            texto = "Kart #" + kart.getNumero();
                        }

                        return super.getListCellRendererComponent(
                                list,
                                texto,
                                index,
                                isSelected,
                                cellHasFocus
                        );
                    }
                }
        );
    }

    private void configureReservaRenderer() {
        reservasList.setCellRenderer(
                (list, value, index, isSelected, cellHasFocus) -> {

                    String text =
                            "Reserva #" + value.getId()
                                    + " | Cliente: " + value.getCliente().getNome()
                                    + " | Kart #" + value.getKart().getNumero()
                                    + " | Pista: " + value.getPistaNome()
                                    + " | " + value.getDataHoraInicio().format(DATE_TIME_FORMAT)
                                    + " -> " + value.getDataHoraFim().format(DATE_TIME_FORMAT)
                                    + " | " + value.getEstado();

                    return new DefaultListCellRenderer()
                            .getListCellRendererComponent(
                                    list,
                                    text,
                                    index,
                                    isSelected,
                                    cellHasFocus
                            );
                }
        );
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