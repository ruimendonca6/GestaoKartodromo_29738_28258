package pt.kartodromo.desktop.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.CompoundBorder;

public final class UiStyle {

    public static final Color BACKGROUND_COLOR = new Color(245, 247, 250);
    public static final Color CARD_COLOR = Color.WHITE;
    public static final Color BORDER_COLOR = new Color(215, 220, 225);

    public static final Color PRIMARY_BLUE = new Color(33, 150, 243);
    public static final Color CREATE_GREEN = new Color(46, 125, 50);
    public static final Color UPDATE_BLUE = new Color(21, 101, 192);
    public static final Color DELETE_RED = new Color(198, 40, 40);
    public static final Color CLEAR_GRAY = new Color(97, 97, 97);

    private UiStyle() {
    }

    public static JPanel createCard(Component component) {
        JPanel card = new JPanel(new java.awt.BorderLayout());
        card.setBackground(CARD_COLOR);

        card.setBorder(
                new CompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, 1),
                        BorderFactory.createEmptyBorder(14, 14, 14, 14)
                )
        );

        card.add(component, java.awt.BorderLayout.CENTER);
        return card;
    }

    public static JButton createActionButton(String text, Color background) {
        JButton button = new JButton(text);

        button.setBackground(background);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));

        button.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(background.darker()),
                        BorderFactory.createEmptyBorder(7, 14, 7, 14)
                )
        );

        return button;
}

    public static void styleTable(JTable table) {
        table.setRowHeight(32);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.setGridColor(new Color(230, 230, 230));
        table.setSelectionBackground(new Color(66, 133, 244));
        table.setSelectionForeground(Color.WHITE);

        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(PRIMARY_BLUE);
        table.getTableHeader().setForeground(Color.WHITE);
    }
}