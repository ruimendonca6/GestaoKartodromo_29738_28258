package pt.kartodromo.desktop.ui.perfil;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import pt.kartodromo.desktop.ui.UiStyle;
import pt.kartodromo.desktop.ui.auth.AuthService;
import pt.kartodromo.desktop.ui.auth.AuthUser;

public class PerfilPanel extends JPanel {

    private final AuthService authService =
            new AuthService();

    private final Runnable logoutAction;

    private AuthUser authenticatedUser;

    private final JTextField usernameField =
            new JTextField();

    private final JTextField roleField =
            new JTextField();

    private final JTextField emailField =
            new JTextField();

    private final JPasswordField currentPasswordField =
            new JPasswordField();

    private final JPasswordField newPasswordField =
            new JPasswordField();

    private final JPasswordField confirmPasswordField =
            new JPasswordField();

    public PerfilPanel(
            AuthUser authenticatedUser,
            Runnable logoutAction) {

        this.authenticatedUser =
                authenticatedUser;

        this.logoutAction =
                logoutAction;

        setLayout(new BorderLayout());
        setBackground(UiStyle.BACKGROUND_COLOR);

        JScrollPane scrollPane =
                new JScrollPane(buildContent());

        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(UiStyle.BACKGROUND_COLOR);

        add(scrollPane, BorderLayout.CENTER);

        loadUserData();
    }

    private JPanel buildContent() {
        JPanel content =
                new JPanel(new BorderLayout(18, 18));

        content.setBackground(UiStyle.BACKGROUND_COLOR);

        content.setBorder(
                BorderFactory.createEmptyBorder(
                        20,
                        20,
                        20,
                        20
                )
        );

        content.add(buildProfileHeader(), BorderLayout.NORTH);
        content.add(buildCenterCards(), BorderLayout.CENTER);
        content.add(buildSessionCard(), BorderLayout.SOUTH);

        return content;
    }

