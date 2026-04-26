package pt.kartodromo.desktop.ui.karts;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.CompoundBorder;
import javax.swing.table.DefaultTableModel;

import pt.kartodromo.core.bll.CategoriaKartService;
import pt.kartodromo.core.bll.KartService;
import pt.kartodromo.core.model.CategoriaKart;
import pt.kartodromo.core.model.Kart;
import pt.kartodromo.core.model.enums.KartEstado;

public class KartPanel extends JPanel {

    private static final Color BACKGROUND_COLOR = new Color(245, 247, 250);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(215, 220, 225);
    private static final Color PRIMARY_BLUE = new Color(33, 150, 243);
    private static final Color CREATE_GREEN = new Color(46, 125, 50);
    private static final Color UPDATE_BLUE = new Color(21, 101, 192);
    private static final Color DELETE_RED = new Color(198, 40, 40);
    private static final Color CLEAR_GRAY = new Color(97, 97, 97);

    private final KartService kartService =
            new KartService();

    private final CategoriaKartService categoriaService =
            new CategoriaKartService();

    private Long kartSelecionadoId = null;

    private final JTextField numeroField =
            new JTextField("1");

    private final JComboBox<CategoriaKart> categoriaCombo =
            new JComboBox<>();

    private final JComboBox<KartEstado> estadoCombo =
            new JComboBox<>(KartEstado.values());

    private final JCheckBox disponivelCheck =
            new JCheckBox("Disponível", true);

    private final DefaultTableModel kartsTableModel =
            new DefaultTableModel(
                    new Object[]{
                            "ID",
                            "Número",
                            "Categoria",
                            "Estado",
                            "Disponível"
                    },
                    0
            ) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

    private final JTable kartsTable =
            new JTable(kartsTableModel);

    public KartPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        configureComboRenderer();

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

        criarButton.addActionListener(e -> criarKart());
        atualizarButton.addActionListener(e -> atualizarKart());
        removerButton.addActionListener(e -> removerKart());
        limparButton.addActionListener(e -> limparFormulario());

        JPanel fieldsPanel =
                new JPanel(new GridBagLayout());

        fieldsPanel.setBackground(CARD_COLOR);

        fieldsPanel.setBorder(
                BorderFactory.createTitledBorder("🏎 Dados do Kart")
        );

        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.insets =
                new Insets(7, 8, 7, 8);

        gbc.fill =
                GridBagConstraints.HORIZONTAL;

        gbc.weightx =
                1;

        addFormRow(fieldsPanel, gbc, 0, "Número", numeroField);
        addFormRow(fieldsPanel, gbc, 1, "Categoria", categoriaCombo);
        addFormRow(fieldsPanel, gbc, 2, "Estado", estadoCombo);
        addFormRow(fieldsPanel, gbc, 3, "Disponibilidade", disponivelCheck);

