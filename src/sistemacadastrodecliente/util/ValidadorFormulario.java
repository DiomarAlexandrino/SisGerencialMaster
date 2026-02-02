package sistemacadastrodecliente.util;

import sistemacadastrodecliente.model.enums.TipoValidacao;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import static sistemacadastrodecliente.model.enums.TipoValidacao.CEP;
import sistemacadastrodecliente.view.SimpleDocumentListener;
import sistemacadastrodecliente.view.TelaDoCadastro;

/**
 * Validador de formulário com validação em tempo real. Atualiza borda, tooltip
 * e avisa o ControleEstadoTela.
 */
public class ValidadorFormulario {

    private final Map<JComponent, TipoValidacao> campos = new HashMap<>();
    private ControleEstadoTela controleEstado;
    private Object selecionado;
    private String valorCidade;
    private TelaDoCadastro telaCadastro;

    public ValidadorFormulario(TelaDoCadastro telaCadastro) {
        this.telaCadastro = telaCadastro;
    }

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
            case CIDADE_OBRIGATORIA -> {
                String cidade = telaCadastro.getTxtCidade().getText().trim();
                System.out.println(cidade);

                if (cidade.isEmpty()) {
                    valido = false;
                    mensagemErro = "Campo obrigatório";
                } else if (!cidade.matches("[A-Za-zÀ-ÿ ]+")) {
                    valido = false;
                    mensagemErro = "Cidade inválida (somente letras e espaços)";
                } else if (cidade.length() < 2 || cidade.length() > 50) {
                    valido = false;
                    mensagemErro = "Cidade deve ter entre 2 e 50 caracteres";
                } else {
                    valido = true;
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

            case FORMATADO_OBRIGATORIO -> {
                String texto = ((JTextComponent) campo).getText().replaceAll("[^0-9]", "");
                valido = !texto.isEmpty();
                if (!valido) {
                    mensagemErro = "Campo obrigatório";
                }
            }
            case COMBO_OBRIGATORIO -> {
                JComboBox<?> combo = (JComboBox<?>) campo;
                selecionado = combo.getSelectedItem();

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
            case CEP -> {

                if (!(campo instanceof JTextComponent)) {
                    mensagemErro = "Componente inválido para CEP";
                    break;
                }

                String texto = ((JTextComponent) campo).getText().replaceAll("\\D", ""); // só números

                if (texto.isEmpty()) {
                    atualizarBordaNeutra(campo);
                    return true;
                }

                // Pega o campo da cidade do formulário diretamente
                String cidade = null;
                for (JComponent c : campos.keySet()) {
                    if (campos.get(c) == TipoValidacao.CIDADE_OBRIGATORIA) {
                        cidade = ((JTextComponent) c).getText().trim();
                        break;
                    }
                }

                String uf = selecionado != null ? selecionado.toString().split(" - ")[0] : "";

                if (texto.length() < 8) {
                    mensagemErro = "CEP incompleto";
                } else if (cidade == null || cidade.isEmpty()) {
                    mensagemErro = "Preencha a cidade antes de validar o CEP";
                } else if (uf.isEmpty()) {
                    mensagemErro = "Selecione o estado antes de validar o CEP";
                } else if (ValidadorCEP.validar(texto, cidade, uf)) {
                    valido = true;

                } else {
                    mensagemErro = "CEP inválido para a cidade/estado informados";
                }

            }

            case DATA_OBRIGATORIA -> {
                CampoDataComCalendario componenteData = (CampoDataComCalendario) campo;
                // O  método getDate() já retorna null se:
                // 1. Estiver vazio/incompleto
                // 2. A data for inexistente (ex: 31/02)
                // 3. A idade for menor que 12 anos
                java.time.LocalDate data = componenteData.getDate();

                if (data == null) {
                    valido = false;
                    // Precisamos descobrir por que é null para dar a mensagem correta
                    String texto = componenteData.tfData.getText().replace("_", "").replace("/", "").trim();
                    if (texto.isEmpty()) {
                        mensagemErro = "Data é obrigatória";
                    } else {
                        mensagemErro = "Data inválida ou idade mínima de 12 anos não atingida";
                    }
                } else {
                    valido = true;
                    mensagemErro = null;
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
            case CAMPO_MAX_10 -> {
                String texto = ((JTextComponent) campo).getText().trim();
                if (texto.isEmpty()) {
                    valido = false;
                    mensagemErro = "Campo obrigatório";
                } else if (texto.length() > 10) {
                    valido = false;
                    mensagemErro = "O texto deve ter no máximo 10 caracteres";
                } else {
                    valido = true; // ✅ Aqui a borda ficará verde
                }
            }

        }
        if (!valido) {
            // Aqui você pode exibir mensagem de erro ou armazenar em algum lugar
            System.out.println(mensagemErro);
        }

        atualizarBordaETooltip(campo, valido, mensagemErro);

        // 🔔 AVISA O CONTROLE DE ESTADO
        if (controleEstado != null) {
            controleEstado.atualizarFormularioValido(formularioValido());
        }

        return valido;
    }

    public boolean formularioValido() {
        return campos.entrySet().stream().allMatch(entry -> {
            JComponent campo = entry.getKey();
            TipoValidacao tipo = entry.getValue();

            return switch (tipo) {
                case TEXTO_OBRIGATORIO ->
                    !((JTextComponent) campo).getText().trim().isEmpty();

                case CIDADE_OBRIGATORIA -> {
                    String cidade = ((JTextComponent) campo).getText().trim();
                    yield !cidade.isEmpty() && cidade.matches("[A-Za-zÀ-ÿ ]+") && cidade.length() >= 2;
                }

                case EMAIL -> {
                    String texto = ((JTextComponent) campo).getText().trim();
                    yield texto.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$") && texto.length() <= 50;
                }

                case CPF -> {
                    String texto = ((JTextComponent) campo).getText().replaceAll("[^0-9]", "");
                    yield texto.length() == 11 && ValidadorCPF.validar(texto);
                }

                case TELEFONE ->
                    ((JTextComponent) campo).getText().replaceAll("\\D", "").length() == 11;

                case CEP -> {
                    String texto = ((JTextComponent) campo).getText().replaceAll("\\D", "");
                    // Aqui você pode decidir se valida o CEP completo ou apenas o tamanho
                    yield texto.length() == 8;
                }

                case COMBO_OBRIGATORIO -> {
                    JComboBox<?> combo = (JComboBox<?>) campo;
                    Object sel = combo.getSelectedItem();
                    yield sel != null && sel != sistemacadastrodecliente.model.enums.UF.UF;
                }

                case DATA_OBRIGATORIA ->
                    ((CampoDataComCalendario) campo).getDate() != null;

                case CAMPO_MAX_80 -> {
                    String texto = ((JTextComponent) campo).getText().trim();
                    yield !texto.isEmpty() && texto.length() <= 80;
                }
                case CAMPO_MAX_10 -> {
                    String texto = ((JTextComponent) campo).getText().trim();
                    yield !texto.isEmpty() && texto.length() <= 10;
                }

                default ->
                    true;
            };
        });
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

    public void revalidarCEP() {
        for (JComponent campo : campos.keySet()) {
            if (campos.get(campo) == TipoValidacao.CEP) {
                validarCampo(campo);
                break;
            }
        }
    }

    private void atualizarBordaNeutra(JComponent campo) {
        campo.setBorder(new LineBorder(Color.GRAY, 1));
        campo.setToolTipText(null);
    }

    public void atualizarFormularioValido(boolean tudoValido) {
        // btnSalvar é o botão da sua TelaDoCadastro
        telaCadastro.getBtnSalvar().setEnabled(tudoValido);
    }
}
