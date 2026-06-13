package pt.kartodromo.desktop.ui.relatorios;

import pt.kartodromo.desktop.ui.UiStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RelatoriosPanel extends JPanel {

    private final JCheckBox chkClientes  = new JCheckBox("Clientes");
    private final JCheckBox chkReservas  = new JCheckBox("Reservas");
    private final JCheckBox chkCorridas  = new JCheckBox("Corridas");
    private final JCheckBox chkReceita   = new JCheckBox("Receita");
    private final JLabel    lblStatus    = new JLabel(" ");
    private final JProgressBar progress  = new JProgressBar();
    private final PdfExporter exporter   = new PdfExporter();

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public RelatoriosPanel() {
        setLayout(new BorderLayout());
        setBackground(UiStyle.BACKGROUND_COLOR);

        // ---- Header ----
        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setBackground(UiStyle.BACKGROUND_COLOR);
        header.setBorder(new EmptyBorder(24, 24, 12, 24));

        JLabel titulo = new JLabel("Relatorios");
        titulo.setFont(UiStyle.sectionFont());
        titulo.setForeground(UiStyle.PRIMARY_RED);
        header.add(titulo, BorderLayout.NORTH);

        JLabel sub = new JLabel("Selecione os relatorios e escolha a pasta de destino");
        sub.setFont(UiStyle.labelFont().deriveFont(12f));
        sub.setForeground(UiStyle.TEXT_GRAY);
        header.add(sub, BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);

        // ---- Centro com duas cards ----
        JPanel centro = new JPanel();
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
        centro.setBackground(UiStyle.BACKGROUND_COLOR);
        centro.setBorder(new EmptyBorder(0, 24, 24, 24));

        centro.add(buildSelecaoCard());
        centro.add(Box.createVerticalStrut(16));
        centro.add(buildExportCard());
        centro.add(Box.createVerticalGlue());

        add(centro, BorderLayout.CENTER);
    }

    // ---- Card de selecao de relatorios ----
    private JPanel buildSelecaoCard() {
        JPanel inner = new JPanel(new BorderLayout(0, 12));
        inner.setOpaque(false);

        JLabel labelTitulo = new JLabel("Selecionar Relatorios");
        labelTitulo.setFont(UiStyle.labelFont().deriveFont(Font.BOLD, 13f));
        labelTitulo.setForeground(UiStyle.TEXT_GRAY);
        inner.add(labelTitulo, BorderLayout.NORTH);

        JPanel checks = new JPanel(new GridLayout(2, 2, 24, 12));
        checks.setOpaque(false);

        styleCheck(chkClientes, new Color(21, 101, 192));
        styleCheck(chkReservas, new Color(106, 27, 154));
        styleCheck(chkCorridas, new Color(27, 94, 32));
        styleCheck(chkReceita,  new Color(183, 28, 28));

        checks.add(chkClientes);
        checks.add(chkReservas);
        checks.add(chkCorridas);
        checks.add(chkReceita);
        inner.add(checks, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btns.setOpaque(false);

        JButton btnTodos   = smallBtn("Selecionar Todos");
        JButton btnNenhum  = smallBtn("Limpar Selecao");
        btnTodos.addActionListener(e -> setAll(true));
        btnNenhum.addActionListener(e -> setAll(false));
        btns.add(btnTodos);
        btns.add(btnNenhum);
        inner.add(btns, BorderLayout.SOUTH);

        return UiStyle.createCard(inner);
    }

    // ---- Card de exportacao ----
    private JPanel buildExportCard() {
        JPanel inner = new JPanel(new BorderLayout(0, 10));
        inner.setOpaque(false);

        JButton btnExportar = UiStyle.createActionButton("Exportar para PDF", UiStyle.CREATE_GREEN);
        btnExportar.setPreferredSize(new Dimension(200, 42));
        btnExportar.setFont(btnExportar.getFont().deriveFont(Font.BOLD, 13f));
        btnExportar.setMaximumSize(new Dimension(220, 42));
        btnExportar.setAlignmentX(LEFT_ALIGNMENT);

        progress.setVisible(false);
        progress.setIndeterminate(true);
        progress.setStringPainted(true);
        progress.setString("A exportar...");

        JPanel topRow = new JPanel(new BorderLayout(12, 0));
        topRow.setOpaque(false);
        topRow.add(btnExportar, BorderLayout.WEST);
        topRow.add(progress, BorderLayout.CENTER);

        lblStatus.setFont(UiStyle.labelFont().deriveFont(11f));
        lblStatus.setForeground(UiStyle.TEXT_GRAY);

        inner.add(topRow, BorderLayout.NORTH);
        inner.add(lblStatus, BorderLayout.CENTER);

        btnExportar.addActionListener(e -> exportar());

        return UiStyle.createCard(inner);
    }

    // ---- Exportacao ----
    private void exportar() {
        if (!chkClientes.isSelected() && !chkReservas.isSelected()
                && !chkCorridas.isSelected() && !chkReceita.isSelected()) {
            setStatus("Selecione pelo menos um relatorio.", UiStyle.PRIMARY_RED);
            return;
        }

        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setDialogTitle("Escolher pasta de destino");
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File pasta = fc.getSelectedFile();
        String ts  = LocalDateTime.now().format(FMT);

        progress.setVisible(true);
        setStatus("A gerar PDFs...", UiStyle.TEXT_GRAY);

        new SwingWorker<Void, String>() {
            int ok = 0, err = 0;

            @Override
            protected Void doInBackground() {
                if (chkClientes.isSelected()) run("clientes_" + ts + ".pdf", f -> exporter.exportarClientes(f));
                if (chkReservas.isSelected()) run("reservas_"  + ts + ".pdf", f -> exporter.exportarReservas(f));
                if (chkCorridas.isSelected()) run("corridas_"  + ts + ".pdf", f -> exporter.exportarCorridas(f));
                if (chkReceita.isSelected())  run("receita_"   + ts + ".pdf", f -> exporter.exportarReceita(f));
                return null;
            }

            private void run(String nome, ExportTask t) {
                try { t.run(new File(pasta, nome)); ok++; }
                catch (Exception ex) { err++; publish("Erro: " + ex.getMessage()); }
            }

            @Override
            protected void process(List<String> chunks) {
                setStatus(chunks.get(chunks.size() - 1), UiStyle.PRIMARY_RED);
            }

            @Override
            protected void done() {
                progress.setVisible(false);
                if (err == 0) {
                    setStatus(ok + " PDF(s) exportados para: " + pasta.getAbsolutePath(), new Color(27, 94, 32));
                } else {
                    setStatus(ok + " exportados, " + err + " com erro. Pasta: " + pasta.getAbsolutePath(), UiStyle.PRIMARY_RED);
                }
            }
        }.execute();
    }

    // ---- Helpers ----
    private void setAll(boolean v) {
        chkClientes.setSelected(v);
        chkReservas.setSelected(v);
        chkCorridas.setSelected(v);
        chkReceita.setSelected(v);
    }

    private void setStatus(String msg, Color cor) {
        lblStatus.setText(msg);
        lblStatus.setForeground(cor);
    }

    private void styleCheck(JCheckBox cb, Color cor) {
        cb.setOpaque(false);
        cb.setFont(UiStyle.labelFont().deriveFont(Font.BOLD, 13f));
        cb.setForeground(cor);
        cb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private JButton smallBtn(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(UiStyle.labelFont().deriveFont(11f));
        btn.setForeground(UiStyle.TEXT_GRAY);
        btn.setBackground(new Color(245, 245, 245));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    @FunctionalInterface
    private interface ExportTask {
        void run(File f) throws Exception;
    }

    public void refreshData() {}
}