        JPanel actionsPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT,
                                10,
                                6
                        )
                );

        actionsPanel.setBackground(CARD_COLOR);

        actionsPanel.add(criarButton);
        actionsPanel.add(atualizarButton);
        actionsPanel.add(removerButton);
        actionsPanel.add(limparButton);

        JPanel form =
                new JPanel(
                        new BorderLayout(10, 10)
                );

        form.setBackground(CARD_COLOR);
        form.add(fieldsPanel, BorderLayout.CENTER);
        form.add(actionsPanel, BorderLayout.SOUTH);

        return form;
    }

    private JScrollPane buildTable() {
        kartsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        kartsTable.setAutoCreateRowSorter(true);
        kartsTable.setRowHeight(32);
        kartsTable.setFillsViewportHeight(true);
        kartsTable.setGridColor(new Color(230, 230, 230));
        kartsTable.setSelectionBackground(new Color(66, 133, 244));
        kartsTable.setSelectionForeground(Color.WHITE);

        kartsTable.getTableHeader().setReorderingAllowed(false);

        kartsTable.getTableHeader().setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        14
                )
        );

        kartsTable.getTableHeader().setBackground(PRIMARY_BLUE);
        kartsTable.getTableHeader().setForeground(Color.WHITE);

        kartsTable
                .getSelectionModel()
                .addListSelectionListener(e -> {
                    if (!e.getValueIsAdjusting()) {
                        carregarKartSelecionado();
                    }
                });

        JScrollPane scroll =
                new JScrollPane(kartsTable);

        scroll.setBorder(
                BorderFactory.createTitledBorder("Karts")
        );

        return scroll;
    }

    private void criarKart() {
        try {
            CategoriaKart categoria =
                    (CategoriaKart) categoriaCombo.getSelectedItem();

            if (categoria == null) {
                showError("Crie uma categoria antes de criar karts.");
                return;
            }

            Kart kart =
                    kartService.criarKart(
                            Integer.parseInt(numeroField.getText().trim()),
                            (KartEstado) estadoCombo.getSelectedItem(),
                            disponivelCheck.isSelected(),
                            categoria.getId()
                    );

            showInfo("Kart criado com sucesso: " + kart.getId());

            limparFormulario();
            refreshData();

        } catch (NumberFormatException ex) {
            showError("O número do kart deve ser numérico.");
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void atualizarKart() {
        if (kartSelecionadoId == null) {
            showError("Selecione um kart.");
            return;
        }

        try {
            kartService.atualizarEstado(
                    kartSelecionadoId,
                    (KartEstado) estadoCombo.getSelectedItem()
            );

            kartService.definirDisponibilidade(
                    kartSelecionadoId,
                    disponivelCheck.isSelected()
            );

            showInfo("Kart atualizado com sucesso.");

            limparFormulario();
            refreshData();

        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void removerKart() {
        if (kartSelecionadoId == null) {
            showError("Selecione um kart.");
            return;
        }

        showError("A remoção de karts ainda não está disponível no backend.");
    }

    private void carregarKartSelecionado() {
        int selectedRow =
                kartsTable.getSelectedRow();

        if (selectedRow == -1) {
            return;
        }

        int modelRow =
                kartsTable.convertRowIndexToModel(selectedRow);

        kartSelecionadoId =
                (Long) kartsTableModel.getValueAt(modelRow, 0);

        numeroField.setText(
                kartsTableModel.getValueAt(modelRow, 1).toString()
        );

        selecionarCategoriaPorDescricao(
                kartsTableModel.getValueAt(modelRow, 2).toString()
        );

        estadoCombo.setSelectedItem(
                kartsTableModel.getValueAt(modelRow, 3)
        );

        disponivelCheck.setSelected(
                Boolean.parseBoolean(
                        kartsTableModel.getValueAt(modelRow, 4).toString()
                )
        );
    }

    private void selecionarCategoriaPorDescricao(String descricao) {
        for (int i = 0; i < categoriaCombo.getItemCount(); i++) {
            CategoriaKart categoria =
                    categoriaCombo.getItemAt(i);

            if (categoria != null
                    && categoria.getDescricao() != null
                    && categoria.getDescricao().equals(descricao)) {

                categoriaCombo.setSelectedItem(categoria);
                return;
            }
        }
    }

    private void limparFormulario() {
        kartSelecionadoId = null;

        numeroField.setText("1");
        disponivelCheck.setSelected(true);
        estadoCombo.setSelectedItem(KartEstado.OPERACIONAL);

        if (categoriaCombo.getItemCount() > 0) {
            categoriaCombo.setSelectedIndex(0);
        }

        kartsTable.clearSelection();
    }

    public void refreshData() {
        refreshCategorias();
        refreshKarts();
    }

    private void refreshCategorias() {
        categoriaCombo.removeAllItems();

        for (CategoriaKart categoria : categoriaService.listarCategorias()) {
            categoriaCombo.addItem(categoria);
        }
    }

    private void refreshKarts() {
        kartsTableModel.setRowCount(0);

        for (Kart kart : kartService.listarKarts()) {
            kartsTableModel.addRow(
                    new Object[]{
                            kart.getId(),
                            kart.getNumero(),
                            kart.getCategoria() != null
                                    ? kart.getCategoria().getDescricao()
                                    : "",
                            kart.getEstado(),
                            kart.isDisponivel()
                                    ? "Sim"
                                    : "Não"
                    }
            );
        }
    }

    private void configureComboRenderer() {
        categoriaCombo.setRenderer(
                new DefaultListCellRenderer() {
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
                }
        );
    }

    private JPanel createCard(Component component) {
        JPanel card =
                new JPanel(new BorderLayout());

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
        JButton button =
                new JButton(text);

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
            Component component) {

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.gridwidth = 1;

        JLabel labelComponent =
                new JLabel(label);

        labelComponent.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        13
                )
        );

        panel.add(labelComponent, gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 1;
        gbc.gridwidth = 1;

        component.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        13
                )
        );

        panel.add(component, gbc);
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