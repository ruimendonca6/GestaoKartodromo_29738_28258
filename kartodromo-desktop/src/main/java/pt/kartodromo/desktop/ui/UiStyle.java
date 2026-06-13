package pt.kartodromo.desktop.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.CompoundBorder;

import pt.kartodromo.desktop.ui.components.RoundedCardPanel;

public final class UiStyle {

    public static final Color BACKGROUND_COLOR =
            new Color(245, 247, 250);

    public static final Color CARD_COLOR =
            Color.WHITE;

    public static final Color BORDER_COLOR =
            new Color(225, 225, 225);

    public static final Color PRIMARY_BLUE =
            new Color(33, 150, 243);

    public static final Color CREATE_GREEN =
            new Color(46, 125, 50);

    public static final Color UPDATE_BLUE =
            new Color(21, 101, 192);

    public static final Color DELETE_RED =
            new Color(198, 40, 40);

    public static final Color CLEAR_GRAY =
            new Color(97, 97, 97);

    public static final Color PRIMARY_RED =
            new Color(198, 40, 40);

    public static final Color BLACK =
            new Color(20, 20, 20);

    public static final Color TEXT_GRAY =
            new Color(110, 110, 110);

    public static final Color LIGHT_RED =
            new Color(255, 235, 235);

    public static final Color RED =
            new Color(211, 47, 47);

    private UiStyle() {
    }

    public static JPanel createCard(Component component) {

        RoundedCardPanel card =
                new RoundedCardPanel(
                        new BorderLayout(),
                        22,
                        CARD_COLOR,
                        true
                );

        card.setBorder(
                new CompoundBorder(
                        BorderFactory.createMatteBorder(
                                0,
                                4,
                                0,
                                0,
                                PRIMARY_RED
                        ),
                        BorderFactory.createEmptyBorder(
                                18,
                                18,
                                18,
                                18
                        )
                )
        );

        card.add(component, BorderLayout.CENTER);

        return card;
    }

    public static JPanel createSimpleCard(Component component) {

        RoundedCardPanel card =
                new RoundedCardPanel(
                        new BorderLayout(),
                        22,
                        CARD_COLOR,
                        true
                );

        card.setBorder(
                new CompoundBorder(
                        BorderFactory.createLineBorder(
                                BORDER_COLOR,
                                1
                        ),
                        BorderFactory.createEmptyBorder(
                                18,
                                18,
                                18,
                                18
                        )
                )
        );

        card.add(component, BorderLayout.CENTER);

        return card;
    }

    public static JButton createActionButton(
            String text,
            Color background) {

        JButton button =
                new JButton(text);

        button.setBackground(background);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.setFont(
                new Font(
                        "Segoe UI Emoji",
                        Font.BOLD,
                        13
                )
        );

        button.setBorder(
                BorderFactory.createEmptyBorder(
                        9,
                        18,
                        9,
                        18
                )
        );

        return button;
    }

    public static JButton createPrimaryButton(String text) {
        return createActionButton(text, BLACK);
    }

    public static JButton createDangerButton(String text) {

        JButton button =
                new JButton(text);

        button.setBackground(Color.WHITE);
        button.setForeground(PRIMARY_RED);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        13
                )
        );

        button.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                PRIMARY_RED
                        ),
                        BorderFactory.createEmptyBorder(
                                10,
                                18,
                                10,
                                18
                        )
                )
        );

        button.addMouseListener(
                new java.awt.event.MouseAdapter() {

                    @Override
                    public void mouseEntered(
                            java.awt.event.MouseEvent e) {

                        button.setBackground(PRIMARY_RED);
                        button.setForeground(Color.WHITE);
                    }

                    @Override
                    public void mouseExited(
                            java.awt.event.MouseEvent e) {

                        button.setBackground(Color.WHITE);
                        button.setForeground(PRIMARY_RED);
                    }
                }
        );

        return button;
    }

    public static void styleTable(JTable table) {

        table.setRowHeight(32);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);

        table.setGridColor(
                new Color(230, 230, 230)
        );

        table.setSelectionBackground(
                new Color(66, 133, 244)
        );

        table.setSelectionForeground(Color.WHITE);

        table.getTableHeader()
                .setReorderingAllowed(false);

        table.getTableHeader().setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        14
                )
        );

        table.getTableHeader()
                .setBackground(PRIMARY_BLUE);

        table.getTableHeader()
                .setForeground(Color.WHITE);
    }

    /**
     * Título principal de página.
     */
    public static JLabel createPageTitle(String title) {

        JLabel header =
                new JLabel(title);

        header.setFont(
                new Font(
                        "Segoe UI Emoji",
                        Font.BOLD,
                        26
                )
        );

        header.setForeground(PRIMARY_RED);

        header.setBorder(
                BorderFactory.createEmptyBorder(
                        20,
                        24,
                        10,
                        0
                )
        );

        return header;
    }

    public static Font titleFont() {
        return new Font(
                "Segoe UI Emoji",
                Font.BOLD,
                26
        );
    }

    public static Font labelFont() {
        return new Font(
                "Segoe UI",
                Font.PLAIN,
                13
        );
    }

    public static Font sectionFont() {
        return new Font(
                "Segoe UI",
                Font.BOLD,
                16
        );
    }
}