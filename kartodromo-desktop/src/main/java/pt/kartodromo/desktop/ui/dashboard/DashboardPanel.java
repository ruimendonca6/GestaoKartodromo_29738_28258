package pt.kartodromo.desktop.ui.dashboard;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;

import pt.kartodromo.core.bll.CategoriaKartService;
import pt.kartodromo.core.bll.ClienteService;
import pt.kartodromo.core.bll.CorridaService;
import pt.kartodromo.core.bll.KartService;
import pt.kartodromo.core.bll.ReservaService;
import pt.kartodromo.core.model.Corrida;
import pt.kartodromo.core.model.Reserva;
import pt.kartodromo.core.model.enums.ReservaEstado;

public class DashboardPanel extends JPanel {

    private static final String BACKGROUND_IMAGE_PATH = "/images/dashboardimage2.png";
    private static final Color TEXT_COLOR = new Color(33, 33, 33);
    private static final Color HEADER_TEXT_COLOR = Color.WHITE;
    private static final Color SUBTITLE_COLOR = new Color(210, 210, 210);
    private static final Color TODAY_BAR_BG = new Color(0, 0, 0, 110);

    private final ClienteService clienteService = new ClienteService();
    private final CategoriaKartService categoriaService = new CategoriaKartService();
    private final KartService kartService = new KartService();
    private final CorridaService corridaService = new CorridaService();
    private final ReservaService reservaService = new ReservaService();

    // KPI value labels
    private final JLabel clientesValue = new JLabel("0", SwingConstants.CENTER);
    private final JLabel categoriasValue = new JLabel("0", SwingConstants.CENTER);
    private final JLabel kartsValue = new JLabel("0", SwingConstants.CENTER);
    private final JLabel corridasValue = new JLabel("0", SwingConstants.CENTER);
    private final JLabel reservasValue = new JLabel("0", SwingConstants.CENTER);
    private final JLabel pendenteValue = new JLabel("0", SwingConstants.CENTER);

    // KPI subtitle labels (dynamic context)
    private final JLabel clientesSub = new JLabel("registados", SwingConstants.CENTER);
    private final JLabel categoriasSub = new JLabel("disponíveis", SwingConstants.CENTER);
    private final JLabel kartsSub = new JLabel("na frota", SwingConstants.CENTER);
    private final JLabel corridasSub = new JLabel("realizadas", SwingConstants.CENTER);
    private final JLabel reservasSub = new JLabel("no total", SwingConstants.CENTER);
    private final JLabel pendenteSub = new JLabel("a aguardar", SwingConstants.CENTER);

    // Today strip labels
    private final JLabel todayCorridas = new JLabel("—");
    private final JLabel todayReservas = new JLabel("—");
    private final JLabel todayConfirmadas = new JLabel("—");
    private final JLabel todayCanceladas = new JLabel("—");

    public DashboardPanel() {
        setLayout(new BorderLayout());

        BackgroundPanel backgroundPanel = new BackgroundPanel(BACKGROUND_IMAGE_PATH);
        backgroundPanel.setLayout(new BorderLayout(0, 16));
        backgroundPanel.setBorder(BorderFactory.createEmptyBorder(28, 28, 20, 28));

        backgroundPanel.add(buildHeaderPanel(), BorderLayout.NORTH);
        backgroundPanel.add(buildCardsPanel(), BorderLayout.CENTER);
        backgroundPanel.add(buildTodayStrip(), BorderLayout.SOUTH);

        add(backgroundPanel, BorderLayout.CENTER);

        refreshData();
    }

    // ─── Header ──────────────────────────────────────────────────────────────

