package pt.kartodromo.desktop.ui.clientes;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import pt.kartodromo.core.bll.ClienteService;
import pt.kartodromo.core.model.Cliente;

public class ClientePanel extends JPanel {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ClienteService clienteService =
            new ClienteService();

    private Long clienteSelecionadoId = null;

    private final JTextField nomeField =
            new JTextField();

    private final JTextField dataNascimentoField =
            new JTextField("2000-01-01");

    private final JTextField emailField =
            new JTextField();

    private final JTextField nivelField =
            new JTextField("0");

    private final DefaultTableModel clientesTableModel =
            new DefaultTableModel(
                    new Object[]{
                            "ID",
                            "Nome",
                            "Data Nascimento",
                            "Email",
                            "Nível Experiência"
                    },
                    0
            ) {
                @Override
                public boolean isCellEditable(
                        int row,
                        int column) {
                    return false;
                }
            };

    private final JTable clientesTable =
            new JTable(clientesTableModel);

    public ClientePanel() {

        setLayout(
                new BorderLayout(12,12)
        );

        setBorder(
                BorderFactory.createEmptyBorder(
                        12,12,12,12
                )
        );

        add(
                buildForm(),
                BorderLayout.NORTH
        );

        add(
                buildTable(),
                BorderLayout.CENTER
        );

        refreshData();
    }

    private JPanel buildForm() {

        JButton criarButton =
                new JButton("Criar cliente");

        JButton atualizarButton =
                new JButton("Atualizar cliente");

        JButton removerButton =
                new JButton("Remover cliente");

        JButton limparButton =
                new JButton("Limpar seleção");


        criarButton.addActionListener(
                e -> criarCliente()
        );

        atualizarButton.addActionListener(
                e -> atualizarCliente()
        );

        removerButton.addActionListener(
                e -> removerCliente()
        );

        limparButton.addActionListener(
                e -> limparFormulario()
        );

        JPanel form =
                new JPanel(
                        new GridLayout(
                                7,
                                2,
                                8,
                                8
                        )
                );

        form.setBorder(
                BorderFactory.createTitledBorder(
                        "Gestão de cliente"
                )
        );

        form.add(new JLabel("Nome"));
        form.add(nomeField);

        form.add(
                new JLabel(
                        "Data nascimento (yyyy-MM-dd)"
                )
        );
        form.add(dataNascimentoField);

        form.add(
                new JLabel("Email")
        );
        form.add(emailField);

        form.add(
                new JLabel(
                        "Nível experiência (0-5)"
                )
        );
        form.add(nivelField);

        form.add(criarButton);
        form.add(atualizarButton);

        form.add(removerButton);
        form.add(limparButton);

        return form;
    }

    private JScrollPane buildTable() {

        clientesTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        clientesTable.setAutoCreateRowSorter(
                true
        );

        clientesTable
                .getSelectionModel()
                .addListSelectionListener(e -> {

                    if(!e.getValueIsAdjusting()){
                        carregarClienteSelecionado();
                    }

                });

        JScrollPane scroll =
                new JScrollPane(
                        clientesTable
                );

        scroll.setBorder(
                BorderFactory.createTitledBorder(
                        "Clientes"
                )
        );

        return scroll;
    }

    private void criarCliente() {

        try {

            Cliente cliente =
                    clienteService.criarCliente(
                            nomeField.getText().trim(),
                            LocalDate.parse(
                                    dataNascimentoField
                                            .getText()
                                            .trim(),
                                    DATE_FORMAT
                            ),
                            emailField.getText().trim(),
                            Integer.parseInt(
                                    nivelField
                                            .getText()
                                            .trim()
                            )
                    );

            showInfo(
                    "Cliente criado com sucesso: "
                            + cliente.getId()
            );

            limparFormulario();
            refreshData();

        }

        catch(DateTimeParseException ex){
            showError(
                    "Data inválida."
            );
        }

        catch(NumberFormatException ex){
            showError(
                    "Nível inválido."
            );
        }

        catch(RuntimeException ex){
            showError(
                    ex.getMessage()
            );
        }
    }

    private void atualizarCliente() {

        if(clienteSelecionadoId == null){
            showError(
                    "Selecione um cliente."
            );
            return;
        }

        try{

            clienteService.atualizarCliente(
                    clienteSelecionadoId,
                    nomeField.getText().trim(),
                    LocalDate.parse(
                            dataNascimentoField
                                    .getText()
                                    .trim(),
                            DATE_FORMAT
                    ),
                    emailField.getText().trim(),
                    Integer.parseInt(
                            nivelField
                                    .getText()
                                    .trim()
                    )
            );

            showInfo(
                    "Cliente atualizado."
            );

            limparFormulario();
            refreshData();

        }

        catch(Exception ex){
            showError(
                    ex.getMessage()
            );
        }
    }

    private void removerCliente() {

        if(clienteSelecionadoId == null){
            showError(
                    "Selecione cliente para remover."
            );
            return;
        }

        int confirmacao =
                JOptionPane.showConfirmDialog(
                        this,
                        "Deseja remover o cliente?",
                        "Confirmar remoção",
                        JOptionPane.YES_NO_OPTION
                );

        if(confirmacao != JOptionPane.YES_OPTION){
            return;
        }

        try{

            clienteService.removerCliente(
                    clienteSelecionadoId
            );

            showInfo(
                    "Cliente removido com sucesso."
            );

            limparFormulario();
            refreshData();

        }

        catch(Exception ex){
            showError(
                    ex.getMessage()
            );
        }
    }

    private void carregarClienteSelecionado(){

        int selectedRow =
                clientesTable.getSelectedRow();

        if(selectedRow==-1){
            return;
        }

        int modelRow =
                clientesTable
                        .convertRowIndexToModel(
                                selectedRow
                        );

        clienteSelecionadoId =
                (Long) clientesTableModel
                        .getValueAt(
                                modelRow,
                                0
                        );

        nomeField.setText(
                clientesTableModel
                        .getValueAt(
                                modelRow,
                                1
                        ).toString()
        );

        dataNascimentoField.setText(
                clientesTableModel
                        .getValueAt(
                                modelRow,
                                2
                        ).toString()
        );

        emailField.setText(
                clientesTableModel
                        .getValueAt(
                                modelRow,
                                3
                        ).toString()
        );

        nivelField.setText(
                clientesTableModel
                        .getValueAt(
                                modelRow,
                                4
                        ).toString()
        );
    }

    private void limparFormulario(){

        clienteSelecionadoId=null;

        nomeField.setText("");
        emailField.setText("");
        nivelField.setText("0");

        dataNascimentoField.setText(
                "2000-01-01"
        );

        clientesTable.clearSelection();
    }

    public void refreshData(){

        clientesTableModel.setRowCount(
                0
        );

        for(
                Cliente c :
                clienteService.listarClientes()
        ){

            clientesTableModel.addRow(
                    new Object[]{
                            c.getId(),
                            c.getNome(),
                            c.getDataNascimento(),
                            c.getEmail(),
                            c.getNivelExperiencia()
                    }
            );

        }
    }

    private void showInfo(String msg){
        JOptionPane.showMessageDialog(
                this,
                msg,
                "Sucesso",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showError(String msg){
        JOptionPane.showMessageDialog(
                this,
                msg,
                "Erro",
                JOptionPane.ERROR_MESSAGE
        );
    }
}