package pt.kartodromo.desktop.ui.categorias;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;

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

import pt.kartodromo.core.bll.CategoriaKartService;
import pt.kartodromo.core.model.CategoriaKart;

public class CategoriaPanel extends JPanel {

    private static final Color BACKGROUND_COLOR = new Color(245, 247, 250);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(215, 220, 225);
    private static final Color PRIMARY_BLUE = new Color(33, 150, 243);
    private static final Color CREATE_GREEN = new Color(46, 125, 50);
    private static final Color UPDATE_BLUE = new Color(21, 101, 192);
    private static final Color DELETE_RED = new Color(198, 40, 40);
    private static final Color CLEAR_GRAY = new Color(97, 97, 97);

    private final CategoriaKartService categoriaService = new CategoriaKartService();

    private Long categoriaSelecionadaId = null;

    private final JTextField cilindradaField = new JTextField("270");
    private final JTextField descricaoField = new JTextField();
    private final JTextField idadeMinimaField = new JTextField("12");
    private final JTextField experienciaMinimaField = new JTextField("0");
    private final JTextField precoBaseField = new JTextField("25.00");

    private final DefaultTableModel categoriasTableModel =
            new DefaultTableModel(
                    new Object[]{
                            "ID",
                            "Cilindrada",
                            "Descrição",
                            "Idade Mínima",
                            "Experiência Mínima",
                            "Preço Base"
                    },
                    0
            ) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

    private final JTable categoriasTable = new JTable(categoriasTableModel);

