package pt.kartodromo.desktop.ui.calendario;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import pt.kartodromo.core.bll.ReservaService;
import pt.kartodromo.core.model.Reserva;
import pt.kartodromo.core.model.enums.ReservaEstado;
import pt.kartodromo.desktop.ui.UiStyle;

public class CalendarioPanel extends JPanel {

    private static final int SLOT_HEIGHT = 38;
    private static final LocalTime HORA_ABERTURA = LocalTime.of(9, 0);
    private static final LocalTime HORA_FECHO = LocalTime.of(22, 0);
    private static final int TOTAL_SLOTS = (int) Duration.between(HORA_ABERTURA, HORA_FECHO).toMinutes() / 30;
    private static final int TIME_LABEL_WIDTH = 62;

    private static final Color[] PISTA_COLORS = {
        new Color(66, 133, 244),
        new Color(52, 168, 83),
        new Color(251, 152, 0),
        new Color(103, 58, 183),
        new Color(0, 150, 136),
        new Color(233, 30, 99),
    };

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ReservaService reservaService = new ReservaService();
    private List<Reserva> allReservas = new ArrayList<>();
    private LocalDate currentDate = LocalDate.now();

    // Diária
    private JLabel diariaTitleLabel;
    private TimelinePanel timelinePanel;

    // Semanal
    private JLabel semanalTitleLabel;
    private JPanel semanalGrid;

    // Mensal
    private JLabel mensalTitleLabel;
    private JPanel mensalGrid;

    // Por Pista
    private JComboBox<String> pistaCombo;
    private DefaultTableModel pistaTableModel;

    private final JPanel[] tabContents = new JPanel[4];
    private final javax.swing.JTabbedPane tabs = new javax.swing.JTabbedPane();

