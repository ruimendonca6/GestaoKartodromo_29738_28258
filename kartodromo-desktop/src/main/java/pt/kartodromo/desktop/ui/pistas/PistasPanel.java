package pt.kartodromo.desktop.ui.pistas;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import pt.kartodromo.core.bll.PistaService;
import pt.kartodromo.core.model.Pista;
import pt.kartodromo.desktop.ui.UiStyle;

public class PistasPanel extends JPanel {

    private final boolean mostrarGestao;

    private static final Color ATIVA_COLOR =
            new Color(46, 125, 50);

    private static final Color INATIVA_COLOR =
            new Color(158, 158, 158);

    private static final String[] IMAGENS_DISPONIVEIS = {
            "/images/pista1.png",
            "/images/pista2.png",
            "/images/pista3.png"
    };

    private final Consumer<String> onPistaSelecionada;

    private final PistaService pistaService =
            new PistaService();

    private Long pistaSelecionadaId = null;

    private final JTextField nomeField =
            new JTextField();

    private final JSpinner comprimentoSpinner =
            new JSpinner(
                    new SpinnerNumberModel(
                            500,
                            1,
                            9999,
                            10
                    )
            );

    private final JSpinner capacidadeSpinner =
            new JSpinner(
                    new SpinnerNumberModel(
                            10,
                            1,
                            999,
                            1
                    )
            );

    private final JComboBox<String> imagemCombo =
            new JComboBox<>(IMAGENS_DISPONIVEIS);

    private final DefaultTableModel tableModel =
            new DefaultTableModel(
                    new Object[]{
                            "Nome",
                            "Comprimento (m)",
                            "Capacidade",
                            "Estado"
                    },
                    0
            ) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

    private final JTable table =
            new JTable(tableModel);

    private final JButton toggleBtn =
            UiStyle.createActionButton(
                    "Ativar / Inativar",
                    new Color(245, 124, 0)
            );

            

        public PistasPanel() {
                this(null, true);
        }

        public PistasPanel(Consumer<String> onPistaSelecionada) {
                this(onPistaSelecionada, true);
        }