    private JPanel buildProfileHeader() {
        JPanel panel =
                new JPanel(new BorderLayout(18, 0));

        panel.setOpaque(false);

        JLabel avatar =
                new JLabel(
                        authenticatedUser
                                .getUsername()
                                .substring(0, 1)
                                .toUpperCase(),
                        JLabel.CENTER
                );

        avatar.setOpaque(true);
        avatar.setBackground(UiStyle.LIGHT_RED);
        avatar.setForeground(UiStyle.PRIMARY_RED);

        avatar.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        28
                )
        );

        avatar.setBorder(
                BorderFactory.createEmptyBorder(
                        18,
                        24,
                        18,
                        24
                )
        );

        JPanel infoPanel =
                new JPanel(new GridLayout(4, 1, 0, 3));

        infoPanel.setOpaque(false);

        JLabel section =
                createSectionLabel("CONTA");

        JLabel username =
                createTitleLabel(
                        authenticatedUser.getUsername()
                );

        JLabel email =
                createDescriptionLabel(
                        authenticatedUser.getEmail() == null
                                || authenticatedUser.getEmail().isBlank()
                                ? "Email não definido"
                                : authenticatedUser.getEmail()
                );

        JLabel role =
                createBadgeLabel(
                        "Perfil: "
                                + authenticatedUser
                                .getRole()
                                .name()
                );

        infoPanel.add(section);
        infoPanel.add(username);
        infoPanel.add(email);
        infoPanel.add(role);

        panel.add(avatar, BorderLayout.WEST);
        panel.add(infoPanel, BorderLayout.CENTER);

        return UiStyle.createCard(panel);
    }

    private JPanel buildCenterCards() {
        JPanel center =
                new JPanel(new GridLayout(1, 2, 18, 18));

        center.setBackground(UiStyle.BACKGROUND_COLOR);

        center.add(
                UiStyle.createCard(
                        buildProfileEditPanel()
                )
        );

        center.add(
                UiStyle.createCard(
                        buildPasswordPanel()
                )
        );

        return center;
    }

    private JPanel buildProfileEditPanel() {
        JPanel panel =
                new JPanel(new BorderLayout(12, 12));

        panel.setOpaque(false);

        JLabel title =
                createSectionLabel("DADOS DO PERFIL");

        JPanel fields =
                new JPanel(new GridBagLayout());

        fields.setOpaque(false);

        usernameField.setEditable(false);
        roleField.setEditable(false);

        GridBagConstraints gbc =
                createGbc();

        addFormRow(fields, gbc, 0, "Utilizador", usernameField);
        addFormRow(fields, gbc, 1, "Perfil", roleField);
        addFormRow(fields, gbc, 2, "Email", emailField);

        JButton saveButton =
                UiStyle.createPrimaryButton("Guardar alterações");

        saveButton.addActionListener(e -> updateProfile());

        JPanel actions =
                new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

        actions.setOpaque(false);
        actions.add(saveButton);

        panel.add(title, BorderLayout.NORTH);
        panel.add(fields, BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildPasswordPanel() {
        JPanel panel =
                new JPanel(new BorderLayout(12, 12));

        panel.setOpaque(false);

        JLabel title =
                createSectionLabel("SEGURANÇA");

        JPanel fields =
                new JPanel(new GridBagLayout());

        fields.setOpaque(false);

        GridBagConstraints gbc =
                createGbc();

        addFormRow(fields, gbc, 0, "Password atual", currentPasswordField);
        addFormRow(fields, gbc, 1, "Nova password", newPasswordField);
        addFormRow(fields, gbc, 2, "Confirmar password", confirmPasswordField);

        JButton clearButton =
                UiStyle.createActionButton(
                        "Limpar password",
                        UiStyle.CLEAR_GRAY
                );

        clearButton.addActionListener(e -> clearPasswordFields());

        JPanel actions =
                new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

        actions.setOpaque(false);
        actions.add(clearButton);

        panel.add(title, BorderLayout.NORTH);
        panel.add(fields, BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildSessionCard() {
        JPanel panel =
                new JPanel(new BorderLayout(12, 12));

        panel.setOpaque(false);

        JLabel title =
                createSectionLabel("SESSÃO");

        JButton logoutButton =
                UiStyle.createDangerButton("Encerrar sessão");

        logoutButton.addActionListener(e -> logout());

        panel.add(title, BorderLayout.NORTH);
        panel.add(logoutButton, BorderLayout.CENTER);

        return UiStyle.createCard(panel);
    }

    private void loadUserData() {
        usernameField.setText(authenticatedUser.getUsername());
        roleField.setText(authenticatedUser.getRole().name());
        emailField.setText(authenticatedUser.getEmail());
    }

    private void updateProfile() {
        String currentPassword =
                new String(currentPasswordField.getPassword());

        String newPassword =
                new String(newPasswordField.getPassword());

        String confirmPassword =
                new String(confirmPasswordField.getPassword());

        try {
            authenticatedUser =
                    authService.updateProfile(
                            authenticatedUser,
                            emailField.getText(),
                            currentPassword,
                            newPassword,
                            confirmPassword
                    );

            showInfo("Perfil atualizado com sucesso.");

            clearPasswordFields();
            loadUserData();

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
        int confirmacao =
                JOptionPane.showConfirmDialog(
                        this,
                        "Deseja encerrar a sessão?",
                        "Confirmar logout",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

        if (confirmacao == JOptionPane.YES_OPTION) {
            logoutAction.run();
        }
    }

    public void refreshData() {
        loadUserData();
    }

    private JLabel createSectionLabel(String text) {
        JLabel label =
                new JLabel(text);

        label.setFont(UiStyle.sectionFont());
        label.setForeground(UiStyle.PRIMARY_RED);

        return label;
    }

    private JLabel createTitleLabel(String text) {
        JLabel label =
                new JLabel(text);

        label.setFont(UiStyle.titleFont());
        label.setForeground(UiStyle.BLACK);

        return label;
    }

    private JLabel createDescriptionLabel(String text) {
        JLabel label =
                new JLabel(text);

        label.setFont(UiStyle.labelFont());
        label.setForeground(UiStyle.TEXT_GRAY);

        return label;
    }

    private JLabel createBadgeLabel(String text) {
        JLabel label =
                new JLabel(text);

        label.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );

        label.setForeground(UiStyle.PRIMARY_RED);

        return label;
    }

    private GridBagConstraints createGbc() {
        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.insets =
                new Insets(8, 0, 8, 0);

        gbc.fill =
                GridBagConstraints.HORIZONTAL;

        gbc.weightx =
                1;

        return gbc;
    }

    private void addFormRow(
            JPanel panel,
            GridBagConstraints gbc,
            int row,
            String label,
            Component component) {

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.gridwidth = 1;

        JLabel labelComponent =
                new JLabel(label);

        labelComponent.setFont(UiStyle.labelFont());
        labelComponent.setForeground(UiStyle.BLACK);

        panel.add(labelComponent, gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(8, 14, 8, 0);

        component.setFont(UiStyle.labelFont());

        panel.add(component, gbc);

        gbc.insets = new Insets(8, 0, 8, 0);
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