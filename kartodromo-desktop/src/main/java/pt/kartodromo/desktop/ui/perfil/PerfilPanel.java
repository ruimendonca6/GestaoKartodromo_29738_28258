package pt.kartodromo.desktop.ui.perfil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import pt.kartodromo.desktop.ui.auth.AuthRole;
import pt.kartodromo.desktop.ui.UiStyle;
import pt.kartodromo.desktop.ui.auth.AuthService;
import pt.kartodromo.desktop.ui.auth.AuthUser;

public class PerfilPanel extends JPanel {

    private final AuthService authService = new AuthService();
    private final Runnable logoutAction;
    private AuthUser authenticatedUser;

    private final JTextField usernameField    = new JTextField();
    private final JTextField roleField        = new JTextField();
    private final JTextField emailField       = new JTextField();
    private final JPasswordField currentPasswordField  = new JPasswordField();
    private final JPasswordField newPasswordField      = new JPasswordField();
    private final JPasswordField confirmPasswordField  = new JPasswordField();

    // Header labels that need refreshing
    private JLabel avatarLabel;
    private JLabel headerUsernameLabel;
    private JLabel headerEmailLabel;

    public PerfilPanel(AuthUser authenticatedUser, Runnable logoutAction) {
        this.authenticatedUser = authenticatedUser;
        this.logoutAction = logoutAction;

        setLayout(new BorderLayout());
        setBackground(UiStyle.BACKGROUND_COLOR);

        JScrollPane scrollPane = new JScrollPane(buildContent());
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(UiStyle.BACKGROUND_COLOR);

        add(scrollPane, BorderLayout.CENTER);
        loadUserData();
    }

