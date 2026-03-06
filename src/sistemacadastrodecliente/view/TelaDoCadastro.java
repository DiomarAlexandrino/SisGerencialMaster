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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.ParseException;
import java.util.List;
import sistemacadastrodecliente.util.CampoDataComCalendario;
import sistemacadastrodecliente.util.ControleEstadoTela;

import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import sistemacadastrodecliente.model.enums.EstadoTela;
import sistemacadastrodecliente.util.ValidadorCEP;
import sistemacadastrodecliente.util.ValidadorCEP.DadosCEP;
import java.awt.event.ActionListener;
import sistemacadastrodecliente.util.ComboBoxUF;

public final class TelaDoCadastro extends JFrame {

    private ControleDoCadastro controller = new ControleDoCadastro();

    private JButton btnSalvar, btnEditar, btnExcluir, btnCancelar, btnNovo;
    private JComboBox<TemaEnum> cbTema;
    private TemaEnum temaSelecionado = TemaEnum.CLARO;

    private JTextField txtNome, txtEmail, txtEndereco, txtNumero, txtCidade, txtBairro;
    private JFormattedTextField txtCPF, txtTelefone, txtCEP;
    private CampoDataComCalendario campoDataNascimento;
    private ComboBoxUF cbEstado;
    private JTextArea txtObservacao;

    private final Border bordaNeutra = BorderFactory.createLineBorder(Color.GRAY);

    private JTable tabelaClientes;
    private DefaultTableModel modelTabela;
    private int idSelecionado = -1;

    private final ClienteDAO clienteDAO = new ClienteDAO();
    private ValidadorFormulario validadorFormulario;
    private ControleEstadoTela controle = null;
    private int linhaModel;
    private int clienteId;
    private Cliente cliente;
    private JPanel painelFormulario;
    private boolean atualizandoCEP = false;

    private ValidadorCEP.DadosCEP ultimoCepBuscado;

    ValidadorCEP.DadosCEP dados = null;
    private JPanel painelAviso;
    private JButton btnFecharAviso;