        public PistasPanel(
                Consumer<String> onPistaSelecionada,
                boolean mostrarGestao) {

                this.onPistaSelecionada = onPistaSelecionada;
                this.mostrarGestao = mostrarGestao;

                setLayout(new BorderLayout(0, 10));
                setBackground(UiStyle.BACKGROUND_COLOR);
                setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));

                JTabbedPane tabs = new JTabbedPane();
                tabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));

                tabs.addTab("Pistas", buildCardsTab());

                if (mostrarGestao) {
                        tabs.addTab("Gestão", buildGestaoTab());
                }

                add(UiStyle.createPageTitle("Pistas"), BorderLayout.NORTH);
                add(tabs, BorderLayout.CENTER);

                loadData();
        }

    private JScrollPane buildCardsTab() {
        JPanel grid =
                new JPanel(new GridLayout(1, 3, 25, 25));

        grid.setBackground(UiStyle.BACKGROUND_COLOR);

        grid.setBorder(
                BorderFactory.createEmptyBorder(
                        20,
                        20,
                        20,
                        20
                )
        );

        grid.add(
                createTrackCard(
                        "Pista Técnica",
                        "/images/pista1.png",
                        "5/5",
                        "650 m",
                        "18",
                        "Treino / Técnica"
                )
        );

        grid.add(
                createTrackCard(
                        "Pista Completa",
                        "/images/pista2.png",
                        "4/5",
                        "1200 m",
                        "24",
                        "Corrida Oficial"
                )
        );

        grid.add(
                createTrackCard(
                        "Pista Sprint",
                        "/images/pista3.png",
                        "3/5",
                        "450 m",
                        "10",
                        "Sessões rápidas"
                )
        );

        JScrollPane scroll =
                new JScrollPane(grid);

        scroll.setBorder(null);
        scroll.getViewport().setBackground(UiStyle.BACKGROUND_COLOR);

        return scroll;
    }

    private JPanel createTrackCard(
            String nome,
            String imagem,
            String dificuldade,
            String comprimento,
            String curvas,
            String tipo) {

        JPanel content =
                new JPanel(new BorderLayout(15, 15));

        content.setOpaque(false);

        JLabel tituloLabel =
                new JLabel(nome, SwingConstants.CENTER);

        tituloLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        24
                )
        );

        tituloLabel.setForeground(UiStyle.PRIMARY_RED);

        JLabel imagemLabel =
                new JLabel("", SwingConstants.CENTER);

        ImageIcon icon =
                loadImage(imagem, 350, 220);

        if (icon != null) {
            imagemLabel.setIcon(icon);
        } else {
            imagemLabel.setText("Imagem não encontrada");
            imagemLabel.setFont(
                    new Font(
                            "Segoe UI",
                            Font.PLAIN,
                            14
                    )
            );
        }

        JPanel infoPanel =
                new JPanel(new GridLayout(4, 1, 0, 6));

        infoPanel.setOpaque(false);

        infoPanel.add(createInfoLabel("Dificuldade: " + dificuldade));
        infoPanel.add(createInfoLabel("Comprimento: " + comprimento));
        infoPanel.add(createInfoLabel("Curvas: " + curvas));
        infoPanel.add(createInfoLabel("Tipo: " + tipo));

        JButton selecionarBtn =
                UiStyle.createActionButton(
                        "Selecionar Pista",
                        UiStyle.PRIMARY_RED
                );

        selecionarBtn.addActionListener(
                e -> selecionarPista(nome)
        );

        JPanel bottomPanel =
                new JPanel(new BorderLayout(12, 12));

        bottomPanel.setOpaque(false);
        bottomPanel.add(infoPanel, BorderLayout.CENTER);
        bottomPanel.add(selecionarBtn, BorderLayout.SOUTH);

        content.add(tituloLabel, BorderLayout.NORTH);
        content.add(imagemLabel, BorderLayout.CENTER);
        content.add(bottomPanel, BorderLayout.SOUTH);

        return UiStyle.createCard(content);
    }

    private void selecionarPista(String nome) {
        if (onPistaSelecionada != null) {
            onPistaSelecionada.accept(nome);
        }

        JOptionPane.showMessageDialog(
                this,
                nome + " selecionada com sucesso.",
                "Pista selecionada",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private JLabel createInfoLabel(String text) {
        JLabel label =
                new JLabel(text);

        label.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        15
                )
        );

        label.setForeground(UiStyle.BLACK);

        return label;
    }

    private ImageIcon loadImage(
            String path,
            int width,
            int height) {

        if (path == null || path.isBlank()) {
            return null;
        }

        try {
            java.net.URL url =
                    getClass().getResource(path);

            if (url == null) {
                return null;
            }

            Image scaled =
                    new ImageIcon(url)
                            .getImage()
                            .getScaledInstance(
                                    width,
                                    height,
                                    Image.SCALE_SMOOTH
                            );

            return new ImageIcon(scaled);

        } catch (Exception ex) {
            return null;
        }
    }

    private JPanel buildGestaoTab() {
        JPanel tab =
                new JPanel(new BorderLayout(18, 18));

        tab.setBackground(UiStyle.BACKGROUND_COLOR);

        tab.setBorder(
                BorderFactory.createEmptyBorder(
                        20,
                        20,
                        20,
                        20
                )
        );

        tab.add(buildFormCard(), BorderLayout.WEST);
        tab.add(buildTableCard(), BorderLayout.CENTER);

        return tab;
    }

    private JPanel buildFormCard() {
        JPanel form =
                new JPanel(new BorderLayout(12, 12));

        form.setOpaque(false);

        JLabel title =
                new JLabel("PISTA");

        title.setFont(UiStyle.sectionFont());
        title.setForeground(UiStyle.PRIMARY_RED);

        JPanel fields =
                new JPanel(new GridBagLayout());

        fields.setOpaque(false);

        GridBagConstraints gbc =
                createGbc();

        nomeField.setPreferredSize(
                new Dimension(
                        200,
                        28
                )
        );

        imagemCombo.setRenderer(
                new DefaultListCellRenderer() {

                    @Override
                    public Component getListCellRendererComponent(
                            JList<?> list,
                            Object value,
                            int index,
                            boolean isSelected,
                            boolean cellHasFocus) {

                        super.getListCellRendererComponent(
                                list,
                                value,
                                index,
                                isSelected,
                                cellHasFocus
                        );

                        if (value instanceof String path) {
                            setText(
                                    path.substring(
                                            path.lastIndexOf('/') + 1
                                    )
                            );
                        }

                        return this;
                    }
                }
        );

        addFormRow(fields, gbc, 0, "Nome", nomeField);
        addFormRow(fields, gbc, 1, "Comprimento (m)", comprimentoSpinner);
        addFormRow(fields, gbc, 2, "Capacidade", capacidadeSpinner);
        addFormRow(fields, gbc, 3, "Imagem", imagemCombo);

        JButton criarBtn =
                UiStyle.createActionButton(
                        "Criar",
                        UiStyle.CREATE_GREEN
                );

        JButton editarBtn =
                UiStyle.createActionButton(
                        "Editar",
                        UiStyle.UPDATE_BLUE
                );

        JButton eliminarBtn =
                UiStyle.createActionButton(
                        "Eliminar",
                        UiStyle.DELETE_RED
                );

        JButton limparBtn =
                UiStyle.createActionButton(
                        "Limpar",
                        UiStyle.CLEAR_GRAY
                );

        criarBtn.addActionListener(e -> criarPista());
        editarBtn.addActionListener(e -> editarPista());
        eliminarBtn.addActionListener(e -> eliminarPista());
        limparBtn.addActionListener(e -> limparFormulario());
        toggleBtn.addActionListener(e -> toggleAtiva());

        JPanel topBtns =
                new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));

        topBtns.setOpaque(false);
        topBtns.add(criarBtn);
        topBtns.add(editarBtn);
        topBtns.add(eliminarBtn);
        topBtns.add(limparBtn);

        JPanel bottomBtns =
                new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

        bottomBtns.setOpaque(false);
        bottomBtns.add(toggleBtn);

        JPanel actions =
                new JPanel(new BorderLayout(0, 8));

        actions.setOpaque(false);
        actions.add(topBtns, BorderLayout.NORTH);
        actions.add(bottomBtns, BorderLayout.SOUTH);

        form.add(title, BorderLayout.NORTH);
        form.add(fields, BorderLayout.CENTER);
        form.add(actions, BorderLayout.SOUTH);

        JPanel card =
                UiStyle.createCard(form);

        card.setPreferredSize(
                new Dimension(
                        340,
                        0
                )
        );

        return card;
    }

    private JPanel buildTableCard() {
        JPanel panel =
                new JPanel(new BorderLayout(12, 12));

        panel.setOpaque(false);

        JLabel title =
                new JLabel("LISTA DE PISTAS");

        title.setFont(UiStyle.sectionFont());
        title.setForeground(UiStyle.PRIMARY_RED);

        UiStyle.styleTable(table);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.getSelectionModel()
                .addListSelectionListener(e -> {
                    if (!e.getValueIsAdjusting()) {
                        preencherFormulario();
                    }
                });

        table.getColumnModel()
                .getColumn(3)
                .setCellRenderer(
                        new DefaultTableCellRenderer() {

                            @Override
                            public Component getTableCellRendererComponent(
                                    JTable table,
                                    Object value,
                                    boolean isSelected,
                                    boolean hasFocus,
                                    int row,
                                    int column) {

                                super.getTableCellRendererComponent(
                                        table,
                                        value,
                                        isSelected,
                                        hasFocus,
                                        row,
                                        column
                                );

                                boolean ativa =
                                        "Ativa".equals(value);

                                setForeground(
                                        isSelected
                                                ? Color.WHITE
                                                : ativa
                                                ? ATIVA_COLOR
                                                : INATIVA_COLOR
                                );

                                setFont(
                                        getFont().deriveFont(
                                                Font.BOLD
                                        )
                                );

                                setHorizontalAlignment(
                                        SwingConstants.CENTER
                                );

                                return this;
                            }
                        }
                );

        DefaultTableCellRenderer center =
                new DefaultTableCellRenderer();

        center.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        table.getColumnModel().getColumn(1).setCellRenderer(center);
        table.getColumnModel().getColumn(2).setCellRenderer(center);

        JScrollPane scroll =
                new JScrollPane(table);

        scroll.setBorder(
                BorderFactory.createLineBorder(
                        UiStyle.BORDER_COLOR
                )
        );

        panel.add(title, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        return UiStyle.createCard(panel);
    }

    private void criarPista() {
        try {
            pistaService.criarPista(
                    nomeField.getText().trim(),
                    (int) comprimentoSpinner.getValue(),
                    (int) capacidadeSpinner.getValue(),
                    (String) imagemCombo.getSelectedItem()
            );

            showInfo("Pista criada com sucesso.");
            limparFormulario();
            loadData();

        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void editarPista() {
        if (pistaSelecionadaId == null) {
            showError("Selecione uma pista para editar.");
            return;
        }

        try {
            pistaService.atualizarPista(
                    pistaSelecionadaId,
                    nomeField.getText().trim(),
                    (int) comprimentoSpinner.getValue(),
                    (int) capacidadeSpinner.getValue(),
                    (String) imagemCombo.getSelectedItem()
            );

            showInfo("Pista atualizada com sucesso.");
            limparFormulario();
            loadData();

        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void eliminarPista() {
        if (pistaSelecionadaId == null) {
            showError("Selecione uma pista para eliminar.");
            return;
        }

        int confirm =
                JOptionPane.showConfirmDialog(
                        this,
                        "Eliminar a pista \""
                                + nomeField.getText()
                                + "\"?",
                        "Confirmar eliminação",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            pistaService.eliminarPista(pistaSelecionadaId);

            showInfo("Pista eliminada.");
            limparFormulario();
            loadData();

        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void toggleAtiva() {
        if (pistaSelecionadaId == null) {
            showError("Selecione uma pista para ativar/inativar.");
            return;
        }

        try {
            Pista updated =
                    pistaService.toggleAtiva(pistaSelecionadaId);

            String estado =
                    updated.isAtiva()
                            ? "ativada"
                            : "inativada";

            showInfo("Pista " + estado + " com sucesso.");

            loadData();
            selectById(pistaSelecionadaId);

        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void preencherFormulario() {
        int row =
                table.getSelectedRow();

        if (row < 0) {
            return;
        }

        int modelRow =
                table.convertRowIndexToModel(row);

        String nome =
                (String) tableModel.getValueAt(modelRow, 0);

        nomeField.setText(nome);
        comprimentoSpinner.setValue(tableModel.getValueAt(modelRow, 1));
        capacidadeSpinner.setValue(tableModel.getValueAt(modelRow, 2));

        pistaService.listarPistas()
                .stream()
                .filter(p -> p.getNome().equals(nome))
                .findFirst()
                .ifPresent(p -> {
                    pistaSelecionadaId = p.getId();

                    String img =
                            p.getImagemPath();

                    if (img != null) {
                        imagemCombo.setSelectedItem(img);
                    }
                });
    }

    private void limparFormulario() {
        pistaSelecionadaId = null;
        nomeField.setText("");
        comprimentoSpinner.setValue(500);
        capacidadeSpinner.setValue(10);
        imagemCombo.setSelectedIndex(0);
        table.clearSelection();
    }

    private void loadData() {
        tableModel.setRowCount(0);

        for (Pista pista : pistaService.listarPistas()) {
            tableModel.addRow(
                    new Object[]{
                            pista.getNome(),
                            pista.getComprimento(),
                            pista.getCapacidade(),
                            pista.isAtiva()
                                    ? "Ativa"
                                    : "Inativa"
                    }
            );
        }
    }

    private void selectById(Long id) {
        List<Pista> pistas =
                pistaService.listarPistas();

        for (int i = 0; i < pistas.size(); i++) {
            if (pistas.get(i).getId().equals(id)) {
                table.setRowSelectionInterval(i, i);
                return;
            }
        }
    }

    public void refreshData() {
        loadData();
    }

    private GridBagConstraints createGbc() {
        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.insets =
                new Insets(
                        8,
                        0,
                        8,
                        0
                );

        gbc.fill =
                GridBagConstraints.HORIZONTAL;

        gbc.weightx =
                1;

        return gbc;
    }

    private void addFormRow(
            JPanel panel,
            GridBagConstraints gbc,
            int row,
            String labelText,
            Component component) {

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(8, 0, 8, 0);

        JLabel label =
                new JLabel(labelText);

        label.setFont(UiStyle.labelFont());
        label.setForeground(UiStyle.BLACK);

        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.insets = new Insets(8, 14, 8, 0);

        component.setFont(UiStyle.labelFont());

        panel.add(component, gbc);

        gbc.insets = new Insets(8, 0, 8, 0);
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