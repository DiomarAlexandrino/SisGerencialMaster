package sistemacadastrodecliente.util;

import sistemacadastrodecliente.model.enums.EstadoTela;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class ControleEstadoTela {

    private JTable tabela;
    private JPanel painelFormulario;
    private JButton btnNovo, btnEditar, btnExcluir, btnSalvar, btnCancelar;

    private EstadoTela estadoAtual = EstadoTela.NAVEGANDO;
    private final CampoDataComCalendario campoDataNascimento;

  

    public ControleEstadoTela(JTable tabela, JPanel painelFormulario, JButton btnNovo, JButton btnEditar, JButton btnExcluir, JButton btnSalvar, JButton btnCancelar, CampoDataComCalendario campoDataNascimento) {
        this.tabela = tabela;
        this.painelFormulario = painelFormulario;
        this.btnNovo = btnNovo;
        this.btnEditar = btnEditar;
        this.btnExcluir = btnExcluir;
        this.btnSalvar = btnSalvar;
        this.btnCancelar = btnCancelar;
        
        this.campoDataNascimento = campoDataNascimento; // agora é o campo real da tela

        atualizarComponentes();
    }

    public ControleEstadoTela() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public void setEstado(EstadoTela estado) {
        this.estadoAtual = estado;
        atualizarComponentes();
    }

private void atualizarComponentes() {
    switch (estadoAtual) {
        case NAVEGANDO -> {
            tabela.setEnabled(true);
            painelFormulario.setEnabled(false);
            habilitarComponentesFormulario(false);

            // DESABILITA o campo de data real
            campoDataNascimento.setEnabledCampo(false);

            btnNovo.setEnabled(true);
            btnEditar.setEnabled(tabela.getSelectedRow() != -1);
            btnExcluir.setEnabled(tabela.getSelectedRow() != -1);
            btnSalvar.setEnabled(false);
            btnCancelar.setEnabled(false);
        }
        case ADICIONANDO, EDITANDO -> {
            tabela.setEnabled(false);
            painelFormulario.setEnabled(true);
            habilitarComponentesFormulario(true);

            // HABILITA o campo de data real
            campoDataNascimento.setEnabledCampo(true);

            btnNovo.setEnabled(false);
            btnEditar.setEnabled(false);
            btnExcluir.setEnabled(false);
            btnSalvar.setEnabled(true);
            btnCancelar.setEnabled(true);
        }
    }
}



    private void habilitarComponentesFormulario(boolean habilitar) {
        // Itera sobre todos os componentes do painel do formulário
        for (Component c : painelFormulario.getComponents()) {
            c.setEnabled(habilitar);

        }
    }

    // Método chamado pelo ValidadorFormulario para habilitar ou desabilitar o botão salvar
    public void atualizarFormularioValido(boolean valido) {
        if (btnSalvar != null) {
            btnSalvar.setEnabled(valido);
        }
    }

    public EstadoTela getEstadoAtual() {
        return estadoAtual;
    }
}
