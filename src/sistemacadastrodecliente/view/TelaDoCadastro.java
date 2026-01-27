package sistemacadastrodecliente.view;

import sistemacadastrodecliente.controller.ControleDoCadastro;
import sistemacadastrodecliente.dao.ClienteDAO;
import sistemacadastrodecliente.model.Cliente;
import sistemacadastrodecliente.model.enums.UF;
import sistemacadastrodecliente.model.enums.TipoValidacao;
import sistemacadastrodecliente.util.ValidadorFormulario;
import sistemacadastrodecliente.view.temas.Tema;
import sistemacadastrodecliente.view.temas.TemaEnum;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.text.ParseException;
import java.util.List;
import sistemacadastrodecliente.util.CampoDataComCalendario;
import sistemacadastrodecliente.util.ControleEstadoTela;

import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import sistemacadastrodecliente.model.enums.EstadoTela;

public final class TelaDoCadastro extends JFrame {

    private ControleDoCadastro controller = new ControleDoCadastro();

    private JButton btnSalvar, btnEditar, btnExcluir, btnCancelar, btnNovo;
    private JComboBox<TemaEnum> cbTema;
    private TemaEnum temaSelecionado = TemaEnum.CLARO;

    private JTextField txtNome, txtEmail, txtEndereco, txtNumero, txtCidade, txtCpf, txtDataNascimento;
    private JFormattedTextField txtCPF, txtTelefone, txtCEP;
    private CampoDataComCalendario campoDataNascimento;
    private JComboBox<UF> cbEstado;
    private JTextArea txtObservacao;

    private final Border bordaNeutra = BorderFactory.createLineBorder(Color.GRAY);

    private JTable tabelaClientes;
    private DefaultTableModel modelTabela;
    private int idSelecionado = -1;

    private final ClienteDAO clienteDAO = new ClienteDAO();
    private ValidadorFormulario validador;
    private ControleEstadoTela controle = null;
    private int linhaModel;
    private int clienteId;
    private Cliente cliente;
    private JPanel painelFormulario;

    public TelaDoCadastro() {
        inicializarTela();

        inicializarTabelaEControle(
                painelFormulario,
                btnNovo,
                btnEditar,
                btnExcluir,
                btnSalvar,
                btnCancelar,
                campoDataNascimento
        );

        selecionandoClienteDaTabela();
        inicializarOuvintes();
        carregarTabela();
        inicializarValidacoes();
        aplicarTemaSelecionado();
        limparCampos();
        setVisible(true);

    }

    private JPanel criarPainelCliente() {

        JPanel painel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        // Nome
        c.gridx = 0;
        c.gridy = 0;
        painel.add(new JLabel("Nome:"), c);
        c.gridx = 1;
        c.gridwidth = 3;
        txtNome = new JTextField(30);
        painel.add(txtNome, c);

        // CPF
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        painel.add(new JLabel("CPF:"), c);
        c.gridx = 1;
        txtCPF = new JFormattedTextField(criarMascara("###.###.###-##"));
        painel.add(txtCPF, c);

        // Data Nascimento
        c.gridx = 2;
        painel.add(new JLabel("Data Nasc:"), c);
        c.gridx = 3;
        campoDataNascimento = new CampoDataComCalendario();
        painel.add(campoDataNascimento, c);

        // Email
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 3;
        painel.add(new JLabel("E-mail:"), c);
        c.gridx = 1;
        txtEmail = new JTextField(30);
        painel.add(txtEmail, c);

        // Telefone
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 1;
        painel.add(new JLabel("Telefone:"), c);
        c.gridx = 1;
        txtTelefone = new JFormattedTextField(criarMascara("(##) #####-####"));
        painel.add(txtTelefone, c);

        // CEP
        c.gridx = 0;
        c.gridy = 4;
        painel.add(new JLabel("CEP:"), c);
        c.gridx = 1;
        txtCEP = new JFormattedTextField(criarMascara("#####-###"));
        painel.add(txtCEP, c);

        // Endereço
        c.gridx = 2;
        painel.add(new JLabel("Endereço:"), c);
        c.gridx = 3;
        txtEndereco = new JTextField(30);
        painel.add(txtEndereco, c);

        // Nº
        c.gridx = 0;
        c.gridy = 5;
        painel.add(new JLabel("Nº:"), c);
        c.gridx = 1;
        txtNumero = new JTextField(10);
        painel.add(txtNumero, c);

        // Cidade
        c.gridx = 2;
        painel.add(new JLabel("Cidade:"), c);
        c.gridx = 3;
        txtCidade = new JTextField(20);
        painel.add(txtCidade, c);

        // Estado
        c.gridx = 0;
        c.gridy = 6;
        painel.add(new JLabel("Estado:"), c);
        c.gridx = 1;
        cbEstado = new JComboBox<>(UF.values());
        cbEstado.setSelectedItem(UF.UF);
        painel.add(cbEstado, c);

        return painel;
    }

