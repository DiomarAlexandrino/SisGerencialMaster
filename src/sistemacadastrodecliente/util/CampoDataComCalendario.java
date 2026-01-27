package sistemacadastrodecliente.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.text.MaskFormatter;
import sistemacadastrodecliente.view.temas.TemaEnum;

public class CampoDataComCalendario extends JPanel {

    private final JFormattedTextField tfData;
    private final JButton btnCalendario;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
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

            // Mês (em português)
            String[] meses = java.util.Arrays.stream(Month.values())
                    .map(m -> {
                        String nome = m.getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
                        return nome.substring(0, 1).toUpperCase() + nome.substring(1); // primeira letra maiúscula
                    })
                    .toArray(String[]::new);

            JComboBox<String> mesCombo = new JComboBox<>(meses);

            // Dia
            JPanel daysPanel = new JPanel(new GridLayout(6, 7, 2, 2));

            // Atualizar dias conforme mês/ano selecionados
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
                        if (onChange != null) {
                            onChange.run();
                        }
                    });
                    daysPanel.add(dayBtn);
                }

                daysPanel.revalidate();
                daysPanel.repaint();
            };

            yearCombo.addActionListener(a -> atualizarDias.run());
            mesCombo.addActionListener(a -> atualizarDias.run());

            c.gridx = 0;
            c.gridy = 0;
            panel.add(new JLabel("Ano:"), c);
            c.gridx = 1;
            panel.add(yearCombo, c);
            c.gridx = 0;
            c.gridy = 1;
            panel.add(new JLabel("Mês:"), c);
            c.gridx = 1;
            panel.add(mesCombo, c);
            c.gridx = 0;
            c.gridy = 2;
            c.gridwidth = 2;
            panel.add(daysPanel, c);

            atualizarDias.run(); // inicializa os dias

            popup.add(panel, BorderLayout.CENTER);
            popup.pack();
            popup.setLocationRelativeTo(this);
            popup.setVisible(true);
        });
    }

    // ================= API PÚBLICA =================
    public void setDate(LocalDate date) {
        if (date != null) {
            tfData.setValue(null);
            tfData.setText(date.format(formatter));
        } else {
            tfData.setValue(null);
        }
        if (onChange != null) {
            onChange.run();
        }
    }

    public LocalDate getDate() {
        try {
            String texto = tfData.getText();
            if (texto.contains("_") || texto.isBlank()) {
                return null;
            }

            LocalDate data = LocalDate.parse(texto, formatter);

            // Validação de idade mínima
            LocalDate hoje = LocalDate.now();
            int idade = java.time.Period.between(data, hoje).getYears();
            if (idade < 12) {
                // Se tiver menos de 12 anos, considera inválido
                setCampoValido(false); // marca visualmente
                return null;
            }

            setCampoValido(true); // campo válido
            return data;
        } catch (Exception e) {
            setCampoValido(false); // data inválida
            return null;
        }
    }

    public void adicionarListenerValidacao(Runnable listener) {
        this.onChange = listener;

        tfData.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                disparar();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                disparar();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                disparar();
            }

            private void disparar() {
                if (onChange != null) {
                    onChange.run();
                }
            }
        });

        tfData.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (onChange != null) {
                    onChange.run();
                }
            }
        });
    }

    public void setEnabledCampo(boolean enabled) {
        tfData.setEnabled(enabled);      // bloqueia digitação
        btnCalendario.setEnabled(enabled); // bloqueia o botão do calendário
        if (enabled) {
            setBackground(Color.WHITE);    // aparência ativa
            tfData.setBackground(Color.WHITE);
        } else {
            setBackground(Color.LIGHT_GRAY);  // aparência desativada
            tfData.setBackground(Color.GRAY);
        }
    }

    public void setCampoValido(Boolean valido) {
        if (valido == null) {
            // campo vazio / não avaliado
            tfData.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        } else if (valido) {
            // válido
            tfData.setBorder(BorderFactory.createLineBorder(Color.GREEN));
        } else {
            // inválido
            tfData.setBorder(BorderFactory.createLineBorder(Color.RED));
        }
    }

    public void resetarVisual() {
        tfData.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    }

    public void aplicarTema(TemaEnum tema) {
        setBackground(tema.getBackground());
        tfData.setBackground(tema.getBackground());
        tfData.setForeground(tema.getForeground());
        btnCalendario.setBackground(tema.getBackground());
        btnCalendario.setForeground(tema.getForeground());
    }

}
