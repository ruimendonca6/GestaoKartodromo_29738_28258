package pt.kartodromo.desktop.ui.dashboard;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;

import pt.kartodromo.core.bll.CategoriaKartService;
import pt.kartodromo.core.bll.ClienteService;
import pt.kartodromo.core.bll.CorridaService;
import pt.kartodromo.core.bll.KartService;

public class DashboardPanel extends JPanel {

    private static final String BACKGROUND_IMAGE_PATH =
            "/images/dashboardimage2.png";

    private static final Color TEXT_COLOR =
            new Color(33, 33, 33);

    private static final Color HEADER_TEXT_COLOR =
            Color.WHITE;

    private final ClienteService clienteService =
            new ClienteService();

    private final CategoriaKartService categoriaService =
            new CategoriaKartService();

    private final KartService kartService =
            new KartService();

    private final CorridaService corridaService =
            new CorridaService();

    private final JLabel clientesValue =
            new JLabel("0", SwingConstants.CENTER);

    private final JLabel categoriasValue =
            new JLabel("0", SwingConstants.CENTER);

    private final JLabel kartsValue =
            new JLabel("0", SwingConstants.CENTER);

    private final JLabel corridasValue =
            new JLabel("0", SwingConstants.CENTER);

    private final JLabel reservasValue =
            new JLabel("-", SwingConstants.CENTER);

