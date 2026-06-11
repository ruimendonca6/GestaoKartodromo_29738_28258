package pt.kartodromo.desktop.ui.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;

import javax.swing.JPanel;

public class RoundedCardPanel extends JPanel {

    private final int radius;
    private final Color backgroundColor;
    private final boolean shadowEnabled;

    public RoundedCardPanel() {
        this(22, Color.WHITE, true);
    }

    public RoundedCardPanel(int radius, Color backgroundColor, boolean shadowEnabled) {
        this.radius = radius;
        this.backgroundColor = backgroundColor;
        this.shadowEnabled = shadowEnabled;

        setOpaque(false);
    }

    public RoundedCardPanel(LayoutManager layout, int radius, Color backgroundColor, boolean shadowEnabled) {
        super(layout);

        this.radius = radius;
        this.backgroundColor = backgroundColor;
        this.shadowEnabled = shadowEnabled;

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

        int shadowOffset = shadowEnabled ? 5 : 0;

        if (shadowEnabled) {
            g2.setColor(new Color(0, 0, 0, 25));

            g2.fillRoundRect(
                    shadowOffset,
                    shadowOffset,
                    getWidth() - shadowOffset - 1,
                    getHeight() - shadowOffset - 1,
                    radius,
                    radius
            );
        }

        g2.setColor(backgroundColor);

        g2.fillRoundRect(
                0,
                0,
                getWidth() - shadowOffset - 1,
                getHeight() - shadowOffset - 1,
                radius,
                radius
        );

        g2.dispose();

        super.paintComponent(g);
    }
}