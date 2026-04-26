package pt.kartodromo.desktop.ui.clientes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.CompoundBorder;
import javax.swing.table.DefaultTableModel;

import pt.kartodromo.core.bll.ClienteService;
import pt.kartodromo.core.model.Cliente;

public class ClientePanel extends JPanel {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final Color BACKGROUND_COLOR =
            new Color(245, 247, 250);

    private static final Color CARD_COLOR =
            Color.WHITE;

    private static final Color BORDER_COLOR =
            new Color(215, 220, 225);

    private static final Color PRIMARY_BLUE =
            new Color(33, 150, 243);

    private static final Color CREATE_GREEN =
            new Color(46, 125, 50);

    private static final Color UPDATE_BLUE =
            new Color(21, 101, 192);

    private static final Color DELETE_RED =
            new Color(198, 40, 40);

    private static final Color CLEAR_GRAY =
            new Color(97, 97, 97);

    private final ClienteService clienteService =
            new ClienteService();

    private Long clienteSelecionadoId = null;

    private final JTextField nomeField =
            new JTextField();

    private final JTextField dataNascimentoField =
            new JTextField("2000-01-01");

    private final JTextField emailField =
            new JTextField();

    private final JTextField nivelField =
            new JTextField("0");

    private final DefaultTableModel clientesTableModel =
            new DefaultTableModel(
                    new Object[]{
                            "ID",
                            "Nome",
                            "Data Nascimento",
                            "Email",
                            "Nível Experiência"
                    },
                    0
            ) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

    private final JTable clientesTable =
            new JTable(clientesTableModel);

