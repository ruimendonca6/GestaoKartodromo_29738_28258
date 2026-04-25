package pt.kartodromo.desktop.ui.corridas;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import pt.kartodromo.core.bll.CategoriaKartService;
import pt.kartodromo.core.bll.CorridaService;
import pt.kartodromo.core.model.CategoriaKart;
import pt.kartodromo.core.model.Corrida;

public class CorridaPanel extends JPanel {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final CorridaService corridaService = new CorridaService();
    private final CategoriaKartService categoriaService = new CategoriaKartService();

    private final DefaultListModel<Corrida> corridasModel = new DefaultListModel<>();
    private final DefaultComboBoxModel<CategoriaKart> categoriaComboModel = new DefaultComboBoxModel<>();

    public CorridaPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(buildForm(), BorderLayout.NORTH);
        add(buildList(), BorderLayout.CENTER);

        refreshData();
    }

    private JPanel buildForm() {
        JTextField dataHoraField = new JTextField("2030-01-01 10:00");
        JTextField duracaoField = new JTextField("15");
        JTextField vagasField = new JTextField("10");
        JTextField layoutField = new JTextField("Pista Completa");
        JComboBox<CategoriaKart> categoriaCombo = new JComboBox<>(categoriaComboModel);

        JButton criarButton = new JButton("Criar corrida");

        criarButton.addActionListener(e -> {
            try {
                CategoriaKart categoria = (CategoriaKart) categoriaCombo.getSelectedItem();

                if (categoria == null) {
                    showError("Crie uma categoria antes de criar corridas.");
                    return;
                }

                Corrida corrida = corridaService.criarCorrida(
                        LocalDateTime.parse(dataHoraField.getText().trim(), DATE_TIME_FORMAT),
                        Integer.parseInt(duracaoField.getText().trim()),
                        Integer.parseInt(vagasField.getText().trim()),
                        categoria.getId(),
                        layoutField.getText().trim()
                );

                showInfo("Corrida criada com sucesso: " + corrida.getId());
                refreshData();

            } catch (DateTimeParseException ex) {
                showError("Data/hora inválida. Formato: yyyy-MM-dd HH:mm.");
            } catch (NumberFormatException ex) {
                showError("Duração e vagas devem ser valores numéricos.");
            } catch (RuntimeException ex) {
                showError(ex.getMessage());
            }
        });

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Nova corrida"));

        form.add(new JLabel("Data/hora (yyyy-MM-dd HH:mm)"));
        form.add(dataHoraField);

        form.add(new JLabel("Duração (minutos)"));
        form.add(duracaoField);

        form.add(new JLabel("Vagas máximas"));
        form.add(vagasField);

        form.add(new JLabel("Layout"));
        form.add(layoutField);

        form.add(new JLabel("Categoria"));
        form.add(categoriaCombo);

        form.add(new JLabel());
        form.add(criarButton);

        return form;
    }

    private JScrollPane buildList() {
        JList<Corrida> corridasList = new JList<>(corridasModel);
        JScrollPane listPane = new JScrollPane(corridasList);
        listPane.setBorder(BorderFactory.createTitledBorder("Corridas"));
        return listPane;
    }

    public void refreshData() {
        refreshCategorias();
        refreshCorridas();
    }

    private void refreshCategorias() {
        categoriaComboModel.removeAllElements();

        for (CategoriaKart categoria : categoriaService.listarCategorias()) {
            categoriaComboModel.addElement(categoria);
        }
    }

    private void refreshCorridas() {
        corridasModel.clear();

        for (Corrida corrida : corridaService.listarCorridas()) {
            corridasModel.addElement(corrida);
        }
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erro", JOptionPane.ERROR_MESSAGE);
    }
}