package pt.kartodromo.desktop.ui.utilizadores;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import pt.kartodromo.desktop.ui.UiStyle;
import pt.kartodromo.desktop.ui.auth.AuthRole;
import pt.kartodromo.desktop.ui.auth.AuthService;
import pt.kartodromo.desktop.ui.auth.AuthUser;

public class UtilizadoresPanel extends JPanel {

    private final AuthService authService = new AuthService();

    private String utilizadorSelecionadoUsername = null;

    private final JTextField usernameField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JComboBox<AuthRole> roleCombo = new JComboBox<>(AuthRole.values());
    private final JComboBox<String> filtroCombo = new JComboBox<>(new String[]{"Todos", "Administrador", "Funcionário", "Cliente"});

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Utilizador", "Email", "Perfil"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(tableModel);

    public UtilizadoresPanel() {
        setLayout(new BorderLayout(18, 18));
        setBackground(UiStyle.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(buildFormCard(), BorderLayout.WEST);
        add(buildTableCard(), BorderLayout.CENTER);

        styleRoleCombo();
        loadData();
    }

    private JPanel buildFormCard() {
        JPanel form = new JPanel(new BorderLayout(12, 12));
        form.setOpaque(false);

        JLabel title = new JLabel("UTILIZADOR");
        title.setFont(UiStyle.sectionFont());
        title.setForeground(UiStyle.PRIMARY_RED);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints gbc = createGbc();

        usernameField.setPreferredSize(new java.awt.Dimension(200, 28));
        passwordField.setPreferredSize(new java.awt.Dimension(200, 28));

        addFormRow(fields, gbc, 0, "Utilizador", usernameField);
        addFormRow(fields, gbc, 1, "Email", emailField);
        addFormRow(fields, gbc, 2, "Password", passwordField);
        addFormRow(fields, gbc, 3, "Perfil", roleCombo);

        JButton criarBtn = UiStyle.createActionButton("Criar", UiStyle.CREATE_GREEN);
        JButton editarBtn = UiStyle.createActionButton("Editar", UiStyle.UPDATE_BLUE);
        JButton eliminarBtn = UiStyle.createActionButton("Eliminar", UiStyle.DELETE_RED);
        JButton limparBtn = UiStyle.createActionButton("Limpar", UiStyle.CLEAR_GRAY);

        criarBtn.addActionListener(e -> criarUtilizador());
        editarBtn.addActionListener(e -> editarUtilizador());
        eliminarBtn.addActionListener(e -> eliminarUtilizador());
        limparBtn.addActionListener(e -> limparFormulario());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        buttons.setOpaque(false);
        buttons.add(criarBtn);
        buttons.add(editarBtn);
        buttons.add(eliminarBtn);
        buttons.add(limparBtn);

        form.add(title, BorderLayout.NORTH);
        form.add(fields, BorderLayout.CENTER);
        form.add(buttons, BorderLayout.SOUTH);

        JPanel card = UiStyle.createCard(form);
        card.setPreferredSize(new java.awt.Dimension(340, 0));
        return card;
    }

    private JPanel buildTableCard() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("LISTA DE UTILIZADORES");
        title.setFont(UiStyle.sectionFont());
        title.setForeground(UiStyle.PRIMARY_RED);

        JPanel filtroPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filtroPanel.setOpaque(false);
        filtroPanel.add(new JLabel("Filtrar:"));
        filtroPanel.add(filtroCombo);

        filtroCombo.addActionListener(e -> loadData());

        header.add(title, BorderLayout.WEST);
        header.add(filtroPanel, BorderLayout.EAST);

        UiStyle.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                preencherFormulario();
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UiStyle.BORDER_COLOR));

        panel.add(header, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        return UiStyle.createCard(panel);
    }

    private void criarUtilizador() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        AuthRole role = (AuthRole) roleCombo.getSelectedItem();

        try {
            authService.criarUtilizadorAdmin(username, email, password, role);
            showInfo("Utilizador criado com sucesso.");
            limparFormulario();
            loadData();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void editarUtilizador() {
        if (utilizadorSelecionadoUsername == null) {
            showError("Selecione um utilizador para editar.");
            return;
        }

        String email = emailField.getText().trim();
        AuthRole role = (AuthRole) roleCombo.getSelectedItem();
        String newPassword = new String(passwordField.getPassword());

        try {
            authService.atualizarUtilizadorAdmin(
                    utilizadorSelecionadoUsername,
                    email,
                    role,
                    newPassword.isBlank() ? null : newPassword
            );
            showInfo("Utilizador atualizado com sucesso.");
            limparFormulario();
            loadData();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void eliminarUtilizador() {
        if (utilizadorSelecionadoUsername == null) {
            showError("Selecione um utilizador para eliminar.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Eliminar o utilizador \"" + utilizadorSelecionadoUsername + "\"?",
                "Confirmar eliminação",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            authService.eliminarUtilizador(utilizadorSelecionadoUsername);
            showInfo("Utilizador eliminado.");
            limparFormulario();
            loadData();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void preencherFormulario() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        utilizadorSelecionadoUsername = (String) tableModel.getValueAt(row, 0);
        usernameField.setText(utilizadorSelecionadoUsername);
        emailField.setText((String) tableModel.getValueAt(row, 1));
        String roleName = (String) tableModel.getValueAt(row, 2);
        try {
            roleCombo.setSelectedItem(AuthRole.valueOf(roleName));
        } catch (Exception ignored) {}
        passwordField.setText("");
        usernameField.setEditable(false);
    }

    private void limparFormulario() {
        utilizadorSelecionadoUsername = null;
        usernameField.setText("");
        usernameField.setEditable(true);
        emailField.setText("");
        passwordField.setText("");
        roleCombo.setSelectedIndex(0);
        table.clearSelection();
    }

    private void loadData() {
        tableModel.setRowCount(0);
        String filtro = (String) filtroCombo.getSelectedItem();

        List<AuthUser> utilizadores = authService.listarUtilizadores();
        for (AuthUser u : utilizadores) {
            if (filtroMatch(u.getRole(), filtro)) {
                tableModel.addRow(new Object[]{
                    u.getUsername(),
                    u.getEmail(),
                    u.getRole().name()
                });
            }
        }
    }

    private boolean filtroMatch(AuthRole role, String filtro) {
        return switch (filtro) {
            case "Administrador" -> role == AuthRole.ADMIN;
            case "Funcionário" -> role == AuthRole.FUNCIONARIO;
            case "Cliente" -> role == AuthRole.CLIENTE;
            default -> true;
        };
    }

    public void refreshData() {
        loadData();
    }

    private void styleRoleCombo() {
        roleCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof AuthRole role) {
                    setText(switch (role) {
                        case ADMIN -> "Administrador";
                        case FUNCIONARIO -> "Funcionário";
                        case CLIENTE -> "Cliente";
                    });
                }
                return this;
            }
        });
    }

    private GridBagConstraints createGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        return gbc;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, Component component) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(8, 0, 8, 0);
        JLabel label = new JLabel(labelText);
        label.setFont(UiStyle.labelFont());
        label.setForeground(UiStyle.BLACK);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.insets = new Insets(8, 14, 8, 0);
        component.setFont(UiStyle.labelFont());
        panel.add(component, gbc);
        gbc.insets = new Insets(8, 0, 8, 0);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erro", JOptionPane.ERROR_MESSAGE);
    }
}
