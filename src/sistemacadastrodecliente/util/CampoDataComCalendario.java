package sistemacadastrodecliente.util;

import java.awt.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.MaskFormatter;
import java.text.ParseException;

import sistemacadastrodecliente.view.temas.TemaEnum;

public class CampoDataComCalendario extends JPanel {

    final JFormattedTextField tfData;
    private final JButton btnCalendario;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT); // ✅ Strict para datas reais
    private Runnable onChange; // listener externo para validar

    public CampoDataComCalendario() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

        tfData = criarCampoComMascara();
        btnCalendario = new JButton("📅");
        btnCalendario.setMargin(new Insets(2, 6, 2, 6));

        add(tfData);
        add(btnCalendario);

        configurarCalendario();
        adicionarListenerValidacao(null); // garante validação ao digitar
    }

    private JFormattedTextField criarCampoComMascara() {
        try {
            MaskFormatter mask = new MaskFormatter("##/##/####");
            mask.setPlaceholderCharacter('_');
            JFormattedTextField tf = new JFormattedTextField(mask);
            tf.setColumns(10);
            tf.setFocusLostBehavior(JFormattedTextField.COMMIT);
            return tf;
        } catch (ParseException e) {
            throw new RuntimeException("Erro ao criar máscara de data", e);
        }
    }

    private void configurarCalendario() {
        btnCalendario.addActionListener(e -> {
            JDialog popup = new JDialog(SwingUtilities.getWindowAncestor(this),
                    "Selecionar Data", Dialog.ModalityType.APPLICATION_MODAL);
            popup.setLayout(new BorderLayout());

            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(5, 5, 5, 5);

            // Ano
            JComboBox<Integer> yearCombo = new JComboBox<>();
            int anoAtual = LocalDate.now().getYear();
            for (int i = anoAtual; i >= anoAtual - 100; i--) {
                yearCombo.addItem(i);
            }

            // Mês
            String[] meses = java.util.Arrays.stream(Month.values())
                    .map(m -> m.getDisplayName(java.time.format.TextStyle.FULL, new Locale("pt", "BR")))
                    .map(s -> s.substring(0,1).toUpperCase() + s.substring(1))
                    .toArray(String[]::new);

            JComboBox<String> mesCombo = new JComboBox<>(meses);

            // Dias
            JPanel daysPanel = new JPanel(new GridLayout(6, 7, 2, 2));

            Runnable atualizarDias = () -> {
                daysPanel.removeAll();
                int mesIndex = mesCombo.getSelectedIndex() + 1;
                int ano = (int) yearCombo.getSelectedItem();
                int diasNoMes = java.time.YearMonth.of(ano, mesIndex).lengthOfMonth();

                for (int d = 1; d <= diasNoMes; d++) {
                    int dia = d;
                    JButton dayBtn = new JButton(String.valueOf(d));
                    dayBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    dayBtn.setBackground(new Color(0xF0F0F0));
                    dayBtn.setBorder(new LineBorder(new Color(0xDDDDDD), 1, true));

                    dayBtn.addActionListener(ev -> {
                        setDate(LocalDate.of(ano, mesIndex, dia));
                        popup.setVisible(false);
                        if (onChange != null) onChange.run();
                    });
                    daysPanel.add(dayBtn);
                }
                daysPanel.revalidate();
                daysPanel.repaint();
            };

            yearCombo.addActionListener(a -> atualizarDias.run());
            mesCombo.addActionListener(a -> atualizarDias.run());

            c.gridx = 0; c.gridy = 0;
            panel.add(new JLabel("Ano:"), c);
            c.gridx = 1;
            panel.add(yearCombo, c);
            c.gridx = 0; c.gridy = 1;
            panel.add(new JLabel("Mês:"), c);
            c.gridx = 1;
            panel.add(mesCombo, c);
            c.gridx = 0; c.gridy = 2; c.gridwidth = 2;
            panel.add(daysPanel, c);

            atualizarDias.run();

            popup.add(panel, BorderLayout.CENTER);
            popup.pack();
            popup.setLocationRelativeTo(this);
            popup.setVisible(true);
        });
    }

    // ================= API PÚBLICA =================
    public void setDate(LocalDate date) {
        if (date != null) {
            tfData.setText(date.format(formatter));
            setCampoValido(true);
        } else {
            tfData.setText("");
            setCampoValido(false);
        }
        if (onChange != null) onChange.run();
    }

    public LocalDate getDate() {
    String texto = tfData.getText().trim();

    // Verifica se o campo está vazio ou incompleto
    if (texto.isEmpty() || texto.contains("_")) {
        setCampoValido(false);
        return null;
    }

    try {
        // Converte o texto em LocalDate usando o formatter definido
        LocalDate data = LocalDate.parse(texto, formatter);

        // Usa o método de validação de idade mínima e máxima
        if (!ValidarIdade.validarIdade(data)) {
            setCampoValido(false);
            return null;
        }

        // Se passou na validação, marca o campo como válido
        setCampoValido(true);
        return data;

    } catch (DateTimeParseException e) {
        // Caso o texto não seja uma data válida
        setCampoValido(false);
        return null;
    }
}

    public void adicionarListenerValidacao(Runnable listener) {
        this.onChange = listener;

        tfData.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { disparar(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { disparar(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { disparar(); }

            private void disparar() {
                getDate(); // dispara validação e atualiza borda
                if (onChange != null) onChange.run();
            }
        });

        tfData.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                getDate(); // dispara validação ao perder foco
                if (onChange != null) onChange.run();
            }
        });
    }

    public void setEnabledCampo(boolean enabled) {
        tfData.setEnabled(enabled);
        btnCalendario.setEnabled(enabled);
        Color bg = enabled ? Color.WHITE : Color.LIGHT_GRAY;
        setBackground(bg);
        tfData.setBackground(bg);
        btnCalendario.setBackground(bg);
    }

    public void setCampoValido(Boolean valido) {
        if (valido == null) {
            tfData.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        } else if (valido) {
            tfData.setBorder(BorderFactory.createLineBorder(Color.GREEN));
        } else {
            tfData.setBorder(BorderFactory.createLineBorder(Color.RED));
        }
    }

    public void resetarVisual() {
        tfData.setText("");
        setCampoValido(false);
    }

 public void aplicarTema(TemaEnum tema) {

    // Fundo da tela/painel
    setBackground(tema.getBackground());

    // Campo de texto
    tfData.setBackground(tema.getBackground());
    tfData.setForeground(tema.getForeground());
    tfData.setCaretColor(tema.getForeground());

    // Botão calendário
    btnCalendario.setBackground(tema.getBackground().darker());
    btnCalendario.setForeground(tema.getForeground());
}
}
