package pt.kartodromo.desktop.ui.manutencao;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import pt.kartodromo.core.bll.ManutencaoService;
import pt.kartodromo.core.model.Kart;
import pt.kartodromo.core.model.Manutencao;
import pt.kartodromo.core.model.enums.KartEstado;
import pt.kartodromo.core.model.enums.TipoManutencao;
import pt.kartodromo.desktop.ui.UiStyle;

public class ManutencaoPanel extends JPanel {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ManutencaoService service = new ManutencaoService();

    // Registar form
    private JComboBox<KartItem> kartCombo;
    private JTextArea descricaoArea;
    private JComboBox<TipoManutencao> tipoCombo;
    private JSpinner diasRevisaoSpinner;
    private JComboBox<String> bloquearCombo;

    // Histórico table
    private DefaultTableModel historicoModel;
    private JTable historicoTable;
    private Long manutencaoSelecionadaId;

    // Próxima revisão table
    private DefaultTableModel revisaoModel;

    // Karts table
    private DefaultTableModel kartsModel;
    private JTable kartsTable;
    private Long kartSelecionadoId;

    public ManutencaoPanel() {
        setLayout(new BorderLayout());
        setBackground(UiStyle.BACKGROUND_COLOR);

        add(
                UiStyle.createPageTitle("Manutenção"),
                BorderLayout.NORTH
        );

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(UiStyle.BACKGROUND_COLOR);
        tabs.addTab("Registar", buildRegistarTab());
        tabs.addTab("Histórico", buildHistoricoTab());
        tabs.addTab("Próxima Revisão", buildRevisaoTab());
        tabs.addTab("Estado dos Karts", buildKartsTab());
        add(tabs, BorderLayout.CENTER);

        loadData();
    }

    // ── TAB REGISTAR ────────────────────────────────────────────────────────────

