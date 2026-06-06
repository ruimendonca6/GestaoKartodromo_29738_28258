package pt.kartodromo.desktop.ui.auth;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;

public class LoginDialog extends JDialog {

    private static final Color BACKGROUND_COLOR = new Color(245, 247, 250);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(215, 220, 225);
    private static final Color PRIMARY_BLUE = new Color(21, 101, 192);
    private static final Color CREATE_GREEN = new Color(46, 125, 50);

    private final AuthService authService = new AuthService();

    private AuthUser authenticatedUser;

    private final JTextField loginUsernameField = new JTextField("admin");
    private final JPasswordField loginPasswordField = new JPasswordField("admin");

    private final JTextField registerUsernameField = new JTextField();
    private final JTextField registerEmailField = new JTextField();
    private final JPasswordField registerPasswordField = new JPasswordField();
    private final JPasswordField registerConfirmPasswordField = new JPasswordField();

    private final JComboBox<AuthRole> roleCombo =
            new JComboBox<>(new AuthRole[]{
                    AuthRole.CLIENTE,
                    AuthRole.FUNCIONARIO
            });

    public LoginDialog(JFrame parent) {
        super(parent, "Autenticação - Kartódromo", true);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(520, 430);
        setMinimumSize(new Dimension(480, 400));
        setLocationRelativeTo(parent);

        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND_COLOR);

        add(buildContent(), BorderLayout.CENTER);
    }

    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBackground(BACKGROUND_COLOR);
        content.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel title = new JLabel("Gestódromo - Gestão de um Kartódromo");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(PRIMARY_BLUE);

        JLabel subtitle = new JLabel("Inicie sessão ou crie uma nova conta");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setOpaque(false);
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Login", buildLoginPanel());
        tabs.addTab("Criar conta", buildRegisterPanel());

        content.add(header, BorderLayout.NORTH);
        content.add(createCard(tabs), BorderLayout.CENTER);

        return content;
    }

    private JPanel buildLoginPanel() {
        JButton loginButton = createActionButton("Entrar", PRIMARY_BLUE);
        loginButton.addActionListener(e -> doLogin());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addRow(panel, gbc, 0, "Utilizador", loginUsernameField);
        addRow(panel, gbc, 1, "Password", loginPasswordField);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1;

        panel.add(loginButton, gbc);

        return panel;
    }

    private JPanel buildRegisterPanel() {
        JButton registerButton = createActionButton("Criar conta", CREATE_GREEN);
        registerButton.addActionListener(e -> doRegister());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addRow(panel, gbc, 0, "Novo utilizador", registerUsernameField);
        addRow(panel, gbc, 1, "Email", registerEmailField);
        addRow(panel, gbc, 2, "Password", registerPasswordField);
        addRow(panel, gbc, 3, "Confirmar password", registerConfirmPasswordField);
        addRow(panel, gbc, 4, "Tipo de conta", roleCombo);

        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.weightx = 1;

        panel.add(registerButton, gbc);

        return panel;
    }

    private void doLogin() {
        String username = loginUsernameField.getText();
        String password = new String(loginPasswordField.getPassword());

        Optional<AuthUser> user = authService.login(username, password);

        if (user.isEmpty()) {
            showError("Utilizador ou password inválidos.");
            return;
        }

        authenticatedUser = user.get();
        dispose();
    }

    private void doRegister() {
        String username = registerUsernameField.getText();
        String email = registerEmailField.getText();
        String password = new String(registerPasswordField.getPassword());
        String confirmPassword = new String(registerConfirmPasswordField.getPassword());
        AuthRole role = (AuthRole) roleCombo.getSelectedItem();

        try {
            authService.register(username, email, password, confirmPassword, role);

            showInfo("Conta criada com sucesso. Já pode fazer login.");

            loginUsernameField.setText(username.trim().toLowerCase());
            loginPasswordField.setText("");

            registerUsernameField.setText("");
            registerEmailField.setText("");
            registerPasswordField.setText("");
            registerConfirmPasswordField.setText("");
            roleCombo.setSelectedItem(AuthRole.CLIENTE);

        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    public AuthUser getAuthenticatedUser() {
        return authenticatedUser;
    }

    private void addRow(
            JPanel panel,
            GridBagConstraints gbc,
            int row,
            String label,
            java.awt.Component component) {

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;

        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        panel.add(labelComponent, gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 1;

        component.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(component, gbc);
    }

    private JPanel createCard(java.awt.Component component) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_COLOR);

        card.setBorder(
                new CompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, 1),
                        BorderFactory.createEmptyBorder(14, 14, 14, 14)
                )
        );

        card.add(component, BorderLayout.CENTER);

        return card;
    }

    private JButton createActionButton(String text, Color background) {
        JButton button = new JButton(text);

        button.setBackground(background);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);

        button.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(background.darker()),
                        BorderFactory.createEmptyBorder(7, 14, 7, 14)
                )
        );

        return button;
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Sucesso",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Erro",
                JOptionPane.ERROR_MESSAGE
        );
    }
}