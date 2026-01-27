package sistemacadastrodecliente.controller;

import sistemacadastrodecliente.dao.ClienteDAO;
import sistemacadastrodecliente.model.Cliente;
import sistemacadastrodecliente.model.enums.UF;
import sistemacadastrodecliente.util.CampoDataComCalendario;
import sistemacadastrodecliente.view.TelaDoCadastro;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import sistemacadastrodecliente.model.enums.EstadoTela;
import static sistemacadastrodecliente.model.enums.EstadoTela.NAVEGANDO;
import sistemacadastrodecliente.util.ControleEstadoTela;
import sistemacadastrodecliente.util.ValidadorCPF;
import sistemacadastrodecliente.view.TelaDoCadastro;

public class ControleDoCadastro {

    private ClienteDAO clienteDAO;

    public ControleDoCadastro() {
        this.clienteDAO = new ClienteDAO();
    }

    // ================== PREENCHER CAMPOS ==================
    public void preencherCamposParaEdicao(
            TelaDoCadastro tela,
            Cliente cliente,
            CampoDataComCalendario campoDataNascimento) {

        if (cliente == null) {
            return;
        }

        tela.setClienteId(cliente.getId());
        tela.getTxtNome().setText(cliente.getNome() != null ? cliente.getNome() : "");

        // CPF
        if (cliente.getCpf() != null) {
            String cpfFormatado = cliente.getCpf().replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
            tela.getTxtCPF().setText(cpfFormatado);
        }

        // Data de nascimento - Date -> LocalDate
        LocalDate dataNascimento = tela.getCampoDataNascimento().getDate();
        cliente.setDataNascimento(dataNascimento);

        tela.getTxtEmail().setText(cliente.getEmail() != null ? cliente.getEmail() : "");

        // Telefone
        if (cliente.getTelefone() != null && cliente.getTelefone().length() >= 11) {
            String telFormatado = cliente.getTelefone().replaceAll("(\\d{2})(\\d{5})(\\d{4})", "($1) $2-$3");
            tela.getTxtTelefone().setText(telFormatado);
        } else {
            tela.getTxtTelefone().setText(cliente.getTelefone() != null ? cliente.getTelefone() : "");
        }

        // CEP
        if (cliente.getCep() != null && cliente.getCep().length() >= 8) {
            String cepFormatado = cliente.getCep().replaceAll("(\\d{5})(\\d{3})", "$1-$2");
            tela.getTxtCEP().setText(cepFormatado);
        } else {
            tela.getTxtCEP().setText(cliente.getCep() != null ? cliente.getCep() : "");
        }

        tela.getTxtEndereco().setText(cliente.getEndereco() != null ? cliente.getEndereco() : "");
        tela.getTxtNumero().setText(cliente.getNumero() != null ? cliente.getNumero() : "");
        tela.getTxtCidade().setText(cliente.getCidade() != null ? cliente.getCidade() : "");
        tela.getTxtObservacao().setText(cliente.getObservacao() != null ? cliente.getObservacao() : "");

        // Estado
        if (cliente.getUf() != null && !cliente.getUf().isEmpty()) {
            try {
                tela.getCbEstado().setSelectedItem(UF.valueOf(cliente.getUf()));
            } catch (IllegalArgumentException e) {
                for (UF uf : UF.values()) {
                    if (uf.getSigla().equalsIgnoreCase(cliente.getUf())) {
                        tela.getCbEstado().setSelectedItem(uf);
                        break;
                    }
                }
            }
        }
    }

    // ================== SALVAR CLIENTE ==================
  public boolean salvarCliente(TelaDoCadastro tela, Cliente cliente, int clienteId) {

    try {
        // 1️⃣ Validação do celular
        String telefone = cliente.getTelefone();
        if (!validarCelular(telefone)) {
            JOptionPane.showMessageDialog(tela, 
                "Número de celular inválido! Informe um número com DDD e começando com 9.");
            return false;
        }
        // Formata antes de salvar
        cliente.setTelefone(formatarCelular(telefone));

        // 2️⃣ Validação do CPF
        String cpf = cliente.getCpf();
        if (!ValidadorCPF.validar(cpf)) {
            JOptionPane.showMessageDialog(tela, "CPF inválido!");
            return false;
        }

        // 3️⃣ Salvamento
        if (clienteId == -1) { // NOVO
            if (clienteDAO.existeCpf(cpf)) {
                JOptionPane.showMessageDialog(tela, "CPF já cadastrado!");
                return false;
            }

            clienteDAO.cadastrar(cliente);
            return true;
        } else { // EDIÇÃO
            cliente.setId(clienteId);
            Cliente outro = clienteDAO.buscarPorCpf(cpf);
            if (outro != null && outro.getId() != clienteId) {
                JOptionPane.showMessageDialog(tela, "CPF já cadastrado em outro cliente!");
                return false;
            }
            clienteDAO.atualizar(cliente);
            return true;
        }

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(tela, "Erro ao salvar cliente: " + ex.getMessage());
        ex.printStackTrace();
        return false;
    }
}