    private MaskFormatter criarMascara(String mask) {
        try {
            MaskFormatter mf = new MaskFormatter(mask);
            mf.setPlaceholderCharacter('_');
            return mf;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void inicializarTabelaEControle(JPanel painelFormulario,
            JButton btnNovo,
            JButton btnEditar,
            JButton btnExcluir,
            JButton btnSalvar,
            JButton btnCancelar,
            CampoDataComCalendario campoDataNascimento) {

        // ============================
        // INICIALIZA MODELO E TABELA
        // ============================
        modelTabela = new DefaultTableModel(
                new Object[]{"ID", "Nome", "CPF", "Telefone", "Cidade"}, 0
        );
        tabelaClientes = new JTable(modelTabela);

        // Oculta coluna de ID
        tabelaClientes.removeColumn(tabelaClientes.getColumnModel().getColumn(0));
        // Coloca a tabela dentro de um scroll
        JScrollPane scrollTabela = new JScrollPane(tabelaClientes);
        scrollTabela.setPreferredSize(new Dimension(1000, 200));
        add(scrollTabela, BorderLayout.SOUTH);

        // ============================
        // CONFIGURA CONTROLE DE ESTADO
        // ============================
        controle = new ControleEstadoTela(
                tabelaClientes,
                painelFormulario,
                btnNovo,
                btnEditar,
                btnExcluir,
                btnSalvar,
                btnCancelar,
                campoDataNascimento
        );

        // ============================
        // Configura seleção de linha na tabela
        // ============================
        tabelaClientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Atualiza estado inicial
        controle.setEstado(EstadoTela.NAVEGANDO);
    }

    private void selecionandoClienteDaTabela() {
        tabelaClientes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int linhaSelecionada = tabelaClientes.getSelectedRow();

                if (linhaSelecionada != -1) {
                    // Converter para índice do modelo (porque a tabela pode estar ordenada/filtrada)
                    int linhaModelo = tabelaClientes.convertRowIndexToModel(linhaSelecionada);
                    Object idObj = modelTabela.getValueAt(linhaModelo, 0); // ID está na coluna 0

                    if (idObj != null) {
                        try {
                            int id = Integer.parseInt(idObj.toString());

                            // Buscar cliente no DAO
                            Cliente clienteSelecionado = clienteDAO.buscarPorId(id);

                            if (clienteSelecionado != null) {
                                preencherCamposCliente(clienteSelecionado);
                                controle.setEstado(EstadoTela.NAVEGANDO);
                            } else {
                                System.out.println("Cliente não encontrado para ID: " + id);
                                limparCampos();
                            }
                        } catch (NumberFormatException ex) {
                            System.out.println("Erro ao converter ID: " + ex.getMessage());
                        }
                    }
                } else {
                    // Nenhuma linha selecionada
                    limparCampos();
                    controle.setEstado(EstadoTela.NAVEGANDO);
                }
            }
        });
    }

    private void inicializarOuvintes() {
        // =========================
        // BOTÃO NOVO
        // =========================
        btnNovo.addActionListener(e -> {
            idSelecionado = -1;
            limparCampos();
            controle.setEstado(EstadoTela.ADICIONANDO);
            campoDataNascimento.setEnabledCampo(true);
        });

        // =========================
        // BOTÃO EDITAR
        // =========================
        btnEditar.addActionListener(e -> {
            int linhaSelecionada = tabelaClientes.getSelectedRow();
            if (linhaSelecionada != -1) {
                // Converter para índice do modelo
                int linhaModelo = tabelaClientes.convertRowIndexToModel(linhaSelecionada);
                Object idObj = modelTabela.getValueAt(linhaModelo, 0);

                if (idObj != null) {
                    try {
                        idSelecionado = Integer.parseInt(idObj.toString());
                        carregarClienteSelecionado();
                        controle.setEstado(EstadoTela.EDITANDO);
                        campoDataNascimento.setEnabledCampo(true);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Erro ao obter ID do cliente: " + ex.getMessage());
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Selecione um cliente para editar!");
            }
        });

        // =========================
        // BOTÃO CANCELAR
        // =========================
        btnCancelar.addActionListener(e -> {
            controle.setEstado(EstadoTela.NAVEGANDO);
            campoDataNascimento.setEnabledCampo(false);
            campoDataNascimento.setBorder(bordaNeutra);
        });

        // =========================
        // PROTEÇÃO DA TABELA (não clicável se desabilitada)
        // =========================
        tabelaClientes.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (!tabelaClientes.isEnabled()) {
                    e.consume();
                }
            }
        });

        // =========================
        // VALIDAÇÃO DO COMBO
        // =========================
        cbEstado.addActionListener(e -> validador.validarCampo(cbEstado));

    }

    private void aplicarTemaSelecionado() {
        Tema.aplicarTema(getContentPane(), temaSelecionado);
        if (tabelaClientes != null) {
            tabelaClientes.setBackground(temaSelecionado.getBackground());
            tabelaClientes.setForeground(temaSelecionado.getForeground());
            tabelaClientes.getTableHeader().setBackground(temaSelecionado.getBackground().darker());
            tabelaClientes.getTableHeader().setForeground(temaSelecionado.getForeground());
        }
        if (campoDataNascimento != null) {
            campoDataNascimento.aplicarTema(temaSelecionado);
        }
    }

    private void inicializarValidacoes() {
        validador = new ValidadorFormulario();

        // Campos obrigatórios
        validador.registrarCampo(txtNome, TipoValidacao.TEXTO_OBRIGATORIO);
        validador.registrarCampo(txtNome, TipoValidacao.CAMPO_MAX_80);
        validador.registrarCampo(txtEndereco, TipoValidacao.TEXTO_OBRIGATORIO);
        validador.registrarCampo(txtEndereco, TipoValidacao.CAMPO_MAX_80);
        validador.registrarCampo(txtNumero, TipoValidacao.TEXTO_OBRIGATORIO);
        validador.registrarCampo(txtNumero, TipoValidacao.CAMPO_MAX_80);
        validador.registrarCampo(txtCidade, TipoValidacao.TEXTO_OBRIGATORIO);
        validador.registrarCampo(txtCidade, TipoValidacao.CAMPO_MAX_80);

        // Email
        validador.registrarCampo(txtEmail, TipoValidacao.EMAIL);

        // Campos formatados
        validador.registrarCampo(txtCPF, TipoValidacao.CPF); // valida CPF real
        validador.registrarCampo(txtTelefone, TipoValidacao.TELEFONE);
        validador.registrarCampo(txtCEP, TipoValidacao.CEP); // valida CEP real

        // Combo e data
        validador.registrarCampo(cbEstado, TipoValidacao.COMBO_OBRIGATORIO);
        validador.registrarCampo(campoDataNascimento, TipoValidacao.DATA_OBRIGATORIA);
    }

    void carregarTabela() {
        modelTabela.setRowCount(0);
        List<Cliente> clientes = clienteDAO.listar();

        for (Cliente c : clientes) {

            String telefone = c.getTelefone();
            String telefoneFormatado = "";

            if (telefone != null && !telefone.isEmpty()) {
                telefoneFormatado = telefone.replaceFirst(
                        "(\\d{2})(\\d{5})(\\d{4})",
                        "($1) $2-$3"
                );
            }

            modelTabela.addRow(new Object[]{
                c.getId(),
                c.getNome(),
                c.getCpf(),
                telefoneFormatado,
                c.getCidade()
            });
        }
    }

    private void preencherCamposCliente(Cliente c) {
        if (c == null) {
            System.out.println("⚠️ Cliente é null em preencherCamposCliente");
            return;
        }

        clienteId = c.getId();
        idSelecionado = c.getId();

        // Usar SwingUtilities para garantir que a atualização seja feita na thread da UI
        SwingUtilities.invokeLater(() -> {
            txtNome.setText(c.getNome() != null ? c.getNome() : "");

            // CPF formatado
            if (c.getCpf() != null) {
                String cpfFormatado = c.getCpf().replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
                txtCPF.setText(cpfFormatado);
            } else {
                txtCPF.setText("");
            }

            // Data de nascimento
            if (c.getDataNascimento() != null) {
                campoDataNascimento.setDate(c.getDataNascimento());
            } else {
                campoDataNascimento.resetarVisual();
            }

            txtEmail.setText(c.getEmail() != null ? c.getEmail() : "");

            // Telefone formatado
            if (c.getTelefone() != null && c.getTelefone().length() >= 11) {
                String telFormatado = c.getTelefone().replaceAll("(\\d{2})(\\d{5})(\\d{4})", "($1) $2-$3");
                txtTelefone.setText(telFormatado);
            } else {
                txtTelefone.setText(c.getTelefone() != null ? c.getTelefone() : "");
            }

            // CEP formatado
            if (c.getCep() != null && c.getCep().length() >= 8) {
                String cepFormatado = c.getCep().replaceAll("(\\d{5})(\\d{3})", "$1-$2");
                txtCEP.setText(cepFormatado);
            } else {
                txtCEP.setText(c.getCep() != null ? c.getCep() : "");
            }

            txtEndereco.setText(c.getEndereco() != null ? c.getEndereco() : "");
            txtNumero.setText(c.getNumero() != null ? c.getNumero() : "");
            txtCidade.setText(c.getCidade() != null ? c.getCidade() : "");

            // Estado
            if (c.getUf() != null && !c.getUf().isEmpty() && !c.getUf().equalsIgnoreCase("UF")) {
                try {
                    cbEstado.setSelectedItem(UF.valueOf(c.getUf()));
                } catch (IllegalArgumentException e) {
                    // Se não encontrar o estado, tenta buscar pela sigla
                    boolean encontrado = false;
                    for (UF uf : UF.values()) {
                        if (uf.getSigla().equalsIgnoreCase(c.getUf())) {
                            cbEstado.setSelectedItem(uf);
                            encontrado = true;
                            break;
                        }
                    }
                    if (!encontrado) {
                        cbEstado.setSelectedItem(UF.UF);
                    }
                }
            } else {
                cbEstado.setSelectedItem(UF.UF);
            }

            txtObservacao.setText(c.getObservacao() != null ? c.getObservacao() : "");

            // Forçar atualização da interface
            revalidate();
            repaint();

            // Focar no formulário
            txtNome.requestFocusInWindow();

            System.out.println("✅ Campos preenchidos para cliente ID: " + clienteId + " - " + c.getNome());
        });
    }

    void limparCampos() {

        clienteId = -1;

        campoDataNascimento.resetarVisual();
        txtNome.setText("");
        txtNome.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        txtNome.setToolTipText(null);
        txtCPF.setText("");
        txtCPF.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        txtCPF.setToolTipText(null);
        campoDataNascimento.setDate(null);
        campoDataNascimento.setCampoValido(null);
        campoDataNascimento.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        txtEmail.setText("");
        txtEmail.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        txtEmail.setToolTipText(null);
        txtTelefone.setText("");
        txtTelefone.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        txtTelefone.setToolTipText(null);
        txtCEP.setText("");
        txtCEP.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        txtCEP.setToolTipText(null);
        txtEndereco.setText("");
        txtEndereco.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        txtEndereco.setToolTipText(null);
        txtNumero.setText("");
        txtNumero.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        txtNumero.setToolTipText(null);
        txtCidade.setText("");
        txtCidade.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        txtCidade.setToolTipText(null);
        cbEstado.setSelectedItem(UF.UF);
        cbEstado.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        cbEstado.setToolTipText(null);
        txtObservacao.setText("");

        tabelaClientes.clearSelection();

    }

    private Cliente RegistrarClientedoFormulario() {
        if (!validador.formularioValido()) {
            JOptionPane.showMessageDialog(this, "Preencha corretamente os campos destacados em vermelho.");
            return null;
        }

        Cliente c = new Cliente();
        c.setNome(txtNome.getText().trim());
        c.setCpf(txtCPF.getText().replaceAll("[^0-9]", ""));
        c.setDataNascimento(campoDataNascimento.getDate());
        c.setEmail(txtEmail.getText().trim());
        c.setTelefone(txtTelefone.getText().replaceAll("[^0-9]", ""));
        c.setEndereco(txtEndereco.getText().trim());
        c.setNumero(txtNumero.getText().trim());
        c.setCidade(txtCidade.getText().trim());
        c.setUf(((UF) cbEstado.getSelectedItem()).getSigla());
        c.setCep(txtCEP.getText().replace("-", "").trim());
        c.setObservacao(txtObservacao.getText().trim());

        return c;
    }

    private void excluirCliente() {
        boolean excluiu = controller.excluirCliente(this);
        if (excluiu) {
            carregarTabela();
            limparCampos();
            controle.setEstado(EstadoTela.NAVEGANDO);
        }
    }

    private void carregarClienteSelecionado() {
        // Pega a linha selecionada da tabela
        int linha = tabelaClientes.getSelectedRow();
        if (linha == -1) {
            return; // nenhuma linha selecionada
        }

        // Converter índice da view para o model
        // SEMPRE usar o MODEL
        idSelecionado = (int) modelTabela.getValueAt(linhaModel, 0);
        String cpf = modelTabela.getValueAt(linhaModel, 2).toString();

        Cliente c = clienteDAO.buscarPorCpf(cpf);
        if (c == null) {
            return;
        }

        txtNome.setText(c.getNome());
        txtCPF.setText(c.getCpf());
        campoDataNascimento.setDate(c.getDataNascimento());
        txtEmail.setText(c.getEmail());
        txtTelefone.setText(c.getTelefone());
        txtCEP.setText(c.getCep());
        txtEndereco.setText(c.getEndereco());
        txtNumero.setText(c.getNumero());
        txtCidade.setText(c.getCidade());
        cbEstado.setSelectedItem(UF.valueOf(c.getUf()));
        txtObservacao.setText(c.getObservacao());
    }

    private void inicializarTela() {
        // Configurações da janela
        setTitle("Cadastro de Cliente");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // ========================= Painel topo: tema =========================
        JPanel painelTopo = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        painelTopo.add(new JLabel("Tema:"));
        cbTema = new JComboBox<>(TemaEnum.values());
        cbTema.setSelectedItem(temaSelecionado);
        cbTema.addActionListener(e -> {
            temaSelecionado = (TemaEnum) cbTema.getSelectedItem();
            aplicarTemaSelecionado();
        });
        painelTopo.add(cbTema);
        add(painelTopo, BorderLayout.NORTH);

        // ========================= Painel formulário =========================
        painelFormulario = criarPainelCliente();

        txtObservacao = new JTextArea(8, 50);
        JScrollPane scrollObs = new JScrollPane(txtObservacao);

        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Cliente", painelFormulario);
        abas.addTab("Observação", scrollObs);

        // ========================= Painel botões =========================
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));

        btnNovo = new JButton("Novo", criarIcone("novo.png"));
        btnEditar = new JButton("Editar", criarIcone("editar.png"));
        btnExcluir = new JButton("Excluir", criarIcone("excluir.png"));
        btnSalvar = new JButton("Salvar", criarIcone("salvar.png"));
        btnCancelar = new JButton("Cancelar", criarIcone("cancelar.png"));
        btnNovo.addActionListener(e -> acaoNovo());
        btnEditar.addActionListener(e -> editarCliente());
        btnExcluir.addActionListener(e -> excluirCliente());
        btnSalvar.addActionListener(e -> salvarCliente());
        btnCancelar.addActionListener(e -> acaoCancelar());

        painelBotoes.add(btnNovo);
        painelBotoes.add(btnEditar);
        painelBotoes.add(btnExcluir);
        painelBotoes.add(btnSalvar);
        painelBotoes.add(btnCancelar);

        // ========================= Painel central =========================
        JPanel painelCentral = new JPanel(new BorderLayout());
        painelCentral.add(abas, BorderLayout.CENTER);
        painelCentral.add(painelBotoes, BorderLayout.SOUTH);

        add(painelCentral, BorderLayout.CENTER);
    }

    private void acaoNovo() {
        // Limpa o formulário
        limparCampos();

        // Prepara para adicionar novo cliente
        clienteId = -1; // Nenhum cliente selecionado
        campoDataNascimento.setEnabledCampo(true); // habilita o campo de data
        tabelaClientes.clearSelection(); // desmarca qualquer seleção na tabela
    }

    private void acaoCancelar() {
        // Limpa os campos
        limparCampos();

        // Desativa campos que não devem ser editados
        campoDataNascimento.setEnabledCampo(false);

        // Retorna o estado da tela para "navegando"
        controle.setEstado(EstadoTela.NAVEGANDO);
    }

    private void salvarCliente() {
        Cliente cliente = controller.getClienteFromFields(this);
        if (cliente == null) {
            return;
        }

        boolean sucesso = controller.salvarCliente(this, cliente, clienteId);

        if (sucesso) {
            if (clienteId == -1) {
                JOptionPane.showMessageDialog(
                        this,
                        "Cliente cadastrado com sucesso!",
                        "Cadastro",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Cliente atualizado com sucesso!",
                        "Atualização",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

            clienteId = -1; // 🔥 ESSENCIAL
            carregarTabela();
            limparCampos();
            controle.setEstado(EstadoTela.NAVEGANDO);
        }
    }

    private void editarCliente() {
        boolean ok = controller.editarCliente(this, tabelaClientes, modelTabela);

        if (ok) {
            controle.setEstado(EstadoTela.EDITANDO);
            campoDataNascimento.setEnabledCampo(true);
        }
    }

    // Adicione estes getters na classe TelaDoCadastro:
    public ValidadorFormulario getValidador() {
        return validador;
    }

    public int getClienteId() {
        return clienteId;
    }

    public ControleEstadoTela getControleEstado() {
        return controle;
    }

    public EstadoTela getEstadoTelaEnum() {
        return EstadoTela.NAVEGANDO; // ou implemente conforme sua necessidade
    }

    public void setClienteId(int id) {
        if (id <= 0) {
            limparCampos();
            clienteId = -1;
            controle.setEstado(EstadoTela.NAVEGANDO);
            return;
        }

        // Buscar cliente pelo ID
        Cliente clienteSelecionado = clienteDAO.buscarPorId(id);
        if (clienteSelecionado != null) {
            clienteId = clienteSelecionado.getId();
            preencherCamposCliente(clienteSelecionado);

            // Selecionar na tabela, se estiver visível
            for (int i = 0; i < modelTabela.getRowCount(); i++) {
                if ((int) modelTabela.getValueAt(i, 0) == id) {
                    int linhaView = tabelaClientes.convertRowIndexToView(i);
                    tabelaClientes.setRowSelectionInterval(linhaView, linhaView);
                    break;
                }
            }

            controle.setEstado(EstadoTela.NAVEGANDO);
        } else {
            JOptionPane.showMessageDialog(this, "Cliente não encontrado para ID: " + id);
            limparCampos();
            clienteId = -1;
            controle.setEstado(EstadoTela.NAVEGANDO);
        }
    }

    public ControleDoCadastro getController() {
        return controller;
    }

    public void setController(ControleDoCadastro controller) {
        this.controller = controller;
    }

    public JButton getBtnSalvar() {
        return btnSalvar;
    }

    public void setBtnSalvar(JButton btnSalvar) {
        this.btnSalvar = btnSalvar;
    }

    public JButton getBtnEditar() {
        return btnEditar;
    }

    public void setBtnEditar(JButton btnEditar) {
        this.btnEditar = btnEditar;
    }

    public JButton getBtnExcluir() {
        return btnExcluir;
    }

    public void setBtnExcluir(JButton btnExcluir) {
        this.btnExcluir = btnExcluir;
    }

    public JButton getBtnCancelar() {
        return btnCancelar;
    }

    public void setBtnCancelar(JButton btnCancelar) {
        this.btnCancelar = btnCancelar;
    }

    public JButton getBtnNovo() {
        return btnNovo;
    }

    public void setBtnNovo(JButton btnNovo) {
        this.btnNovo = btnNovo;
    }

    public JComboBox<TemaEnum> getCbTema() {
        return cbTema;
    }

    public void setCbTema(JComboBox<TemaEnum> cbTema) {
        this.cbTema = cbTema;
    }

    public TemaEnum getTemaSelecionado() {
        return temaSelecionado;
    }

    public void setTemaSelecionado(TemaEnum temaSelecionado) {
        this.temaSelecionado = temaSelecionado;
    }

    public JTextField getTxtNome() {
        return txtNome;
    }

    public void setTxtNome(JTextField txtNome) {
        this.txtNome = txtNome;
    }

    public JTextField getTxtEmail() {
        return txtEmail;
    }

    public void setTxtEmail(JTextField txtEmail) {
        this.txtEmail = txtEmail;
    }

    public JTextField getTxtEndereco() {
        return txtEndereco;
    }

    public void setTxtEndereco(JTextField txtEndereco) {
        this.txtEndereco = txtEndereco;
    }

    public JTextField getTxtNumero() {
        return txtNumero;
    }

    public void setTxtNumero(JTextField txtNumero) {
        this.txtNumero = txtNumero;
    }

    public JTextField getTxtCidade() {
        return txtCidade;
    }

    public void setTxtCidade(JTextField txtCidade) {
        this.txtCidade = txtCidade;
    }

    public JTextField getTxtCpf() {
        return txtCpf;
    }

    public void setTxtCpf(JTextField txtCpf) {
        this.txtCpf = txtCpf;
    }

    public JTextField getTxtDataNascimento() {
        return txtDataNascimento;
    }

    public void setTxtDataNascimento(JTextField txtDataNascimento) {
        this.txtDataNascimento = txtDataNascimento;
    }

    public JFormattedTextField getTxtCPF() {
        return txtCPF;
    }

    public void setTxtCPF(JFormattedTextField txtCPF) {
        this.txtCPF = txtCPF;
    }

    public JFormattedTextField getTxtTelefone() {
        return txtTelefone;
    }

    public void setTxtTelefone(JFormattedTextField txtTelefone) {
        this.txtTelefone = txtTelefone;
    }

    public JFormattedTextField getTxtCEP() {
        return txtCEP;
    }

    public void setTxtCEP(JFormattedTextField txtCEP) {
        this.txtCEP = txtCEP;
    }

    public CampoDataComCalendario getCampoDataNascimento() {
        return campoDataNascimento;
    }

    public void setCampoDataNascimento(CampoDataComCalendario campoDataNascimento) {
        this.campoDataNascimento = campoDataNascimento;
    }

    public JComboBox<UF> getCbEstado() {
        return cbEstado;
    }

    public void setCbEstado(JComboBox<UF> cbEstado) {
        this.cbEstado = cbEstado;
    }

    public JTextArea getTxtObservacao() {
        return txtObservacao;
    }

    public void setTxtObservacao(JTextArea txtObservacao) {
        this.txtObservacao = txtObservacao;
    }

    public JTable getTabelaClientes() {
        return tabelaClientes;
    }

    public void setTabelaClientes(JTable tabelaClientes) {
        this.tabelaClientes = tabelaClientes;
    }

    public DefaultTableModel getModelTabela() {
        return modelTabela;
    }

    public void setModelTabela(DefaultTableModel modelTabela) {
        this.modelTabela = modelTabela;
    }

    public int getIdSelecionado() {
        return idSelecionado;
    }

    public void setIdSelecionado(int idSelecionado) {
        this.idSelecionado = idSelecionado;
    }

    public ControleEstadoTela getControle() {
        return controle;
    }

    public void setControle(EstadoTela controle) {
        this.controle.setEstado(controle);
    }

    public int getLinhaModel() {
        return linhaModel;
    }

    public void setLinhaModel(int linhaModel) {
        this.linhaModel = linhaModel;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public ClienteDAO getClienteDAO() {
        return clienteDAO;
    }

    public JPanel getPainelFormulario() {
        return painelFormulario;
    }

    public void setPainelFormulario(JPanel painelFormulario) {
        this.painelFormulario = painelFormulario;
    }

    private ImageIcon criarIcone(String nomeArquivo) {
        // Caminho absoluto do pacote icons
        java.net.URL url = getClass().getResource("/sistemacadastrodecliente/view/icons/" + nomeArquivo);
        if (url != null) {
            return new ImageIcon(url);
        } else {
            System.err.println("Ícone não encontrado: " + nomeArquivo);
            return null;
        }
    }
}
