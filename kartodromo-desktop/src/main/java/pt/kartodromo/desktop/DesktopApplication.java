package pt.kartodromo.desktop;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatLightLaf;

import pt.kartodromo.core.config.HibernateUtil;
import pt.kartodromo.desktop.ui.KartodromoDesktopFrame;
import pt.kartodromo.desktop.ui.auth.AuthUser;
import pt.kartodromo.desktop.ui.auth.LoginDialog;

public class DesktopApplication {

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(
                new Thread(HibernateUtil::shutdown)
        );

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(new FlatLightLaf());
            } catch (Exception e) {
                System.err.println("Erro ao aplicar tema FlatLaf.");
            }

            LoginDialog loginDialog =
                    new LoginDialog(null);

            loginDialog.setVisible(true);

            AuthUser authenticatedUser =
                    loginDialog.getAuthenticatedUser();

            if (authenticatedUser == null) {
                System.exit(0);
                return;
            }

            new KartodromoDesktopFrame(authenticatedUser)
                    .setVisible(true);
        });
    }
}