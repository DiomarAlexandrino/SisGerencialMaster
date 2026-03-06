package sistemacadastrodecliente.util;

import sistemacadastrodecliente.model.enums.TipoValidacao;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.HashMap;
import java.util.Map;
import sistemacadastrodecliente.model.enums.EstadoTela;
import static sistemacadastrodecliente.model.enums.TipoValidacao.*;
import sistemacadastrodecliente.model.enums.UF;
import sistemacadastrodecliente.view.SimpleDocumentListener;
import sistemacadastrodecliente.view.TelaDoCadastro;

/**
 * Validador de formulário com validação em tempo real. Exibe popup apenas ao
 * sair do campo.
 */
public class ValidadorFormulario {

    private final Map<JComponent, TipoValidacao> campos = new HashMap<>();
    private ControleEstadoTela controleEstado;
    private TelaDoCadastro telaCadastro;
    private boolean validando;

    public ValidadorFormulario(TelaDoCadastro telaCadastro) {
        this.telaCadastro = telaCadastro;
    }

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
        validarCampo(campo, false); // valida imediatamente sem popup
    }

    // =========================
    // LISTENERS
    // =========================
    private void adicionarListener(JComponent campo) {

        // Validação ao perder foco → mostra popup se inválido
        campo.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                validarCampo(campo, true); // mostrar popup
            }
        });

        // Validação em tempo real sem popup
        if (campo instanceof JTextComponent tf) {
            tf.getDocument().addDocumentListener(SimpleDocumentListener.of(() -> validarCampo(campo, false)));
        } else if (campo instanceof JComboBox<?> combo) {
            combo.addActionListener(e -> validarCampo(campo, false));
        } else if (campo instanceof CampoDataComCalendario data) {

            // Digitação → sem popup
            data.adicionarListenerValidacao(() -> validarCampo(campo, false));

            // FocusLost → com popup
            data.tfData.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    validarCampo(campo, true);
                }
            });

            // 🔥 Clique no calendário → com popup
            data.setOnDateSelected(() -> validarCampo(campo, true));
        }
    }

    // =========================
    // VALIDAÇÃO
    // =========================
