package pt.kartodromo.desktop;

import javax.swing.SwingUtilities;
import pt.kartodromo.core.config.HibernateUtil;
import pt.kartodromo.desktop.ui.KartodromoDesktopFrame;

public class DesktopApplication {

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(HibernateUtil::shutdown));
        SwingUtilities.invokeLater(() -> new KartodromoDesktopFrame().setVisible(true));
    }
}
