package pt.kartodromo.desktop.ui.disponibilidade;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import pt.kartodromo.core.bll.ReservaService;
import pt.kartodromo.core.model.Reserva;
import pt.kartodromo.core.model.enums.ReservaEstado;
import pt.kartodromo.desktop.ui.UiStyle;

public class DisponibilidadePanel extends JPanel {

    private static final LocalTime HORA_ABERTURA = LocalTime.of(9, 0);
    private static final LocalTime HORA_FECHO = LocalTime.of(22, 0);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 14);

    private final ReservaService reservaService = new ReservaService();
    private List<Reserva> allReservas = new ArrayList<>();
    private LocalDate currentDate = LocalDate.now();

    // Tab 1 — Períodos Livres
    private JLabel livresDateLabel;
    private JComboBox<String> livresPistaCombo;
    private JPanel livresTimelinePanel;

    // Tab 2 — Períodos Ocupados
    private JLabel ocupadosDateLabel;
    private JComboBox<String> ocupadosPistaCombo;
    private DefaultTableModel ocupadosModel;

    // Tab 3 — Conflitos
    private DefaultTableModel conflitosModel;
    private JLabel conflitosStatusLabel;

    public DisponibilidadePanel() {
        setLayout(new BorderLayout());
        setBackground(UiStyle.BACKGROUND_COLOR);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(FONT_BOLD);
        tabs.setBackground(UiStyle.BACKGROUND_COLOR);

        tabs.addTab("🟢 Períodos Livres", buildLivresTab());
        tabs.addTab("🔴 Períodos Ocupados", buildOcupadosTab());
        tabs.addTab("⚠️ Conflitos", buildConflitosTab());

        tabs.addChangeListener(e -> refreshActiveTab(tabs.getSelectedIndex()));

        add(tabs, BorderLayout.CENTER);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tab 1: Períodos Livres
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildLivresTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(UiStyle.BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel navBar = buildNavBar(
                e -> { currentDate = currentDate.minusDays(1); refreshLivres(); },
                e -> { currentDate = currentDate.plusDays(1); refreshLivres(); },
                e -> { currentDate = LocalDate.now(); refreshLivres(); }
        );
        livresDateLabel = (JLabel) navBar.getComponent(1);

        livresPistaCombo = new JComboBox<>();
        livresPistaCombo.setPreferredSize(new Dimension(170, 28));
        livresPistaCombo.setFont(FONT_NORMAL);
        livresPistaCombo.addActionListener(e -> refreshLivres());

        navBar.add(Box.createHorizontalStrut(16));
        navBar.add(new JLabel("Pista:"));
        navBar.add(livresPistaCombo);

        livresTimelinePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawLivresTimeline((Graphics2D) g);
            }
        };
        livresTimelinePanel.setBackground(Color.WHITE);
        livresTimelinePanel.setPreferredSize(new Dimension(800, 600));

        JScrollPane scroll = new JScrollPane(livresTimelinePanel);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createLineBorder(UiStyle.BORDER_COLOR));

        panel.add(navBar, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void drawLivresTimeline(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = Math.max(livresTimelinePanel.getWidth(), 600);
        int leftMargin = 64;
        int rightMargin = 24;
        int topMargin = 28;
        int timelineWidth = width - leftMargin - rightMargin;
        int barH = 52;
        int totalMinutes = (int) Duration.between(HORA_ABERTURA, HORA_FECHO).toMinutes();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, livresTimelinePanel.getHeight());

        // Title
        g.setFont(FONT_HEADER);
        g.setColor(UiStyle.BLACK);
        String selectedPista = (String) livresPistaCombo.getSelectedItem();
        boolean todasPistas = selectedPista == null || "Todas as pistas".equals(selectedPista);
        String pistaLabel = todasPistas ? "Todas as pistas" : selectedPista;
        g.drawString("Disponibilidade — " + currentDate.format(DATE_FMT) + "  |  " + pistaLabel, leftMargin, topMargin - 6);

        // Occupied reservations for this date/pista
        List<Reserva> filtered = allReservas.stream()
                .filter(r -> r.getEstado() != ReservaEstado.CANCELADA)
                .filter(r -> r.getDataHoraInicio().toLocalDate().equals(currentDate))
                .filter(r -> todasPistas || r.getPistaNome().equalsIgnoreCase(selectedPista))
                .sorted(Comparator.comparing(Reserva::getDataHoraInicio))
                .collect(Collectors.toList());

        // Hour tick marks
        g.setFont(FONT_SMALL);
        g.setColor(new Color(160, 160, 160));
        for (int h = 9; h <= 22; h++) {
            int mins = (h - 9) * 60;
            int x = leftMargin + (int) ((double) mins / totalMinutes * timelineWidth);
            g.drawLine(x, topMargin + 2, x, topMargin + barH + 8);
            g.drawString(String.format("%02d:00", h), x - 14, topMargin + barH + 22);
        }

        // Base bar — all free (green)
        g.setColor(new Color(200, 240, 200));
        g.fillRoundRect(leftMargin, topMargin, timelineWidth, barH, 10, 10);
        g.setColor(new Color(100, 180, 100));
        g.drawRoundRect(leftMargin, topMargin, timelineWidth, barH, 10, 10);

        // Occupied blocks (red overlay)
        for (Reserva r : filtered) {
            LocalTime start = r.getDataHoraInicio().toLocalTime();
            LocalTime end   = r.getDataHoraFim().toLocalTime();
            if (start.isBefore(HORA_ABERTURA)) start = HORA_ABERTURA;
            if (end.isAfter(HORA_FECHO))       end   = HORA_FECHO;

            int startMins = (int) Duration.between(HORA_ABERTURA, start).toMinutes();
            int endMins   = (int) Duration.between(HORA_ABERTURA, end).toMinutes();
            int x = leftMargin + (int) ((double) startMins / totalMinutes * timelineWidth);
            int w = (int) ((double) (endMins - startMins) / totalMinutes * timelineWidth);
            if (w < 2) w = 2;

            g.setColor(new Color(220, 70, 70, 200));
            g.fillRoundRect(x, topMargin, w, barH, 8, 8);
            g.setColor(new Color(170, 30, 30));
            g.drawRoundRect(x, topMargin, w, barH, 8, 8);

            if (w > 36) {
                g.setColor(Color.WHITE);
                g.setFont(new Font("Segoe UI", Font.BOLD, 11));
                String label = r.getCliente().getNome().split(" ")[0];
                if (label.length() > 8) label = label.substring(0, 7) + "…";
                g.drawString(label, x + 4, topMargin + barH / 2 + 4);
                g.setFont(FONT_SMALL);
            }
        }

        // Legend
        int legendY = topMargin + barH + 38;
        g.setFont(FONT_SMALL);
        g.setColor(new Color(200, 240, 200));
        g.fillRect(leftMargin, legendY - 10, 14, 12);
        g.setColor(new Color(100, 180, 100));
        g.drawRect(leftMargin, legendY - 10, 14, 12);
        g.setColor(UiStyle.TEXT_GRAY);
        g.drawString("Livre", leftMargin + 18, legendY);

        g.setColor(new Color(220, 70, 70, 200));
        g.fillRect(leftMargin + 70, legendY - 10, 14, 12);
        g.setColor(new Color(170, 30, 30));
        g.drawRect(leftMargin + 70, legendY - 10, 14, 12);
        g.setColor(UiStyle.TEXT_GRAY);
        g.drawString("Ocupado", leftMargin + 88, legendY);

        // Free periods list
        int y = legendY + 36;
        List<LocalTime[]> freePeriods = computeFreePeriods(filtered);

        g.setFont(FONT_BOLD);
        g.setColor(UiStyle.BLACK);
        g.drawString("Períodos Livres  (" + freePeriods.size() + ")", leftMargin, y);
        y += 6;
        g.setColor(new Color(220, 220, 220));
        g.drawLine(leftMargin, y, leftMargin + timelineWidth, y);
        y += 16;

        if (freePeriods.isEmpty()) {
            g.setFont(FONT_NORMAL);
            g.setColor(UiStyle.TEXT_GRAY);
            g.drawString("Nenhum período livre neste dia para a pista selecionada.", leftMargin, y);
            y += 22;
        } else {
            for (LocalTime[] period : freePeriods) {
                Duration dur = Duration.between(period[0], period[1]);
                long totalMins = dur.toMinutes();
                String durStr = totalMins >= 60
                        ? (totalMins / 60) + "h" + (totalMins % 60 > 0 ? (totalMins % 60) + "m" : "")
                        : totalMins + " min";

                g.setColor(new Color(60, 160, 60));
                g.fillOval(leftMargin, y - 11, 12, 12);
                g.setColor(UiStyle.BLACK);
                g.setFont(FONT_NORMAL);
                g.drawString(
                        period[0].format(TIME_FMT) + " — " + period[1].format(TIME_FMT)
                        + "   (" + durStr + ")",
                        leftMargin + 18, y
                );
                y += 24;
            }
        }

        // Occupied periods list
        y += 8;
        g.setFont(FONT_BOLD);
        g.setColor(UiStyle.BLACK);
        g.drawString("Períodos Ocupados  (" + filtered.size() + ")", leftMargin, y);
        y += 6;
        g.setColor(new Color(220, 220, 220));
        g.drawLine(leftMargin, y, leftMargin + timelineWidth, y);
        y += 16;

        if (filtered.isEmpty()) {
            g.setFont(FONT_NORMAL);
            g.setColor(UiStyle.TEXT_GRAY);
            g.drawString("Nenhuma reserva ativa neste dia.", leftMargin, y);
            y += 22;
        } else {
            for (Reserva r : filtered) {
                g.setColor(new Color(200, 50, 50));
                g.fillOval(leftMargin, y - 11, 12, 12);
                g.setColor(UiStyle.BLACK);
                g.setFont(FONT_NORMAL);
                g.drawString(
                        r.getDataHoraInicio().toLocalTime().format(TIME_FMT)
                        + " — " + r.getDataHoraFim().toLocalTime().format(TIME_FMT)
                        + "   " + r.getCliente().getNome()
                        + "  |  Kart #" + r.getKart().getNumero()
                        + "  |  " + r.getPistaNome()
                        + "  [" + r.getEstado() + "]",
                        leftMargin + 18, y
                );
                y += 24;
            }
        }

        // Resize if needed
        int needed = y + 32;
        if (needed > livresTimelinePanel.getPreferredSize().height) {
            livresTimelinePanel.setPreferredSize(new Dimension(width, needed));
            livresTimelinePanel.revalidate();
        }
    }

    private List<LocalTime[]> computeFreePeriods(List<Reserva> occupied) {
        List<LocalTime[]> intervals = occupied.stream()
                .map(r -> new LocalTime[]{
            r.getDataHoraInicio().toLocalTime().isBefore(HORA_ABERTURA)
            ? HORA_ABERTURA : r.getDataHoraInicio().toLocalTime(),
            r.getDataHoraFim().toLocalTime().isAfter(HORA_FECHO)
            ? HORA_FECHO : r.getDataHoraFim().toLocalTime()
        })
                .sorted(Comparator.comparing(a -> a[0]))
                .collect(Collectors.toList());

        // Merge overlapping intervals
        List<LocalTime[]> merged = new ArrayList<>();
        for (LocalTime[] interval : intervals) {
            if (merged.isEmpty() || !merged.get(merged.size() - 1)[1].isAfter(interval[0])) {
                merged.add(new LocalTime[]{interval[0], interval[1]});
            } else if (interval[1].isAfter(merged.get(merged.size() - 1)[1])) {
                merged.get(merged.size() - 1)[1] = interval[1];
            }
        }

        // Gaps = free periods
        List<LocalTime[]> free = new ArrayList<>();
        LocalTime cursor = HORA_ABERTURA;
        for (LocalTime[] m : merged) {
            if (cursor.isBefore(m[0])) {
                free.add(new LocalTime[]{cursor, m[0]});
            }
            if (m[1].isAfter(cursor)) {
                cursor = m[1];
            }
        }
        if (cursor.isBefore(HORA_FECHO)) {
            free.add(new LocalTime[]{cursor, HORA_FECHO});
        }
        return free;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tab 2: Períodos Ocupados
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildOcupadosTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(UiStyle.BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel navBar = buildNavBar(
                e -> { currentDate = currentDate.minusDays(1); refreshOcupados(); },
                e -> { currentDate = currentDate.plusDays(1); refreshOcupados(); },
                e -> { currentDate = LocalDate.now(); refreshOcupados(); }
        );
        ocupadosDateLabel = (JLabel) navBar.getComponent(1);

        ocupadosPistaCombo = new JComboBox<>();
        ocupadosPistaCombo.setPreferredSize(new Dimension(170, 28));
        ocupadosPistaCombo.setFont(FONT_NORMAL);
        ocupadosPistaCombo.addActionListener(e -> refreshOcupados());

        navBar.add(Box.createHorizontalStrut(16));
        navBar.add(new JLabel("Pista:"));
        navBar.add(ocupadosPistaCombo);

        String[] cols = {"Início", "Fim", "Duração", "Cliente", "Kart", "Pista", "Estado"};
        ocupadosModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable table = new JTable(ocupadosModel);
        UiStyle.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Color rows by estado
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) {
                    String estado = (String) t.getValueAt(row, 6);
                    if ("CANCELADA".equals(estado)) {
                        c.setBackground(new Color(245, 245, 245));
                        c.setForeground(Color.GRAY);
                    } else if ("CONFIRMADA".equals(estado)) {
                        c.setBackground(new Color(240, 255, 240));
                        c.setForeground(UiStyle.BLACK);
                    } else {
                        c.setBackground(new Color(255, 250, 235));
                        c.setForeground(UiStyle.BLACK);
                    }
                }
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UiStyle.BORDER_COLOR));

        panel.add(navBar, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tab 3: Conflitos
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildConflitosTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(UiStyle.BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topBar.setBackground(UiStyle.BACKGROUND_COLOR);

        conflitosStatusLabel = new JLabel("A verificar...");
        conflitosStatusLabel.setFont(FONT_BOLD);

        JButton refreshBtn = UiStyle.createPrimaryButton("↺  Atualizar");
        refreshBtn.addActionListener(e -> {
            try {
                allReservas = reservaService.listarReservas();
            } catch (Exception ex) {
                allReservas = new ArrayList<>();
            }
            refreshConflitos();
        });

        topBar.add(conflitosStatusLabel);
        topBar.add(refreshBtn);

        String[] cols = {"Tipo", "ID Res. A", "ID Res. B", "Cliente A", "Cliente B", "Período A", "Período B", "Recurso"};
        conflitosModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable table = new JTable(conflitosModel);
        UiStyle.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) {
                    String tipo = (String) t.getValueAt(row, 0);
                    if (tipo != null && tipo.contains("Pista") && tipo.contains("Kart")) {
                        c.setBackground(new Color(255, 220, 220));
                    } else {
                        c.setBackground(new Color(255, 240, 235));
                    }
                    c.setForeground(UiStyle.BLACK);
                }
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UiStyle.BORDER_COLOR));

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Navigation bar helper
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildNavBar(
            java.awt.event.ActionListener prevAction,
            java.awt.event.ActionListener nextAction,
            java.awt.event.ActionListener todayAction) {

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        bar.setBackground(UiStyle.BACKGROUND_COLOR);

        JButton prev = new JButton("◀");
        prev.setFont(FONT_NORMAL);
        prev.addActionListener(prevAction);

        JLabel dateLabel = new JLabel("", SwingConstants.CENTER);
        dateLabel.setFont(FONT_BOLD);
        dateLabel.setPreferredSize(new Dimension(120, 28));

        JButton next = new JButton("▶");
        next.setFont(FONT_NORMAL);
        next.addActionListener(nextAction);

        JButton today = new JButton("Hoje");
        today.setFont(FONT_NORMAL);
        today.addActionListener(todayAction);

        bar.add(prev);
        bar.add(dateLabel);
        bar.add(next);
        bar.add(today);

        return bar;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Refresh logic
    // ─────────────────────────────────────────────────────────────────────────

    public void refreshData() {
        try {
            allReservas = reservaService.listarReservas();
        } catch (Exception e) {
            allReservas = new ArrayList<>();
        }
        syncPistaCombos();
        refreshLivres();
        refreshOcupados();
        refreshConflitos();
    }

    private void refreshActiveTab(int idx) {
        try {
            allReservas = reservaService.listarReservas();
        } catch (Exception e) {
            allReservas = new ArrayList<>();
        }
        syncPistaCombos();
        switch (idx) {
            case 0 -> refreshLivres();
            case 1 -> refreshOcupados();
            case 2 -> refreshConflitos();
        }
    }

    private void syncPistaCombos() {
        List<String> pistas = allReservas.stream()
                .map(Reserva::getPistaNome)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        String selLivres   = (String) livresPistaCombo.getSelectedItem();
        String selOcupados = (String) ocupadosPistaCombo.getSelectedItem();

        livresPistaCombo.removeAllItems();
        ocupadosPistaCombo.removeAllItems();
        livresPistaCombo.addItem("Todas as pistas");
        ocupadosPistaCombo.addItem("Todas as pistas");

        for (String p : pistas) {
            livresPistaCombo.addItem(p);
            ocupadosPistaCombo.addItem(p);
        }

        if (selLivres != null)   livresPistaCombo.setSelectedItem(selLivres);
        if (selOcupados != null) ocupadosPistaCombo.setSelectedItem(selOcupados);
    }

    private void refreshLivres() {
        if (livresDateLabel != null) {
            livresDateLabel.setText(currentDate.format(DATE_FMT));
        }
        if (livresTimelinePanel != null) {
            livresTimelinePanel.setPreferredSize(new Dimension(800, 600));
            livresTimelinePanel.repaint();
        }
    }

    private void refreshOcupados() {
        if (ocupadosDateLabel != null) {
            ocupadosDateLabel.setText(currentDate.format(DATE_FMT));
        }
        if (ocupadosModel == null) return;

        ocupadosModel.setRowCount(0);
        String sel = (String) ocupadosPistaCombo.getSelectedItem();
        boolean todas = sel == null || "Todas as pistas".equals(sel);

        allReservas.stream()
                .filter(r -> r.getEstado() != ReservaEstado.CANCELADA)
                .filter(r -> r.getDataHoraInicio().toLocalDate().equals(currentDate))
                .filter(r -> todas || r.getPistaNome().equalsIgnoreCase(sel))
                .sorted(Comparator.comparing(Reserva::getDataHoraInicio))
                .forEach(r -> {
                    long mins = Duration.between(r.getDataHoraInicio(), r.getDataHoraFim()).toMinutes();
                    String durStr = mins >= 60
                            ? (mins / 60) + "h" + (mins % 60 > 0 ? (mins % 60) + "m" : "")
                            : mins + " min";
                    ocupadosModel.addRow(new Object[]{
                        r.getDataHoraInicio().toLocalTime().format(TIME_FMT),
                        r.getDataHoraFim().toLocalTime().format(TIME_FMT),
                        durStr,
                        r.getCliente().getNome(),
                        "#" + r.getKart().getNumero(),
                        r.getPistaNome(),
                        r.getEstado().name()
                    });
                });
    }

    private void refreshConflitos() {
        if (conflitosModel == null) return;
        conflitosModel.setRowCount(0);

        List<Reserva> active = allReservas.stream()
                .filter(r -> r.getEstado() != ReservaEstado.CANCELADA)
                .collect(Collectors.toList());

        int count = 0;
        for (int i = 0; i < active.size(); i++) {
            for (int j = i + 1; j < active.size(); j++) {
                Reserva a = active.get(i);
                Reserva b = active.get(j);

                if (!overlaps(a, b)) continue;

                boolean pistaConflito = a.getPistaNome().equalsIgnoreCase(b.getPistaNome());
                boolean kartConflito  = a.getKart().getId().equals(b.getKart().getId());

                if (!pistaConflito && !kartConflito) continue;

                String tipo = (kartConflito && pistaConflito) ? "Pista + Kart"
                        : kartConflito ? "Kart" : "Pista";
                String recurso = kartConflito
                        ? "Kart #" + a.getKart().getNumero()
                        : a.getPistaNome();

                conflitosModel.addRow(new Object[]{
                    tipo,
                    "#" + a.getId(),
                    "#" + b.getId(),
                    a.getCliente().getNome(),
                    b.getCliente().getNome(),
                    a.getDataHoraInicio().format(DT_FMT) + "–" + a.getDataHoraFim().toLocalTime().format(TIME_FMT),
                    b.getDataHoraInicio().format(DT_FMT) + "–" + b.getDataHoraFim().toLocalTime().format(TIME_FMT),
                    recurso
                });
                count++;
            }
        }

        if (count == 0) {
            conflitosStatusLabel.setText("✅  Nenhum conflito de reservas encontrado");
            conflitosStatusLabel.setForeground(new Color(30, 140, 30));
        } else {
            conflitosStatusLabel.setText("⚠️  " + count + " conflito(s) detectado(s)");
            conflitosStatusLabel.setForeground(UiStyle.DELETE_RED);
        }
    }

    private boolean overlaps(Reserva a, Reserva b) {
        return a.getDataHoraInicio().isBefore(b.getDataHoraFim())
                && a.getDataHoraFim().isAfter(b.getDataHoraInicio());
    }
}
