package pt.kartodromo.desktop.ui.resultados;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
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

import pt.kartodromo.core.bll.ResultadoService;
import pt.kartodromo.core.model.Resultado;
import pt.kartodromo.desktop.ui.UiStyle;

public class ResultadosPanel extends JPanel {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ResultadoService service = new ResultadoService();

    // Registar form
    private JTextField pilotoField;
    private JTextField corridaField;
    private JSpinner minSpinner;
    private JSpinner secSpinner;
    private JSpinner msSpinner;
    private JSpinner posicaoSpinner;

    // Tempos table
    private DefaultTableModel temposModel;
    private JTable temposTable;
    private Long resultadoSelecionadoId;

    // Melhor volta table
    private DefaultTableModel melhorVoltaModel;

    // Classificação table
    private DefaultTableModel classificacaoModel;

    public ResultadosPanel() {
        setLayout(new BorderLayout());
        setBackground(UiStyle.BACKGROUND_COLOR);

        JLabel header = new JLabel("Resultados");
        header.setFont(new Font("Segoe UI Emoji", Font.BOLD, 26));
        header.setForeground(UiStyle.PRIMARY_RED);
        header.setBorder(BorderFactory.createEmptyBorder(20, 24, 10, 0));
        add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(UiStyle.BACKGROUND_COLOR);
        tabs.addTab("Registar Tempo", buildRegistarTab());
        tabs.addTab("Tempos dos Pilotos", buildTemposTab());
        tabs.addTab("Melhor Volta", buildMelhorVoltaTab());
        tabs.addTab("Classificação", buildClassificacaoTab());
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

        pilotoField = new JTextField(25);
        pilotoField.setFont(UiStyle.labelFont());

        corridaField = new JTextField(25);
        corridaField.setFont(UiStyle.labelFont());

        // Tempo: mm:ss:ms
        minSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 99, 1));
        secSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));
        msSpinner  = new JSpinner(new SpinnerNumberModel(0, 0, 999, 1));
        minSpinner.setPreferredSize(new Dimension(60, 30));
        secSpinner.setPreferredSize(new Dimension(60, 30));
        msSpinner.setPreferredSize(new Dimension(70, 30));

        JPanel tempoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        tempoPanel.setBackground(UiStyle.BACKGROUND_COLOR);
        tempoPanel.add(minSpinner);
        tempoPanel.add(new JLabel("min"));
        tempoPanel.add(secSpinner);
        tempoPanel.add(new JLabel("seg"));
        tempoPanel.add(msSpinner);
        tempoPanel.add(new JLabel("ms"));

        posicaoSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        posicaoSpinner.setFont(UiStyle.labelFont());

        int row = 0;
        addFormRow(form, gbc, row++, "Nome do Piloto:", pilotoField);
        addFormRow(form, gbc, row++, "Nome da Corrida:", corridaField);
        addFormRow(form, gbc, row++, "Tempo de Volta:", tempoPanel);
        addFormRow(form, gbc, row++, "Posição:", posicaoSpinner);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setBackground(UiStyle.BACKGROUND_COLOR);

        JButton btnRegistar = UiStyle.createActionButton("Registar", UiStyle.CREATE_GREEN);
        JButton btnLimpar   = UiStyle.createActionButton("Limpar", UiStyle.CLEAR_GRAY);

        btnRegistar.addActionListener(e -> registarTempo());
        btnLimpar.addActionListener(e -> limparFormulario());

        buttons.add(btnRegistar);
        buttons.add(btnLimpar);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.insets = new Insets(14, 8, 6, 8);
        form.add(buttons, gbc);

        wrapper.add(UiStyle.createCard(form), BorderLayout.NORTH);
        return wrapper;
    }

    // ── TAB TEMPOS ────────────────────────────────────────────────────────────────

    private JPanel buildTemposTab() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 12));
        wrapper.setBackground(UiStyle.BACKGROUND_COLOR);
        wrapper.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        String[] cols = {"ID", "Piloto", "Corrida", "Tempo", "Posição", "Data"};
        temposModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        temposTable = new JTable(temposModel);
        UiStyle.styleTable(temposTable);
        temposTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        temposTable.getColumnModel().getColumn(0).setMinWidth(0);
        temposTable.getColumnModel().getColumn(0).setMaxWidth(0);

        // Color posição top 3
        temposTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, focus, r, c);
                if (!sel && v instanceof Integer pos) {
                    if (pos == 1) setForeground(new Color(212, 175, 55));
                    else if (pos == 2) setForeground(new Color(160, 160, 160));
                    else if (pos == 3) setForeground(new Color(176, 100, 50));
                    else setForeground(UiStyle.TEXT_GRAY);
                }
                return this;
            }
        });

        temposTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = temposTable.getSelectedRow();
                if (row >= 0) {
                    resultadoSelecionadoId = (Long) temposModel.getValueAt(
                            temposTable.convertRowIndexToModel(row), 0);
                }
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnPanel.setBackground(UiStyle.BACKGROUND_COLOR);

        JButton btnEliminar = UiStyle.createActionButton("Eliminar", UiStyle.DELETE_RED);
        JButton btnAtualizar = UiStyle.createActionButton("Atualizar", UiStyle.CLEAR_GRAY);

        btnEliminar.addActionListener(e -> eliminarResultado());
        btnAtualizar.addActionListener(e -> loadData());

        btnPanel.add(btnEliminar);
        btnPanel.add(btnAtualizar);

        wrapper.add(btnPanel, BorderLayout.NORTH);
        wrapper.add(new JScrollPane(temposTable), BorderLayout.CENTER);
        return wrapper;
    }

    // ── TAB MELHOR VOLTA ──────────────────────────────────────────────────────────

    private JPanel buildMelhorVoltaTab() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 12));
        wrapper.setBackground(UiStyle.BACKGROUND_COLOR);
        wrapper.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel info = new JLabel("Melhor tempo de volta registado por cada piloto");
        info.setFont(UiStyle.sectionFont());
        info.setForeground(UiStyle.TEXT_GRAY);
        info.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        String[] cols = {"Piloto", "Melhor Volta", "Corrida", "Data"};
        melhorVoltaModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable melhorVoltaTable = new JTable(melhorVoltaModel);
        UiStyle.styleTable(melhorVoltaTable);

        // Gold color for tempo column
        melhorVoltaTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, focus, r, c);
                if (!sel) setForeground(new Color(180, 130, 0));
                setFont(getFont().deriveFont(Font.BOLD));
                return this;
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnPanel.setBackground(UiStyle.BACKGROUND_COLOR);
        JButton btnAtualizar = UiStyle.createActionButton("Atualizar", UiStyle.CLEAR_GRAY);
        btnAtualizar.addActionListener(e -> loadMelhorVolta());
        btnPanel.add(btnAtualizar);

        wrapper.add(info, BorderLayout.NORTH);
        wrapper.add(new JScrollPane(melhorVoltaTable), BorderLayout.CENTER);
        wrapper.add(btnPanel, BorderLayout.SOUTH);
        return wrapper;
    }

    // ── TAB CLASSIFICAÇÃO ─────────────────────────────────────────────────────────

    private JPanel buildClassificacaoTab() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 12));
        wrapper.setBackground(UiStyle.BACKGROUND_COLOR);
        wrapper.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel info = new JLabel("Classificação geral por posição e tempo de volta");
        info.setFont(UiStyle.sectionFont());
        info.setForeground(UiStyle.TEXT_GRAY);
        info.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        String[] cols = {"Pos.", "Piloto", "Corrida", "Tempo", "Data"};
        classificacaoModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable classificacaoTable = new JTable(classificacaoModel);
        UiStyle.styleTable(classificacaoTable);

        classificacaoTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, focus, r, c);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!sel && v instanceof Integer pos) {
                    if (pos == 1) { setForeground(new Color(212, 175, 55)); setFont(getFont().deriveFont(Font.BOLD)); }
                    else if (pos == 2) { setForeground(new Color(160, 160, 160)); setFont(getFont().deriveFont(Font.BOLD)); }
                    else if (pos == 3) { setForeground(new Color(176, 100, 50)); setFont(getFont().deriveFont(Font.BOLD)); }
                    else setForeground(UiStyle.TEXT_GRAY);
                }
                return this;
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnPanel.setBackground(UiStyle.BACKGROUND_COLOR);
        JButton btnAtualizar = UiStyle.createActionButton("Atualizar", UiStyle.CLEAR_GRAY);
        btnAtualizar.addActionListener(e -> loadClassificacao());
        btnPanel.add(btnAtualizar);

        wrapper.add(info, BorderLayout.NORTH);
        wrapper.add(new JScrollPane(classificacaoTable), BorderLayout.CENTER);
        wrapper.add(btnPanel, BorderLayout.SOUTH);
        return wrapper;
    }

    // ── ACTIONS ──────────────────────────────────────────────────────────────────

    private void registarTempo() {
        String piloto = pilotoField.getText().trim();
        String corrida = corridaField.getText().trim();
        int min = (int) minSpinner.getValue();
        int sec = (int) secSpinner.getValue();
        int ms  = (int) msSpinner.getValue();
        int posicao = (int) posicaoSpinner.getValue();

        long tempoMs = (long) min * 60_000 + (long) sec * 1_000 + ms;

        try {
            service.registar(piloto, corrida, tempoMs, posicao, LocalDateTime.now());
            JOptionPane.showMessageDialog(this, "Tempo registado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            limparFormulario();
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarResultado() {
        if (resultadoSelecionadoId == null) {
            JOptionPane.showMessageDialog(this, "Selecione um resultado.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Eliminar este resultado?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                service.eliminar(resultadoSelecionadoId);
                resultadoSelecionadoId = null;
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void limparFormulario() {
        pilotoField.setText("");
        corridaField.setText("");
        minSpinner.setValue(0);
        secSpinner.setValue(0);
        msSpinner.setValue(0);
        posicaoSpinner.setValue(1);
    }

    // ── DATA LOADING ──────────────────────────────────────────────────────────────

    public void refreshData() {
        loadData();
    }

    private void loadData() {
        loadTempos();
        loadMelhorVolta();
        loadClassificacao();
    }

    private void loadTempos() {
        temposModel.setRowCount(0);
        for (Resultado r : service.listarTodos()) {
            temposModel.addRow(new Object[]{
                r.getId(),
                r.getNomePiloto(),
                r.getNomeCorrida(),
                formatTempo(r.getTempoMs()),
                r.getPosicao(),
                r.getDataCorrida() != null ? r.getDataCorrida().format(FMT) : "-"
            });
        }
    }

    private void loadMelhorVolta() {
        melhorVoltaModel.setRowCount(0);
        for (Resultado r : service.listarMelhorVoltaPorPiloto()) {
            melhorVoltaModel.addRow(new Object[]{
                r.getNomePiloto(),
                formatTempo(r.getTempoMs()),
                r.getNomeCorrida(),
                r.getDataCorrida() != null ? r.getDataCorrida().format(FMT) : "-"
            });
        }
    }

    private void loadClassificacao() {
        classificacaoModel.setRowCount(0);
        for (Resultado r : service.listarClassificacaoGeral()) {
            classificacaoModel.addRow(new Object[]{
                r.getPosicao(),
                r.getNomePiloto(),
                r.getNomeCorrida(),
                formatTempo(r.getTempoMs()),
                r.getDataCorrida() != null ? r.getDataCorrida().format(FMT) : "-"
            });
        }
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────────

    private static String formatTempo(long ms) {
        long min = ms / 60_000;
        long sec = (ms % 60_000) / 1_000;
        long millis = ms % 1_000;
        return String.format("%02d:%02d.%03d", min, sec, millis);
    }

    private void addFormRow(JPanel form, GridBagConstraints gbc, int row, String label, Component field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UiStyle.sectionFont());
        lbl.setForeground(UiStyle.TEXT_GRAY);
        form.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        form.add(field, gbc);
    }
}
