package pt.kartodromo.desktop.ui.clientes;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import pt.kartodromo.core.bll.ClienteService;
import pt.kartodromo.core.model.Cliente;

public class ClientePanel extends JPanel {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ClienteService clienteService = new ClienteService();
    private final DefaultListModel<Cliente> clientesModel = new DefaultListModel<>();

    public ClientePanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(buildForm(), BorderLayout.NORTH);
        add(buildList(), BorderLayout.CENTER);

        refreshData();
    }

    private JPanel buildForm() {
        JTextField nomeField = new JTextField();
        JTextField dataNascimentoField = new JTextField("2000-01-01");
        JTextField emailField = new JTextField();
        JTextField nivelField = new JTextField("0");

        JButton criarButton = new JButton("Criar cliente");

        criarButton.addActionListener(e -> {
            try {
                Cliente cliente = clienteService.criarCliente(
                        nomeField.getText().trim(),
                        LocalDate.parse(dataNascimentoField.getText().trim(), DATE_FORMAT),
                        emailField.getText().trim(),
                        Integer.parseInt(nivelField.getText().trim())
                );

                showInfo("Cliente criado com sucesso: " + cliente.getId());

                nomeField.setText("");
                emailField.setText("");
                nivelField.setText("0");

                refreshData();

            } catch (DateTimeParseException ex) {
                showError("Data de nascimento inválida. Formato: yyyy-MM-dd.");
            } catch (NumberFormatException ex) {
                showError("O nível de experiência deve ser um número entre 0 e 5.");
            } catch (RuntimeException ex) {
                showError(ex.getMessage());
            }
        });

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Novo cliente"));

        form.add(new JLabel("Nome"));
        form.add(nomeField);

        form.add(new JLabel("Data nascimento (yyyy-MM-dd)"));
        form.add(dataNascimentoField);

        form.add(new JLabel("Email"));
        form.add(emailField);

        form.add(new JLabel("Nível experiência (0-5)"));
        form.add(nivelField);

        form.add(new JLabel());
        form.add(criarButton);

        return form;
    }

    private JScrollPane buildList() {
        JList<Cliente> clientesList = new JList<>(clientesModel);
        JScrollPane listPane = new JScrollPane(clientesList);
        listPane.setBorder(BorderFactory.createTitledBorder("Clientes"));
        return listPane;
    }

    public void refreshData() {
        clientesModel.clear();

        for (Cliente cliente : clienteService.listarClientes()) {
            clientesModel.addElement(cliente);
        }
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erro", JOptionPane.ERROR_MESSAGE);
    }
}