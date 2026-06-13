package pt.kartodromo.desktop.ui.corridas;

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

import pt.kartodromo.core.bll.CategoriaKartService;
import pt.kartodromo.core.bll.ClienteService;
import pt.kartodromo.core.bll.CorridaService;
import pt.kartodromo.core.model.CategoriaKart;
import pt.kartodromo.core.model.Cliente;
import pt.kartodromo.core.model.Corrida;
import pt.kartodromo.desktop.ui.UiStyle;

public class CorridaPanel extends JPanel {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final CorridaService corridaService = new CorridaService();
    private final CategoriaKartService categoriaService = new CategoriaKartService();
    private final ClienteService clienteService = new ClienteService();

    private final Runnable abrirPistasAction;

    private Long corridaSelecionadaId = null;

    private String layoutSelecionado = "Pista Completa";

    private final JTextField dataHoraField =
            new JTextField(LocalDateTime.now().format(DATE_TIME_FORMAT));

    private final JTextField duracaoField = new JTextField("15");
    private final JTextField vagasField = new JTextField("10");

    private final JLabel layoutSelecionadoLabel =
            new JLabel(layoutSelecionado);

    private final JComboBox<CategoriaKart> categoriaCombo = new JComboBox<>();
    private final JComboBox<Cliente> clienteCombo = new JComboBox<>();

    private final DefaultTableModel corridasTableModel =
            new DefaultTableModel(
                    new Object[]{
                            "ID",
                            "Data/Hora",
                            "Duração",
                            "Vagas",
                            "Cliente",
                            "Categoria",
                            "Layout"
                    },
                    0
            ) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

    private final JTable corridasTable = new JTable(corridasTableModel);

    public CorridaPanel() {
        this(null);
    }

    public CorridaPanel(Runnable abrirPistasAction) {
        this.abrirPistasAction = abrirPistasAction;

        setLayout(new BorderLayout(20, 20));
        setBackground(UiStyle.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        configureComboRenderers();

        add(UiStyle.createCard(buildForm()), BorderLayout.NORTH);
        add(UiStyle.createCard(buildTable()), BorderLayout.CENTER);

        refreshData();
    }

    private JPanel buildForm() {
        JButton criarButton = UiStyle.createActionButton("+ Nova", UiStyle.CREATE_GREEN);
        JButton atualizarButton = UiStyle.createActionButton("✎ Atualizar", UiStyle.UPDATE_BLUE);
        JButton removerButton = UiStyle.createActionButton("🗑 Remover", UiStyle.DELETE_RED);
        JButton limparButton = UiStyle.createActionButton("↺ Limpar", UiStyle.CLEAR_GRAY);

        criarButton.addActionListener(e -> criarCorrida());
        atualizarButton.addActionListener(e -> atualizarCorrida());
        removerButton.addActionListener(e -> removerCorrida());
        limparButton.addActionListener(e -> limparFormulario());

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);
        fieldsPanel.setBorder(BorderFactory.createTitledBorder("🏁 Dados da Corrida"));

        GridBagConstraints gbc = createGbc();

        addFormRow(fieldsPanel, gbc, 0, "Data/hora (yyyy-MM-dd HH:mm)", dataHoraField);
        addFormRow(fieldsPanel, gbc, 1, "Duração (minutos)", duracaoField);
        addFormRow(fieldsPanel, gbc, 2, "Vagas máximas", vagasField);
        addFormRow(fieldsPanel, gbc, 3, "Layout", buildLayoutSelector());
        addFormRow(fieldsPanel, gbc, 4, "Cliente", clienteCombo);
        addFormRow(fieldsPanel, gbc, 5, "Categoria", categoriaCombo);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        actionsPanel.setOpaque(false);
        actionsPanel.add(criarButton);
        actionsPanel.add(atualizarButton);
        actionsPanel.add(removerButton);
        actionsPanel.add(limparButton);

        JPanel form = new JPanel(new BorderLayout(10, 10));
        form.setOpaque(false);
        form.add(fieldsPanel, BorderLayout.CENTER);
        form.add(actionsPanel, BorderLayout.SOUTH);

        return form;
    }

