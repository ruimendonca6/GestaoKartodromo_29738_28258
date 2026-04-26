package pt.kartodromo.desktop.ui.corridas;

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

import pt.kartodromo.core.bll.CategoriaKartService;
import pt.kartodromo.core.bll.ClienteService;
import pt.kartodromo.core.bll.CorridaService;
import pt.kartodromo.core.model.CategoriaKart;
import pt.kartodromo.core.model.Cliente;
import pt.kartodromo.core.model.Corrida;

public class CorridaPanel extends JPanel {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final CorridaService corridaService = new CorridaService();
    private final CategoriaKartService categoriaService = new CategoriaKartService();
    private final ClienteService clienteService = new ClienteService();

    private final DefaultListModel<Corrida> corridasModel = new DefaultListModel<>();
    private final DefaultComboBoxModel<CategoriaKart> categoriaComboModel = new DefaultComboBoxModel<>();
    private final DefaultComboBoxModel<Cliente> clienteComboModel = new DefaultComboBoxModel<>();
    private final JList<Corrida> corridasList = new JList<>(corridasModel);

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
        JComboBox<Cliente> clienteCombo = new JComboBox<>(clienteComboModel);

        JButton criarButton = new JButton("Criar corrida");
        JButton atualizarButton = new JButton("Atualizar corrida selecionada");
        JButton eliminarButton = new JButton("Eliminar corrida selecionada");

        clienteCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus
            ) {
                String texto = "";
                if (value instanceof Cliente) {
                    texto = ((Cliente) value).getNome();
                }
                return super.getListCellRendererComponent(list, texto, index, isSelected, cellHasFocus);
            }
        });

        criarButton.addActionListener(e -> {
            try {
                CategoriaKart categoria = (CategoriaKart) categoriaCombo.getSelectedItem();
                Cliente cliente = (Cliente) clienteCombo.getSelectedItem();

                if (categoria == null || cliente == null) {
                    showError("Selecione cliente e categoria antes de criar corridas.");
                    return;
                }

                Corrida corrida = corridaService.criarCorrida(
                        LocalDateTime.parse(dataHoraField.getText().trim(), DATE_TIME_FORMAT),
                        Integer.parseInt(duracaoField.getText().trim()),
                        Integer.parseInt(vagasField.getText().trim()),
                        categoria.getId(),
                        cliente.getId(),
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

        atualizarButton.addActionListener(e -> {
            Corrida corridaSelecionada = corridasList.getSelectedValue();
            if (corridaSelecionada == null) {
                showError("Selecione uma corrida para atualizar.");
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
                        corridaSelecionada.getId(),
                        LocalDateTime.parse(dataHoraField.getText().trim(), DATE_TIME_FORMAT),
                        Integer.parseInt(duracaoField.getText().trim()),
                        Integer.parseInt(vagasField.getText().trim()),
                        categoria.getId(),
                        cliente.getId(),
                        layoutField.getText().trim()
                );

                showInfo("Corrida atualizada com sucesso.");
                refreshData();
            } catch (DateTimeParseException ex) {
                showError("Data/hora inválida. Formato: yyyy-MM-dd HH:mm.");
            } catch (NumberFormatException ex) {
                showError("Duração e vagas devem ser valores numéricos.");
            } catch (RuntimeException ex) {
                showError(ex.getMessage());
            }
        });

        eliminarButton.addActionListener(e -> {
            Corrida corridaSelecionada = corridasList.getSelectedValue();
            if (corridaSelecionada == null) {
                showError("Selecione uma corrida para eliminar.");
                return;
            }

            try {
                corridaService.eliminarCorrida(corridaSelecionada.getId());
                showInfo("Corrida eliminada com sucesso.");
                refreshData();
            } catch (RuntimeException ex) {
                showError(ex.getMessage());
            }
        });

        corridasList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }

            Corrida corridaSelecionada = corridasList.getSelectedValue();
            if (corridaSelecionada == null) {
                return;
            }

            dataHoraField.setText(corridaSelecionada.getDataHoraInicio().format(DATE_TIME_FORMAT));
            duracaoField.setText(String.valueOf(corridaSelecionada.getDuracaoMinutos()));
            vagasField.setText(String.valueOf(corridaSelecionada.getVagasMaximas()));
            layoutField.setText(corridaSelecionada.getLayoutNome());
            categoriaCombo.setSelectedItem(corridaSelecionada.getCategoria());
            clienteCombo.setSelectedItem(corridaSelecionada.getCliente());
        });

        JPanel form = new JPanel(new GridLayout(8, 2, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Nova corrida"));

        form.add(new JLabel("Data/hora (yyyy-MM-dd HH:mm)"));
        form.add(dataHoraField);

        form.add(new JLabel("Duração (minutos)"));
        form.add(duracaoField);

        form.add(new JLabel("Vagas máximas"));
        form.add(vagasField);

        form.add(new JLabel("Layout"));
        form.add(layoutField);

        form.add(new JLabel("Cliente"));
        form.add(clienteCombo);

        form.add(new JLabel("Categoria"));
        form.add(categoriaCombo);

        form.add(criarButton);
        form.add(atualizarButton);
        form.add(new JLabel());
        form.add(eliminarButton);

        return form;
    }

    private JScrollPane buildList() {
        JScrollPane listPane = new JScrollPane(corridasList);
        listPane.setBorder(BorderFactory.createTitledBorder("Corridas"));
        return listPane;
    }

    public void refreshData() {
        refreshClientes();
        refreshCategorias();
        refreshCorridas();
    }

    private void refreshClientes() {
        clienteComboModel.removeAllElements();

        for (Cliente cliente : clienteService.listarClientes()) {
            clienteComboModel.addElement(cliente);
        }
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
