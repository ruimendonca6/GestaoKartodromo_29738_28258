package pt.kartodromo.desktop.ui.categorias;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import pt.kartodromo.core.bll.CategoriaKartService;
import pt.kartodromo.core.model.CategoriaKart;

public class CategoriaPanel extends JPanel {

    private final CategoriaKartService categoriaService = new CategoriaKartService();
    private final DefaultListModel<CategoriaKart> categoriasModel = new DefaultListModel<>();

    public CategoriaPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(buildForm(), BorderLayout.NORTH);
        add(buildList(), BorderLayout.CENTER);

        refreshData();
    }

    private JPanel buildForm() {
        JTextField cilindradaField = new JTextField("270");
        JTextField descricaoField = new JTextField();
        JTextField idadeMinimaField = new JTextField("12");
        JTextField experienciaMinimaField = new JTextField("0");
        JTextField precoBaseField = new JTextField("25.00");

        JButton criarButton = new JButton("Criar categoria");

        criarButton.addActionListener(e -> {
            try {
                CategoriaKart categoria = categoriaService.criarCategoria(
                        Integer.parseInt(cilindradaField.getText().trim()),
                        descricaoField.getText().trim(),
                        Integer.parseInt(idadeMinimaField.getText().trim()),
                        Integer.parseInt(experienciaMinimaField.getText().trim()),
                        new BigDecimal(precoBaseField.getText().trim())
                );

                showInfo("Categoria criada com sucesso: " + categoria.getId());

                descricaoField.setText("");
                refreshData();

            } catch (NumberFormatException ex) {
                showError("Verifique os campos numéricos da categoria.");
            } catch (RuntimeException ex) {
                showError(ex.getMessage());
            }
        });

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Nova categoria"));

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

        form.add(new JLabel());
        form.add(criarButton);

        return form;
    }

    private JScrollPane buildList() {
        JList<CategoriaKart> categoriasList = new JList<>(categoriasModel);
        JScrollPane listPane = new JScrollPane(categoriasList);
        listPane.setBorder(BorderFactory.createTitledBorder("Categorias"));
        return listPane;
    }

    public void refreshData() {
        categoriasModel.clear();

        for (CategoriaKart categoria : categoriaService.listarCategorias()) {
            categoriasModel.addElement(categoria);
        }
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erro", JOptionPane.ERROR_MESSAGE);
    }
}