    private JPanel buildLayoutSelector() {
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

        layoutSelecionadoLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        layoutSelecionadoLabel.setForeground(UiStyle.BLACK);

        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setOpaque(false);
        panel.add(selecionarPistaButton, BorderLayout.WEST);
        panel.add(layoutSelecionadoLabel, BorderLayout.CENTER);

        return panel;
    }

    private JScrollPane buildTable() {
        corridasTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UiStyle.styleTable(corridasTable);

        corridasTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                carregarCorridaSelecionada();
            }
        });

        JScrollPane scroll = new JScrollPane(corridasTable);
        scroll.setBorder(BorderFactory.createTitledBorder("Corridas"));

        return scroll;
    }

    private void criarCorrida() {
        try {
            CategoriaKart categoria = (CategoriaKart) categoriaCombo.getSelectedItem();
            Cliente cliente = (Cliente) clienteCombo.getSelectedItem();

            if (categoria == null || cliente == null) {
                showError("Selecione cliente e categoria.");
                return;
            }

            Corrida corrida = corridaService.criarCorrida(
                    LocalDateTime.parse(dataHoraField.getText().trim(), DATE_TIME_FORMAT),
                    Integer.parseInt(duracaoField.getText().trim()),
                    Integer.parseInt(vagasField.getText().trim()),
                    categoria.getId(),
                    cliente.getId(),
                    layoutSelecionado
            );

            showInfo("Corrida criada com sucesso: " + corrida.getId());

            limparFormulario();
            refreshData();

        } catch (DateTimeParseException ex) {
            showError("Data/hora inválida. Formato: yyyy-MM-dd HH:mm.");
        } catch (NumberFormatException ex) {
            showError("Duração e vagas devem ser valores numéricos.");
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void atualizarCorrida() {
        if (corridaSelecionadaId == null) {
            showError("Selecione uma corrida.");
            return;
        }

        try {
            CategoriaKart categoria = (CategoriaKart) categoriaCombo.getSelectedItem();
            Cliente cliente = (Cliente) clienteCombo.getSelectedItem();

            if (categoria == null || cliente == null) {
                showError("Selecione cliente e categoria.");
                return;
            }

            corridaService.atualizarCorrida(
                    corridaSelecionadaId,
                    LocalDateTime.parse(dataHoraField.getText().trim(), DATE_TIME_FORMAT),
                    Integer.parseInt(duracaoField.getText().trim()),
                    Integer.parseInt(vagasField.getText().trim()),
                    categoria.getId(),
                    cliente.getId(),
                    layoutSelecionado
            );

            showInfo("Corrida atualizada com sucesso.");

            limparFormulario();
            refreshData();

        } catch (DateTimeParseException ex) {
            showError("Data/hora inválida. Formato: yyyy-MM-dd HH:mm.");
        } catch (NumberFormatException ex) {
            showError("Duração e vagas devem ser valores numéricos.");
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void removerCorrida() {
        if (corridaSelecionadaId == null) {
            showError("Selecione uma corrida.");
            return;
        }

        int confirmacao =
                JOptionPane.showConfirmDialog(
                        this,
                        "Deseja remover a corrida selecionada?",
                        "Confirmar remoção",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

        if (confirmacao != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            corridaService.eliminarCorrida(corridaSelecionadaId);

            showInfo("Corrida removida com sucesso.");

            limparFormulario();
            refreshData();

        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void carregarCorridaSelecionada() {
        int selectedRow = corridasTable.getSelectedRow();

        if (selectedRow == -1) {
            return;
        }

        int modelRow = corridasTable.convertRowIndexToModel(selectedRow);

        corridaSelecionadaId = (Long) corridasTableModel.getValueAt(modelRow, 0);

        Corrida corrida = encontrarCorridaPorId(corridaSelecionadaId);

        if (corrida == null) {
            return;
        }

        dataHoraField.setText(corrida.getDataHoraInicio().format(DATE_TIME_FORMAT));
        duracaoField.setText(String.valueOf(corrida.getDuracaoMinutos()));
        vagasField.setText(String.valueOf(corrida.getVagasMaximas()));
        setLayoutSelecionado(corrida.getLayoutNome());

        if (corrida.getCliente() != null) {
            selecionarClientePorId(corrida.getCliente().getId());
        }

        if (corrida.getCategoria() != null) {
            selecionarCategoriaPorId(corrida.getCategoria().getId());
        }
    }

    public void setLayoutSelecionado(String layoutSelecionado) {
        if (layoutSelecionado == null || layoutSelecionado.isBlank()) {
            this.layoutSelecionado = "Pista Completa";
        } else {
            this.layoutSelecionado = layoutSelecionado;
        }

        layoutSelecionadoLabel.setText(this.layoutSelecionado);
    }

    private Corrida encontrarCorridaPorId(Long id) {
        for (Corrida corrida : corridaService.listarCorridas()) {
            if (corrida.getId().equals(id)) {
                return corrida;
            }
        }

        return null;
    }

    private void selecionarClientePorId(Long id) {
        for (int i = 0; i < clienteCombo.getItemCount(); i++) {
            Cliente cliente = clienteCombo.getItemAt(i);

            if (cliente.getId().equals(id)) {
                clienteCombo.setSelectedItem(cliente);
                return;
            }
        }
    }

    private void selecionarCategoriaPorId(Long id) {
        for (int i = 0; i < categoriaCombo.getItemCount(); i++) {
            CategoriaKart categoria = categoriaCombo.getItemAt(i);

            if (categoria.getId().equals(id)) {
                categoriaCombo.setSelectedItem(categoria);
                return;
            }
        }
    }

    private void limparFormulario() {
        corridaSelecionadaId = null;

        dataHoraField.setText(LocalDateTime.now().format(DATE_TIME_FORMAT));
        duracaoField.setText("15");
        vagasField.setText("10");
        setLayoutSelecionado("Pista Completa");

        if (clienteCombo.getItemCount() > 0) {
            clienteCombo.setSelectedIndex(0);
        }

        if (categoriaCombo.getItemCount() > 0) {
            categoriaCombo.setSelectedIndex(0);
        }

        corridasTable.clearSelection();
    }

    public void refreshData() {
        refreshClientes();
        refreshCategorias();
        refreshCorridas();
    }

    private void refreshClientes() {
        clienteCombo.removeAllItems();

        for (Cliente cliente : clienteService.listarClientes()) {
            clienteCombo.addItem(cliente);
        }
    }

    private void refreshCategorias() {
        categoriaCombo.removeAllItems();

        for (CategoriaKart categoria : categoriaService.listarCategorias()) {
            categoriaCombo.addItem(categoria);
        }
    }

    private void refreshCorridas() {
        corridasTableModel.setRowCount(0);

        for (Corrida corrida : corridaService.listarCorridas()) {
            corridasTableModel.addRow(
                    new Object[]{
                            corrida.getId(),
                            corrida.getDataHoraInicio().format(DATE_TIME_FORMAT),
                            corrida.getDuracaoMinutos(),
                            corrida.getVagasMaximas(),
                            corrida.getCliente() != null
                                    ? corrida.getCliente().getNome()
                                    : "",
                            corrida.getCategoria() != null
                                    ? corrida.getCategoria().getDescricao()
                                    : "",
                            corrida.getLayoutNome()
                    }
            );
        }
    }

    private void configureComboRenderers() {
        clienteCombo.setRenderer(new DefaultListCellRenderer() {
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
        });

        categoriaCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {

                String text = "";

                if (value instanceof CategoriaKart categoria) {
                    text = categoria.getDescricao();
                }

                return super.getListCellRendererComponent(
                        list,
                        text,
                        index,
                        isSelected,
                        cellHasFocus
                );
            }
        });
    }

    private GridBagConstraints createGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 8, 7, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
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

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erro", JOptionPane.ERROR_MESSAGE);
    }
}