    public ClientePanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createCard(buildForm()), BorderLayout.NORTH);
        add(createCard(buildTable()), BorderLayout.CENTER);

        refreshData();
    }

    private JPanel buildForm() {
        JButton criarButton =
                createActionButton("＋ Novo", CREATE_GREEN);

        JButton atualizarButton =
                createActionButton("✎ Atualizar", UPDATE_BLUE);

        JButton removerButton =
                createActionButton("🗑 Remover", DELETE_RED);

        JButton limparButton =
                createActionButton("↺ Limpar", CLEAR_GRAY);

        criarButton.addActionListener(e -> criarCliente());
        atualizarButton.addActionListener(e -> atualizarCliente());
        removerButton.addActionListener(e -> removerCliente());
        limparButton.addActionListener(e -> limparFormulario());

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBackground(CARD_COLOR);
        fieldsPanel.setBorder(
                BorderFactory.createTitledBorder("👤 Dados do Cliente")
        );

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 8, 7, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        addFormRow(fieldsPanel, gbc, 0, "Nome", nomeField);
        addFormRow(fieldsPanel, gbc, 1, "Data nascimento (yyyy-MM-dd)", dataNascimentoField);
        addFormRow(fieldsPanel, gbc, 2, "Email", emailField);
        addFormRow(fieldsPanel, gbc, 3, "Nível experiência (0-5)", nivelField);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        actionsPanel.setBackground(CARD_COLOR);

        actionsPanel.add(criarButton);
        actionsPanel.add(atualizarButton);
        actionsPanel.add(removerButton);
        actionsPanel.add(limparButton);

        JPanel form = new JPanel(new BorderLayout(10, 10));
        form.setBackground(CARD_COLOR);
        form.add(fieldsPanel, BorderLayout.CENTER);
        form.add(actionsPanel, BorderLayout.SOUTH);

        return form;
    }

    private JPanel createCard(java.awt.Component component) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_COLOR);

        card.setBorder(
                new CompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, 1),
                        BorderFactory.createEmptyBorder(14, 14, 14, 14)
                )
        );

        card.add(component, BorderLayout.CENTER);

        return card;
    }

    private JButton createActionButton(String text, Color background) {
        JButton button = new JButton(text);

        button.setBackground(background);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(background.darker()),
                        BorderFactory.createEmptyBorder(7, 14, 7, 14)
                )
        );

        return button;
    }

    private void addFormRow(
            JPanel panel,
            GridBagConstraints gbc,
            int row,
            String label,
            JTextField field) {

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.gridwidth = 1;

        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        panel.add(labelComponent, gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 1;
        gbc.gridwidth = 1;

        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(field, gbc);
    }

    private JScrollPane buildTable() {
        clientesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        clientesTable.setAutoCreateRowSorter(true);
        clientesTable.setRowHeight(32);
        clientesTable.setFillsViewportHeight(true);
        clientesTable.setGridColor(new Color(230, 230, 230));
        clientesTable.setSelectionBackground(new Color(66, 133, 244));
        clientesTable.setSelectionForeground(Color.WHITE);

        clientesTable.getTableHeader().setReorderingAllowed(false);
        clientesTable.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 14)
        );
        clientesTable.getTableHeader().setBackground(PRIMARY_BLUE);
        clientesTable.getTableHeader().setForeground(Color.WHITE);

        clientesTable
                .getSelectionModel()
                .addListSelectionListener(e -> {
                    if (!e.getValueIsAdjusting()) {
                        carregarClienteSelecionado();
                    }
                });

        JScrollPane scroll = new JScrollPane(clientesTable);
        scroll.setBorder(BorderFactory.createTitledBorder("Clientes"));

        return scroll;
    }

    private void criarCliente() {
        try {
            Cliente cliente =
                    clienteService.criarCliente(
                            nomeField.getText().trim(),
                            LocalDate.parse(
                                    dataNascimentoField
                                            .getText()
                                            .trim(),
                                    DATE_FORMAT
                            ),
                            emailField.getText().trim(),
                            Integer.parseInt(
                                    nivelField
                                            .getText()
                                            .trim()
                            )
                    );

            showInfo(
                    "Cliente criado com sucesso: "
                            + cliente.getId()
            );

            limparFormulario();
            refreshData();

        } catch (DateTimeParseException ex) {
            showError("Data inválida. Use o formato yyyy-MM-dd.");
        } catch (NumberFormatException ex) {
            showError("Nível inválido. Deve ser um número entre 0 e 5.");
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void atualizarCliente() {
        if (clienteSelecionadoId == null) {
            showError("Selecione um cliente.");
            return;
        }

        try {
            clienteService.atualizarCliente(
                    clienteSelecionadoId,
                    nomeField.getText().trim(),
                    LocalDate.parse(
                            dataNascimentoField
                                    .getText()
                                    .trim(),
                            DATE_FORMAT
                    ),
                    emailField.getText().trim(),
                    Integer.parseInt(
                            nivelField
                                    .getText()
                                    .trim()
                    )
            );

            showInfo("Cliente atualizado com sucesso.");

            limparFormulario();
            refreshData();

        } catch (DateTimeParseException ex) {
            showError("Data inválida. Use o formato yyyy-MM-dd.");
        } catch (NumberFormatException ex) {
            showError("Nível inválido. Deve ser um número entre 0 e 5.");
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void removerCliente() {
        if (clienteSelecionadoId == null) {
            showError("Selecione cliente para remover.");
            return;
        }

        int confirmacao =
                JOptionPane.showConfirmDialog(
                        this,
                        "Deseja remover o cliente selecionado?",
                        "Confirmar remoção",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

        if (confirmacao != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            clienteService.removerCliente(clienteSelecionadoId);

            showInfo("Cliente removido com sucesso.");

            limparFormulario();
            refreshData();

        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void carregarClienteSelecionado() {
        int selectedRow = clientesTable.getSelectedRow();

        if (selectedRow == -1) {
            return;
        }

        int modelRow =
                clientesTable.convertRowIndexToModel(selectedRow);

        clienteSelecionadoId =
                (Long) clientesTableModel.getValueAt(modelRow, 0);

        nomeField.setText(
                clientesTableModel.getValueAt(modelRow, 1).toString()
        );

        dataNascimentoField.setText(
                clientesTableModel.getValueAt(modelRow, 2).toString()
        );

        emailField.setText(
                clientesTableModel.getValueAt(modelRow, 3).toString()
        );

        nivelField.setText(
                clientesTableModel.getValueAt(modelRow, 4).toString()
        );
    }

    private void limparFormulario() {
        clienteSelecionadoId = null;

        nomeField.setText("");
        emailField.setText("");
        nivelField.setText("0");
        dataNascimentoField.setText("2000-01-01");

        clientesTable.clearSelection();
    }

    public void refreshData() {
        clientesTableModel.setRowCount(0);

        for (Cliente cliente : clienteService.listarClientes()) {
            clientesTableModel.addRow(
                    new Object[]{
                            cliente.getId(),
                            cliente.getNome(),
                            cliente.getDataNascimento(),
                            cliente.getEmail(),
                            cliente.getNivelExperiencia()
                    }
            );
        }
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