    // ================== EDITAR CLIENTE ==================
    public boolean editarCliente(TelaDoCadastro tela,
            JTable tabelaClientes,
            DefaultTableModel modelTabela) {

        int linha = tabelaClientes.getSelectedRow();
        if (linha == -1) {
            JOptionPane.showMessageDialog(tela, "Selecione um cliente para editar!");
            return false;
        }

        try {
            // ID (coluna oculta)
            int id = (int) modelTabela.getValueAt(linha, 0);

            // CPF (coluna 2)
            String cpf = (String) modelTabela.getValueAt(linha, 2);

            Cliente c = clienteDAO.buscarPorCpf(cpf);
            if (c == null) {
                JOptionPane.showMessageDialog(tela, "Cliente não encontrado!");
                return false;
            }

            // Preencher campos da tela
            tela.setClienteId(c.getId());
            tela.getTxtNome().setText(c.getNome());
            tela.getTxtCPF().setText(formatarCPF(c.getCpf()));
            tela.getCampoDataNascimento().setDate(c.getDataNascimento());
            tela.getTxtEmail().setText(c.getEmail());
            tela.getTxtTelefone().setText(formatarCelular(c.getTelefone()));
            tela.getTxtCEP().setText(formatarCEP(c.getCep()));
            tela.getTxtEndereco().setText(c.getEndereco());
            tela.getTxtNumero().setText(c.getNumero());
            tela.getTxtCidade().setText(c.getCidade());

            if (c.getUf() != null) {
                tela.getCbEstado().setSelectedItem(UF.valueOf(c.getUf()));
            }

            tela.getTxtObservacao().setText(c.getObservacao());

            return true;

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(tela, "Erro ao editar cliente: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    // ================== EXCLUIR CLIENTE ==================
    public boolean excluirCliente(TelaDoCadastro tela) {

        int clienteId = tela.getClienteId();
        if (clienteId == -1) {
            JOptionPane.showMessageDialog(tela, "Nenhum cliente selecionado para exclusão!");
            return false;
        }

        int confirm = JOptionPane.showConfirmDialog(
                tela,
                "Deseja realmente excluir o cliente?",
                "Confirmação",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return false;
        }

        boolean sucesso = clienteDAO.excluir(clienteId);

        if (!sucesso) {
            JOptionPane.showMessageDialog(tela, "Erro ao excluir cliente. Tente novamente.");
            return false;
        }

        JOptionPane.showMessageDialog(tela, "Cliente excluído com sucesso!");
        return true;
    }

    // ================== OBTER CLIENTE DOS CAMPOS ==================
    public Cliente getClienteFromFields(TelaDoCadastro tela) {
        // Validações
        if (!tela.getValidador().formularioValido()) {
            JOptionPane.showMessageDialog(tela, "Preencha corretamente os campos destacados em vermelho.");
            return null;
        }

        Cliente cliente = new Cliente();

        // Nome
        cliente.setNome(tela.getTxtNome().getText().trim());

        // CPF: remove tudo que não é número
        String cpf = tela.getTxtCPF().getText().replaceAll("[^0-9]", "");
        cliente.setCpf(cpf);

        // Data de nascimento
        cliente.setDataNascimento(tela.getCampoDataNascimento().getDate());

        // Email
        cliente.setEmail(tela.getTxtEmail().getText().trim());

        // Telefone: remove tudo que não é número
        String telefone = tela.getTxtTelefone().getText().replaceAll("[^0-9]", "");
        cliente.setTelefone(telefone);

        // Endereço, número, cidade
        cliente.setEndereco(tela.getTxtEndereco().getText().trim());
        cliente.setNumero(tela.getTxtNumero().getText().trim());
        cliente.setCidade(tela.getTxtCidade().getText().trim());

        // Estado (UF)
        UF uf = (UF) tela.getCbEstado().getSelectedItem();
        if (uf != null) {
            cliente.setUf(uf.getSigla());
        }

        // CEP
        String cep = tela.getTxtCEP().getText().replaceAll("[^0-9]", "");
        cliente.setCep(cep);

        // Observação
        cliente.setObservacao(tela.getTxtObservacao().getText().trim());

        return cliente;
    }

    private String formatarCPF(String cpf) {
        if (cpf == null) {
            return "";
        }

        // Remove qualquer caractere que não seja número
        String numeros = cpf.replaceAll("\\D", "");

        // Verifica se tem 11 dígitos
        if (numeros.length() != 11) {
            return cpf; // retorna como está se não tiver 11 dígitos
        }

        // Aplica a máscara xxx.xxx.xxx-xx
        return numeros.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
    }

    private String formatarCEP(String cep) {
        if (cep == null) {
            return "";
        }

        // Remove tudo que não é número
        String numeros = cep.replaceAll("\\D", "");

        // Verifica se tem 8 dígitos
        if (numeros.length() == 8) {
            return numeros.replaceAll("(\\d{5})(\\d{3})", "$1-$2");
        } else {
            return cep; // retorna como está se não tiver 8 dígitos
        }
    }

    // Valida se o telefone é celular válido
    private boolean validarCelular(String telefone) {
        if (telefone == null) {
            return false;
        }

        String numeros = telefone.replaceAll("\\D", ""); // remove tudo que não é número

        if (numeros.length() != 11) {
            return false; // deve ter 11 dígitos
        }
        String ddd = numeros.substring(0, 2);
        int dddInt;
        try {
            dddInt = Integer.parseInt(ddd);
        } catch (NumberFormatException e) {
            return false;
        }
        if (dddInt < 11 || dddInt > 99) {
            return false; // DDD válido
        }
        if (numeros.charAt(2) != '9') {
            return false; // celular começa com 9
        }
        return true;
    }

// Formata o celular para exibição (XX) XXXXX-XXXX
    private String formatarCelular(String telefone) {
        if (!validarCelular(telefone)) {
            return telefone;
        }

        String numeros = telefone.replaceAll("\\D", "");
        String ddd = numeros.substring(0, 2);
        String parte1 = numeros.substring(2, 7);
        String parte2 = numeros.substring(7);
        return String.format("(%s) %s-%s", ddd, parte1, parte2);
    }

}