    private JPanel buildHeaderPanel() {
        JLabel title = new JLabel("Dashboard do Kartódromo");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(HEADER_TEXT_COLOR);

        String today = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy",
                        new java.util.Locale("pt", "PT")));

        JLabel subtitle = new JLabel("Resumo geral  ·  " + today);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(SUBTITLE_COLOR);

        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 4));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        panel.add(title);
        panel.add(subtitle);
        return panel;
    }

    // ─── KPI Cards ───────────────────────────────────────────────────────────

    private JPanel buildCardsPanel() {
        JPanel grid = new JPanel(new GridLayout(2, 3, 18, 18));
        grid.setOpaque(false);

        grid.add(createCard("Clientes",    clientesValue,  clientesSub,  new Color(33, 150, 243)));
        grid.add(createCard("Categorias",  categoriasValue, categoriasSub, new Color(123, 31, 162)));
        grid.add(createCard("Karts",       kartsValue,     kartsSub,     new Color(46, 125, 50)));
        grid.add(createCard("Corridas",    corridasValue,  corridasSub,  new Color(251, 140, 0)));
        grid.add(createCard("Reservas",    reservasValue,  reservasSub,  new Color(198, 40, 40)));
        grid.add(createCard("Pendentes",   pendenteValue,  pendenteSub,  new Color(255, 152, 0)));

        return grid;
    }

    private JPanel createCard(String title, JLabel valueLabel,
                               JLabel subLabel, Color accentColor) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 6));

        JPanel shadow = new RoundedPanel(new BorderLayout(), 20, new Color(0, 0, 0, 60));
        shadow.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel card = new RoundedPanel(new BorderLayout(0, 0), 20, new Color(255, 255, 255, 240));
        card.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1, true),
                BorderFactory.createEmptyBorder(18, 24, 14, 24)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(new Color(100, 100, 100));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 52));
        valueLabel.setForeground(accentColor);
        valueLabel.setHorizontalAlignment(SwingConstants.LEFT);

        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(new Color(120, 120, 120));
        subLabel.setHorizontalAlignment(SwingConstants.LEFT);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(valueLabel);
        textPanel.add(subLabel);

        card.add(textPanel, BorderLayout.CENTER);

        // Bottom accent bar
        JPanel accentBar = new RoundedAccentBar(accentColor, 20);

        JPanel cardContainer = new JPanel(new BorderLayout());
        cardContainer.setOpaque(false);
        cardContainer.add(card, BorderLayout.CENTER);
        cardContainer.add(accentBar, BorderLayout.SOUTH);

        shadow.add(cardContainer, BorderLayout.CENTER);
        wrapper.add(shadow, BorderLayout.CENTER);
        return wrapper;
    }

    // ─── Today Strip ─────────────────────────────────────────────────────────

    private JPanel buildTodayStrip() {
        JPanel strip = new RoundedPanel(new BorderLayout(0, 0), 14, TODAY_BAR_BG);
        strip.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        strip.setPreferredSize(new Dimension(0, 58));

        JLabel titleLbl = new JLabel("Hoje  ·");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLbl.setForeground(Color.WHITE);
        titleLbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 16));

        JPanel items = new JPanel(new GridLayout(1, 4, 24, 0));
        items.setOpaque(false);

        items.add(buildTodayItem("Corridas hoje:", todayCorridas));
        items.add(buildTodayItem("Reservas hoje:", todayReservas));
        items.add(buildTodayItem("Confirmadas:", todayConfirmadas));
        items.add(buildTodayItem("Canceladas:", todayCanceladas));

        strip.add(titleLbl, BorderLayout.WEST);
        strip.add(items, BorderLayout.CENTER);
        return strip;
    }

    private JPanel buildTodayItem(String labelText, JLabel valueLabel) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(SUBTITLE_COLOR);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        valueLabel.setForeground(Color.WHITE);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setOpaque(false);
        p.add(lbl);
        p.add(Box.createHorizontalStrut(6));
        p.add(valueLabel);
        return p;
    }

    // ─── Refresh ─────────────────────────────────────────────────────────────

    public void refreshData() {
        List<Reserva> reservas = reservaService.listarReservas();
        List<Corrida> corridas = corridaService.listarCorridas();

        // KPI values
        clientesValue.setText(String.valueOf(clienteService.listarClientes().size()));
        categoriasValue.setText(String.valueOf(categoriaService.listarCategorias().size()));
        kartsValue.setText(String.valueOf(kartService.listarKarts().size()));
        corridasValue.setText(String.valueOf(corridas.size()));
        reservasValue.setText(String.valueOf(reservas.size()));

        long pendentes = reservas.stream()
                .filter(r -> r.getEstado() == ReservaEstado.PENDENTE)
                .count();
        pendenteValue.setText(String.valueOf(pendentes));

        long confirmadas = reservas.stream()
                .filter(r -> r.getEstado() == ReservaEstado.CONFIRMADA)
                .count();

        // Dynamic subtitles
        reservasSub.setText(confirmadas + " confirmadas");
        pendenteSub.setText("a aguardar confirmação");
        clientesSub.setText("registados");
        categoriasSub.setText("disponíveis");
        kartsSub.setText("na frota");
        corridasSub.setText("realizadas");

        // Today stats
        LocalDate today = LocalDate.now();

        long corridasHoje = corridas.stream()
                .filter(c -> c.getDataHoraInicio() != null &&
                        c.getDataHoraInicio().toLocalDate().equals(today))
                .count();

        List<Reserva> reservasHoje = reservas.stream()
                .filter(r -> r.getDataHoraInicio() != null &&
                        r.getDataHoraInicio().toLocalDate().equals(today))
                .toList();

        long confirmadasHoje = reservasHoje.stream()
                .filter(r -> r.getEstado() == ReservaEstado.CONFIRMADA)
                .count();
        long canceladasHoje = reservasHoje.stream()
                .filter(r -> r.getEstado() == ReservaEstado.CANCELADA)
                .count();

        todayCorridas.setText(String.valueOf(corridasHoje));
        todayReservas.setText(String.valueOf(reservasHoje.size()));
        todayConfirmadas.setText(String.valueOf(confirmadasHoje));
        todayCanceladas.setText(String.valueOf(canceladasHoje));
    }

    // ─── Inner helpers ───────────────────────────────────────────────────────

    private static class BackgroundPanel extends JPanel {

        private final Image backgroundImage;

        BackgroundPanel(String imagePath) {
            ImageIcon icon = new ImageIcon(DashboardPanel.class.getResource(imagePath));
            this.backgroundImage = icon.getImage();
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
            g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.40f));
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }

    private static class RoundedPanel extends JPanel {

        private final int radius;
        private final Color backgroundColor;

        RoundedPanel(java.awt.LayoutManager layout, int radius, Color backgroundColor) {
            super(layout);
            this.radius = radius;
            this.backgroundColor = backgroundColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundedAccentBar extends JPanel {

        private final Color color;
        private final int radius;

        RoundedAccentBar(Color color, int radius) {
            this.color = color;
            this.radius = radius;
            setOpaque(false);
            setPreferredSize(new Dimension(0, 5));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillRoundRect(0, 0, getWidth(), getHeight() + radius, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
