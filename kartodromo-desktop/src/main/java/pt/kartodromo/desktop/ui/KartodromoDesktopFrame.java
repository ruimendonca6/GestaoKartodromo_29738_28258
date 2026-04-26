package pt.kartodromo.desktop.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import pt.kartodromo.core.bll.CategoriaKartService;
import pt.kartodromo.core.bll.ClienteService;
import pt.kartodromo.core.bll.CorridaService;
import pt.kartodromo.core.bll.KartService;
import pt.kartodromo.core.bll.ReservaService;
import pt.kartodromo.core.model.CategoriaKart;
import pt.kartodromo.core.model.Cliente;
import pt.kartodromo.core.model.Corrida;
import pt.kartodromo.core.model.Kart;
import pt.kartodromo.core.model.Reserva;
import pt.kartodromo.core.model.enums.KartEstado;
import pt.kartodromo.core.model.enums.ReservaEstado;

public class KartodromoDesktopFrame extends JFrame {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ClienteService clienteService = new ClienteService();
    private final CategoriaKartService categoriaService = new CategoriaKartService();
    private final CorridaService corridaService = new CorridaService();
    private final KartService kartService = new KartService();
    private final ReservaService reservaService = new ReservaService();

    private final DefaultListModel<Cliente> clientesModel = new DefaultListModel<>();
    private final DefaultListModel<CategoriaKart> categoriasModel = new DefaultListModel<>();
    private final DefaultListModel<Corrida> corridasModel = new DefaultListModel<>();
    private final DefaultListModel<Kart> kartsModel = new DefaultListModel<>();
    private final DefaultListModel<String> reservasModel = new DefaultListModel<>();
    private final DefaultListModel<Reserva> reservasAtivasModel = new DefaultListModel<>();

    private final DefaultComboBoxModel<Cliente> clienteReservaComboModel = new DefaultComboBoxModel<>();
    private final DefaultComboBoxModel<Kart> kartReservaComboModel = new DefaultComboBoxModel<>();
    private final DefaultComboBoxModel<CategoriaKart> categoriaCorridaComboModel = new DefaultComboBoxModel<>();
    private final DefaultComboBoxModel<CategoriaKart> categoriaKartComboModel = new DefaultComboBoxModel<>();
    private final JList<Reserva> reservasAtivasList = new JList<>(reservasAtivasModel);

    public KartodromoDesktopFrame() {
        setTitle("Kartodromo - Desktop");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Clientes", buildClientesTab());
        tabs.addTab("Categorias", buildCategoriasTab());
        tabs.addTab("Karts", buildKartsTab());
        tabs.addTab("Corridas", buildCorridasTab());
        tabs.addTab("Reservas", buildReservasTab());

        add(tabs, BorderLayout.CENTER);
        refreshAllData();
    }