    // ─── Layout ──────────────────────────────────────────────────────────────

    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout(18, 18));
        content.setBackground(UiStyle.BACKGROUND_COLOR);
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        content.add(buildProfileHeader(), BorderLayout.NORTH);
        content.add(buildCenterCards(),   BorderLayout.CENTER);
        content.add(buildSessionCard(),   BorderLayout.SOUTH);
        return content;
    }

    // ─── Header card ─────────────────────────────────────────────────────────

    private JPanel buildProfileHeader() {
        JPanel outer = new JPanel(new BorderLayout(20, 0));
        outer.setOpaque(false);

        // Circular avatar
        String initial = authenticatedUser.getUsername().substring(0, 1).toUpperCase();
        Color roleColor = roleColor(authenticatedUser.getRole());
        avatarLabel = new JLabel(initial, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(roleColor.brighter());
                g2.fillOval(2, 2, getWidth() - 4, getHeight() - 4);
                g2.setColor(roleColor);
                g2.setStroke(new java.awt.BasicStroke(2.5f));
                g2.drawOval(2, 2, getWidth() - 4, getHeight() - 4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatarLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        avatarLabel.setForeground(roleColor.darker().darker());
        avatarLabel.setPreferredSize(new Dimension(84, 84));
        avatarLabel.setOpaque(false);

        JPanel avatarWrapper = new JPanel(new BorderLayout());
        avatarWrapper.setOpaque(false);
        avatarWrapper.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        avatarWrapper.add(avatarLabel, BorderLayout.CENTER);

        // Info panel
        headerUsernameLabel = new JLabel(authenticatedUser.getUsername());
        headerUsernameLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        headerUsernameLabel.setForeground(UiStyle.BLACK);

        String emailText = authenticatedUser.getEmail() == null
                || authenticatedUser.getEmail().isBlank()
                ? "Email não definido"
                : authenticatedUser.getEmail();
        headerEmailLabel = new JLabel(emailText);
        headerEmailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        headerEmailLabel.setForeground(UiStyle.TEXT_GRAY);

        JLabel rolePill = buildRolePill(authenticatedUser.getRole());

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);
        info.add(headerUsernameLabel);
        info.add(Box.createVerticalStrut(4));
        info.add(headerEmailLabel);
        info.add(Box.createVerticalStrut(8));
        info.add(rolePill);

        outer.add(avatarWrapper, BorderLayout.WEST);
        outer.add(info, BorderLayout.CENTER);

        return UiStyle.createCard(outer);
    }

    /** Colored pill label for the role badge. */
    private JLabel buildRolePill(AuthRole role) {
        String text = switch (role) {
            case ADMIN       -> "Administrador";
            case FUNCIONARIO -> "Funcionário";
            case CLIENTE     -> "Cliente";
        };
        Color bg = roleColor(role);

        JLabel pill = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 30));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.setColor(bg);
                g2.setStroke(new java.awt.BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        pill.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pill.setForeground(bg.darker());
        pill.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        pill.setOpaque(false);
        return pill;
    }

    private Color roleColor(AuthRole role) {
        return switch (role) {
            case ADMIN       -> UiStyle.PRIMARY_RED;
            case FUNCIONARIO -> UiStyle.PRIMARY_BLUE;
            case CLIENTE     -> new Color(46, 125, 50);
        };
    }

    // ─── Center cards ────────────────────────────────────────────────────────

    private JPanel buildCenterCards() {
        JPanel center = new JPanel(new GridLayout(1, 2, 18, 18));
        center.setBackground(UiStyle.BACKGROUND_COLOR);
        center.add(UiStyle.createCard(buildProfileEditPanel()));
        center.add(UiStyle.createCard(buildPasswordPanel()));
        return center;
    }

    private JPanel buildProfileEditPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);

        JLabel title = sectionLabel("DADOS DO PERFIL");

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);

        usernameField.setEditable(false);
        usernameField.setBackground(new Color(245, 245, 245));
        roleField.setEditable(false);
        roleField.setBackground(new Color(245, 245, 245));

        GridBagConstraints gbc = createGbc();
        addFormRow(fields, gbc, 0, "Utilizador", usernameField);
        addFormRow(fields, gbc, 1, "Perfil",     roleField);
        addFormRow(fields, gbc, 2, "Email",       emailField);

        JButton saveBtn = UiStyle.createPrimaryButton("Guardar dados");
        saveBtn.addActionListener(e -> updateProfileOnly());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actions.setOpaque(false);
        actions.add(saveBtn);

        panel.add(title,   BorderLayout.NORTH);
        panel.add(fields,  BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildPasswordPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);

        JLabel title = sectionLabel("SEGURANÇA");

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);

        GridBagConstraints gbc = createGbc();
        addFormRow(fields, gbc, 0, "Password atual",    currentPasswordField);
        addFormRow(fields, gbc, 1, "Nova password",     newPasswordField);
        addFormRow(fields, gbc, 2, "Confirmar password", confirmPasswordField);

        JButton changeBtn = UiStyle.createPrimaryButton("Alterar password");
        changeBtn.addActionListener(e -> updatePasswordOnly());

        JButton clearBtn = UiStyle.createActionButton("Limpar", UiStyle.CLEAR_GRAY);
        clearBtn.addActionListener(e -> clearPasswordFields());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actions.setOpaque(false);
        actions.add(changeBtn);
        actions.add(Box.createHorizontalStrut(10));
        actions.add(clearBtn);

        panel.add(title,   BorderLayout.NORTH);
        panel.add(fields,  BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    // ─── Session card ────────────────────────────────────────────────────────

    private JPanel buildSessionCard() {
        JPanel panel = new JPanel(new BorderLayout(12, 8));
        panel.setOpaque(false);

        JLabel title = sectionLabel("SESSÃO");

        JLabel hint = new JLabel("A sessão atual será encerrada e voltará ao ecrã de login.");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hint.setForeground(UiStyle.TEXT_GRAY);

        JButton logoutBtn = UiStyle.createDangerButton("Encerrar sessão");
        logoutBtn.addActionListener(e -> logout());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bottom.setOpaque(false);
        bottom.add(logoutBtn);

        panel.add(title,  BorderLayout.NORTH);
        panel.add(hint,   BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);

        return UiStyle.createCard(panel);
    }

    // ─── Actions ─────────────────────────────────────────────────────────────

    private void loadUserData() {
        usernameField.setText(authenticatedUser.getUsername());
        roleField.setText(authenticatedUser.getRole().name());
        emailField.setText(authenticatedUser.getEmail());
    }

    private void updateProfileOnly() {
        try {
            authenticatedUser = authService.updateProfile(
                    authenticatedUser,
                    emailField.getText(),
                    "",   // no password change
                    "",
                    ""
            );
            showInfo("Dados do perfil atualizados com sucesso.");
            loadUserData();
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void updatePasswordOnly() {
        String current = new String(currentPasswordField.getPassword());
        String newPwd  = new String(newPasswordField.getPassword());
        String confirm = new String(confirmPasswordField.getPassword());

        if (current.isBlank() || newPwd.isBlank()) {
            showError("Preencha todos os campos de password.");
            return;
        }

        try {
            authenticatedUser = authService.updateProfile(
                    authenticatedUser,
                    authenticatedUser.getEmail(),
                    current,
                    newPwd,
                    confirm
            );
            showInfo("Password alterada com sucesso.");
            clearPasswordFields();
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void clearPasswordFields() {
        currentPasswordField.setText("");
        newPasswordField.setText("");
        confirmPasswordField.setText("");
    }

    private void logout() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Deseja encerrar a sessão?",
                "Confirmar logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (choice == JOptionPane.YES_OPTION) {
            logoutAction.run();
        }
    }

    public void refreshData() {
        loadUserData();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UiStyle.sectionFont());
        lbl.setForeground(UiStyle.PRIMARY_RED);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        return lbl;
    }

    private GridBagConstraints createGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(8, 0, 8, 0);
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        return gbc;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc,
                             int row, String labelText, Component component) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.gridwidth = 1;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UiStyle.labelFont());
        lbl.setForeground(UiStyle.BLACK);
        panel.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        gbc.insets = new Insets(8, 14, 8, 0);
        component.setFont(UiStyle.labelFont());
        panel.add(component, gbc);
        gbc.insets = new Insets(8, 0, 8, 0);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Sucesso",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erro",
                JOptionPane.ERROR_MESSAGE);
    }
}
