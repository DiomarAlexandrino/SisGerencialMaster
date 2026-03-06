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
            .withResolverStyle(ResolverStyle.STRICT);
    private Runnable onChange;
    private Runnable onDateSelected;
    
    private boolean modoNavegacao = true;
    private Boolean ultimoEstadoValido = null;
    private TemaEnum temaAtual = TemaEnum.CLARO;
    
    // ✅ CORES PARA CAMPOS DESABILITADOS POR TEMA
    private static final Color FUNDO_DESABILITADO_CLARO = new Color(240, 240, 240);
    private static final Color FUNDO_DESABILITADO_ESCURO = new Color(30, 30, 30); // Preto suave
    private static final Color FUNDO_DESABILITADO_VERDE = new Color(30, 30, 30); // Preto suave também para verde
    
    private static final Color TEXTO_DESABILITADO_CLARO = Color.BLACK;
    private static final Color TEXTO_DESABILITADO_ESCURO = new Color(200, 200, 200); // Cinza claro
    private static final Color TEXTO_DESABILITADO_VERDE = new Color(200, 200, 200); // Cinza claro

    public Runnable getOnDateSelected() {
        return onDateSelected;
    }

    public void setOnDateSelected(Runnable listener) {
        this.onDateSelected = listener;
    }

    public CampoDataComCalendario() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

        tfData = criarCampoComMascara();
        btnCalendario = new JButton("📅");
        btnCalendario.setMargin(new Insets(2, 6, 2, 6));

        add(tfData);
        add(btnCalendario);

        configurarCalendario();
        adicionarListenerValidacao(null);
        
        setCampoValido(null);
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
            if (!tfData.isEnabled()) {
                return;
            }
            
            JDialog popup = new JDialog(SwingUtilities.getWindowAncestor(this),
                    "Selecionar Data", Dialog.ModalityType.APPLICATION_MODAL);
            popup.setLayout(new BorderLayout());
            
            popup.getContentPane().setBackground(temaAtual.getBackground());
            popup.setBackground(temaAtual.getBackground());

            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBackground(temaAtual.getBackground());
            
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(5, 5, 5, 5);

            // Ano - COMBOBOX com tema
            JLabel lblAno = new JLabel("Ano:");
            lblAno.setForeground(temaAtual.getForeground());
            
            JComboBox<Integer> yearCombo = new JComboBox<>();
            int anoAtual = LocalDate.now().getYear();
            for (int i = anoAtual; i >= anoAtual - 100; i--) {
                yearCombo.addItem(i);
            }
            aplicarTemaCombo(yearCombo);

            // Mês - COMBOBOX com tema
            JLabel lblMes = new JLabel("Mês:");
            lblMes.setForeground(temaAtual.getForeground());
            
            String[] meses = java.util.Arrays.stream(Month.values())
                    .map(m -> m.getDisplayName(java.time.format.TextStyle.FULL, new Locale("pt", "BR")))
                    .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1))
                    .toArray(String[]::new);

            JComboBox<String> mesCombo = new JComboBox<>(meses);
            aplicarTemaCombo(mesCombo);

            // Dias - PAINEL com tema
            JPanel daysPanel = new JPanel(new GridLayout(6, 7, 2, 2));
            daysPanel.setBackground(temaAtual.getBackground());
            daysPanel.setForeground(temaAtual.getForeground());

            Runnable atualizarDias = () -> {
                daysPanel.removeAll();
                int mesIndex = mesCombo.getSelectedIndex() + 1;
                int ano = (int) yearCombo.getSelectedItem();
                int diasNoMes = java.time.YearMonth.of(ano, mesIndex).lengthOfMonth();

                for (int d = 1; d <= diasNoMes; d++) {
                    int dia = d;
                    JButton dayBtn = new JButton(String.valueOf(d));
                    dayBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    
                    if (temaAtual == TemaEnum.ESCURO || temaAtual == TemaEnum.VERDE) {
                        dayBtn.setBackground(new Color(60, 60, 60));
                        dayBtn.setForeground(Color.WHITE);
                        dayBtn.setBorder(new LineBorder(new Color(80, 80, 80), 1, true));
                    } else {
                        dayBtn.setBackground(new Color(0xF0F0F0));
                        dayBtn.setForeground(Color.BLACK);
                        dayBtn.setBorder(new LineBorder(new Color(0xDDDDDD), 1, true));
                    }
                    
                    dayBtn.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseEntered(java.awt.event.MouseEvent evt) {
                            if (temaAtual == TemaEnum.ESCURO || temaAtual == TemaEnum.VERDE) {
                                dayBtn.setBackground(new Color(80, 80, 80));
                            } else {
                                dayBtn.setBackground(new Color(220, 220, 220));
                            }
                        }
                        public void mouseExited(java.awt.event.MouseEvent evt) {
                            if (temaAtual == TemaEnum.ESCURO || temaAtual == TemaEnum.VERDE) {
                                dayBtn.setBackground(new Color(60, 60, 60));
                            } else {
                                dayBtn.setBackground(new Color(0xF0F0F0));
                            }
                        }
                    });

                    dayBtn.addActionListener(ev -> {
                        setDate(LocalDate.of(ano, mesIndex, dia));
                        popup.setVisible(false);
                        dispararAlteracao();

                        if (onDateSelected != null) {
                            onDateSelected.run();
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
            panel.add(lblAno, c);
            c.gridx = 1;
            panel.add(yearCombo, c);
            c.gridx = 0;
            c.gridy = 1;
            panel.add(lblMes, c);
            c.gridx = 1;
            panel.add(mesCombo, c);
            c.gridx = 0;
            c.gridy = 2;
            c.gridwidth = 2;
            panel.add(daysPanel, c);

            atualizarDias.run();

            popup.add(panel, BorderLayout.CENTER);
            popup.pack();
            popup.setLocationRelativeTo(this);
            popup.setVisible(true);
        });
    }
    
    private void aplicarTemaCombo(JComboBox<?> combo) {
        combo.setBackground(temaAtual.getBackground());
        combo.setForeground(temaAtual.getForeground());
        combo.setOpaque(true);
        
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {

                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);

                label.setOpaque(true);

                if (isSelected) {
                    if (temaAtual == TemaEnum.ESCURO || temaAtual == TemaEnum.VERDE) {
                        label.setBackground(new Color(80, 80, 80));
                        label.setForeground(Color.WHITE);
                    } else {
                        label.setBackground(temaAtual.getBackground().darker());
                        label.setForeground(temaAtual.getForeground());
                    }
                } else {
                    label.setBackground(temaAtual.getBackground());
                    label.setForeground(temaAtual.getForeground());
                }

                return label;
            }
        });
        
        Component editor = combo.getEditor().getEditorComponent();
        if (editor != null) {
            editor.setBackground(temaAtual.getBackground());
            editor.setForeground(temaAtual.getForeground());
        }
        
        combo.updateUI();
        combo.repaint();
    }

    public void setDate(LocalDate date) {
        if (date != null) {
            tfData.setText(date.format(formatter));
            if (!modoNavegacao) {
                setCampoValido(true);
            } else {
                ultimoEstadoValido = true;
            }
        } else {
            tfData.setText("");
            if (!modoNavegacao) {
                setCampoValido(false);
            } else {
                ultimoEstadoValido = false;
            }
        }
        if (onChange != null) {
            onChange.run();
        }
    }

    public LocalDate getDate() {
        String texto = tfData.getText().trim();

        if (texto.isEmpty() || texto.contains("_")) {
            if (!modoNavegacao) {
                setCampoValido(false);
            }
            return null;
        }

        try {
            LocalDate data = LocalDate.parse(texto, formatter);
            if (!modoNavegacao) {
                setCampoValido(true);
            }
            return data;
        } catch (DateTimeParseException e) {
            if (!modoNavegacao) {
                setCampoValido(false);
            }
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
                getDate();
                if (onChange != null) {
                    onChange.run();
                }
            }
        });
    }

    public void setModoNavegacao(boolean navegacao) {
        this.modoNavegacao = navegacao;
        
        if (navegacao) {
            tfData.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            ultimoEstadoValido = null;
        } else {
            if (ultimoEstadoValido != null) {
                setCampoValido(ultimoEstadoValido);
            } else {
                getDate();
            }
        }
    }

    /**
     * ✅ NOVO: Retorna a cor de fundo apropriada para campo desabilitado baseado no tema
     */
    private Color getFundoDesabilitado() {
        switch (temaAtual) {
            case ESCURO:
                return FUNDO_DESABILITADO_ESCURO; // Preto suave
            case VERDE:
                return FUNDO_DESABILITADO_VERDE; // Preto suave
            case CLARO:
            default:
                return FUNDO_DESABILITADO_CLARO; // Cinza claro
        }
    }
    
    /**
     * ✅ NOVO: Retorna a cor do texto apropriada para campo desabilitado baseado no tema
     */
    private Color getTextoDesabilitado() {
        switch (temaAtual) {
            case ESCURO:
                return TEXTO_DESABILITADO_ESCURO; // Cinza claro (visível no preto)
            case VERDE:
                return TEXTO_DESABILITADO_VERDE; // Cinza claro
            case CLARO:
            default:
                return TEXTO_DESABILITADO_CLARO; // Preto
        }
    }

    public void setEnabledCampo(boolean enabled) {
        tfData.setEnabled(enabled);
        btnCalendario.setEnabled(enabled);
        
        setModoNavegacao(!enabled);
        
        // ✅ Define cores baseadas no tema e no estado enabled/disabled
        if (enabled) {
            // Campo habilitado - usa cores do tema
            tfData.setBackground(temaAtual.getBackground());
            tfData.setForeground(temaAtual.getForeground());
            btnCalendario.setBackground(temaAtual.getBackground().darker());
            btnCalendario.setForeground(temaAtual.getForeground());
        } else {
            // ✅ Campo desabilitado - cores específicas por tema
            Color fundoDesabilitado = getFundoDesabilitado();
            Color textoDesabilitado = getTextoDesabilitado();
            
            tfData.setBackground(fundoDesabilitado);
            tfData.setForeground(textoDesabilitado);
            btnCalendario.setBackground(fundoDesabilitado);
            btnCalendario.setForeground(textoDesabilitado);
        }
        
        setBackground(temaAtual.getBackground());
    }

    public void setCampoValido(Boolean valido) {
        if (modoNavegacao) {
            tfData.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            return;
        }
        
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
        ultimoEstadoValido = false;
        if (!modoNavegacao) {
            setCampoValido(false);
        } else {
            tfData.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        }
    }

    private void dispararAlteracao() {
        if (onChange != null) {
            onChange.run();
        }
    }

    public void aplicarTema(TemaEnum tema) {
        this.temaAtual = tema;
        
        setBackground(tema.getBackground());

        // ✅ Atualiza cores baseado no novo tema e no estado atual
        boolean enabled = tfData.isEnabled();
        if (enabled) {
            tfData.setBackground(tema.getBackground());
            tfData.setForeground(tema.getForeground());
            btnCalendario.setBackground(tema.getBackground().darker());
            btnCalendario.setForeground(tema.getForeground());
        } else {
            // Campo desabilitado com o novo tema
            tfData.setBackground(getFundoDesabilitado());
            tfData.setForeground(getTextoDesabilitado());
            btnCalendario.setBackground(getFundoDesabilitado());
            btnCalendario.setForeground(getTextoDesabilitado());
        }
        
        tfData.setCaretColor(enabled ? tema.getForeground() : getTextoDesabilitado());
    }

    public void setOnChange(Runnable listener) {
        this.onChange = listener;
    }
}