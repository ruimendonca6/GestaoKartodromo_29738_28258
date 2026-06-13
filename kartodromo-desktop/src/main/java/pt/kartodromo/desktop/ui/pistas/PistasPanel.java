package pt.kartodromo.desktop.ui.pistas;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import pt.kartodromo.desktop.ui.UiStyle;

public class PistasPanel extends JPanel {

    private final Consumer<String> onPistaSelecionada;

    public PistasPanel() {
        this(null);
    }

    public PistasPanel(Consumer<String> onPistaSelecionada) {
        this.onPistaSelecionada = onPistaSelecionada;

        setLayout(new BorderLayout());
        setBackground(UiStyle.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(buildContent(), BorderLayout.CENTER);
    }

    private JScrollPane buildContent() {
        JPanel grid = new JPanel(new GridLayout(1, 3, 25, 25));
        grid.setBackground(UiStyle.BACKGROUND_COLOR);

        grid.add(createTrackCard(
                "Pista Técnica",
                "/images/pista1.png",
                "5/5",
                "650 m",
                "18",
                "Treino / Técnica"
        ));

        grid.add(createTrackCard(
                "Pista Completa",
                "/images/pista2.png",
                "4/5",
                "1200 m",
                "24",
                "Corrida Oficial"
        ));

        grid.add(createTrackCard(
                "Pista Sprint",
                "/images/pista3.png",
                "3/5",
                "450 m",
                "10",
                "Sessões rápidas"
        ));

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UiStyle.BACKGROUND_COLOR);

        return scroll;
    }

    private JPanel createTrackCard(
            String nome,
            String imagem,
            String dificuldade,
            String comprimento,
            String curvas,
            String tipo) {

        JPanel content = new JPanel(new BorderLayout(15, 15));
        content.setOpaque(false);

        JLabel tituloLabel = new JLabel(nome, SwingConstants.CENTER);
        tituloLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        tituloLabel.setForeground(UiStyle.PRIMARY_RED);

        JLabel imagemLabel = new JLabel("", SwingConstants.CENTER);

        ImageIcon icon = loadImage(imagem, 350, 220);

        if (icon != null) {
            imagemLabel.setIcon(icon);
        } else {
            imagemLabel.setText("Imagem não encontrada");
            imagemLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        }

        JPanel infoPanel = new JPanel(new GridLayout(4, 1, 0, 6));
        infoPanel.setOpaque(false);

        infoPanel.add(createInfoLabel("Dificuldade: " + dificuldade));
        infoPanel.add(createInfoLabel("Comprimento: " + comprimento));
        infoPanel.add(createInfoLabel("Curvas: " + curvas));
        infoPanel.add(createInfoLabel("Tipo: " + tipo));

        JButton selecionarButton =
                UiStyle.createActionButton(
                        "Selecionar Pista",
                        UiStyle.PRIMARY_RED
                );

        selecionarButton.addActionListener(e -> selecionarPista(nome));

        JPanel bottomPanel = new JPanel(new BorderLayout(12, 12));
        bottomPanel.setOpaque(false);
        bottomPanel.add(infoPanel, BorderLayout.CENTER);
        bottomPanel.add(selecionarButton, BorderLayout.SOUTH);

        content.add(tituloLabel, BorderLayout.NORTH);
        content.add(imagemLabel, BorderLayout.CENTER);
        content.add(bottomPanel, BorderLayout.SOUTH);

        return UiStyle.createCard(content);
    }

    private void selecionarPista(String nome) {
        if (onPistaSelecionada != null) {
            onPistaSelecionada.accept(nome);
        }

        JOptionPane.showMessageDialog(
                this,
                nome + " selecionada com sucesso.",
                "Pista selecionada",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        label.setForeground(UiStyle.BLACK);
        return label;
    }

    private ImageIcon loadImage(String path, int width, int height) {
        try {
            java.net.URL imageUrl = getClass().getResource(path);

            if (imageUrl == null) {
                return null;
            }

            ImageIcon original = new ImageIcon(imageUrl);

            Image scaled = original
                    .getImage()
                    .getScaledInstance(
                            width,
                            height,
                            Image.SCALE_SMOOTH
                    );

            return new ImageIcon(scaled);

        } catch (Exception ex) {
            return null;
        }
    }

    public void refreshData() {
        // Futuramente pode carregar layouts da base de dados.
    }
}