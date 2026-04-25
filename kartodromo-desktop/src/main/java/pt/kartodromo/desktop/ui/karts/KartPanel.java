package pt.kartodromo.desktop.ui.karts;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import pt.kartodromo.core.bll.CategoriaKartService;
import pt.kartodromo.core.bll.KartService;
import pt.kartodromo.core.model.CategoriaKart;
import pt.kartodromo.core.model.Kart;
import pt.kartodromo.core.model.enums.KartEstado;

public class KartPanel extends JPanel {

    private final KartService kartService = new KartService();
    private final CategoriaKartService categoriaService = new CategoriaKartService();

    private final DefaultListModel<Kart> kartsModel = new DefaultListModel<>();
    private final DefaultComboBoxModel<CategoriaKart> categoriaComboModel = new DefaultComboBoxModel<>();

    private final JList<Kart> kartsList = new JList<>(kartsModel);

    public KartPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(buildForm(), BorderLayout.NORTH);
        add(buildList(), BorderLayout.CENTER);
        add(buildUpdatePanel(), BorderLayout.SOUTH);

        refreshData();
    }

    private JPanel buildForm() {
        javax.swing.JTextField numeroField = new javax.swing.JTextField("1");
        JComboBox<CategoriaKart> categoriaCombo = new JComboBox<>(categoriaComboModel);
        JComboBox<KartEstado> estadoCombo = new JComboBox<>(KartEstado.values());
        JCheckBox disponivelCheck = new JCheckBox("Disponível", true);

        JButton criarButton = new JButton("Criar kart");

        criarButton.addActionListener(e -> {
            try {
                CategoriaKart categoria = (CategoriaKart) categoriaCombo.getSelectedItem();

                if (categoria == null) {
                    showError("Crie uma categoria antes de criar karts.");
                    return;
                }

                KartEstado estado = (KartEstado) estadoCombo.getSelectedItem();

                Kart kart = kartService.criarKart(
                        Integer.parseInt(numeroField.getText().trim()),
                        estado,
                        disponivelCheck.isSelected(),
                        categoria.getId()
                );

                showInfo("Kart criado com sucesso: " + kart.getId());

                numeroField.setText("");
                disponivelCheck.setSelected(true);
                estadoCombo.setSelectedItem(KartEstado.OPERACIONAL);

                refreshData();

            } catch (NumberFormatException ex) {
                showError("O número do kart deve ser numérico.");
            } catch (RuntimeException ex) {
                showError(ex.getMessage());
            }
        });

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Novo kart"));

        form.add(new JLabel("Número"));
        form.add(numeroField);

        form.add(new JLabel("Categoria"));
        form.add(categoriaCombo);

        form.add(new JLabel("Estado"));
        form.add(estadoCombo);

        form.add(new JLabel("Disponibilidade"));
        form.add(disponivelCheck);

        form.add(new JLabel());
        form.add(criarButton);

        return form;
    }

    private JScrollPane buildList() {
        JScrollPane listPane = new JScrollPane(kartsList);
        listPane.setBorder(BorderFactory.createTitledBorder("Karts"));
        return listPane;
    }

    private JPanel buildUpdatePanel() {
        JComboBox<KartEstado> novoEstadoCombo = new JComboBox<>(KartEstado.values());
        JCheckBox novaDisponibilidadeCheck = new JCheckBox("Disponível");

        JButton atualizarEstadoButton = new JButton("Atualizar estado");
        JButton atualizarDisponibilidadeButton = new JButton("Atualizar disponibilidade");

        atualizarEstadoButton.addActionListener(e -> {
            Kart kartSelecionado = kartsList.getSelectedValue();

            if (kartSelecionado == null) {
                showError("Selecione um kart para atualizar o estado.");
                return;
            }

            try {
                KartEstado novoEstado = (KartEstado) novoEstadoCombo.getSelectedItem();
                kartService.atualizarEstado(kartSelecionado.getId(), novoEstado);

                showInfo("Estado atualizado com sucesso.");
                refreshData();

            } catch (RuntimeException ex) {
                showError(ex.getMessage());
            }
        });

        atualizarDisponibilidadeButton.addActionListener(e -> {
            Kart kartSelecionado = kartsList.getSelectedValue();

            if (kartSelecionado == null) {
                showError("Selecione um kart para atualizar disponibilidade.");
                return;
            }

            try {
                kartService.definirDisponibilidade(
                        kartSelecionado.getId(),
                        novaDisponibilidadeCheck.isSelected()
                );

                showInfo("Disponibilidade atualizada com sucesso.");
                refreshData();

            } catch (RuntimeException ex) {
                showError(ex.getMessage());
            }
        });

        kartsList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }

            Kart kartSelecionado = kartsList.getSelectedValue();

            if (kartSelecionado != null) {
                novoEstadoCombo.setSelectedItem(kartSelecionado.getEstado());
                novaDisponibilidadeCheck.setSelected(kartSelecionado.isDisponivel());
            }
        });

        JPanel updatePanel = new JPanel(new GridLayout(2, 3, 8, 8));
        updatePanel.setBorder(BorderFactory.createTitledBorder("Gestão do kart selecionado"));

        updatePanel.add(new JLabel("Novo estado"));
        updatePanel.add(novoEstadoCombo);
        updatePanel.add(atualizarEstadoButton);

        updatePanel.add(new JLabel("Disponibilidade"));
        updatePanel.add(novaDisponibilidadeCheck);
        updatePanel.add(atualizarDisponibilidadeButton);

        return updatePanel;
    }

    public void refreshData() {
        refreshCategorias();
        refreshKarts();
    }

    private void refreshCategorias() {
        categoriaComboModel.removeAllElements();

        for (CategoriaKart categoria : categoriaService.listarCategorias()) {
            categoriaComboModel.addElement(categoria);
        }
    }

    private void refreshKarts() {
        kartsModel.clear();

        for (Kart kart : kartService.listarKarts()) {
            kartsModel.addElement(kart);
        }
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erro", JOptionPane.ERROR_MESSAGE);
    }
}