package pt.kartodromo.desktop.ui.notificacoes;

import pt.kartodromo.core.bll.NotificacaoService;
import pt.kartodromo.core.bll.NotificacaoService.Notificacao;
import pt.kartodromo.core.bll.NotificacaoService.TipoNotificacao;
import pt.kartodromo.desktop.ui.UiStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class NotificacaoPanel extends JPanel {

    private static final Color COR_KART     = new Color(230, 81, 0);
    private static final Color COR_RESERVA  = new Color(21, 101, 192);
    private static final Color COR_CORRIDA  = new Color(27, 94, 32);
    private static final Color COR_KART_BG  = new Color(255, 243, 224);
    private static final Color COR_RES_BG   = new Color(227, 242, 253);
    private static final Color COR_COR_BG   = new Color(232, 245, 233);

    private static final String[] COLUNAS = {"Tipo", "Titulo", "Detalhe", "Hora"};

    private final NotificacaoService service = new NotificacaoService();
    private final DefaultTableModel tableModel;
    private final JLabel lblUltimaAtualizacao;
    private final JLabel lblContador;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public NotificacaoPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UiStyle.BACKGROUND_COLOR);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UiStyle.BACKGROUND_COLOR);
        header.setBorder(new EmptyBorder(20, 20, 10, 20));

        JLabel titulo = new JLabel("Notificacoes");
        titulo.setFont(UiStyle.sectionFont());
        titulo.setForeground(UiStyle.PRIMARY_RED);
        header.add(titulo, BorderLayout.WEST);

        lblContador = new JLabel("0 alertas ativos");
        lblContador.setFont(UiStyle.labelFont().deriveFont(Font.BOLD, 13f));
        lblContador.setForeground(UiStyle.TEXT_GRAY);
        header.add(lblContador, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // Legenda
        JPanel legenda = buildLegenda();
        add(legenda, BorderLayout.SOUTH);

        // Tabela
        tableModel = new DefaultTableModel(COLUNAS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tabela = new JTable(tableModel);
        UiStyle.styleTable(tabela);
        tabela.setRowHeight(36);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(160);
        tabela.getColumnModel().getColumn(0).setMaxWidth(200);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(80);
        tabela.getColumnModel().getColumn(3).setMaxWidth(100);

        tabela.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean selected, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, value, selected, focus, row, col);
                if (!selected) {
                    String tipo = (String) tableModel.getValueAt(row, 0);
                    if (tipo != null && tipo.startsWith("Kart")) {
                        setBackground(COR_KART_BG);
                        setForeground(col == 0 ? COR_KART : Color.DARK_GRAY);
                    } else if (tipo != null && tipo.startsWith("Reserva")) {
                        setBackground(COR_RES_BG);
                        setForeground(col == 0 ? COR_RESERVA : Color.DARK_GRAY);
                    } else if (tipo != null && tipo.startsWith("Corrida")) {
                        setBackground(COR_COR_BG);
                        setForeground(col == 0 ? COR_CORRIDA : Color.DARK_GRAY);
                    } else {
                        setBackground(Color.WHITE);
                        setForeground(Color.DARK_GRAY);
                    }
                    if (col == 0) setFont(getFont().deriveFont(Font.BOLD));
                    else setFont(getFont().deriveFont(Font.PLAIN));
                }
                setBorder(new EmptyBorder(4, 8, 4, 8));
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));
        scroll.getViewport().setBackground(Color.WHITE);

        // Painel central com botao de atualizar + tabela
        JPanel centro = new JPanel(new BorderLayout(0, 8));
        centro.setBackground(UiStyle.BACKGROUND_COLOR);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        toolbar.setBackground(UiStyle.BACKGROUND_COLOR);
        toolbar.setBorder(new EmptyBorder(0, 20, 0, 20));

        JButton btnAtualizar = UiStyle.createActionButton("Atualizar", UiStyle.UPDATE_BLUE);
        btnAtualizar.addActionListener(e -> loadData());
        toolbar.add(btnAtualizar);

        lblUltimaAtualizacao = new JLabel("Ultima atualizacao: --");
        lblUltimaAtualizacao.setFont(UiStyle.labelFont().deriveFont(11f));
        lblUltimaAtualizacao.setForeground(UiStyle.TEXT_GRAY);
        toolbar.add(lblUltimaAtualizacao);

        centro.add(toolbar, BorderLayout.NORTH);
        centro.add(scroll, BorderLayout.CENTER);

        add(centro, BorderLayout.CENTER);

        loadData();
    }

    private JPanel buildLegenda() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 6));
        p.setBackground(UiStyle.BACKGROUND_COLOR);
        p.setBorder(new EmptyBorder(4, 20, 12, 20));
        p.add(legendaChip("Kart em Manutencao", COR_KART_BG, COR_KART));
        p.add(legendaChip("Reserva Proxima (< 2h)", COR_RES_BG, COR_RESERVA));
        p.add(legendaChip("Corrida a Iniciar (< 30min)", COR_COR_BG, COR_CORRIDA));
        return p;
    }

    private JLabel legendaChip(String texto, Color bg, Color fg) {
        JLabel l = new JLabel("  " + texto + "  ");
        l.setOpaque(true);
        l.setBackground(bg);
        l.setForeground(fg);
        l.setFont(UiStyle.labelFont().deriveFont(Font.BOLD, 11f));
        l.setBorder(BorderFactory.createLineBorder(fg, 1, true));
        return l;
    }

    private void loadData() {
        tableModel.setRowCount(0);
        try {
            List<Notificacao> lista = service.listarTodas();
            for (Notificacao n : lista) {
                tableModel.addRow(new Object[]{
                        labelTipo(n.tipo()),
                        n.titulo(),
                        n.detalhe(),
                        n.tempo()
                });
            }
            int total = lista.size();
            lblContador.setText(total == 0 ? "Sem alertas ativos" : total + " alerta(s) ativo(s)");
            lblContador.setForeground(total == 0 ? UiStyle.TEXT_GRAY : UiStyle.PRIMARY_RED);
        } catch (Exception ex) {
            lblContador.setText("Erro ao carregar notificacoes");
        }
        lblUltimaAtualizacao.setText("Ultima atualizacao: " + LocalDateTime.now().format(FMT));
    }

    private String labelTipo(TipoNotificacao tipo) {
        return switch (tipo) {
            case KART_MANUTENCAO -> "Kart em Manutencao";
            case RESERVA_PROXIMA -> "Reserva Proxima";
            case CORRIDA_PROXIMA -> "Corrida a Iniciar";
        };
    }

    public void refreshData() {
        loadData();
    }
}