// =========================
// VALIDAÇÃO (COM BORDA VERDE)
// =========================
    public boolean validarCampo(JComponent campo, boolean mostrarPopup) {
        // PREVENIR RECURSÃO INFINITA
        if (validando) {
            return true;
        }

        try {
            validando = true;

            // Verifica estado da tela
            EstadoTela estado = null;
            if (controleEstado != null) {
                estado = controleEstado.getEstadoAtual();
            }
            if (estado != EstadoTela.ADICIONANDO && estado != EstadoTela.EDITANDO) {
                atualizarBordaNeutra(campo);
                return true;
            }

            // Se não estiver em modo de edição, apenas coloca borda neutra e retorna
            if (estado != EstadoTela.ADICIONANDO && estado != EstadoTela.EDITANDO) {
                atualizarBordaNeutra(campo);
                return true;
            }

            boolean valido = true;
            String mensagemErro = null;
            TipoValidacao tipo = campos.get(campo);
            if (tipo == null) {
                return true;
            }

            try {
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
                        }
                    }
                    case CIDADE_OBRIGATORIA -> {
                        String cidade = ((JTextComponent) campo).getText().trim();
                        if (cidade.isEmpty()) {
                            valido = false;
                            mensagemErro = "Campo obrigatório";
                        } else if (!cidade.matches("[A-Za-zÀ-ÿ ]+")) {
                            valido = false;
                            mensagemErro = "Cidade inválida (somente letras)";
                        } else if (cidade.length() < 2 || cidade.length() > 50) {
                            valido = false;
                            mensagemErro = "Cidade deve ter entre 2 e 50 caracteres";
                        }
                    }
                    case EMAIL -> {
                        String texto = ((JTextComponent) campo).getText().trim();
                        if (texto.isEmpty()) {
                            valido = false;
                            mensagemErro = "Campo obrigatório";
                        } else if (!texto.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                            valido = false;
                            mensagemErro = "Email inválido";
                        } else if (texto.length() > 40) {
                            valido = false;
                            mensagemErro = "O email deve ter no máximo 40 caracteres";
                        }
                    }
                    case CPF -> {
                        String texto = ((JTextComponent) campo).getText().replaceAll("\\D", "");
                        if (texto.isEmpty()) {
                            valido = false;
                            mensagemErro = "Campo obrigatório";
                        } else if (texto.length() < 11) {
                            valido = false;
                            mensagemErro = "CPF incompleto";
                        } else if (!ValidadorCPF.validar(texto)) {
                            valido = false;
                            mensagemErro = "CPF inválido";
                        }
                    }
                    case TELEFONE -> {
                        String numeros = ((JTextComponent) campo).getText().replaceAll("\\D", "");
                        if (numeros.isEmpty()) {
                            valido = false;
                            mensagemErro = "Telefone obrigatório";
                        } else if (numeros.length() != 11) {
                            valido = false;
                            mensagemErro = "Telefone incompleto (DDD + 9 números)";
                        }
                    }
                    case COMBO_OBRIGATORIO -> {
                        JComboBox<?> combo = (JComboBox<?>) campo;
                        Object sel = combo.getSelectedItem();
                        if (sel == null || (sel instanceof UF uf && uf == UF.UF)) {
                            valido = false;
                            mensagemErro = "Selecione um estado válido";
                        }
                    }
                    case DATA_OBRIGATORIA -> {
                        CampoDataComCalendario componenteData = (CampoDataComCalendario) campo;
                        String texto = componenteData.tfData.getText().trim();

                        if (texto.isEmpty() || texto.replace("_", "").replace("/", "").isEmpty()) {
                            valido = false;
                            mensagemErro = "Data é obrigatória";
                        } else if (texto.contains("_")) {
                            valido = false;
                            mensagemErro = "Data incompleta";
                        } else {
                            try {
                                java.time.LocalDate data = componenteData.getDate();
                                if (data == null) {
                                    valido = false;
                                    mensagemErro = "Data inválida";
                                } else {
                                    String motivo = ValidarIdade.validarIdadeComMotivo(data);
                                    if (motivo != null) {
                                        valido = false;
                                        mensagemErro = motivo;
                                    } else {
                                        valido = true;
                                        mensagemErro = null;
                                    }
                                }
                            } catch (Exception e) {
                                valido = false;
                                mensagemErro = "Data inválida";
                            }
                        }
                    }
                    case CEP -> {
                        // Só valida CEP se estivermos editando/adicionando
                        if (estado == EstadoTela.ADICIONANDO || estado == EstadoTela.EDITANDO) {
                            String cep = ((JTextComponent) campo).getText().replaceAll("\\D", "");
                            if (cep.isEmpty()) {
                                valido = false;
                                mensagemErro = "CEP obrigatório";
                            } else if (cep.length() < 8) {
                                valido = false;
                                mensagemErro = "CEP incompleto";
                            } else {
                                valido = true;
                                mensagemErro = null;
                            }
                        }
                    }
                    case ENDERECO_OBRIGATORIO -> {
                        String texto = ((JTextComponent) campo).getText().trim();
                        if (texto.isEmpty()) {
                            valido = false;
                            mensagemErro = "Endereço obrigatório";
                        }
                    }
                    case BAIRRO_OBRIGATORIO -> {
                        String texto = ((JTextComponent) campo).getText().trim();
                        if (texto.isEmpty()) {
                            valido = false;
                            mensagemErro = "Bairro obrigatório";
                        }
                    }
                    case NUMERO_OBRIGATORIO -> {
                        String texto = ((JTextComponent) campo).getText().trim();
                        if (texto.isEmpty()) {
                            valido = false;
                            mensagemErro = "Número obrigatório";
                        }
                    }
                    case APENAS_LETRAS -> {
                        String texto = ((JTextComponent) campo).getText().trim();
                        if (!texto.isEmpty() && !texto.matches("[A-Za-zÀ-ÿ ]+")) {
                            valido = false;
                            mensagemErro = "Apenas letras são permitidas";
                        }
                    }
                    case ALFA_NUMERICO -> {
                        String texto = ((JTextComponent) campo).getText().trim();
                        if (!texto.isEmpty() && !texto.matches("[A-Za-z0-9 ]+")) {
                            valido = false;
                            mensagemErro = "Apenas letras e números são permitidos";
                        }
                    }
                }
            } catch (Exception e) {
                valido = false;
                mensagemErro = "Erro na validação";
                e.printStackTrace();
            }

            // Atualiza borda e tooltip
            atualizarBordaETooltip(campo, valido, mensagemErro);

            // Mostra popup apenas em ADICIONANDO/EDITANDO
            if (!valido && mostrarPopup && (estado == EstadoTela.ADICIONANDO || estado == EstadoTela.EDITANDO)) {
                mostrarPopupAviso(mensagemErro, campo);
            }

            // Atualiza botão Salvar
            if (controleEstado != null) {
                controleEstado.atualizarFormularioValido(formularioValido());
            }

            return valido;
        } finally {
            validando = false;
        }

    }
    // =========================
    // BORDA E TOOLTIP
    // =========================
    // =========================
    // BORDA E TOOLTIP (COM VERDE)
    // =========================

    private void atualizarBordaETooltip(JComponent campo, boolean valido, String mensagemErro) {
        Color cor = valido ? Color.GREEN : Color.RED;
        int espessura = valido ? 2 : 2; // Pode ajustar a espessura se quiser

        if (campo instanceof CampoDataComCalendario data) {
            data.setCampoValido(valido);
            data.setToolTipText(mensagemErro);
            if (valido) {
                data.setBorder(new LineBorder(Color.GREEN, 2));
            } else {
                data.setBorder(new LineBorder(Color.RED, 2));
            }
        } else {
            campo.setBorder(new LineBorder(cor, espessura));
            campo.setToolTipText(mensagemErro);
        }
    }

    private void atualizarBordaNeutra(JComponent campo) {
        if (campo instanceof CampoDataComCalendario data) {
            data.setBorder(new LineBorder(Color.GRAY, 1));
        } else {
            campo.setBorder(new LineBorder(Color.GRAY, 1));
        }
        campo.setToolTipText(null);
    }

    public boolean formularioValido() {
        return campos.keySet().stream().allMatch(c -> validarCampo(c, false));
    }

    // =========================
    // POPUP
    // =========================
    public void mostrarPopupAviso(String mensagem, JComponent campo) {
        if (mensagem == null || mensagem.isBlank() || campo == null) {
            return;
        }

        JWindow popup = new JWindow(telaCadastro);
        popup.setBackground(new Color(0, 0, 0, 0));

        JPanel painel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 250, 205));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(new Color(0, 0, 0, 50));
                g2.fillRoundRect(2, 2, getWidth(), getHeight(), 15, 15);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        painel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        painel.add(new JLabel(mensagem));
        popup.add(painel);
        popup.pack();

        try {
            Point pos = campo.getLocationOnScreen();
            int x = pos.x;
            int y = pos.y - popup.getHeight() - 5;
            if (y < 0) {
                y = pos.y + campo.getHeight() + 5;
            }
            popup.setLocation(x, y);
        } catch (Exception ex) {
            popup.setLocationRelativeTo(campo);
        }

        popup.setVisible(true);
        new Timer(3000, e -> popup.dispose()).start();
    }

    // =========================
    // CEP (Assíncrono)
    // =========================
    private void validarCEPAoTeclar(JTextComponent campo) {
        String cep = campo.getText().replaceAll("\\D", "");
        if (cep.isEmpty() || cep.length() < 8) {
            SwingUtilities.invokeLater(() -> atualizarBordaNeutra(campo));
            return;
        }
        // Aqui você mantém seu SwingWorker do CEP
        // popup será mostrado só ao focusLost pelo listener
    }

    public void revalidarCEP() {
        for (JComponent campo : campos.keySet()) {
            if (campos.get(campo) == TipoValidacao.CEP) {
                validarCampo(campo, false);
                break;
            }
        }
    }

    // ValidadorFormulario.java
    public boolean validarCampoSemPopup(Object campo) {
        // Verifica se o campo é do tipo CampoDataComCalendario
        if (campo instanceof CampoDataComCalendario componenteData) {
            java.time.LocalDate data = componenteData.getDate();
            if (data == null) {
                // Campo vazio ou inválido
                return false;
            } else {
                // Opcional: validar idade ou outras regras
                String motivo = ValidarIdade.validarIdadeComMotivo(data);
                return motivo == null;
            }
        }

        // Adicione outros tipos de campo aqui, se necessário
        if (campo instanceof JTextField textField) {
            String texto = textField.getText().trim();
            return !texto.isEmpty();
        }

        if (campo instanceof JComboBox combo) {
            return combo.getSelectedItem() != null;
        }

        // Para outros tipos, assume válido
        return true;
    }
}