    private JPanel buildRegistarTab() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UiStyle.BACKGROUND_COLOR);
        wrapper.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UiStyle.BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;

        kartCombo = new JComboBox<>();
        kartCombo.setFont(UiStyle.labelFont());
        kartCombo.setPreferredSize(new Dimension(280, 32));

        tipoCombo = new JComboBox<>(TipoManutencao.values());
        tipoCombo.setFont(UiStyle.labelFont());

        descricaoArea = new JTextArea(4, 30);
        descricaoArea.setFont(UiStyle.labelFont());
        descricaoArea.setLineWrap(true);
        descricaoArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descricaoArea);
        descScroll.setPreferredSize(new Dimension(280, 90));

        diasRevisaoSpinner = new JSpinner(new SpinnerNumberModel(30, 0, 999, 1));
        diasRevisaoSpinner.setFont(UiStyle.labelFont());

        bloquearCombo = new JComboBox<>(new String[]{"Sim – bloquear kart", "Não"});
        bloquearCombo.setFont(UiStyle.labelFont());

        int row = 0;
        addFormRow(form, gbc, row++, "Kart:", kartCombo);
        addFormRow(form, gbc, row++, "Tipo:", tipoCombo);
        addFormRow(form, gbc, row++, "Descrição:", descScroll);
        addFormRow(form, gbc, row++, "Próxima revisão (dias):", diasRevisaoSpinner);
        addFormRow(form, gbc, row++, "Bloquear kart:", bloquearCombo);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setBackground(UiStyle.BACKGROUND_COLOR);

        javax.swing.JButton btnRegistar = UiStyle.createActionButton("Registar Manutenção", UiStyle.CREATE_GREEN);
        javax.swing.JButton btnLimpar = UiStyle.createActionButton("Limpar", UiStyle.CLEAR_GRAY);

        btnRegistar.addActionListener(e -> registarManutencao());
        btnLimpar.addActionListener(e -> limparFormulario());

        buttons.add(btnRegistar);
        buttons.add(btnLimpar);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.insets = new Insets(14, 8, 6, 8);
        form.add(buttons, gbc);

        wrapper.add(UiStyle.createCard(form), BorderLayout.NORTH);
        return wrapper;
    }

    // ── TAB HISTÓRICO ────────────────────────────────────────────────────────────

    private JPanel buildHistoricoTab() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 12));
        wrapper.setBackground(UiStyle.BACKGROUND_COLOR);
        wrapper.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        String[] cols = {"ID", "Kart Nº", "Tipo", "Data Entrada", "Data Saída", "Próxima Revisão", "Estado", "Descrição"};
        historicoModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        historicoTable = new JTable(historicoModel);
        UiStyle.styleTable(historicoTable);
        historicoTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historicoTable.getColumnModel().getColumn(0).setMinWidth(0);
        historicoTable.getColumnModel().getColumn(0).setMaxWidth(0);

        // color Estado column
        historicoTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, focus, r, c);
                if (!sel) {
                    String val = v != null ? v.toString() : "";
                    if ("Em Curso".equals(val)) {
                        setForeground(new Color(200, 100, 0));
                    } else {
                        setForeground(new Color(46, 125, 50));
                    }
                }
                return this;
            }
        });

        historicoTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = historicoTable.getSelectedRow();
                if (row >= 0) {
                    manutencaoSelecionadaId = (Long) historicoModel.getValueAt(
                            historicoTable.convertRowIndexToModel(row), 0);
                }
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnPanel.setBackground(UiStyle.BACKGROUND_COLOR);

        javax.swing.JButton btnConcluir = UiStyle.createActionButton("Marcar Concluída", new Color(46, 125, 50));
        javax.swing.JButton btnLibertar = UiStyle.createActionButton("Libertar Kart", new Color(21, 101, 192));
        javax.swing.JButton btnAtualizar = UiStyle.createActionButton("Atualizar", UiStyle.CLEAR_GRAY);

        btnConcluir.addActionListener(e -> concluirManutencao(false));
        btnLibertar.addActionListener(e -> concluirManutencao(true));
        btnAtualizar.addActionListener(e -> loadData());

        btnPanel.add(btnConcluir);
        btnPanel.add(btnLibertar);
        btnPanel.add(btnAtualizar);

        wrapper.add(btnPanel, BorderLayout.NORTH);
        wrapper.add(new JScrollPane(historicoTable), BorderLayout.CENTER);
        return wrapper;
    }

    // ── TAB PRÓXIMA REVISÃO ───────────────────────────────────────────────────────

    private JPanel buildRevisaoTab() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 12));
        wrapper.setBackground(UiStyle.BACKGROUND_COLOR);
        wrapper.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterPanel.setBackground(UiStyle.BACKGROUND_COLOR);

        JLabel diasLabel = new JLabel("Mostrar revisões nos próximos:");
        diasLabel.setFont(UiStyle.labelFont());
        JSpinner diasSpinner = new JSpinner(new SpinnerNumberModel(30, 1, 365, 7));
        diasSpinner.setFont(UiStyle.labelFont());
        diasSpinner.setPreferredSize(new Dimension(70, 30));

        javax.swing.JButton btnFiltrar = UiStyle.createActionButton("Filtrar", UiStyle.UPDATE_BLUE);
        btnFiltrar.addActionListener(e -> {
            int dias = (int) diasSpinner.getValue();
            loadRevisoes(dias);
        });

        filterPanel.add(diasLabel);
        filterPanel.add(diasSpinner);
        filterPanel.add(new JLabel("dias"));
        filterPanel.add(btnFiltrar);

        String[] cols = {"Kart Nº", "Tipo", "Data Entrada", "Próxima Revisão", "Dias Restantes", "Descrição"};
        revisaoModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable revisaoTable = new JTable(revisaoModel);
        UiStyle.styleTable(revisaoTable);

        // highlight urgent revisões
        revisaoTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, focus, r, c);
                if (!sel && v instanceof Integer dias) {
                    if (dias <= 7) setForeground(UiStyle.DELETE_RED);
                    else if (dias <= 14) setForeground(new Color(200, 100, 0));
                    else setForeground(new Color(46, 125, 50));
                }
                return this;
            }
        });

        wrapper.add(filterPanel, BorderLayout.NORTH);
        wrapper.add(new JScrollPane(revisaoTable), BorderLayout.CENTER);
        return wrapper;
    }

    // ── TAB ESTADO DOS KARTS ──────────────────────────────────────────────────────

    private JPanel buildKartsTab() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 12));
        wrapper.setBackground(UiStyle.BACKGROUND_COLOR);
        wrapper.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        String[] cols = {"ID", "Nº Kart", "Categoria", "Estado", "Disponível"};
        kartsModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        kartsTable = new JTable(kartsModel);
        UiStyle.styleTable(kartsTable);
        kartsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        kartsTable.getColumnModel().getColumn(0).setMinWidth(0);
        kartsTable.getColumnModel().getColumn(0).setMaxWidth(0);

        kartsTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, focus, r, c);
                if (!sel) {
                    String val = v != null ? v.toString() : "";
                    switch (val) {
                        case "OPERACIONAL" -> setForeground(new Color(46, 125, 50));
                        case "EM_MANUTENCAO" -> setForeground(new Color(200, 100, 0));
                        default -> setForeground(UiStyle.TEXT_GRAY);
                    }
                }
                return this;
            }
        });

        kartsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = kartsTable.getSelectedRow();
                if (row >= 0) {
                    kartSelecionadoId = (Long) kartsModel.getValueAt(
                            kartsTable.convertRowIndexToModel(row), 0);
                }
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnPanel.setBackground(UiStyle.BACKGROUND_COLOR);

        javax.swing.JButton btnBloquear = UiStyle.createActionButton("Bloquear para Manutenção", new Color(245, 124, 0));
        javax.swing.JButton btnLibertar = UiStyle.createActionButton("Libertar Kart", UiStyle.CREATE_GREEN);
        javax.swing.JButton btnAtualizar = UiStyle.createActionButton("Atualizar", UiStyle.CLEAR_GRAY);

        btnBloquear.addActionListener(e -> bloquearKartSelecionado());
        btnLibertar.addActionListener(e -> libertarKartSelecionado());
        btnAtualizar.addActionListener(e -> loadData());

        btnPanel.add(btnBloquear);
        btnPanel.add(btnLibertar);
        btnPanel.add(btnAtualizar);

        wrapper.add(btnPanel, BorderLayout.NORTH);
        wrapper.add(new JScrollPane(kartsTable), BorderLayout.CENTER);
        return wrapper;
    }

    // ── ACTIONS ──────────────────────────────────────────────────────────────────

    private void registarManutencao() {
        KartItem kartItem = (KartItem) kartCombo.getSelectedItem();
        if (kartItem == null) {
            JOptionPane.showMessageDialog(this, "Selecione um kart.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String descricao = descricaoArea.getText();
        TipoManutencao tipo = (TipoManutencao) tipoCombo.getSelectedItem();
        int diasRevisao = (int) diasRevisaoSpinner.getValue();
        boolean bloquear = bloquearCombo.getSelectedIndex() == 0;

        LocalDate proximaRevisao = diasRevisao > 0 ? LocalDate.now().plusDays(diasRevisao) : null;

        try {
            service.registar(kartItem.id(), descricao, tipo, proximaRevisao, bloquear);
            JOptionPane.showMessageDialog(this, "Manutenção registada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            limparFormulario();
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void concluirManutencao(boolean libertarKart) {
        if (manutencaoSelecionadaId == null) {
            JOptionPane.showMessageDialog(this, "Selecione uma manutenção.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            service.concluir(manutencaoSelecionadaId, libertarKart);
            String msg = libertarKart ? "Manutenção concluída e kart libertado!" : "Manutenção marcada como concluída.";
            JOptionPane.showMessageDialog(this, msg, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void bloquearKartSelecionado() {
        if (kartSelecionadoId == null) {
            JOptionPane.showMessageDialog(this, "Selecione um kart.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            service.bloquearKart(kartSelecionadoId);
            JOptionPane.showMessageDialog(this, "Kart bloqueado para manutenção.", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void libertarKartSelecionado() {
        if (kartSelecionadoId == null) {
            JOptionPane.showMessageDialog(this, "Selecione um kart.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            service.libertarKart(kartSelecionadoId);
            JOptionPane.showMessageDialog(this, "Kart libertado e marcado como operacional.", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limparFormulario() {
        descricaoArea.setText("");
        if (kartCombo.getItemCount() > 0) kartCombo.setSelectedIndex(0);
        tipoCombo.setSelectedIndex(0);
        diasRevisaoSpinner.setValue(30);
        bloquearCombo.setSelectedIndex(0);
    }

    // ── DATA LOADING ──────────────────────────────────────────────────────────────

    public void refreshData() {
        loadData();
    }

    private void loadData() {
        loadKartCombo();
        loadHistorico();
        loadRevisoes(30);
        loadKartsEstado();
    }

    private void loadKartCombo() {
        kartCombo.removeAllItems();
        for (Kart k : service.listarKarts()) {
            kartCombo.addItem(new KartItem(k.getId(), k.getNumero(), k.getEstado()));
        }
    }

    private void loadHistorico() {
        historicoModel.setRowCount(0);
        for (Manutencao m : service.listarTodas()) {
            historicoModel.addRow(new Object[]{
                m.getId(),
                m.getKart().getNumero(),
                m.getTipo(),
                m.getDataEntrada() != null ? m.getDataEntrada().format(FMT) : "-",
                m.getDataSaida() != null ? m.getDataSaida().format(FMT) : "-",
                m.getProximaRevisao() != null ? m.getProximaRevisao().format(FMT) : "-",
                m.isConcluida() ? "Concluída" : "Em Curso",
                m.getDescricao()
            });
        }
    }

    private void loadRevisoes(int dias) {
        revisaoModel.setRowCount(0);
        for (Manutencao m : service.listarProximasRevisoes(dias)) {
            long diasRestantes = LocalDate.now().until(m.getProximaRevisao()).getDays();
            revisaoModel.addRow(new Object[]{
                m.getKart().getNumero(),
                m.getTipo(),
                m.getDataEntrada() != null ? m.getDataEntrada().format(FMT) : "-",
                m.getProximaRevisao().format(FMT),
                (int) diasRestantes,
                m.getDescricao()
            });
        }
    }

    private void loadKartsEstado() {
        kartsModel.setRowCount(0);
        for (Kart k : service.listarKarts()) {
            kartsModel.addRow(new Object[]{
                k.getId(),
                k.getNumero(),
                k.getCategoria() != null ? k.getCategoria().getDescricao() : "-",
                k.getEstado().name(),
                k.isDisponivel() ? "Sim" : "Não"
            });
        }
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────────

    private void addFormRow(JPanel form, GridBagConstraints gbc, int row, String label, Component field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UiStyle.sectionFont());
        lbl.setForeground(UiStyle.TEXT_GRAY);
        form.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        form.add(field, gbc);
    }

    private record KartItem(Long id, Integer numero, KartEstado estado) {
        @Override public String toString() {
            return "Kart #" + numero + " [" + estado + "]";
        }
    }
}