    public CalendarioPanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(UiStyle.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));

        tabContents[0] = buildDiariaTab();
        tabContents[1] = buildSemanalTab();
        tabContents[2] = buildMensalTab();
        tabContents[3] = buildPorPistaTab();

        tabs.addTab("Diária", tabContents[0]);
        tabs.addTab("Semanal", tabContents[1]);
        tabs.addTab("Mensal", tabContents[2]);
        tabs.addTab("Por Pista", tabContents[3]);

        tabs.addChangeListener(e -> refreshCurrentView());

        add(UiStyle.createPageTitle("Calendário"), BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    // ─────────────────────────────────────────────────────────────
    // DIÁRIA
    // ─────────────────────────────────────────────────────────────

    private JPanel buildDiariaTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UiStyle.BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        diariaTitleLabel = new JLabel("", JLabel.CENTER);
        diariaTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JButton prev = UiStyle.createActionButton("◀", UiStyle.CLEAR_GRAY);
        JButton next = UiStyle.createActionButton("▶", UiStyle.CLEAR_GRAY);
        JButton hoje = UiStyle.createActionButton("Hoje", UiStyle.PRIMARY_RED);

        prev.addActionListener(e -> { currentDate = currentDate.minusDays(1); refreshDiaria(); });
        next.addActionListener(e -> { currentDate = currentDate.plusDays(1); refreshDiaria(); });
        hoje.addActionListener(e -> { currentDate = LocalDate.now(); refreshDiaria(); });

        panel.add(buildNavPanel(prev, hoje, next, diariaTitleLabel), BorderLayout.NORTH);

        timelinePanel = new TimelinePanel();
        JScrollPane scroll = new JScrollPane(timelinePanel);
        scroll.setBorder(BorderFactory.createLineBorder(UiStyle.BORDER_COLOR));
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void refreshDiaria() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", new Locale("pt", "PT"));
        String title = currentDate.format(fmt);
        diariaTitleLabel.setText(title.substring(0, 1).toUpperCase() + title.substring(1));

        List<Reserva> day = allReservas.stream()
                .filter(r -> r.getDataHoraInicio().toLocalDate().equals(currentDate))
                .sorted(Comparator.comparing(Reserva::getDataHoraInicio))
                .collect(Collectors.toList());

        timelinePanel.setReservas(day);
        timelinePanel.revalidate();
        timelinePanel.repaint();
    }

    private class TimelinePanel extends JPanel {

        private List<Reserva> reservas = new ArrayList<>();

        TimelinePanel() {
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(600, TOTAL_SLOTS * SLOT_HEIGHT + 10));
        }

        void setReservas(List<Reserva> r) {
            this.reservas = r;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int cx = TIME_LABEL_WIDTH + 8;
            int cw = w - cx - 8;

            // Grid rows
            for (int slot = 0; slot <= TOTAL_SLOTS; slot++) {
                int y = slot * SLOT_HEIGHT;
                LocalTime t = HORA_ABERTURA.plusMinutes(slot * 30L);

                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                g2.setColor(UiStyle.TEXT_GRAY);
                g2.drawString(t.format(TIME_FMT), 4, y + 14);

                if (slot < TOTAL_SLOTS) {
                    g2.setColor(slot % 2 == 0 ? Color.WHITE : new Color(250, 250, 250));
                    g2.fillRect(cx, y, cw, SLOT_HEIGHT);
                }

                g2.setColor(slot % 2 == 0 ? new Color(225, 225, 225) : new Color(238, 238, 238));
                g2.drawLine(cx - 4, y, w - 8, y);
            }

            if (reservas.isEmpty()) {
                g2.setFont(new Font("Segoe UI", Font.ITALIC, 14));
                g2.setColor(UiStyle.TEXT_GRAY);
                String msg = "Sem reservas para este dia";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(msg, cx + (cw - fm.stringWidth(msg)) / 2, TOTAL_SLOTS * SLOT_HEIGHT / 2);
                return;
            }

            Map<String, Integer> pistaColorMap = buildPistaColorMap(reservas);

            for (Reserva r : reservas) {
                long startMin = Duration.between(HORA_ABERTURA, r.getDataHoraInicio().toLocalTime()).toMinutes();
                long endMin = Duration.between(HORA_ABERTURA, r.getDataHoraFim().toLocalTime()).toMinutes();
                startMin = Math.max(0, startMin);
                endMin = Math.min(TOTAL_SLOTS * 30L, endMin);
                if (endMin <= startMin) continue;

                int y = (int) (startMin * SLOT_HEIGHT / 30);
                int h = (int) ((endMin - startMin) * SLOT_HEIGHT / 30);

                Color base = r.getEstado() == ReservaEstado.CANCELADA
                        ? new Color(160, 160, 160)
                        : PISTA_COLORS[pistaColorMap.getOrDefault(r.getPistaNome(), 0)];

                // background fill
                g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 35));
                g2.fillRoundRect(cx + 4, y + 2, cw - 8, h - 4, 10, 10);

                // border
                g2.setColor(base);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(cx + 4, y + 2, cw - 8, h - 4, 10, 10);
                g2.setStroke(new BasicStroke(1));

                // left bar
                g2.fillRoundRect(cx + 4, y + 2, 6, h - 4, 4, 4);

                // time
                g2.setColor(base.darker());
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g2.drawString(
                        r.getDataHoraInicio().format(TIME_FMT) + " – " + r.getDataHoraFim().format(TIME_FMT),
                        cx + 14, y + 16
                );

                if (h > 28) {
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    g2.setColor(UiStyle.TEXT_GRAY);
                    String info = r.getCliente().getNome()
                            + "  ·  Kart #" + r.getKart().getNumero()
                            + "  ·  " + r.getPistaNome();
                    FontMetrics fm = g2.getFontMetrics();
                    while (fm.stringWidth(info) > cw - 22 && info.length() > 10) {
                        info = info.substring(0, info.length() - 4) + "...";
                    }
                    g2.drawString(info, cx + 14, y + 30);
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // SEMANAL
    // ─────────────────────────────────────────────────────────────

    private JPanel buildSemanalTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UiStyle.BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        semanalTitleLabel = new JLabel("", JLabel.CENTER);
        semanalTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JButton prev = UiStyle.createActionButton("◀", UiStyle.CLEAR_GRAY);
        JButton next = UiStyle.createActionButton("▶", UiStyle.CLEAR_GRAY);
        JButton hoje = UiStyle.createActionButton("Esta semana", UiStyle.PRIMARY_RED);

        prev.addActionListener(e -> { currentDate = currentDate.minusWeeks(1); refreshSemanal(); });
        next.addActionListener(e -> { currentDate = currentDate.plusWeeks(1); refreshSemanal(); });
        hoje.addActionListener(e -> { currentDate = LocalDate.now(); refreshSemanal(); });

        panel.add(buildNavPanel(prev, hoje, next, semanalTitleLabel), BorderLayout.NORTH);

        semanalGrid = new JPanel(new GridLayout(1, 7, 4, 0));
        semanalGrid.setBackground(UiStyle.BACKGROUND_COLOR);

        JScrollPane scroll = new JScrollPane(semanalGrid);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UiStyle.BACKGROUND_COLOR);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void refreshSemanal() {
        LocalDate weekStart = currentDate.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);
        DateTimeFormatter shortFmt = DateTimeFormatter.ofPattern("dd/MM");
        semanalTitleLabel.setText(
                "Semana " + weekStart.format(shortFmt) + " – " + weekEnd.format(shortFmt) + " / " + weekStart.getYear()
        );

        semanalGrid.removeAll();
        String[] dayNames = {"Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom"};

        for (int d = 0; d < 7; d++) {
            LocalDate day = weekStart.plusDays(d);
            List<Reserva> dayRes = allReservas.stream()
                    .filter(r -> r.getDataHoraInicio().toLocalDate().equals(day))
                    .sorted(Comparator.comparing(Reserva::getDataHoraInicio))
                    .collect(Collectors.toList());
            semanalGrid.add(buildDayColumn(dayNames[d], day, dayRes));
        }

        semanalGrid.revalidate();
        semanalGrid.repaint();
    }

    private JPanel buildDayColumn(String dayName, LocalDate date, List<Reserva> reservas) {
        boolean isToday = date.equals(LocalDate.now());

        JPanel col = new JPanel(new BorderLayout(0, 4));
        col.setBackground(UiStyle.BACKGROUND_COLOR);

        // Header
        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setBackground(isToday ? UiStyle.PRIMARY_RED : UiStyle.CARD_COLOR);
        header.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));

        JLabel nameLabel = new JLabel(dayName, JLabel.CENTER);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        nameLabel.setForeground(isToday ? Color.WHITE : UiStyle.TEXT_GRAY);

        JLabel dateLabel = new JLabel(date.format(DateTimeFormatter.ofPattern("dd/MM")), JLabel.CENTER);
        dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        dateLabel.setForeground(isToday ? Color.WHITE : UiStyle.BLACK);

        header.add(nameLabel);
        header.add(dateLabel);

        // Reservations list
        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(UiStyle.CARD_COLOR);
        list.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));

        if (reservas.isEmpty()) {
            JLabel empty = new JLabel("Sem reservas");
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            empty.setForeground(UiStyle.TEXT_GRAY);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            list.add(Box.createVerticalStrut(8));
            list.add(empty);
        } else {
            Map<String, Integer> colorMap = buildPistaColorMap(reservas);
            for (Reserva r : reservas) {
                list.add(buildReservaChip(r, colorMap));
                list.add(Box.createVerticalStrut(4));
            }
        }

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createLineBorder(UiStyle.BORDER_COLOR));

        col.add(header, BorderLayout.NORTH);
        col.add(scroll, BorderLayout.CENTER);
        return col;
    }

    private JPanel buildReservaChip(Reserva r, Map<String, Integer> colorMap) {
        Color color = r.getEstado() == ReservaEstado.CANCELADA
                ? new Color(150, 150, 150)
                : PISTA_COLORS[colorMap.getOrDefault(r.getPistaNome(), 0)];

        JPanel chip = new JPanel(new GridLayout(3, 1, 0, 1));
        chip.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        chip.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 25));
        chip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, color),
                BorderFactory.createEmptyBorder(4, 6, 4, 4)
        ));

        JLabel time = new JLabel(r.getDataHoraInicio().format(TIME_FMT) + "–" + r.getDataHoraFim().format(TIME_FMT));
        time.setFont(new Font("Segoe UI", Font.BOLD, 11));
        time.setForeground(color.darker());

        JLabel client = new JLabel(r.getCliente().getNome());
        client.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        client.setForeground(UiStyle.BLACK);

        JLabel pista = new JLabel(r.getPistaNome() + "  ·  Kart #" + r.getKart().getNumero());
        pista.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        pista.setForeground(UiStyle.TEXT_GRAY);

        chip.add(time);
        chip.add(client);
        chip.add(pista);
        return chip;
    }

    // ─────────────────────────────────────────────────────────────
    // MENSAL
    // ─────────────────────────────────────────────────────────────

    private JPanel buildMensalTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UiStyle.BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        mensalTitleLabel = new JLabel("", JLabel.CENTER);
        mensalTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JButton prev = UiStyle.createActionButton("◀", UiStyle.CLEAR_GRAY);
        JButton next = UiStyle.createActionButton("▶", UiStyle.CLEAR_GRAY);
        JButton hoje = UiStyle.createActionButton("Este mês", UiStyle.PRIMARY_RED);

        prev.addActionListener(e -> { currentDate = currentDate.minusMonths(1); refreshMensal(); });
        next.addActionListener(e -> { currentDate = currentDate.plusMonths(1); refreshMensal(); });
        hoje.addActionListener(e -> { currentDate = LocalDate.now(); refreshMensal(); });

        panel.add(buildNavPanel(prev, hoje, next, mensalTitleLabel), BorderLayout.NORTH);

        mensalGrid = new JPanel(new BorderLayout(0, 2));
        mensalGrid.setBackground(UiStyle.BACKGROUND_COLOR);

        JScrollPane scroll = new JScrollPane(mensalGrid);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UiStyle.BACKGROUND_COLOR);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void refreshMensal() {
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("pt", "PT"));
        String title = currentDate.format(monthFmt);
        mensalTitleLabel.setText(title.substring(0, 1).toUpperCase() + title.substring(1));

        mensalGrid.removeAll();

        // Day headers row
        String[] dayNames = {"Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom"};
        JPanel headerRow = new JPanel(new GridLayout(1, 7, 2, 0));
        headerRow.setBackground(UiStyle.BACKGROUND_COLOR);
        for (String dn : dayNames) {
            JLabel lbl = new JLabel(dn, JLabel.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setForeground(UiStyle.TEXT_GRAY);
            lbl.setOpaque(true);
            lbl.setBackground(UiStyle.CARD_COLOR);
            lbl.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
            headerRow.add(lbl);
        }

        LocalDate firstOfMonth = currentDate.withDayOfMonth(1);
        int startOffset = firstOfMonth.getDayOfWeek().getValue() - 1; // Mon=0
        int daysInMonth = currentDate.lengthOfMonth();
        int totalCells = startOffset + daysInMonth;
        int rows = (int) Math.ceil(totalCells / 7.0);

        JPanel grid = new JPanel(new GridLayout(rows, 7, 2, 2));
        grid.setBackground(UiStyle.BACKGROUND_COLOR);

        for (int cell = 0; cell < rows * 7; cell++) {
            int dayNum = cell - startOffset + 1;
            if (dayNum < 1 || dayNum > daysInMonth) {
                JPanel empty = new JPanel();
                empty.setBackground(new Color(242, 242, 242));
                grid.add(empty);
            } else {
                LocalDate day = currentDate.withDayOfMonth(dayNum);
                List<Reserva> dayRes = allReservas.stream()
                        .filter(r -> r.getDataHoraInicio().toLocalDate().equals(day))
                        .sorted(Comparator.comparing(Reserva::getDataHoraInicio))
                        .collect(Collectors.toList());
                grid.add(buildMonthCell(day, dayRes));
            }
        }

        mensalGrid.add(headerRow, BorderLayout.NORTH);
        mensalGrid.add(grid, BorderLayout.CENTER);
        mensalGrid.revalidate();
        mensalGrid.repaint();
    }

    private JPanel buildMonthCell(LocalDate date, List<Reserva> reservas) {
        boolean isToday = date.equals(LocalDate.now());

        JPanel cell = new JPanel(new BorderLayout(0, 2));
        cell.setBackground(UiStyle.CARD_COLOR);
        cell.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(isToday ? UiStyle.PRIMARY_RED : UiStyle.BORDER_COLOR, isToday ? 2 : 1),
                BorderFactory.createEmptyBorder(3, 5, 3, 3)
        ));
        cell.setPreferredSize(new Dimension(0, 90));

        JLabel dayLbl = new JLabel(String.valueOf(date.getDayOfMonth()));
        dayLbl.setFont(new Font("Segoe UI", isToday ? Font.BOLD : Font.PLAIN, 13));
        dayLbl.setForeground(isToday ? UiStyle.PRIMARY_RED : UiStyle.BLACK);
        cell.add(dayLbl, BorderLayout.NORTH);

        JPanel items = new JPanel();
        items.setLayout(new BoxLayout(items, BoxLayout.Y_AXIS));
        items.setOpaque(false);

        Map<String, Integer> colorMap = buildPistaColorMap(reservas);
        int shown = Math.min(reservas.size(), 3);
        for (int i = 0; i < shown; i++) {
            Reserva r = reservas.get(i);
            Color c = r.getEstado() == ReservaEstado.CANCELADA
                    ? new Color(150, 150, 150)
                    : PISTA_COLORS[colorMap.getOrDefault(r.getPistaNome(), 0)];

            JLabel chip = new JLabel("● " + r.getDataHoraInicio().format(TIME_FMT) + " " + r.getCliente().getNome());
            chip.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            chip.setForeground(c);
            chip.setMaximumSize(new Dimension(Integer.MAX_VALUE, 14));
            items.add(chip);
        }
        if (reservas.size() > 3) {
            JLabel more = new JLabel("+" + (reservas.size() - 3) + " mais");
            more.setFont(new Font("Segoe UI", Font.BOLD, 10));
            more.setForeground(UiStyle.TEXT_GRAY);
            items.add(more);
        }
        cell.add(items, BorderLayout.CENTER);
        return cell;
    }

    // ─────────────────────────────────────────────────────────────
    // POR PISTA
    // ─────────────────────────────────────────────────────────────

    private JPanel buildPorPistaTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(UiStyle.BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        pistaCombo = new JComboBox<>();
        pistaCombo.setFont(UiStyle.labelFont());
        pistaCombo.addActionListener(e -> refreshPorPista());

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        header.setOpaque(false);
        JLabel lbl = new JLabel("Pista:");
        lbl.setFont(UiStyle.labelFont());
        lbl.setForeground(UiStyle.BLACK);
        header.add(lbl);
        header.add(pistaCombo);

        pistaTableModel = new DefaultTableModel(
                new Object[]{"Início", "Fim", "Cliente", "Kart", "Pista", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        JTable table = new JTable(pistaTableModel);
        UiStyle.styleTable(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UiStyle.BORDER_COLOR));

        panel.add(header, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void refreshPorPista() {
        if (pistaTableModel == null) return;
        String selected = (String) pistaCombo.getSelectedItem();
        pistaTableModel.setRowCount(0);
        if (selected == null) return;

        allReservas.stream()
                .filter(r -> "Todas as pistas".equals(selected) || r.getPistaNome().equals(selected))
                .sorted(Comparator.comparing(Reserva::getDataHoraInicio))
                .forEach(r -> pistaTableModel.addRow(new Object[]{
                    r.getDataHoraInicio().format(DT_FMT),
                    r.getDataHoraFim().format(DT_FMT),
                    r.getCliente().getNome(),
                    "Kart #" + r.getKart().getNumero(),
                    r.getPistaNome(),
                    r.getEstado().name()
                }));
    }

    private void updatePistaCombo() {
        if (pistaCombo == null) return;
        String selected = (String) pistaCombo.getSelectedItem();
        pistaCombo.removeAllItems();
        pistaCombo.addItem("Todas as pistas");
        allReservas.stream()
                .map(Reserva::getPistaNome)
                .distinct()
                .sorted()
                .forEach(pistaCombo::addItem);
        if (selected != null) pistaCombo.setSelectedItem(selected);
    }

    // ─────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────

    private JPanel buildNavPanel(JButton prev, JButton today, JButton next, JLabel title) {
        JPanel nav = new JPanel(new BorderLayout(8, 0));
        nav.setOpaque(false);
        nav.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btns.setOpaque(false);
        btns.add(prev);
        btns.add(today);
        btns.add(next);

        nav.add(btns, BorderLayout.WEST);
        nav.add(title, BorderLayout.CENTER);
        return nav;
    }

    private Map<String, Integer> buildPistaColorMap(List<Reserva> reservas) {
        Map<String, Integer> map = new HashMap<>();
        int idx = 0;
        for (Reserva r : reservas) {
            if (!map.containsKey(r.getPistaNome())) {
                map.put(r.getPistaNome(), idx % PISTA_COLORS.length);
                idx++;
            }
        }
        return map;
    }

    private void refreshCurrentView() {
        try {
            allReservas = reservaService.listarReservas();
        } catch (Exception e) {
            allReservas = new ArrayList<>();
        }
        int idx = tabs.getSelectedIndex();
        switch (idx) {
            case 0 -> refreshDiaria();
            case 1 -> refreshSemanal();
            case 2 -> refreshMensal();
            case 3 -> { updatePistaCombo(); refreshPorPista(); }
        }
    }

    public void refreshData() {
        refreshCurrentView();
    }
}