    public CategoriaPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createCard(buildForm()), BorderLayout.NORTH);
        add(createCard(buildTable()), BorderLayout.CENTER);

        refreshData();
    }

    private JPanel buildForm() {
        JButton criarButton =
                createActionButton("＋ Nova", CREATE_GREEN);

        JButton atualizarButton =
                createActionButton("✎ Atualizar", UPDATE_BLUE);

        JButton removerButton =
                createActionButton("🗑 Remover", DELETE_RED);

        JButton limparButton =
                createActionButton("↺ Limpar", CLEAR_GRAY);

        criarButton.addActionListener(e -> criarCategoria());
        atualizarButton.addActionListener(e -> atualizarCategoria());
        removerButton.addActionListener(e -> removerCategoria());
        limparButton.addActionListener(e -> limparFormulario());

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBackground(CARD_COLOR);
        fieldsPanel.setBorder(
                BorderFactory.createTitledBorder("🏷 Dados da Categoria")
        );

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 8, 7, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        addFormRow(fieldsPanel, gbc, 0, "Cilindrada", cilindradaField);
        addFormRow(fieldsPanel, gbc, 1, "Descrição", descricaoField);
        addFormRow(fieldsPanel, gbc, 2, "Idade mínima", idadeMinimaField);
        addFormRow(fieldsPanel, gbc, 3, "Experiência mínima (0-5)", experienciaMinimaField);
        addFormRow(fieldsPanel, gbc, 4, "Preço base", precoBaseField);

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

    private JScrollPane buildTable() {
        categoriasTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoriasTable.setAutoCreateRowSorter(true);
        categoriasTable.setRowHeight(32);
        categoriasTable.setFillsViewportHeight(true);
        categoriasTable.setGridColor(new Color(230, 230, 230));
        categoriasTable.setSelectionBackground(new Color(66, 133, 244));
        categoriasTable.setSelectionForeground(Color.WHITE);

        categoriasTable.getTableHeader().setReorderingAllowed(false);
        categoriasTable.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 14)
        );
        categoriasTable.getTableHeader().setBackground(PRIMARY_BLUE);
        categoriasTable.getTableHeader().setForeground(Color.WHITE);

        categoriasTable
                .getSelectionModel()
                .addListSelectionListener(e -> {
                    if (!e.getValueIsAdjusting()) {
                        carregarCategoriaSelecionada();
                    }
                });

        JScrollPane scroll = new JScrollPane(categoriasTable);
        scroll.setBorder(BorderFactory.createTitledBorder("Categorias"));

        return scroll;
    }

    private void criarCategoria() {
        try {
            CategoriaKart categoria =
                    categoriaService.criarCategoria(
                            Integer.parseInt(cilindradaField.getText().trim()),
                            descricaoField.getText().trim(),
                            Integer.parseInt(idadeMinimaField.getText().trim()),
                            Integer.parseInt(experienciaMinimaField.getText().trim()),
                            new BigDecimal(precoBaseField.getText().trim())
                    );

            showInfo("Categoria criada com sucesso: " + categoria.getId());

            limparFormulario();
            refreshData();

        } catch (NumberFormatException ex) {
            showError("Verifique os campos numéricos da categoria.");
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void atualizarCategoria() {
        if (categoriaSelecionadaId == null) {
            showError("Selecione uma categoria.");
            return;
        }

        try {
            categoriaService.atualizarCategoria(
                    categoriaSelecionadaId,
                    Integer.parseInt(cilindradaField.getText().trim()),
                    descricaoField.getText().trim(),
                    Integer.parseInt(idadeMinimaField.getText().trim()),
                    Integer.parseInt(experienciaMinimaField.getText().trim()),
                    new BigDecimal(precoBaseField.getText().trim())
            );

            showInfo("Categoria atualizada com sucesso.");

            limparFormulario();
            refreshData();

        } catch (NumberFormatException ex) {
            showError("Verifique os campos numéricos da categoria.");
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void removerCategoria() {
        if (categoriaSelecionadaId == null) {
            showError("Selecione uma categoria para remover.");
            return;
        }

        int confirmacao =
                JOptionPane.showConfirmDialog(
                        this,
                        "Deseja remover a categoria selecionada?",
                        "Confirmar remoção",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

        if (confirmacao != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            categoriaService.removerCategoria(categoriaSelecionadaId);

            showInfo("Categoria removida com sucesso.");

            limparFormulario();
            refreshData();

        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void carregarCategoriaSelecionada() {
        int selectedRow = categoriasTable.getSelectedRow();

        if (selectedRow == -1) {
            return;
        }

        int modelRow =
                categoriasTable.convertRowIndexToModel(selectedRow);

        categoriaSelecionadaId =
                (Long) categoriasTableModel.getValueAt(modelRow, 0);

        cilindradaField.setText(
                categoriasTableModel.getValueAt(modelRow, 1).toString()
        );

        descricaoField.setText(
                categoriasTableModel.getValueAt(modelRow, 2).toString()
        );

        idadeMinimaField.setText(
                categoriasTableModel.getValueAt(modelRow, 3).toString()
        );

        experienciaMinimaField.setText(
                categoriasTableModel.getValueAt(modelRow, 4).toString()
        );

        precoBaseField.setText(
                categoriasTableModel.getValueAt(modelRow, 5).toString()
        );
    }

    private void limparFormulario() {
        categoriaSelecionadaId = null;

        cilindradaField.setText("270");
        descricaoField.setText("");
        idadeMinimaField.setText("12");
        experienciaMinimaField.setText("0");
        precoBaseField.setText("25.00");

        categoriasTable.clearSelection();
    }

    public void refreshData() {
        categoriasTableModel.setRowCount(0);

        for (CategoriaKart categoria : categoriaService.listarCategorias()) {
            categoriasTableModel.addRow(
                    new Object[]{
                            categoria.getId(),
                            categoria.getCilindrada(),
                            categoria.getDescricao(),
                            categoria.getIdadeMinima(),
                            categoria.getExperienciaMinima(),
                            categoria.getPrecoBase()
                    }
            );
        }
    }

    private JPanel createCard(Component component) {
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
            Component component) {

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

        component.setFont(new Font("Segoe UI", Font.PLAIN, 13));
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