    private JPanel buildClientesTab() {
        JTextField nomeField = new JTextField();
        JTextField dataNascimentoField = new JTextField("2000-01-01");
        JTextField emailField = new JTextField();
        JTextField nivelField = new JTextField("0");

        JButton criarButton = new JButton("Criar cliente");
        criarButton.addActionListener(e -> {
            try {
                Cliente cliente = clienteService.criarCliente(
                        nomeField.getText(),
                        LocalDate.parse(dataNascimentoField.getText().trim(), DATE_FORMAT),
                        emailField.getText(),
                        Integer.parseInt(nivelField.getText().trim())
                );
                showInfo("Cliente criado com sucesso: " + cliente.getId());
                nomeField.setText("");
                emailField.setText("");
                nivelField.setText("0");
                refreshAllData();
            } catch (DateTimeParseException ex) {
                showError("Data de nascimento invalida. Formato: yyyy-MM-dd.");
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
        form.add(new JLabel("Nivel experiencia (0-5)"));
        form.add(nivelField);
        form.add(new JLabel());
        form.add(criarButton);

        JList<Cliente> clientesList = new JList<>(clientesModel);
        JScrollPane listPane = new JScrollPane(clientesList);
        listPane.setBorder(BorderFactory.createTitledBorder("Clientes"));

        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.add(form, BorderLayout.NORTH);
        panel.add(listPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildCategoriasTab() {
        JTextField cilindradaField = new JTextField("270");
        JTextField descricaoField = new JTextField();
        JTextField idadeMinimaField = new JTextField("12");
        JTextField experienciaMinimaField = new JTextField("0");
        JTextField precoBaseField = new JTextField("25.00");

        JButton criarButton = new JButton("Criar categoria");
        criarButton.addActionListener(e -> {
            try {
                CategoriaKart categoria = categoriaService.criarCategoria(
                        Integer.parseInt(cilindradaField.getText().trim()),
                        descricaoField.getText(),
                        Integer.parseInt(idadeMinimaField.getText().trim()),
                        Integer.parseInt(experienciaMinimaField.getText().trim()),
                        new BigDecimal(precoBaseField.getText().trim())
                );
                showInfo("Categoria criada com sucesso: " + categoria.getId());
                descricaoField.setText("");
                refreshAllData();
            } catch (RuntimeException ex) {
                showError(ex.getMessage());
            }
        });

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Nova categoria"));
        form.add(new JLabel("Cilindrada"));
        form.add(cilindradaField);
        form.add(new JLabel("Descricao"));
        form.add(descricaoField);
        form.add(new JLabel("Idade minima"));
        form.add(idadeMinimaField);
        form.add(new JLabel("Experiencia minima (0-5)"));
        form.add(experienciaMinimaField);
        form.add(new JLabel("Preco base"));
        form.add(precoBaseField);
        form.add(new JLabel());
        form.add(criarButton);

        JList<CategoriaKart> categoriasList = new JList<>(categoriasModel);
        JScrollPane listPane = new JScrollPane(categoriasList);
        listPane.setBorder(BorderFactory.createTitledBorder("Categorias"));

        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.add(form, BorderLayout.NORTH);
        panel.add(listPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildKartsTab() {
        JTextField numeroField = new JTextField("1");
        JComboBox<CategoriaKart> categoriaCombo = new JComboBox<>(categoriaKartComboModel);
        JComboBox<KartEstado> estadoCombo = new JComboBox<>(KartEstado.values());
        JCheckBox disponivelCheck = new JCheckBox("Disponivel", true);

        JButton criarButton = new JButton("Criar kart");
        criarButton.addActionListener(e -> {
            try {
                CategoriaKart categoria = (CategoriaKart) categoriaCombo.getSelectedItem();
                if (categoria == null) {
                    showError("Crie uma categoria antes de criar karts.");
                    return;
                }

                KartEstado estado = (KartEstado) estadoCombo.getSelectedItem();
                Kart kart = kartService.criarKart(
                        Integer.parseInt(numeroField.getText().trim()),
                        estado,
                        disponivelCheck.isSelected(),
                        categoria.getId()
                );

                showInfo("Kart criado com sucesso: " + kart.getId());
                numeroField.setText("");
                disponivelCheck.setSelected(true);
                estadoCombo.setSelectedItem(KartEstado.OPERACIONAL);
                refreshAllData();
            } catch (RuntimeException ex) {
                showError(ex.getMessage());
            }
        });

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Novo kart"));
        form.add(new JLabel("Numero"));
        form.add(numeroField);
        form.add(new JLabel("Categoria"));
        form.add(categoriaCombo);
        form.add(new JLabel("Estado"));
        form.add(estadoCombo);
        form.add(new JLabel("Disponibilidade"));
        form.add(disponivelCheck);
        form.add(new JLabel());
        form.add(criarButton);

        JList<Kart> kartsList = new JList<>(kartsModel);
        JScrollPane listPane = new JScrollPane(kartsList);
        listPane.setBorder(BorderFactory.createTitledBorder("Karts"));

        JComboBox<KartEstado> novoEstadoCombo = new JComboBox<>(KartEstado.values());
        JCheckBox novaDisponibilidadeCheck = new JCheckBox("Disponivel");

        JButton atualizarEstadoButton = new JButton("Atualizar estado");
        atualizarEstadoButton.addActionListener(e -> {
            Kart kartSelecionado = kartsList.getSelectedValue();
            if (kartSelecionado == null) {
                showError("Selecione um kart para atualizar o estado.");
                return;
            }
            try {
                KartEstado novoEstado = (KartEstado) novoEstadoCombo.getSelectedItem();
                kartService.atualizarEstado(kartSelecionado.getId(), novoEstado);
                showInfo("Estado atualizado com sucesso.");
                refreshAllData();
            } catch (RuntimeException ex) {
                showError(ex.getMessage());
            }
        });

        JButton atualizarDisponibilidadeButton = new JButton("Atualizar disponibilidade");
        atualizarDisponibilidadeButton.addActionListener(e -> {
            Kart kartSelecionado = kartsList.getSelectedValue();
            if (kartSelecionado == null) {
                showError("Selecione um kart para atualizar disponibilidade.");
                return;
            }
            try {
                kartService.definirDisponibilidade(
                        kartSelecionado.getId(),
                        novaDisponibilidadeCheck.isSelected()
                );
                showInfo("Disponibilidade atualizada com sucesso.");
                refreshAllData();
            } catch (RuntimeException ex) {
                showError(ex.getMessage());
            }
        });

        kartsList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            Kart kartSelecionado = kartsList.getSelectedValue();
            if (kartSelecionado != null) {
                novoEstadoCombo.setSelectedItem(kartSelecionado.getEstado());
                novaDisponibilidadeCheck.setSelected(kartSelecionado.isDisponivel());
            }
        });

        JPanel updatePanel = new JPanel(new GridLayout(2, 3, 8, 8));
        updatePanel.setBorder(BorderFactory.createTitledBorder("Gestao do kart selecionado"));
        updatePanel.add(new JLabel("Novo estado"));
        updatePanel.add(novoEstadoCombo);
        updatePanel.add(atualizarEstadoButton);
        updatePanel.add(new JLabel("Disponibilidade"));
        updatePanel.add(novaDisponibilidadeCheck);
        updatePanel.add(atualizarDisponibilidadeButton);

        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.add(form, BorderLayout.NORTH);
        panel.add(listPane, BorderLayout.CENTER);
        panel.add(updatePanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildCorridasTab() {
        JTextField dataHoraField = new JTextField("2030-01-01 10:00");
        JTextField duracaoField = new JTextField("15");
        JTextField vagasField = new JTextField("10");
        JTextField layoutField = new JTextField("Pista Completa");
        JComboBox<CategoriaKart> categoriaCombo = new JComboBox<>(categoriaCorridaComboModel);

        JButton criarButton = new JButton("Criar corrida");
        criarButton.addActionListener(e -> {
            try {
                CategoriaKart categoria = (CategoriaKart) categoriaCombo.getSelectedItem();
                if (categoria == null) {
                    showError("Crie uma categoria antes de criar corridas.");
                    return;
                }

                Corrida corrida = corridaService.criarCorrida(
                        LocalDateTime.parse(dataHoraField.getText().trim(), DATE_TIME_FORMAT),
                        Integer.parseInt(duracaoField.getText().trim()),
                        Integer.parseInt(vagasField.getText().trim()),
                        categoria.getId(),
                        layoutField.getText()
                );
                showInfo("Corrida criada com sucesso: " + corrida.getId());
                refreshAllData();
            } catch (DateTimeParseException ex) {
                showError("Data/hora invalida. Formato: yyyy-MM-dd HH:mm.");
            } catch (RuntimeException ex) {
                showError(ex.getMessage());
            }
        });

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Nova corrida"));
        form.add(new JLabel("Data/hora (yyyy-MM-dd HH:mm)"));
        form.add(dataHoraField);
        form.add(new JLabel("Duracao (minutos)"));
        form.add(duracaoField);
        form.add(new JLabel("Vagas maximas"));
        form.add(vagasField);
        form.add(new JLabel("Layout"));
        form.add(layoutField);
        form.add(new JLabel("Categoria"));
        form.add(categoriaCombo);
        form.add(new JLabel());
        form.add(criarButton);

        JList<Corrida> corridasList = new JList<>(corridasModel);
        JScrollPane listPane = new JScrollPane(corridasList);
        listPane.setBorder(BorderFactory.createTitledBorder("Corridas"));

        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.add(form, BorderLayout.NORTH);
        panel.add(listPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildReservasTab() {
        JComboBox<Cliente> clienteCombo = new JComboBox<>(clienteReservaComboModel);
        JComboBox<Kart> kartCombo = new JComboBox<>(kartReservaComboModel);
        JComboBox<ReservaEstado> estadoCombo = new JComboBox<>(ReservaEstado.values());
        JTextField pistaField = new JTextField("Pista Completa");
        JTextField inicioField = new JTextField("2030-01-01 10:00");
        JTextField fimField = new JTextField("2030-01-01 10:30");

        clienteCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus
            ) {
                String texto = "";
                if (value instanceof Cliente) {
                    texto = ((Cliente) value).getNome();
                }
                return super.getListCellRendererComponent(list, texto, index, isSelected, cellHasFocus);
            }
        });

        kartCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus
            ) {
                String texto = "";
                if (value instanceof Kart) {
                    texto = String.valueOf(((Kart) value).getNumero());
                }
                return super.getListCellRendererComponent(list, texto, index, isSelected, cellHasFocus);
            }
        });

        JButton criarButton = new JButton("Criar reserva");
        criarButton.addActionListener(e -> {
            try {
                Cliente cliente = (Cliente) clienteCombo.getSelectedItem();
                Kart kart = (Kart) kartCombo.getSelectedItem();
                ReservaEstado estado = (ReservaEstado) estadoCombo.getSelectedItem();

                if (cliente == null || kart == null || estado == null) {
                    showError("Selecione cliente, kart e estado.");
                    return;
                }

                Reserva reserva = reservaService.criarReserva(
                        cliente.getId(),
                        kart.getId(),
                        pistaField.getText(),
                        LocalDateTime.parse(inicioField.getText().trim(), DATE_TIME_FORMAT),
                        LocalDateTime.parse(fimField.getText().trim(), DATE_TIME_FORMAT),
                        estado
                );
                showInfo("Reserva criada com sucesso: " + reserva.getId());
                refreshAllData();
                reservasAtivasList.setSelectedValue(reserva, true);
            } catch (DateTimeParseException ex) {
                showError("Data/hora invalida. Formato: yyyy-MM-dd HH:mm.");
            } catch (RuntimeException ex) {
                showError(ex.getMessage());
            }
        });

        JButton atualizarButton = new JButton("Atualizar reserva selecionada");
        atualizarButton.addActionListener(e -> {
            Reserva reservaSelecionada = reservasAtivasList.getSelectedValue();
            if (reservaSelecionada == null) {
                showError("Selecione uma reserva para atualizar.");
                return;
            }
            try {
                Cliente cliente = (Cliente) clienteCombo.getSelectedItem();
                Kart kart = (Kart) kartCombo.getSelectedItem();
                ReservaEstado estado = (ReservaEstado) estadoCombo.getSelectedItem();
                if (cliente == null || kart == null || estado == null) {
                    showError("Selecione cliente, kart e estado.");
                    return;
                }

                reservaService.atualizarReserva(
                        reservaSelecionada.getId(),
                        cliente.getId(),
                        kart.getId(),
                        pistaField.getText(),
                        LocalDateTime.parse(inicioField.getText().trim(), DATE_TIME_FORMAT),
                        LocalDateTime.parse(fimField.getText().trim(), DATE_TIME_FORMAT),
                        estado
                );
                showInfo("Reserva atualizada com sucesso.");
                refreshAllData();
            } catch (DateTimeParseException ex) {
                showError("Data/hora invalida. Formato: yyyy-MM-dd HH:mm.");
            } catch (RuntimeException ex) {
                showError(ex.getMessage());
            }
        });

        JButton eliminarButton = new JButton("Eliminar reserva selecionada");
        eliminarButton.addActionListener(e -> {
            Reserva reservaSelecionada = reservasAtivasList.getSelectedValue();
            if (reservaSelecionada == null) {
                showError("Selecione uma reserva para eliminar.");
                return;
            }
            try {
                reservaService.eliminarReserva(reservaSelecionada.getId());
                showInfo("Reserva eliminada com sucesso.");
                refreshAllData();
            } catch (RuntimeException ex) {
                showError(ex.getMessage());
            }
        });

        JButton listarButton = new JButton("Listar todas as reservas");
        listarButton.addActionListener(e -> loadReservas());

        reservasAtivasList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            Reserva reservaSelecionada = reservasAtivasList.getSelectedValue();
            if (reservaSelecionada == null) {
                return;
            }
            clienteCombo.setSelectedItem(reservaSelecionada.getCliente());
            kartCombo.setSelectedItem(reservaSelecionada.getKart());
            estadoCombo.setSelectedItem(reservaSelecionada.getEstado());
            pistaField.setText(reservaSelecionada.getPistaNome());
            inicioField.setText(reservaSelecionada.getDataHoraInicio().format(DATE_TIME_FORMAT));
            fimField.setText(reservaSelecionada.getDataHoraFim().format(DATE_TIME_FORMAT));
        });

        JPanel form = new JPanel(new GridLayout(8, 2, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Gestao de reservas"));
        form.add(new JLabel("Cliente"));
        form.add(clienteCombo);
        form.add(new JLabel("Kart"));
        form.add(kartCombo);
        form.add(new JLabel("Pista"));
        form.add(pistaField);
        form.add(new JLabel("Inicio (yyyy-MM-dd HH:mm)"));
        form.add(inicioField);
        form.add(new JLabel("Fim (yyyy-MM-dd HH:mm)"));
        form.add(fimField);
        form.add(new JLabel("Estado"));
        form.add(estadoCombo);
        form.add(criarButton);
        form.add(atualizarButton);
        form.add(eliminarButton);
        form.add(listarButton);

        reservasAtivasList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            String text = "Reserva #" + value.getId()
                    + " | Cliente: " + value.getCliente().getNome()
                    + " | Kart #" + value.getKart().getNumero()
                    + " | Pista: " + value.getPistaNome()
                    + " | " + value.getDataHoraInicio().format(DATE_TIME_FORMAT)
                    + " -> " + value.getDataHoraFim().format(DATE_TIME_FORMAT)
                    + " | " + value.getEstado();
            return new javax.swing.DefaultListCellRenderer()
                    .getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
        });

        JScrollPane listPane = new JScrollPane(reservasAtivasList);
        listPane.setBorder(BorderFactory.createTitledBorder("Reservas"));

        JList<String> reservasInfoList = new JList<>(reservasModel);
        JScrollPane infoPane = new JScrollPane(reservasInfoList);
        infoPane.setBorder(BorderFactory.createTitledBorder("Resumo"));

        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.add(form, BorderLayout.NORTH);
        panel.add(listPane, BorderLayout.CENTER);
        panel.add(infoPane, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshAllData() {
        refreshClientes();
        refreshCategorias();
        refreshKarts();
        refreshCorridas();
        loadReservas();
        refreshResumoReservas();
    }

    private void refreshClientes() {
        replaceModel(clientesModel, clienteService.listarClientes());
        replaceComboModel(clienteReservaComboModel, clienteService.listarClientes());
    }

    private void refreshCategorias() {
        List<CategoriaKart> categorias = categoriaService.listarCategorias();
        replaceModel(categoriasModel, categorias);
        replaceComboModel(categoriaCorridaComboModel, categorias);
        replaceComboModel(categoriaKartComboModel, categorias);
    }

    private void refreshKarts() {
        List<Kart> karts = kartService.listarKarts();
        replaceModel(kartsModel, karts);
        replaceComboModel(kartReservaComboModel, karts);
    }

    private void refreshCorridas() {
        List<Corrida> corridas = corridaService.listarCorridas();
        replaceModel(corridasModel, corridas);
    }

    private void refreshResumoReservas() {
        List<Reserva> reservas = reservaService.listarReservas();
        int pendentes = 0;
        int confirmadas = 0;
        int canceladas = 0;
        for (Reserva reserva : reservas) {
            if (reserva.getEstado() == ReservaEstado.PENDENTE) {
                pendentes++;
            } else if (reserva.getEstado() == ReservaEstado.CONFIRMADA) {
                confirmadas++;
            } else if (reserva.getEstado() == ReservaEstado.CANCELADA) {
                canceladas++;
            }
        }

        reservasModel.clear();
        reservasModel.addElement("Clientes registados: " + clienteService.listarClientes().size());
        reservasModel.addElement("Categorias disponiveis: " + categoriaService.listarCategorias().size());
        reservasModel.addElement("Karts registados: " + kartService.listarKarts().size());
        reservasModel.addElement("Corridas registadas: " + corridaService.listarCorridas().size());
        reservasModel.addElement("Reservas totais: " + reservas.size());
        reservasModel.addElement("Reservas pendentes: " + pendentes);
        reservasModel.addElement("Reservas confirmadas: " + confirmadas);
        reservasModel.addElement("Reservas canceladas: " + canceladas);
    }

    private void loadReservas() {
        replaceModel(reservasAtivasModel, reservaService.listarReservas());
    }

    private <T> void replaceModel(DefaultListModel<T> model, List<T> items) {
        model.clear();
        for (T item : items) {
            model.addElement(item);
        }
    }

    private <T> void replaceComboModel(DefaultComboBoxModel<T> model, List<T> items) {
        model.removeAllElements();
        for (T item : items) {
            model.addElement(item);
        }
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erro", JOptionPane.ERROR_MESSAGE);
    }
}
