package pt.kartodromo.desktop.ui.notificacoes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import pt.kartodromo.core.bll.NotificacaoService;
import pt.kartodromo.core.bll.NotificacaoService.Notificacao;
import pt.kartodromo.core.bll.NotificacaoService.TipoNotificacao;
import pt.kartodromo.desktop.ui.UiStyle;

public class NotificacaoPanel extends JPanel {

    private static final Color COR_KART = new Color(230, 81, 0);
    private static final Color COR_RESERVA = new Color(21, 101, 192);
    private static final Color COR_CORRIDA = new Color(27, 94, 32);

    private static final Color COR_KART_BG = new Color(255, 243, 224);
    private static final Color COR_RES_BG = new Color(227, 242, 253);
    private static final Color COR_COR_BG = new Color(232, 245, 233);

    private static final String[] COLUNAS = {
            "Tipo",
            "Título",
            "Detalhe",
            "Hora"
    };

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    private final NotificacaoService service =
            new NotificacaoService();

    private final DefaultTableModel tableModel;

    private final JLabel lblUltimaAtualizacao;
    private final JLabel lblContador;

    public NotificacaoPanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(UiStyle.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));

        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        header.add(
                UiStyle.createPageTitle("Notificações"),
                BorderLayout.WEST
        );

        lblContador = new JLabel("0 alertas ativos");
        lblContador.setFont(UiStyle.labelFont().deriveFont(Font.BOLD, 13f));
        lblContador.setForeground(UiStyle.TEXT_GRAY);
        lblContador.setBorder(new EmptyBorder(24, 0, 0, 20));

        header.add(lblContador, BorderLayout.EAST);

        tableModel =
                new DefaultTableModel(COLUNAS, 0) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };

        JTable tabela = new JTable(tableModel);

        UiStyle.styleTable(tabela);

        tabela.setRowHeight(36);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(160);
        tabela.getColumnModel().getColumn(0).setMaxWidth(200);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(80);
        tabela.getColumnModel().getColumn(3).setMaxWidth(100);

        tabela.setDefaultRenderer(
                Object.class,
                new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(
                            JTable table,
                            Object value,
                            boolean selected,
                            boolean focus,
                            int row,
                            int column) {

                        super.getTableCellRendererComponent(
                                table,
                                value,
                                selected,
                                focus,
                                row,
                                column
                        );

                        if (!selected) {
                            String tipo =
                                    (String) tableModel.getValueAt(row, 0);

                            if (tipo != null && tipo.startsWith("Kart")) {
                                setBackground(COR_KART_BG);
                                setForeground(column == 0 ? COR_KART : Color.DARK_GRAY);
                            } else if (tipo != null && tipo.startsWith("Reserva")) {
                                setBackground(COR_RES_BG);
                                setForeground(column == 0 ? COR_RESERVA : Color.DARK_GRAY);
                            } else if (tipo != null && tipo.startsWith("Corrida")) {
                                setBackground(COR_COR_BG);
                                setForeground(column == 0 ? COR_CORRIDA : Color.DARK_GRAY);
                            } else {
                                setBackground(Color.WHITE);
                                setForeground(Color.DARK_GRAY);
                            }

                            if (column == 0) {
                                setFont(getFont().deriveFont(Font.BOLD));
                            } else {
                                setFont(getFont().deriveFont(Font.PLAIN));
                            }
                        }

                        setBorder(new EmptyBorder(4, 8, 4, 8));

                        return this;
                    }
                }
        );

        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);

        JPanel centro = new JPanel(new BorderLayout(0, 8));
        centro.setBackground(UiStyle.BACKGROUND_COLOR);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        toolbar.setBackground(UiStyle.BACKGROUND_COLOR);

        JButton btnAtualizar =
                UiStyle.createActionButton(
                        "Atualizar",
                        UiStyle.UPDATE_BLUE
                );

        btnAtualizar.addActionListener(e -> loadData());

        toolbar.add(btnAtualizar);

        lblUltimaAtualizacao =
                new JLabel("Última atualização: --");

        lblUltimaAtualizacao.setFont(
                UiStyle.labelFont().deriveFont(11f)
        );

        lblUltimaAtualizacao.setForeground(UiStyle.TEXT_GRAY);

        toolbar.add(lblUltimaAtualizacao);

        centro.add(toolbar, BorderLayout.NORTH);
        centro.add(UiStyle.createCard(scroll), BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        add(buildLegenda(), BorderLayout.SOUTH);

        loadData();
    }

    private JPanel buildLegenda() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 6));

        panel.setBackground(UiStyle.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(4, 0, 0, 0));

        panel.add(legendaChip("Kart em Manutenção", COR_KART_BG, COR_KART));
        panel.add(legendaChip("Reserva Próxima (< 2h)", COR_RES_BG, COR_RESERVA));
        panel.add(legendaChip("Corrida a Iniciar (< 30min)", COR_COR_BG, COR_CORRIDA));

        return panel;
    }

    private JLabel legendaChip(String texto, Color background, Color foreground) {
        JLabel label = new JLabel("  " + texto + "  ");

        label.setOpaque(true);
        label.setBackground(background);
        label.setForeground(foreground);
        label.setFont(UiStyle.labelFont().deriveFont(Font.BOLD, 11f));

        label.setBorder(
                BorderFactory.createLineBorder(foreground)
        );

        return label;
    }

    private void loadData() {
        tableModel.setRowCount(0);

        try {
            List<Notificacao> lista =
                    service.listarTodas();

            for (Notificacao notificacao : lista) {
                tableModel.addRow(
                        new Object[]{
                                labelTipo(notificacao.tipo()),
                                notificacao.titulo(),
                                notificacao.detalhe(),
                                notificacao.tempo()
                        }
                );
            }

            int total =
                    lista.size();

            lblContador.setText(
                    total == 0
                            ? "Sem alertas ativos"
                            : total + " alerta(s) ativo(s)"
            );

            lblContador.setForeground(
                    total == 0
                            ? UiStyle.TEXT_GRAY
                            : UiStyle.PRIMARY_RED
            );

        } catch (Exception ex) {
            lblContador.setText("Erro ao carregar notificações");
            lblContador.setForeground(UiStyle.PRIMARY_RED);
        }

        lblUltimaAtualizacao.setText(
                "Última atualização: "
                        + LocalDateTime.now().format(FMT)
        );
    }

    private String labelTipo(TipoNotificacao tipo) {
        return switch (tipo) {
            case KART_MANUTENCAO -> "Kart em Manutenção";
            case RESERVA_PROXIMA -> "Reserva Próxima";
            case CORRIDA_PROXIMA -> "Corrida a Iniciar";
        };
    }

    public void refreshData() {
        loadData();
    }
}