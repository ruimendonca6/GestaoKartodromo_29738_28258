package pt.kartodromo.desktop.ui.relatorios;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import pt.kartodromo.desktop.ui.UiStyle;

public class RelatoriosPanel extends JPanel {

    private final JCheckBox chkClientes = new JCheckBox("Clientes");
    private final JCheckBox chkReservas = new JCheckBox("Reservas");
    private final JCheckBox chkCorridas = new JCheckBox("Corridas");
    private final JCheckBox chkReceita = new JCheckBox("Receita");

    private final JLabel lblStatus = new JLabel(" ");
    private final JProgressBar progress = new JProgressBar();
    private final PdfExporter exporter = new PdfExporter();

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public RelatoriosPanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(UiStyle.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));

        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setOpaque(false);

        header.add(
                UiStyle.createPageTitle("Relatórios"),
                BorderLayout.NORTH
        );

        JLabel sub = new JLabel("Selecione os relatórios e escolha a pasta de destino");
        sub.setFont(UiStyle.labelFont().deriveFont(12f));
        sub.setForeground(UiStyle.TEXT_GRAY);
        sub.setBorder(new EmptyBorder(0, 24, 8, 0));

        header.add(sub, BorderLayout.SOUTH);

        JPanel centro = new JPanel();
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
        centro.setBackground(UiStyle.BACKGROUND_COLOR);
        centro.setBorder(new EmptyBorder(0, 4, 4, 4));

        centro.add(buildSelecaoCard());
        centro.add(Box.createVerticalStrut(16));
        centro.add(buildExportCard());
        centro.add(Box.createVerticalGlue());

        add(header, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
    }

    private JPanel buildSelecaoCard() {
        JPanel inner = new JPanel(new BorderLayout(0, 12));
        inner.setOpaque(false);

        JLabel labelTitulo = new JLabel("Selecionar Relatórios");
        labelTitulo.setFont(UiStyle.sectionFont());
        labelTitulo.setForeground(UiStyle.PRIMARY_RED);
        inner.add(labelTitulo, BorderLayout.NORTH);

        JPanel checks = new JPanel(new GridLayout(2, 2, 24, 12));
        checks.setOpaque(false);

        styleCheck(chkClientes, new Color(21, 101, 192));
        styleCheck(chkReservas, new Color(106, 27, 154));
        styleCheck(chkCorridas, new Color(27, 94, 32));
        styleCheck(chkReceita, new Color(183, 28, 28));

        checks.add(chkClientes);
        checks.add(chkReservas);
        checks.add(chkCorridas);
        checks.add(chkReceita);

        inner.add(checks, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btns.setOpaque(false);

        JButton btnTodos = smallBtn("Selecionar Todos");
        JButton btnNenhum = smallBtn("Limpar Seleção");

        btnTodos.addActionListener(e -> setAll(true));
        btnNenhum.addActionListener(e -> setAll(false));

        btns.add(btnTodos);
        btns.add(btnNenhum);

        inner.add(btns, BorderLayout.SOUTH);

        return UiStyle.createCard(inner);
    }

    private JPanel buildExportCard() {
        JPanel inner = new JPanel(new BorderLayout(0, 10));
        inner.setOpaque(false);

        JButton btnExportar =
                UiStyle.createActionButton(
                        "Exportar para PDF",
                        UiStyle.CREATE_GREEN
                );

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

    private void exportar() {
        if (!chkClientes.isSelected()
                && !chkReservas.isSelected()
                && !chkCorridas.isSelected()
                && !chkReceita.isSelected()) {

            setStatus("Selecione pelo menos um relatório.", UiStyle.PRIMARY_RED);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("Escolher pasta de destino");

        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File pasta = fileChooser.getSelectedFile();
        String timestamp = LocalDateTime.now().format(FMT);

        progress.setVisible(true);
        setStatus("A gerar PDFs...", UiStyle.TEXT_GRAY);

        new SwingWorker<Void, String>() {

            int ok = 0;
            int err = 0;
            String primeiroErro = "";

            @Override
            protected Void doInBackground() {
                if (chkClientes.isSelected()) {
                    run(
                            "clientes_" + timestamp + ".pdf",
                            file -> exporter.exportarClientes(file)
                    );
                }

                if (chkReservas.isSelected()) {
                    run(
                            "reservas_" + timestamp + ".pdf",
                            file -> exporter.exportarReservas(file)
                    );
                }

                if (chkCorridas.isSelected()) {
                    run(
                            "corridas_" + timestamp + ".pdf",
                            file -> exporter.exportarCorridas(file)
                    );
                }

                if (chkReceita.isSelected()) {
                    run(
                            "receita_" + timestamp + ".pdf",
                            file -> exporter.exportarReceita(file)
                    );
                }

                return null;
            }

            private void run(String nome, ExportTask task) {
                try {
                    task.run(new File(pasta, nome));
                    ok++;

                } catch (Exception ex) {
                    err++;

                    if (primeiroErro.isBlank()) {
                        primeiroErro =
                                ex.getClass().getSimpleName()
                                        + ": "
                                        + ex.getMessage();
                    }

                    publish(
                            "Erro em "
                                    + nome
                                    + ": "
                                    + ex.getMessage()
                    );
                }
            }

            @Override
            protected void process(List<String> chunks) {
                if (chunks != null && !chunks.isEmpty()) {
                    setStatus(
                            chunks.get(chunks.size() - 1),
                            UiStyle.PRIMARY_RED
                    );
                }
            }

            @Override
            protected void done() {
                progress.setVisible(false);

                if (err == 0) {
                    setStatus(
                            ok
                                    + " PDF(s) exportados para: "
                                    + pasta.getAbsolutePath(),
                            new Color(27, 94, 32)
                    );
                    return;
                }

                setStatus(
                        ok
                                + " exportados, "
                                + err
                                + " com erro. "
                                + primeiroErro,
                        UiStyle.PRIMARY_RED
                );

                JOptionPane.showMessageDialog(
                        RelatoriosPanel.this,
                        primeiroErro,
                        "Erro ao exportar PDF",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }.execute();
    }

    private void setAll(boolean value) {
        chkClientes.setSelected(value);
        chkReservas.setSelected(value);
        chkCorridas.setSelected(value);
        chkReceita.setSelected(value);
    }

    private void setStatus(String message, Color color) {
        lblStatus.setText(message);
        lblStatus.setForeground(color);
    }

    private void styleCheck(JCheckBox checkBox, Color color) {
        checkBox.setOpaque(false);
        checkBox.setFont(UiStyle.labelFont().deriveFont(Font.BOLD, 13f));
        checkBox.setForeground(color);
        checkBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private JButton smallBtn(String text) {
        JButton button = new JButton(text);

        button.setFont(UiStyle.labelFont().deriveFont(11f));
        button.setForeground(UiStyle.TEXT_GRAY);
        button.setBackground(new Color(245, 245, 245));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200)),
                        new EmptyBorder(4, 10, 4, 10)
                )
        );

        return button;
    }

    @FunctionalInterface
    private interface ExportTask {
        void run(File file) throws Exception;
    }

    public void refreshData() {
    }
}