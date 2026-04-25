package pt.kartodromo.desktop.ui.categorias;

import java.awt.BorderLayout;
import java.awt.GridLayout;
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
import javax.swing.table.DefaultTableModel;

import pt.kartodromo.core.bll.CategoriaKartService;
import pt.kartodromo.core.model.CategoriaKart;

public class CategoriaPanel extends JPanel {

    private final CategoriaKartService categoriaService =
            new CategoriaKartService();

    private Long categoriaSelecionadaId = null;

    private final JTextField cilindradaField =
            new JTextField("270");

    private final JTextField descricaoField =
            new JTextField();

    private final JTextField idadeMinimaField =
            new JTextField("12");

    private final JTextField experienciaMinimaField =
            new JTextField("0");

    private final JTextField precoBaseField =
            new JTextField("25.00");

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
                public boolean isCellEditable(
                        int row,
                        int column) {
                    return false;
                }
            };

    private final JTable categoriasTable =
            new JTable(categoriasTableModel);

    public CategoriaPanel() {

        setLayout(
                new BorderLayout(12, 12)
        );

        setBorder(
                BorderFactory.createEmptyBorder(
                        12, 12, 12, 12
                )
        );

        add(
                buildForm(),
                BorderLayout.NORTH
        );

        add(
                buildTable(),
                BorderLayout.CENTER
        );

        refreshData();
    }

    private JPanel buildForm() {

        JButton criarButton =
                new JButton("Criar categoria");

        JButton atualizarButton =
                new JButton("Atualizar categoria");

        JButton removerButton =
                new JButton("Remover categoria");

        JButton limparButton =
                new JButton("Limpar seleção");

        criarButton.addActionListener(
                e -> criarCategoria()
        );

        atualizarButton.addActionListener(
                e -> atualizarCategoria()
        );

        removerButton.addActionListener(
                e -> removerCategoria()
        );

        limparButton.addActionListener(
                e -> limparFormulario()
        );

        JPanel form =
                new JPanel(
                        new GridLayout(
                                7,
                                2,
                                8,
                                8
                        )
                );

        form.setBorder(
                BorderFactory.createTitledBorder(
                        "Gestão de categoria"
                )
        );

        form.add(new JLabel("Cilindrada"));
        form.add(cilindradaField);

        form.add(new JLabel("Descrição"));
        form.add(descricaoField);

        form.add(new JLabel("Idade mínima"));
        form.add(idadeMinimaField);

        form.add(new JLabel("Experiência mínima (0-5)"));
        form.add(experienciaMinimaField);

        form.add(new JLabel("Preço base"));
        form.add(precoBaseField);

        form.add(criarButton);
        form.add(atualizarButton);

        form.add(removerButton);
        form.add(limparButton);

        return form;
    }

    private JScrollPane buildTable() {

        categoriasTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        categoriasTable.setAutoCreateRowSorter(
                true
        );

        categoriasTable
                .getSelectionModel()
                .addListSelectionListener(e -> {

                    if (!e.getValueIsAdjusting()) {
                        carregarCategoriaSelecionada();
                    }

                });

        JScrollPane scroll =
                new JScrollPane(
                        categoriasTable
                );

        scroll.setBorder(
                BorderFactory.createTitledBorder(
                        "Categorias"
                )
        );

        return scroll;
    }

    private void criarCategoria() {

        try {

            CategoriaKart categoria =
                    categoriaService.criarCategoria(
                            Integer.parseInt(
                                    cilindradaField
                                            .getText()
                                            .trim()
                            ),
                            descricaoField
                                    .getText()
                                    .trim(),
                            Integer.parseInt(
                                    idadeMinimaField
                                            .getText()
                                            .trim()
                            ),
                            Integer.parseInt(
                                    experienciaMinimaField
                                            .getText()
                                            .trim()
                            ),
                            new BigDecimal(
                                    precoBaseField
                                            .getText()
                                            .trim()
                            )
                    );

            showInfo(
                    "Categoria criada com sucesso: "
                            + categoria.getId()
            );

            limparFormulario();
            refreshData();

        } catch (NumberFormatException ex) {

            showError(
                    "Verifique os campos numéricos da categoria."
            );

        } catch (RuntimeException ex) {

            showError(
                    ex.getMessage()
            );
        }
    }

    private void atualizarCategoria() {

        if (categoriaSelecionadaId == null) {

            showError(
                    "Selecione uma categoria na tabela para atualizar."
            );

            return;
        }

        try {

            categoriaService.atualizarCategoria(
                    categoriaSelecionadaId,
                    Integer.parseInt(
                            cilindradaField
                                    .getText()
                                    .trim()
                    ),
                    descricaoField
                            .getText()
                            .trim(),
                    Integer.parseInt(
                            idadeMinimaField
                                    .getText()
                                    .trim()
                    ),
                    Integer.parseInt(
                            experienciaMinimaField
                                    .getText()
                                    .trim()
                    ),
                    new BigDecimal(
                            precoBaseField
                                    .getText()
                                    .trim()
                    )
            );

            showInfo(
                    "Categoria atualizada com sucesso."
            );

            limparFormulario();
            refreshData();

        } catch (NumberFormatException ex) {

            showError(
                    "Verifique os campos numéricos da categoria."
            );

        } catch (RuntimeException ex) {

            showError(
                    ex.getMessage()
            );
        }
    }

    private void removerCategoria() {

        if (categoriaSelecionadaId == null) {

            showError(
                    "Selecione uma categoria na tabela para remover."
            );

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

            categoriaService.removerCategoria(
                    categoriaSelecionadaId
            );

            showInfo(
                    "Categoria removida com sucesso."
            );

            limparFormulario();
            refreshData();

        } catch (RuntimeException ex) {

            showError(
                    ex.getMessage()
            );
        }
    }

    private void carregarCategoriaSelecionada() {

        int selectedRow =
                categoriasTable.getSelectedRow();

        if (selectedRow == -1) {
            return;
        }

        int modelRow =
                categoriasTable
                        .convertRowIndexToModel(
                                selectedRow
                        );

        categoriaSelecionadaId =
                (Long) categoriasTableModel
                        .getValueAt(
                                modelRow,
                                0
                        );

        cilindradaField.setText(
                categoriasTableModel
                        .getValueAt(
                                modelRow,
                                1
                        )
                        .toString()
        );

        descricaoField.setText(
                categoriasTableModel
                        .getValueAt(
                                modelRow,
                                2
                        )
                        .toString()
        );

        idadeMinimaField.setText(
                categoriasTableModel
                        .getValueAt(
                                modelRow,
                                3
                        )
                        .toString()
        );

        experienciaMinimaField.setText(
                categoriasTableModel
                        .getValueAt(
                                modelRow,
                                4
                        )
                        .toString()
        );

        precoBaseField.setText(
                categoriasTableModel
                        .getValueAt(
                                modelRow,
                                5
                        )
                        .toString()
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

        categoriasTableModel.setRowCount(
                0
        );

        for (
                CategoriaKart categoria :
                categoriaService.listarCategorias()
        ) {

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