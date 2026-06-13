package pt.kartodromo.desktop.ui.estatisticas;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import pt.kartodromo.core.bll.CorridaService;
import pt.kartodromo.core.bll.ReservaService;
import pt.kartodromo.core.model.Corrida;
import pt.kartodromo.core.model.Reserva;
import pt.kartodromo.core.model.enums.ReservaEstado;
import pt.kartodromo.desktop.ui.UiStyle;

public class EstatisticasPanel extends JPanel {

    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_KPI = new Font("Segoe UI", Font.BOLD, 28);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 14);

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm");

    private static final int HORA_ABERTURA_MIN = 9 * 60;
    private static final int HORA_FECHO_MIN = 22 * 60;
    private static final int MINUTOS_DIA = HORA_FECHO_MIN - HORA_ABERTURA_MIN;

    private final ReservaService reservaService = new ReservaService();
    private final CorridaService corridaService = new CorridaService();

    private List<Reserva> allReservas = new ArrayList<>();
    private List<Corrida> allCorridas = new ArrayList<>();

    private LocalDate periodoInicio =
            LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

    private LocalDate periodoFim =
            periodoInicio.plusDays(6);

    private String periodoTipo = "semana";

    private JLabel kpiReceita;
    private JLabel kpiCorridas;
    private JLabel kpiCanceladas;
    private JLabel kpiOcupacao;
    private JLabel kpiReceitaSub;
    private JLabel kpiCorridasSub;
    private JLabel kpiCanceladasSub;
    private JLabel kpiOcupacaoSub;

    private DefaultTableModel corridasModel;
    private DefaultTableModel canceladasModel;
    private DefaultTableModel ocupacaoModel;

    private JPanel chartPanel;
    private Map<String, BigDecimal> chartData = new LinkedHashMap<>();

    public EstatisticasPanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(UiStyle.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));

        JPanel contentPanel = new JPanel(new BorderLayout(0, 12));
        contentPanel.setBackground(UiStyle.BACKGROUND_COLOR);

        contentPanel.add(buildPeriodBar(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setBackground(UiStyle.BACKGROUND_COLOR);

        center.add(buildKpiRow(), BorderLayout.NORTH);
        center.add(buildDetailArea(), BorderLayout.CENTER);

        contentPanel.add(center, BorderLayout.CENTER);

        add(UiStyle.createPageTitle("Estatísticas"), BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        refreshData();
    }

    private JPanel buildPeriodBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        bar.setBackground(UiStyle.CARD_COLOR);
        bar.setBorder(
                BorderFactory.createMatteBorder(
                        0,
                        0,
                        1,
                        0,
                        UiStyle.BORDER_COLOR
                )
        );

        JLabel lbl = new JLabel("Período:");
        lbl.setFont(FONT_BOLD);
        bar.add(lbl);

        ButtonGroup group = new ButtonGroup();

        String[][] options = {
                {"semana", "Esta semana"},
                {"mes", "Este mês"},
                {"ano", "Este ano"}
        };

        for (String[] option : options) {
            JToggleButton button = new JToggleButton(option[1]);
            button.setFont(FONT_NORMAL);
            button.setFocusPainted(false);
            button.setSelected(option[0].equals(periodoTipo));

            button.addActionListener(e -> {
                periodoTipo = option[0];
                updatePeriodRange();
                refreshAll();
            });

            group.add(button);
            bar.add(button);
        }

        return bar;
    }

    private void updatePeriodRange() {
        LocalDate today = LocalDate.now();

        switch (periodoTipo) {
            case "semana" -> {
                periodoInicio =
                        today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                periodoFim =
                        periodoInicio.plusDays(6);
            }

            case "mes" -> {
                periodoInicio =
                        today.withDayOfMonth(1);
                periodoFim =
                        today.with(TemporalAdjusters.lastDayOfMonth());
            }

            case "ano" -> {
                periodoInicio =
                        today.withDayOfYear(1);
                periodoFim =
                        today.with(TemporalAdjusters.lastDayOfYear());
            }

            default -> {
            }
        }
    }

    private JPanel buildKpiRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setBackground(UiStyle.BACKGROUND_COLOR);
        row.setPreferredSize(new Dimension(0, 110));

        kpiReceita = new JLabel("€ 0,00", SwingConstants.CENTER);
        kpiCorridas = new JLabel("0", SwingConstants.CENTER);
        kpiCanceladas = new JLabel("0", SwingConstants.CENTER);
        kpiOcupacao = new JLabel("0%", SwingConstants.CENTER);

        kpiReceitaSub = new JLabel("receita total", SwingConstants.CENTER);
        kpiCorridasSub = new JLabel("corridas realizadas", SwingConstants.CENTER);
        kpiCanceladasSub = new JLabel("reservas canceladas", SwingConstants.CENTER);
        kpiOcupacaoSub = new JLabel("taxa de ocupação", SwingConstants.CENTER);

        row.add(buildKpiCard(kpiReceita, kpiReceitaSub, "💰", new Color(33, 150, 243)));
        row.add(buildKpiCard(kpiCorridas, kpiCorridasSub, "🏁", new Color(76, 175, 80)));
        row.add(buildKpiCard(kpiCanceladas, kpiCanceladasSub, "❌", new Color(244, 67, 54)));
        row.add(buildKpiCard(kpiOcupacao, kpiOcupacaoSub, "📊", new Color(255, 152, 0)));

        return row;
    }

    private JPanel buildKpiCard(
            JLabel valueLabel,
            JLabel subLabel,
            String icon,
            Color accentColor) {

        JPanel card = new JPanel(new BorderLayout(0, 2));
        card.setBackground(UiStyle.CARD_COLOR);

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(UiStyle.BORDER_COLOR, 1),
                                BorderFactory.createEmptyBorder(10, 12, 10, 12)
                        )
                )
        );

        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));

        valueLabel.setFont(FONT_KPI);
        valueLabel.setForeground(accentColor);

        subLabel.setFont(FONT_LABEL);
        subLabel.setForeground(UiStyle.TEXT_GRAY);

        JPanel content = new JPanel(new BorderLayout(0, 2));
        content.setBackground(UiStyle.CARD_COLOR);

        content.add(iconLabel, BorderLayout.NORTH);
        content.add(valueLabel, BorderLayout.CENTER);
        content.add(subLabel, BorderLayout.SOUTH);

        card.add(content, BorderLayout.CENTER);

        return card;
    }

    private JPanel buildDetailArea() {
        JPanel grid = new JPanel(new GridLayout(2, 2, 12, 12));
        grid.setBackground(UiStyle.BACKGROUND_COLOR);

        grid.add(buildChartCard());
        grid.add(buildCorridasCard());
        grid.add(buildCanceladasCard());
        grid.add(buildOcupacaoCard());

        return grid;
    }

    private JPanel buildChartCard() {
        JPanel card = buildDetailCard("Receita por período");

        chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBarChart((Graphics2D) g);
            }
        };

        chartPanel.setBackground(Color.WHITE);

        card.add(chartPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel buildCorridasCard() {
        JPanel card = buildDetailCard("Corridas Realizadas");

        String[] cols = {
                "Data",
                "Hora",
                "Duração",
                "Layout",
                "Categoria",
                "Cliente"
        };

        corridasModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = buildStyledTable(corridasModel);

        card.add(new JScrollPane(table), BorderLayout.CENTER);

        return card;
    }

    private JPanel buildCanceladasCard() {
        JPanel card = buildDetailCard("Reservas Canceladas");

        String[] cols = {
                "Data",
                "Hora Início",
                "Hora Fim",
                "Cliente",
                "Kart",
                "Pista"
        };

        canceladasModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = buildStyledTable(canceladasModel);

        card.add(new JScrollPane(table), BorderLayout.CENTER);

        return card;
    }

    private JPanel buildOcupacaoCard() {
        JPanel card = buildDetailCard("Taxa de Ocupação por Pista");

        String[] cols = {
                "Pista",
                "Reservas",
                "Min. Ocupados",
                "Min. Disponíveis",
                "Taxa"
        };

        ocupacaoModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = buildStyledTable(ocupacaoModel);

        card.add(new JScrollPane(table), BorderLayout.CENTER);

        return card;
    }

    private JPanel buildDetailCard(String title) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(UiStyle.CARD_COLOR);

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UiStyle.BORDER_COLOR, 1),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                )
        );

        JLabel label = new JLabel(title);
        label.setFont(FONT_HEADER);
        label.setForeground(UiStyle.BLACK);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));

        card.add(label, BorderLayout.NORTH);

        return card;
    }

    private JTable buildStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);

        table.setFont(FONT_NORMAL);
        table.setRowHeight(26);
        table.setGridColor(new Color(235, 235, 235));
        table.setSelectionBackground(new Color(66, 133, 244));
        table.setSelectionForeground(Color.WHITE);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);

        table.getTableHeader().setFont(FONT_BOLD);
        table.getTableHeader().setBackground(UiStyle.PRIMARY_BLUE);
        table.getTableHeader().setForeground(Color.WHITE);

        return table;
    }

    private void drawBarChart(Graphics2D g) {
        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );

        int w = Math.max(chartPanel.getWidth(), 300);
        int h = Math.max(chartPanel.getHeight(), 180);

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);

        if (chartData.isEmpty()) {
            g.setFont(FONT_NORMAL);
            g.setColor(UiStyle.TEXT_GRAY);
            g.drawString("Sem dados no período selecionado.", 20, h / 2);
            return;
        }

        int leftMargin = 58;
        int rightMargin = 18;
        int topMargin = 18;
        int bottomMargin = 75;

        int chartW = w - leftMargin - rightMargin;
        int chartH = h - topMargin - bottomMargin;

        if (chartW <= 0 || chartH <= 0) {
            return;
        }

        List<String> labels =
                new ArrayList<>(chartData.keySet());

        List<BigDecimal> values =
                new ArrayList<>(chartData.values());

        BigDecimal maxVal =
                values.stream()
                        .max(Comparator.naturalOrder())
                        .orElse(BigDecimal.ONE);

        if (maxVal.compareTo(BigDecimal.ZERO) == 0) {
            maxVal = BigDecimal.ONE;
        }

        int n = labels.size();
        int gap = Math.max(6, chartW / Math.max(24, n * 3));
        int barW = Math.max(10, (chartW - ((n + 1) * gap)) / n);

        g.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        g.setColor(UiStyle.TEXT_GRAY);

        int ySteps = 4;

        for (int i = 0; i <= ySteps; i++) {
            double pct = (double) i / ySteps;

            int y =
                    topMargin + chartH - (int) (pct * chartH);

            BigDecimal value =
                    maxVal.multiply(BigDecimal.valueOf(pct));

            g.drawString(
                    String.format("%.0f€", value.doubleValue()),
                    4,
                    y + 4
            );

            g.setColor(new Color(230, 230, 230));
            g.drawLine(leftMargin, y, leftMargin + chartW, y);
            g.setColor(UiStyle.TEXT_GRAY);
        }

        Color barColor = new Color(33, 150, 243);
        Color barHighlight = new Color(66, 133, 244);

        for (int i = 0; i < n; i++) {
            BigDecimal value = values.get(i);

            double pct =
                    value.doubleValue() / maxVal.doubleValue();

            int barHeight =
                    (int) (pct * chartH);

            int x =
                    leftMargin + gap + i * (barW + gap);

            int y =
                    topMargin + chartH - barHeight;

            g.setColor(
                    value.compareTo(BigDecimal.ZERO) > 0
                            ? barColor
                            : new Color(220, 220, 220)
            );

            g.fillRoundRect(
                    x,
                    y,
                    barW,
                    barHeight,
                    4,
                    4
            );

            g.setColor(
                    value.compareTo(BigDecimal.ZERO) > 0
                            ? barHighlight
                            : new Color(200, 200, 200)
            );

            g.drawRoundRect(
                    x,
                    y,
                    barW,
                    barHeight,
                    4,
                    4
            );

            if (value.compareTo(BigDecimal.ZERO) > 0) {
                g.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g.setColor(UiStyle.BLACK);

                String valueText =
                        String.format("%.0f€", value.doubleValue());

                g.drawString(
                        valueText,
                        x + 2,
                        Math.max(12, y - 3)
                );
            }

            g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g.setColor(UiStyle.TEXT_GRAY);

            String label =
                    labels.get(i);

            int labelWidth =
                    g.getFontMetrics().stringWidth(label);

            int labelX =
                    x + (barW - labelWidth) / 2;

            int labelY =
                    topMargin + chartH + 18;

            g.drawString(
                    label,
                    labelX,
                    labelY
            );
        }

        g.setColor(new Color(200, 200, 200));

        g.drawLine(
                leftMargin,
                topMargin + chartH,
                leftMargin + chartW,
                topMargin + chartH
        );
    }

    public void refreshData() {
        try {
            allReservas =
                    reservaService.listarReservas();
        } catch (Exception e) {
            allReservas =
                    new ArrayList<>();
        }

        try {
            allCorridas =
                    corridaService.listarCorridas();
        } catch (Exception e) {
            allCorridas =
                    new ArrayList<>();
        }

        refreshAll();
    }

    private void refreshAll() {
        updatePeriodRange();

        List<Reserva> reservasPeriodo =
                allReservas.stream()
                        .filter(r ->
                                !r.getDataHoraInicio()
                                        .toLocalDate()
                                        .isBefore(periodoInicio)
                        )
                        .filter(r ->
                                !r.getDataHoraInicio()
                                        .toLocalDate()
                                        .isAfter(periodoFim)
                        )
                        .collect(Collectors.toList());

        List<Corrida> corridasPeriodo =
                allCorridas.stream()
                        .filter(c ->
                                !c.getDataHoraInicio()
                                        .toLocalDate()
                                        .isBefore(periodoInicio)
                        )
                        .filter(c ->
                                !c.getDataHoraInicio()
                                        .toLocalDate()
                                        .isAfter(periodoFim)
                        )
                        .collect(Collectors.toList());

        computeKpis(reservasPeriodo, corridasPeriodo);
        computeChartData(reservasPeriodo, corridasPeriodo);
        refreshCorridasTable(corridasPeriodo);
        refreshCanceladasTable(reservasPeriodo);
        refreshOcupacaoTable(reservasPeriodo);

        if (chartPanel != null) {
            chartPanel.repaint();
        }
    }

    private void computeKpis(
            List<Reserva> reservas,
            List<Corrida> corridas) {

        BigDecimal recReservas =
                reservas.stream()
                        .filter(r -> r.getEstado() != ReservaEstado.CANCELADA)
                        .map(r -> r.getKart().getCategoria().getPrecoBase())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal recCorridas =
                corridas.stream()
                        .map(c -> c.getCategoria().getPrecoBase())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalReceita =
                recReservas.add(recCorridas);

        long numCorridas =
                corridas.size();

        long numCanceladas =
                reservas.stream()
                        .filter(r -> r.getEstado() == ReservaEstado.CANCELADA)
                        .count();

        long totalReservas =
                reservas.size();

        double pctCancel =
                totalReservas == 0
                        ? 0.0
                        : (double) numCanceladas / totalReservas * 100.0;

        List<Reserva> ativas =
                reservas.stream()
                        .filter(r -> r.getEstado() != ReservaEstado.CANCELADA)
                        .collect(Collectors.toList());

        long numDias =
                periodoInicio.datesUntil(periodoFim.plusDays(1)).count();

        long numPistas =
                ativas.stream()
                        .map(Reserva::getPistaNome)
                        .distinct()
                        .count();

        long disponivel =
                numDias * (numPistas == 0 ? 1 : numPistas) * MINUTOS_DIA;

        long ocupados =
                ativas.stream()
                        .mapToLong(r ->
                                Duration.between(
                                        r.getDataHoraInicio(),
                                        r.getDataHoraFim()
                                ).toMinutes()
                        )
                        .sum();

        double taxaOcupacao =
                disponivel == 0
                        ? 0.0
                        : Math.min(100.0, (double) ocupados / disponivel * 100.0);

        kpiReceita.setText(
                String.format("€ %.2f", totalReceita.doubleValue())
                        .replace('.', ',')
        );

        kpiCorridas.setText(
                String.valueOf(numCorridas)
        );

        kpiCanceladas.setText(
                numCanceladas + " (" + String.format("%.0f%%", pctCancel) + ")"
        );

        kpiOcupacao.setText(
                String.format("%.1f%%", taxaOcupacao)
        );

        String periodoStr =
                periodoInicio.format(DATE_FMT)
                        + " – "
                        + periodoFim.format(DATE_FMT);

        kpiReceitaSub.setText("reservas + corridas");
        kpiCorridasSub.setText(periodoStr);
        kpiCanceladasSub.setText("de " + totalReservas + " reservas");
        kpiOcupacaoSub.setText(numPistas + " pista(s) no período");
    }

    private void computeChartData(
            List<Reserva> reservas,
            List<Corrida> corridas) {

        chartData =
                new LinkedHashMap<>();

        List<Reserva> ativas =
                reservas.stream()
                        .filter(r -> r.getEstado() != ReservaEstado.CANCELADA)
                        .collect(Collectors.toList());

        switch (periodoTipo) {
            case "semana" -> {
                for (int i = 0; i < 7; i++) {
                    LocalDate date =
                            periodoInicio.plusDays(i);

                    String label =
                            date.getDayOfWeek()
                                    .getDisplayName(
                                            TextStyle.SHORT,
                                            new Locale("pt", "PT")
                                    )
                                    .replace(".", "");

                    chartData.put(
                            label,
                            sumReceita(ativas, corridas, date, date)
                    );
                }
            }

            case "mes" -> {
                LocalDate cursor =
                        periodoInicio;

                int semana =
                        1;

                while (!cursor.isAfter(periodoFim)) {
                    LocalDate semanaFim =
                            cursor.plusDays(6).isAfter(periodoFim)
                                    ? periodoFim
                                    : cursor.plusDays(6);

                    chartData.put(
                            "Sem " + semana,
                            sumReceita(ativas, corridas, cursor, semanaFim)
                    );

                    cursor =
                            semanaFim.plusDays(1);

                    semana++;
                }
            }

            case "ano" -> {
                for (Month month : Month.values()) {
                    LocalDate monthStart =
                            LocalDate.of(periodoInicio.getYear(), month, 1);

                    LocalDate monthEnd =
                            monthStart.with(TemporalAdjusters.lastDayOfMonth());

                    String label =
                            month.getDisplayName(
                                            TextStyle.SHORT,
                                            new Locale("pt", "PT")
                                    )
                                    .replace(".", "");

                    chartData.put(
                            label,
                            sumReceita(ativas, corridas, monthStart, monthEnd)
                    );
                }
            }

            default -> {
            }
        }
    }

    private BigDecimal sumReceita(
            List<Reserva> ativas,
            List<Corrida> corridas,
            LocalDate inicio,
            LocalDate fim) {

        BigDecimal recReservas =
                ativas.stream()
                        .filter(r ->
                                !r.getDataHoraInicio()
                                        .toLocalDate()
                                        .isBefore(inicio)
                        )
                        .filter(r ->
                                !r.getDataHoraInicio()
                                        .toLocalDate()
                                        .isAfter(fim)
                        )
                        .map(r -> r.getKart().getCategoria().getPrecoBase())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal recCorridas =
                corridas.stream()
                        .filter(c ->
                                !c.getDataHoraInicio()
                                        .toLocalDate()
                                        .isBefore(inicio)
                        )
                        .filter(c ->
                                !c.getDataHoraInicio()
                                        .toLocalDate()
                                        .isAfter(fim)
                        )
                        .map(c -> c.getCategoria().getPrecoBase())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        return recReservas.add(recCorridas);
    }

    private void refreshCorridasTable(List<Corrida> corridas) {
        if (corridasModel == null) {
            return;
        }

        corridasModel.setRowCount(0);

        corridas.stream()
                .sorted(Comparator.comparing(Corrida::getDataHoraInicio))
                .forEach(c ->
                        corridasModel.addRow(
                                new Object[]{
                                        c.getDataHoraInicio()
                                                .toLocalDate()
                                                .format(DATE_FMT),
                                        c.getDataHoraInicio()
                                                .toLocalTime()
                                                .format(TIME_FMT),
                                        c.getDuracaoMinutos() + " min",
                                        c.getLayoutNome(),
                                        c.getCategoria().getDescricao(),
                                        c.getCliente().getNome()
                                }
                        )
                );
    }

    private void refreshCanceladasTable(List<Reserva> reservas) {
        if (canceladasModel == null) {
            return;
        }

        canceladasModel.setRowCount(0);

        reservas.stream()
                .filter(r -> r.getEstado() == ReservaEstado.CANCELADA)
                .sorted(Comparator.comparing(Reserva::getDataHoraInicio))
                .forEach(r ->
                        canceladasModel.addRow(
                                new Object[]{
                                        r.getDataHoraInicio()
                                                .toLocalDate()
                                                .format(DATE_FMT),
                                        r.getDataHoraInicio()
                                                .toLocalTime()
                                                .format(TIME_FMT),
                                        r.getDataHoraFim()
                                                .toLocalTime()
                                                .format(TIME_FMT),
                                        r.getCliente().getNome(),
                                        "#" + r.getKart().getNumero(),
                                        r.getPistaNome()
                                }
                        )
                );
    }

    private void refreshOcupacaoTable(List<Reserva> reservas) {
        if (ocupacaoModel == null) {
            return;
        }

        ocupacaoModel.setRowCount(0);

        List<Reserva> ativas =
                reservas.stream()
                        .filter(r -> r.getEstado() != ReservaEstado.CANCELADA)
                        .collect(Collectors.toList());

        long numDias =
                periodoInicio.datesUntil(periodoFim.plusDays(1)).count();

        long disponivel =
                numDias * MINUTOS_DIA;

        ativas.stream()
                .map(Reserva::getPistaNome)
                .distinct()
                .sorted()
                .forEach(pista -> {
                    List<Reserva> daPista =
                            ativas.stream()
                                    .filter(r ->
                                            r.getPistaNome()
                                                    .equalsIgnoreCase(pista)
                                    )
                                    .collect(Collectors.toList());

                    long minOcup =
                            daPista.stream()
                                    .mapToLong(r ->
                                            Duration.between(
                                                    r.getDataHoraInicio(),
                                                    r.getDataHoraFim()
                                            ).toMinutes()
                                    )
                                    .sum();

                    double taxa =
                            disponivel == 0
                                    ? 0.0
                                    : Math.min(
                                            100.0,
                                            (double) minOcup / disponivel * 100.0
                                    );

                    ocupacaoModel.addRow(
                            new Object[]{
                                    pista,
                                    daPista.size(),
                                    minOcup + " min",
                                    disponivel + " min",
                                    String.format("%.1f%%", taxa)
                            }
                    );
                });
    }
}