    public TelaDoCadastro() {
        inicializarTela();

        inicializarTabelaEControle(
                painelFormulario,
                btnNovo,
                btnEditar,
                btnExcluir,
                btnSalvar,
                btnCancelar
        );

        selecionandoClienteDaTabela();

        inicializarValidacoes();

        inicializarOuvintes();     // listeners depois
        carregarTabela();

        // Listener do CEP
        txtCEP.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!atualizandoCEP) {
                    buscarCEP();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!atualizandoCEP) {
                    buscarCEP();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (!atualizandoCEP) {
                    buscarCEP();
                }
            }
        });

        limparCampos();
        setVisible(true);

        aplicarTemaSelecionado();
    }

    private JPanel criarPainelCliente() {

        JPanel painel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        // Nome
        c.gridx = 0;
        c.gridy = 1;
        painel.add(new JLabel("Nome:"), c);
        c.gridx = 1;
        c.gridwidth = 3;
        txtNome = new JTextField(30);
        painel.add(txtNome, c);

        // CPF
        c.gridx = 0;
        c.gridy = 2;
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
        c.gridy = 3;
        c.gridwidth = 3;
        painel.add(new JLabel("E-mail:"), c);
        c.gridx = 1;
        txtEmail = new JTextField(30);
        painel.add(txtEmail, c);

        // Telefone
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 1;
        painel.add(new JLabel("Telefone:"), c);
        c.gridx = 1;
        txtTelefone = new JFormattedTextField(criarMascara("(##) #####-####"));
        painel.add(txtTelefone, c);

        // CEP
        c.gridx = 0;
        c.gridy = 5;
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
        c.gridy = 6;
        painel.add(new JLabel("Nº:"), c);
        c.gridx = 1;
        txtNumero = new JTextField(10);
        painel.add(txtNumero, c);

        // Bairro
        c.gridx = 2;
        painel.add(new JLabel("Bairro:"), c);
        c.gridx = 3;
        txtBairro = new JTextField(20);
        painel.add(txtBairro, c);

        // Estado
        c.gridx = 0;
        c.gridy = 7;
        painel.add(new JLabel("Estado:"), c);
        c.gridx = 1;
        cbEstado = new ComboBoxUF();
        painel.add(cbEstado, c);

        // Cidade
        c.gridx = 2;
        painel.add(new JLabel("Cidade:"), c);
        c.gridx = 3;
        txtCidade = new JTextField(20);
        painel.add(txtCidade, c);

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

    private void inicializarTabelaEControle(JPanel painelFormulario, JButton btnNovo, JButton btnEditar, JButton btnExcluir, JButton btnSalvar, JButton btnCancelar) {

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
        // Registrar campos no ValidadorFormulario
        validadorFormulario.registrarCampo(txtNome, TipoValidacao.NOME_OBRIGATORIO);
        validadorFormulario.registrarCampo(txtEmail, TipoValidacao.EMAIL);
        validadorFormulario.registrarCampo(txtCPF, TipoValidacao.CPF);
        validadorFormulario.registrarCampo(txtTelefone, TipoValidacao.TELEFONE);
        validadorFormulario.registrarCampo(txtCEP, TipoValidacao.CEP);
        validadorFormulario.registrarCampo(txtEndereco, TipoValidacao.ENDERECO_OBRIGATORIO);
        validadorFormulario.registrarCampo(txtBairro, TipoValidacao.BAIRRO_OBRIGATORIO);
        validadorFormulario.registrarCampo(txtCidade, TipoValidacao.CIDADE_OBRIGATORIA);
        validadorFormulario.registrarCampo(cbEstado, TipoValidacao.COMBO_OBRIGATORIO);
        validadorFormulario.registrarCampo(txtNumero, TipoValidacao.NUMERO_OBRIGATORIO);
        validadorFormulario.registrarCampo(campoDataNascimento, TipoValidacao.DATA_OBRIGATORIA);

        // =============================
        // Listener especial para CEP
        // =============================
        txtCEP.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String cep = txtCEP.getText().replaceAll("\\D", "");
                if (cep.length() != 8) {
                    return; // popup de CEP inválido já é mostrado pelo ValidadorFormulario
                }

                reiniciarCamposEndereco(); // limpa cidade, bairro, estado etc.

                // Busca assíncrona do CEP
                new Thread(() -> {
                    ValidadorCEP.DadosCEP dados = ValidadorCEP.buscarDados(cep);

                    if (dados != null) {
                        ultimoCepBuscado = dados;
                    }

                    SwingUtilities.invokeLater(() -> {
                        if (dados != null) {
                            preencherCamposEndereco(dados);
                        } else {
                            // mostra popup usando ValidadorFormulario
                            validadorFormulario.mostrarPopupAviso(
                                    "Não foi possível localizar o CEP informado.", txtCEP
                            );
                        }
                    });
                }).start();
            }
        });
    }

    /**
     * Preenche os campos de endereço com os dados retornados do CEP.
     */
    private void preencherCamposEndereco(DadosCEP dados) {
        if (dados == null) {
            return;
        }

        // Logradouro
        if (dados.logradouro != null && !dados.logradouro.isBlank()) {
            txtEndereco.setText(dados.logradouro);
            txtEndereco.setEnabled(false);
        } else {
            txtEndereco.setText("");
            txtEndereco.setEnabled(true);
        }

        // Bairro
        if (dados.bairro != null && !dados.bairro.isBlank()) {
            txtBairro.setText(dados.bairro);
            txtBairro.setEnabled(false);
        } else {
            txtBairro.setText("");
            txtBairro.setEnabled(true);
        }

        // Cidade
        if (dados.cidade != null && !dados.cidade.isBlank()) {
            txtCidade.setText(dados.cidade);
            txtCidade.setEnabled(false);
        } else {
            txtCidade.setText("");
            txtCidade.setEnabled(true);
        }

        // UF
        if (dados.uf != null && !dados.uf.isBlank()) {
            for (UF ufEnum : UF.values()) {
                if (ufEnum.getSigla().equalsIgnoreCase(dados.uf)) {
                    cbEstado.setSelectedItem(ufEnum);
                    cbEstado.setEnabled(false);
                    break;
                }
            }
        } else {
            cbEstado.setSelectedItem(UF.UF);
            cbEstado.setEnabled(true);
        }

        // Formatar CEP
        String cepNumeros = txtCEP.getText().replaceAll("\\D", "");
        if (cepNumeros.length() == 8) {
            txtCEP.setText(cepNumeros.replaceAll("(\\d{5})(\\d{3})", "$1-$2"));
        }

        // Focar no número
        txtNumero.requestFocusInWindow();
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

        if (cbEstado != null) {
            cbEstado.aplicarTema(temaSelecionado);
        }
    }

    private void inicializarValidacoes() {

        validadorFormulario = new ValidadorFormulario(this);
        validadorFormulario.setControle(this.controle);

        // =====================
        // Campos obrigatórios
        // =====================
        validadorFormulario.registrarCampo(txtNome, TipoValidacao.NOME_OBRIGATORIO);

        validadorFormulario.registrarCampo(txtEndereco, TipoValidacao.ENDERECO_OBRIGATORIO);

        validadorFormulario.registrarCampo(txtBairro, TipoValidacao.BAIRRO_OBRIGATORIO);

        validadorFormulario.registrarCampo(txtNumero, TipoValidacao.NUMERO_OBRIGATORIO);

        validadorFormulario.registrarCampo(txtEmail, TipoValidacao.EMAIL);

        validadorFormulario.registrarCampo(txtCPF, TipoValidacao.CPF);
        validadorFormulario.registrarCampo(txtTelefone, TipoValidacao.TELEFONE);

        validadorFormulario.registrarCampo(txtCEP, TipoValidacao.CEP);

        validadorFormulario.registrarCampo(cbEstado, TipoValidacao.COMBO_OBRIGATORIO);
        validadorFormulario.registrarCampo(campoDataNascimento, TipoValidacao.DATA_OBRIGATORIA);

        // =====================
        // Revalidação automática do CEP
        // =====================
        // Quando a cidade perder foco
        txtCidade.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                validadorFormulario.revalidarCEP();
            }
        });
        validadorFormulario.registrarCampo(txtCidade, TipoValidacao.CIDADE_OBRIGATORIA);
        // Quando o estado (UF) for alterado
        cbEstado.addActionListener(e -> validadorFormulario.revalidarCEP());
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
            txtBairro.setText(c.getBairro() != null ? c.getBairro() : "");
            txtCidade.setText(c.getCidade() != null ? c.getCidade() : "");

            // Estado
            if (c.getUf() != null && !c.getUf().isEmpty() && !c.getUf().equalsIgnoreCase("UF")) {
                try {
                    cbEstado.setSelectedUF(c.getUf());
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
            
              cbEstado.setEnabled(false);  // Desabilitado no modo navegação
              cbEstado.aplicarTema(temaSelecionado); 
             
             
            txtObservacao.setText(c.getObservacao() != null ? c.getObservacao() : "");

            // Forçar atualização da interface
            revalidate();
            repaint();

            // Focar no formulário
            txtNome.requestFocusInWindow();

            System.out.println("✅ Campos preenchidos para cliente ID: " + clienteId + " - " + c.getNome());
        });

        atualizarTemaComboEstado();
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
        txtBairro.setText("");
        txtBairro.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        txtBairro.setToolTipText(null);
        txtNumero.setText("");
        txtNumero.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        txtNumero.setToolTipText(null);
        txtCidade.setText("");
        txtCidade.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        txtCidade.setToolTipText(null);
        cbEstado.setSelectedItem(UF.UF);
        cbEstado.setEnabled(false);  // Desabilitado no modo navegação
        cbEstado.aplicarTema(temaSelecionado);  // Forçar tema
        txtObservacao.setText("");

        tabelaClientes.clearSelection();

       
        

    }

    private Cliente RegistrarClientedoFormulario() {

        if (!validadorFormulario.formularioValido()) {
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
        c.setBairro(txtBairro.getText().trim());
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
        int linhaModel = tabelaClientes.convertRowIndexToModel(linha);
        idSelecionado = (int) modelTabela.getValueAt(linhaModel, 0);
        String cpf = modelTabela.getValueAt(linhaModel, 2).toString();

        Cliente c = clienteDAO.buscarPorCpf(cpf);
        if (c == null) {
            return;
        }

        txtNome.setText(c.getNome());
        txtCPF.setText(c.getCpf());

        campoDataNascimento.setDate(c.getDataNascimento());
        campoDataNascimento.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        txtEmail.setText(c.getEmail());
        txtTelefone.setText(c.getTelefone());
        txtCEP.setText(c.getCep());
        txtEndereco.setText(c.getEndereco());
        txtBairro.setText(c.getBairro());
        txtNumero.setText(c.getNumero());
        txtCidade.setText(c.getCidade());
        cbEstado.setSelectedItem(UF.valueOf(c.getUf()));
        txtObservacao.setText(c.getObservacao());
        atualizarTemaComboEstado();
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
        clienteId = -1; // Nenhum cliente selecionadobusca

        // MUDA O ESTADO PARA INSERINDO (isso habilita os campos)
        controle.setEstado(EstadoTela.ADICIONANDO);

        // Habilita explicitamente os campos que podem estar desabilitados
        campoDataNascimento.setEnabledCampo(true);

        // Habilita campos de endereço (caso estejam desabilitados por busca de CEP)
        txtEndereco.setEnabled(true);
        txtBairro.setEnabled(true);
        txtCidade.setEnabled(true);
        cbEstado.setEnabled(true);

        // Desmarca qualquer seleção na tabela
        tabelaClientes.clearSelection();

        // Dá foco ao primeiro campo
        txtNome.requestFocusInWindow();

        campoDataNascimento.setOnChange(() -> {
            boolean valido = validadorFormulario.validarCampoSemPopup(campoDataNascimento);
            controle.atualizarFormularioValido(valido);
        });

        aplicarTemaSelecionado();
    }

    private void acaoCancelar() {

        SwingUtilities.invokeLater(() -> limparCampos());

        campoDataNascimento.setEnabledCampo(false);
        controle.setEstado(EstadoTela.NAVEGANDO);

        SwingUtilities.invokeLater(() -> {
            aplicarTemaSelecionado();

            // 🔥 FORÇA O COMBO APÓS ENABLE/DISABLE
            Tema.aplicarTemaCombo(cbEstado, temaSelecionado);
            cbEstado.setEnabled(true); // garante estado visual correto
            cbEstado.repaint();
        });
    }

    private void salvarCliente() {
        cliente = controller.getClienteFromFields(this);
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

    public void bloquearCidadeEstado(boolean bloquear) {
        txtCidade.setEnabled(!bloquear);
        cbEstado.setEnabled(!bloquear);
    }

    public void reiniciarCamposEndereco() {
        // Limpa o texto
        txtEndereco.setText("");
        txtBairro.setText("");
        txtCidade.setText("");
        cbEstado.setSelectedItem(UF.UF); // Ou o seu valor padrão

        // Reativa os campos
        txtEndereco.setEnabled(true);
        txtBairro.setEnabled(true);
        txtCidade.setEnabled(true);
        cbEstado.setEnabled(true);

    }

    /**
     * Método para forçar a atualização do tema do ComboBox de estado
     */
    private void atualizarTemaComboEstado() {
        if (cbEstado != null && temaSelecionado != null) {
            SwingUtilities.invokeLater(() -> {
                Tema.aplicarTemaCombo(cbEstado, temaSelecionado);

                // Truque adicional: forçar redesenho
                cbEstado.revalidate();
                cbEstado.repaint();

                // Forçar atualização do LookAndFeel para este componente
                cbEstado.updateUI();
            });
        }
    }

    // Adicione estes getters na classe TelaDoCadastro:
    public ValidadorFormulario getValidador() {
        return validadorFormulario;
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

    private void buscarCEP() {

        if (controle.getEstadoAtual() == EstadoTela.NAVEGANDO) {
            return;
        }

        if (atualizandoCEP) {
            return;
        }

        String cep = txtCEP.getText().replaceAll("\\D", "");
        if (cep.length() != 8) {
            return;
        }

        SwingWorker<DadosCEP, Void> worker = new SwingWorker<>() {
            @Override
            protected DadosCEP doInBackground() throws Exception {
                return ValidadorCEP.buscarDados(cep); // ← Faltava o ponto e vírgula
            } // ← Faltava fechar o método

            @Override
            protected void done() { // ← Faltava o 'protected'
                try {
                    DadosCEP dados = get();

                    if (dados != null) {
                        atualizandoCEP = true;
                        preencherCamposEndereco(dados);

                        // Mostrar popup de sucesso
                        String mensagem = String.format(
                                "<html>✅ CEP encontrado!<br>%s, %s<br>%s - %s</html>",
                                dados.logradouro != null ? dados.logradouro : "Logradouro não informado",
                                dados.bairro != null ? dados.bairro : "Bairro não informado",
                                dados.cidade != null ? dados.cidade : "Cidade não informada",
                                dados.uf != null ? dados.uf : "UF não informada"
                        );

                        validadorFormulario.mostrarPopupAviso(mensagem, txtCEP);

                        atualizandoCEP = false;
                    } else {
                        reiniciarCamposEndereco();

                        // Mostrar popup de erro
                        validadorFormulario.mostrarPopupAviso(
                                "❌ CEP não encontrado. Verifique o número digitado.",
                                txtCEP
                        );
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    validadorFormulario.mostrarPopupAviso(
                            "❌ Erro ao buscar CEP: " + e.getMessage(),
                            txtCEP
                    );
                } finally {
                    atualizandoCEP = false; // Garante que a flag seja resetada mesmo em caso de erro
                }
            }
        };

        worker.execute();
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

    public void setCbEstado(ComboBoxUF cbEstado) {
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

    private EstadoTela estadoAtual = EstadoTela.NAVEGANDO; // ou outro estado inicial

    public EstadoTela getEstadoAtual() {
        return estadoAtual;
    }

    public void setEstadoAtual(EstadoTela novoEstado) {
        this.estadoAtual = novoEstado;
    }

    public ValidadorCEP.DadosCEP getUltimoCepBuscado() {
        return ultimoCepBuscado;
    }

    public void setUltimoCepBuscado(ValidadorCEP.DadosCEP dados) {
        this.ultimoCepBuscado = dados;
    }

    public JTextField getTxtBairro() {
        return txtBairro;
    }

    public void setTxtBairro(JTextField txtBairro) {
        this.txtBairro = txtBairro;
    }

}
