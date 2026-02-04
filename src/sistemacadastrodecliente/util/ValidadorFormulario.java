package sistemacadastrodecliente.util;

import sistemacadastrodecliente.model.enums.TipoValidacao;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import sistemacadastrodecliente.model.enums.EstadoTela;
import static sistemacadastrodecliente.model.enums.TipoValidacao.ALFA_NUMERICO;
import static sistemacadastrodecliente.model.enums.TipoValidacao.APENAS_LETRAS;
import static sistemacadastrodecliente.model.enums.TipoValidacao.CEP;
import sistemacadastrodecliente.model.enums.UF;
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

    public ControleEstadoTela getControle() {
        return this.controleEstado;
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

        if (controleEstado != null
                && EstadoTela.NAVEGANDO.equals(controleEstado.getEstadoAtual())) {
            return true; // ✅ considera válido
        }
        boolean valido = true;
        String mensagemErro = null;

        TipoValidacao tipo = campos.get(campo);
        if (tipo == null) {
            return true;
        }

        // Primeiro, checa se está navegando
        switch (tipo) {
            case NOME_OBRIGATORIO -> {
                String texto = ((JTextComponent) campo).getText().trim();

                if (texto.isEmpty()) {
                    valido = false;
                    mensagemErro = "Campo obrigatório";
                } else if (!texto.matches("[A-Za-zÀ-ÿ ]+")) {
                    valido = false;
                    mensagemErro = "Apenas letras são permitidas";

                } else if (texto.length() < 2 || texto.length() > 80) {
                    valido = false;
                    mensagemErro = "Nome deve ter entre 2 e 80 caracteres";

                } else {
                    valido = true;
                }

            }
            case CIDADE_OBRIGATORIA -> {
                String cidade = telaCadastro.getTxtCidade().getText().trim();

                if (cidade.isEmpty()) {
                    valido = false;
                    mensagemErro = "Campo obrigatório";
                } else if (!cidade.matches("[A-Za-zÀ-ÿ ]+")) {
                    valido = false;
                    mensagemErro = "Cidade inválida (somente letras)";
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
                } else if (texto.length() > 80) {
                    valido = false;
                    mensagemErro = "O Texto deve ter no máximo 80 caracteres";
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
                Object sel = combo.getSelectedItem();

                valido = false;

                if (sel instanceof sistemacadastrodecliente.model.enums.UF uf) {
                    valido = uf != sistemacadastrodecliente.model.enums.UF.UF;
                }

                if (!valido) {
                    mensagemErro = "Selecione um estado válido";
                }
            }
            case CEP -> {
                if (campo instanceof JTextComponent tf) {
                    validarCEPAoTeclar(tf);
                    return false; // Retorno temporário, a validação será concluída no SwingWorker
                } else {
                    mensagemErro = "Componente inválido para CEP";
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
            case ENDERECO_OBRIGATORIO -> {
                String texto = ((JTextComponent) campo).getText().trim();

                if (texto.isEmpty()) {
                    valido = false;
                    mensagemErro = "Nome é obrigatório";

                } else if (!texto.isEmpty() && !texto.matches("[A-Za-z0-9 ]+")) {
                    valido = false;
                    mensagemErro = "Apenas letras e números são permitidos";
                } else if (texto.length() > 80) {
                    valido = false;
                    mensagemErro = "O Texto deve ter no máximo 80 caracteres";
                } else {
                    valido = true;
                }
            }
            case NUMERO_OBRIGATORIO -> {
                String texto = ((JTextComponent) campo).getText().trim();
                if (!texto.isEmpty() && !texto.matches("[A-Za-z0-9 ]+")) {
                    valido = false;
                    mensagemErro = "Apenas letras e números são permitidos";
                } else if (texto.isEmpty()) {
                    valido = false;
                    mensagemErro = "Campo obrigatório";
                } else if (texto.length() > 10) {
                    valido = false;
                    mensagemErro = "O texto deve ter no máximo 10 caracteres";
                } else {
                    valido = true; // ✅ Aqui a borda ficará verde
                }
            }

            case APENAS_LETRAS -> {
                String texto = ((JTextComponent) campo).getText().trim();
                if (texto.isEmpty() || !texto.matches("[A-Za-zÀ-ÿ ]+")) {
                    valido = false;
                    mensagemErro = "Apenas letras são permitidas";
                }
            }

            case ALFA_NUMERICO -> {
                String texto = ((JTextComponent) campo).getText().trim();
                if (!texto.isEmpty() && !texto.matches("[A-Za-z0-9 ]+")) {
                    valido = false;
                    mensagemErro = "Apenas letras e números são permitidos";
                } else {
                    valido = true;
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
                case NOME_OBRIGATORIO ->
                    !((JTextComponent) campo).getText().trim().isEmpty();

                case CIDADE_OBRIGATORIA -> {
                    String cidade = ((JTextComponent) campo).getText().trim();
                    // Remove números ou símbolos caso existam
                    cidade = cidade.replaceAll("[0-9]", "");
                    yield !cidade.isEmpty() && cidade.matches("[\\p{L} ]+") && cidade.length() >= 2 && cidade.length() <= 50;
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

                case COMBO_OBRIGATORIO -> {
                    JComboBox<?> combo = (JComboBox<?>) campo;
                    Object sel = combo.getSelectedItem();

                    if (sel == null) {
                        yield false;
                    }

                    if (sel instanceof sistemacadastrodecliente.model.enums.UF ufEnum) {
                        yield ufEnum != sistemacadastrodecliente.model.enums.UF.UF;
                    }

                    yield !sel.toString().toLowerCase().contains("selecione");
                }

                case DATA_OBRIGATORIA ->
                    ((CampoDataComCalendario) campo).getDate() != null;

                case ENDERECO_OBRIGATORIO -> {
                    String texto = ((JTextComponent) campo).getText().trim();
                    yield !texto.isEmpty() && texto.length() <= 80;
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

    private JDialog criarDialogCarregando(String mensagem) {
        JDialog dialog = new JDialog(telaCadastro, "Aguarde", true);
        JLabel label = new JLabel(mensagem, JLabel.CENTER);
        label.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        dialog.getContentPane().add(label);
        dialog.setSize(200, 80);
        dialog.setLocationRelativeTo(telaCadastro);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        return dialog;
    }

    private void validarCEPAoTeclar(JTextComponent campo) {
        String cep = campo.getText().replaceAll("\\D", "");
        if (cep.isEmpty() || cep.length() < 8) {
            atualizarBordaNeutra(campo);
            return;
        }

        JDialog carregando = criarDialogCarregando("Validando CEP...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            private String mensagemErroLocal = null;

            @Override
            protected Boolean doInBackground() {
                String cidade = telaCadastro.getTxtCidade().getText().trim();
                Object sel = telaCadastro.getCbEstado().getSelectedItem();
                String uf = sel instanceof sistemacadastrodecliente.model.enums.UF estado
                        ? estado.getSigla()
                        : "";

                boolean validoLocal = ValidadorCEP.validar(cep, cidade, uf);
                if (!validoLocal) {
                    mensagemErroLocal = "CEP inválido para a cidade/estado informados";
                }
                return validoLocal;
            }

            @Override
            protected void done() {
                try {
                    boolean resultado = get();
                    atualizarBordaETooltip(campo, resultado, mensagemErroLocal);
                    if (controleEstado != null) {
                        controleEstado.atualizarFormularioValido(formularioValido());
                    }
                } catch (Exception e) {
                    atualizarBordaETooltip(campo, false, "Erro na validação do CEP");
                } finally {
                    carregando.dispose();
                }
            }
        };

        worker.execute();
        carregando.setVisible(true);
    }

}
