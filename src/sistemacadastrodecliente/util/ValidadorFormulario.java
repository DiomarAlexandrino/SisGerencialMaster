package sistemacadastrodecliente.util;

import sistemacadastrodecliente.model.enums.TipoValidacao;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import sistemacadastrodecliente.view.SimpleDocumentListener;

/**
 * Validador de formulário com validação em tempo real. Atualiza borda, tooltip
 * e avisa o ControleEstadoTela.
 */
public class ValidadorFormulario {

    private final Map<JComponent, TipoValidacao> campos = new HashMap<>();
    private ControleEstadoTela controleEstado;

    // =========================
    // CONFIGURAÇÃO DO CONTROLE
    // =========================
    public void setControle(ControleEstadoTela controle) {
        this.controleEstado = controle;
    }

    // =========================
    // REGISTRAR CAMPOS
    // =========================
    public void registrarCampo(JComponent campo, TipoValidacao tipo) {
        if (campo == null || tipo == null) {
            return;
        }
        campos.put(campo, tipo);
        adicionarListener(campo);
        validarCampo(campo); // valida imediatamente
    }

    // =========================
    // LISTENERS
    // =========================
    private void adicionarListener(JComponent campo) {
        if (campo instanceof JTextComponent tf) {
            tf.getDocument().addDocumentListener(SimpleDocumentListener.of(() -> validarCampo(campo)));
        } else if (campo instanceof JComboBox<?> combo) {
            combo.addActionListener(e -> validarCampo(campo));
        } else if (campo instanceof CampoDataComCalendario data) {
            data.adicionarListenerValidacao(() -> validarCampo(campo));
        }
    }

    // =========================
    // VALIDAÇÃO
    // =========================
    public boolean validarCampo(JComponent campo) {
        TipoValidacao tipo = campos.get(campo);
        if (tipo == null) {
            return true;
        }

        boolean valido = false;
        String mensagemErro = null;

        switch (tipo) {
            case TEXTO_OBRIGATORIO -> {
                valido = !((JTextComponent) campo).getText().trim().isEmpty();
                if (!valido) {
                    mensagemErro = "Campo obrigatório";
                }
            }
            case EMAIL -> {
                String texto = ((JTextComponent) campo).getText().trim();
                valido = texto.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
                if (!valido) {
                    mensagemErro = "Email inválido";
                 } else if (texto.length() > 50) {
                    valido = false;
                    mensagemErro = "O Texto deve ter no máximo 100 caracteres";
                } else {
                     valido = true;
                 }
            }
            case CPF -> {
                String texto = ((JTextComponent) campo).getText().replaceAll("[^0-9]", "");
                if (texto.length() < 11) {
                    mensagemErro = "CPF incompleto";
                } else if (ValidadorCPF.validar(texto)) {
                    valido = true;
                } else {
                    mensagemErro = "CPF inválido";
                }
            }
            case CEP -> {
                String texto = ((JTextComponent) campo).getText().replaceAll("[^0-9]", "");
                if (texto.length() < 8) {
                    mensagemErro = "CEP incompleto";
                } else if (ValidadorCEP.validar(texto)) {
                    valido = true;
                } else {
                    mensagemErro = "CEP inválido";
                }
            }
            case FORMATADO_OBRIGATORIO -> {
                String texto = ((JTextComponent) campo).getText().replaceAll("[^0-9]", "");
                valido = !texto.isEmpty();
                if (!valido) {
                    mensagemErro = "Campo obrigatório";
                }
            }
            case COMBO_OBRIGATORIO -> {
                JComboBox<?> combo = (JComboBox<?>) campo;
                Object selecionado = combo.getSelectedItem();

                valido = selecionado != null && selecionado != sistemacadastrodecliente.model.enums.UF.UF;

                if (!valido) {
                    mensagemErro = "Selecione um estado válido";
                    combo.setBorder(BorderFactory.createLineBorder(Color.RED));
                    combo.setToolTipText(mensagemErro);
                } else {
                    combo.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                    combo.setToolTipText(null);
                }
            }
            case DATA_OBRIGATORIA -> {
                CampoDataComCalendario data = (CampoDataComCalendario) campo;
                valido = data.getDate() != null;
                if (!valido) {
                    mensagemErro = "Data inválida";
                }
            }
            case TELEFONE -> {
                String texto = ((JTextComponent) campo).getText();
                String numeros = texto.replaceAll("\\D", "");

                if (numeros.isEmpty()) {
                    valido = false;
                    mensagemErro = "Telefone obrigatório";
                } else if (numeros.length() == 11) {
                    valido = true; // ✅ SOMENTE aqui fica verde
                } else {
                    valido = false;
                    mensagemErro = "Telefone incompleto";
                }
            }
            case CAMPO_MAX_80 -> {
                String texto = ((JTextComponent) campo).getText().trim();

                if (texto.isEmpty()) {
                    valido = false;
                    mensagemErro = "Nome é obrigatório";
                } else if (texto.length() > 80) {
                    valido = false;
                    mensagemErro = "O Texto deve ter no máximo 80 caracteres";
                } else {
                    valido = true;
                }
            }
        }

        atualizarBordaETooltip(campo, valido, mensagemErro);

        // 🔔 AVISA O CONTROLE DE ESTADO
        if (controleEstado != null) {
            controleEstado.atualizarFormularioValido(formularioValido());
        }

        return valido;
    }

    public boolean formularioValido() {
        return campos.keySet().stream().allMatch(this::validarCampo);
    }

    // =========================
    // BORDA E TOOLTIP
    // =========================
    private void atualizarBordaETooltip(JComponent campo, boolean valido, String mensagemErro) {
        Color cor = valido ? Color.GREEN : Color.RED;

        if (campo instanceof CampoDataComCalendario data) {
            data.setCampoValido(valido);
            data.setToolTipText(mensagemErro);
        } else {
            campo.setBorder(new LineBorder(cor, 2));
            campo.setToolTipText(mensagemErro);
        }
    }

}