    public DashboardPanel() {
        setLayout(new BorderLayout());

        BackgroundPanel backgroundPanel =
                new BackgroundPanel(BACKGROUND_IMAGE_PATH);

        backgroundPanel.setLayout(new BorderLayout(24, 24));
        backgroundPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        28,
                        28,
                        28,
                        28
                )
        );

        backgroundPanel.add(
                buildHeaderPanel(),
                BorderLayout.NORTH
        );

        backgroundPanel.add(
                buildCardsPanel(),
                BorderLayout.CENTER
        );

        add(
                backgroundPanel,
                BorderLayout.CENTER
        );

        refreshData();
    }

    private JPanel buildHeaderPanel() {
        JLabel title =
                new JLabel(
                        "Dashboard do Kartódromo"
                );

        title.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        32
                )
        );

        title.setForeground(
                HEADER_TEXT_COLOR
        );

        JLabel subtitle =
                new JLabel(
                        "Resumo geral dos dados registados no sistema"
                );

        subtitle.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        16
                )
        );

        subtitle.setForeground(
                new Color(
                        235,
                        235,
                        235
                )
        );

        JPanel headerPanel =
                new JPanel(
                        new GridLayout(
                                2,
                                1,
                                0,
                                6
                        )
                );

        headerPanel.setOpaque(false);
        headerPanel.add(title);
        headerPanel.add(subtitle);

        return headerPanel;
    }

    private JPanel buildCardsPanel() {
        JPanel cardsPanel =
                new JPanel(
                        new GridLayout(
                                2,
                                3,
                                22,
                                22
                        )
                );

        cardsPanel.setOpaque(false);

        cardsPanel.add(
                createCard(
                        "Clientes",
                        clientesValue,
                        new Color(33, 150, 243)
                )
        );

        cardsPanel.add(
                createCard(
                        "Categorias",
                        categoriasValue,
                        new Color(123, 31, 162)
                )
        );

        cardsPanel.add(
                createCard(
                        "Karts",
                        kartsValue,
                        new Color(46, 125, 50)
                )
        );

        cardsPanel.add(
                createCard(
                        "Corridas",
                        corridasValue,
                        new Color(251, 140, 0)
                )
        );

        cardsPanel.add(
                createCard(
                        "Reservas",
                        reservasValue,
                        new Color(198, 40, 40)
                )
        );

        return cardsPanel;
    }

    private JPanel createCard(
            String title,
            JLabel valueLabel,
            Color accentColor) {

        JPanel wrapper =
                new JPanel(
                        new BorderLayout()
                );

        wrapper.setOpaque(false);

        wrapper.setBorder(
                BorderFactory.createEmptyBorder(
                        0,
                        0,
                        8,
                        8
                )
        );

        JPanel shadowPanel =
                new RoundedPanel(
                        new BorderLayout(),
                        22,
                        new Color(
                                0,
                                0,
                                0,
                                70
                        )
                );

        shadowPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        6,
                        6,
                        6,
                        6
                )
        );

        JPanel card =
                new RoundedPanel(
                        new BorderLayout(12, 12),
                        22,
                        new Color(
                                255,
                                255,
                                255,
                                235
                        )
                );

        card.setBorder(
                new CompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(
                                        220,
                                        224,
                                        230
                                ),
                                1,
                                true
                        ),
                        BorderFactory.createEmptyBorder(
                                28,
                                28,
                                28,
                                28
                        )
                )
        );

        JLabel titleLabel =
                new JLabel(
                        title,
                        SwingConstants.CENTER
                );

        titleLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        20
                )
        );

        titleLabel.setForeground(
                accentColor
        );

        valueLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        54
                )
        );

        valueLabel.setForeground(
                TEXT_COLOR
        );

        JLabel footerLabel =
                new JLabel(
                        "Total registado",
                        SwingConstants.CENTER
                );

        footerLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        13
                )
        );

        footerLabel.setForeground(
                new Color(
                        120,
                        120,
                        120
                )
        );

        JPanel accentBar =
                new RoundedAccentBar(
                        accentColor,
                        22
                );

        card.add(
                titleLabel,
                BorderLayout.NORTH
        );

        card.add(
                valueLabel,
                BorderLayout.CENTER
        );

        card.add(
                footerLabel,
                BorderLayout.SOUTH
        );

        JPanel cardContainer =
                new JPanel(
                        new BorderLayout()
                );

        cardContainer.setOpaque(false);

        cardContainer.add(
                card,
                BorderLayout.CENTER
        );

        cardContainer.add(
                accentBar,
                BorderLayout.SOUTH
        );

        shadowPanel.add(
                cardContainer,
                BorderLayout.CENTER
        );

        wrapper.add(
                shadowPanel,
                BorderLayout.CENTER
        );

        return wrapper;
    }

    public void refreshData() {
        clientesValue.setText(
                String.valueOf(
                        clienteService.listarClientes().size()
                )
        );

        categoriasValue.setText(
                String.valueOf(
                        categoriaService.listarCategorias().size()
                )
        );

        kartsValue.setText(
                String.valueOf(
                        kartService.listarKarts().size()
                )
        );

        corridasValue.setText(
                String.valueOf(
                        corridaService.listarCorridas().size()
                )
        );

        // Temporário até o backend disponibilizar listarTodasReservas().
        reservasValue.setText("-");
    }

    private static class BackgroundPanel extends JPanel {

        private final Image backgroundImage;

        BackgroundPanel(String imagePath) {
            ImageIcon imageIcon =
                    new ImageIcon(
                            DashboardPanel.class.getResource(imagePath)
                    );

            this.backgroundImage =
                    imageIcon.getImage();

            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 =
                    (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR
            );

            g2.setComposite(
                    AlphaComposite.getInstance(
                            AlphaComposite.SRC_OVER,
                            0.60f
                    )
            );

            g2.drawImage(
                    backgroundImage,
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    this
            );

            g2.setComposite(
                    AlphaComposite.getInstance(
                            AlphaComposite.SRC_OVER,
                            0.45f
                    )
            );

            g2.setColor(
                    Color.BLACK
            );

            g2.fillRect(
                    0,
                    0,
                    getWidth(),
                    getHeight()
            );

            g2.dispose();
        }
    }

    private static class RoundedPanel extends JPanel {

        private final int radius;
        private final Color backgroundColor;

        RoundedPanel(
                java.awt.LayoutManager layout,
                int radius,
                Color backgroundColor) {

            super(layout);

            this.radius =
                    radius;

            this.backgroundColor =
                    backgroundColor;

            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 =
                    (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(
                    backgroundColor
            );

            g2.fillRoundRect(
                    0,
                    0,
                    getWidth() - 1,
                    getHeight() - 1,
                    radius,
                    radius
            );

            g2.dispose();

            super.paintComponent(g);
        }
    }

    private static class RoundedAccentBar extends JPanel {

        private final Color color;
        private final int radius;

        RoundedAccentBar(
                Color color,
                int radius) {

            this.color =
                    color;

            this.radius =
                    radius;

            setOpaque(false);

            setPreferredSize(
                    new java.awt.Dimension(
                            0,
                            6
                    )
            );
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 =
                    (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(
                    color
            );

            g2.fillRoundRect(
                    0,
                    0,
                    getWidth(),
                    getHeight() + radius,
                    radius,
                    radius
            );

            g2.dispose();

            super.paintComponent(g);
        }
    }
}