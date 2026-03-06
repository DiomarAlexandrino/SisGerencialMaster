package sistemacadastrodecliente.view.temas;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import sistemacadastrodecliente.util.CampoDataComCalendario;
import java.lang.reflect.Field;

public class Tema {

    private static CampoDataComCalendario calendario;
    
    // CORES PARA COMPONENTES DESABILITADOS
    private static final Color FUNDO_DESABILITADO_CLARO = new Color(240, 240, 240);
    private static final Color FUNDO_DESABILITADO_ESCURO = new Color(30, 30, 30); // Preto suave
    private static final Color FUNDO_DESABILITADO_VERDE = new Color(30, 30, 30); // Preto suave
    
    private static final Color TEXTO_DESABILITADO_CLARO = Color.BLACK;
    private static final Color TEXTO_DESABILITADO_ESCURO = new Color(200, 200, 200); // Cinza claro
    private static final Color TEXTO_DESABILITADO_VERDE = new Color(200, 200, 200); // Cinza claro

    public static void setCalendario(CampoDataComCalendario cal) {
        calendario = cal;
    }

    public static void aplicarTema(Component comp, TemaEnum tema) {
        if (comp == null || tema == null) return;

        aplicarTemaPorTipo(comp, tema);

        if (comp instanceof Container container) {
            for (Component child : container.getComponents()) {
                aplicarTema(child, tema);
            }
        }
    }

    private static void aplicarTemaPorTipo(Component comp, TemaEnum tema) {
        // Containers
        if (comp instanceof JPanel || comp instanceof JScrollPane) {
            comp.setBackground(tema.getBackground());
            comp.setForeground(tema.getForeground());
            return;
        }

        // Campos de texto
        if (comp instanceof JTextField || 
            comp instanceof JFormattedTextField || 
            comp instanceof JTextArea) {
            
            aplicarTemaCampoTexto(comp, tema);
            return;
        }

        // ComboBox - TRATAMENTO ESPECIAL
        if (comp instanceof JComboBox<?> combo) {
            aplicarTemaComboBox(combo, tema);
            return;
        }

        // Botões
        if (comp instanceof JButton botao) {
            aplicarTemaBotao(botao, tema);
            return;
        }

        // Labels
        if (comp instanceof JLabel label) {
            aplicarTemaLabel(label, tema);
            return;
        }
        
        // CampoDataComCalendario
        if (comp instanceof CampoDataComCalendario campo) {
            campo.aplicarTema(tema);
            return;
        }
    }

    private static void aplicarTemaCampoTexto(Component comp, TemaEnum tema) {
        if (!comp.isEnabled()) {
            comp.setBackground(getFundoDesabilitado(tema));
            comp.setForeground(getTextoDesabilitado(tema));
        } else {
            comp.setBackground(tema.getBackground());
            comp.setForeground(tema.getForeground());
        }

        if (comp instanceof JTextComponent textComponent) {
            textComponent.setCaretColor(
                comp.isEnabled() ? tema.getForeground() : getTextoDesabilitado(tema)
            );
        }
    }

    /**
     * MÉTODO CORRIGIDO: Força a aplicação de cores no ComboBox desabilitado
     */
    private static void aplicarTemaComboBox(JComboBox<?> combo, TemaEnum tema) {
        
        // Cores baseadas no estado enabled
        Color fundo, texto;
        
        if (!combo.isEnabled()) {
            fundo = getFundoDesabilitado(tema);
            texto = getTextoDesabilitado(tema);
        } else {
            fundo = tema.getBackground();
            texto = tema.getForeground();
        }
        
        // Aplicar cores ao ComboBox
        combo.setBackground(fundo);
        combo.setForeground(texto);
        combo.setOpaque(true);
        
        // Forçar cor de fundo no componente interno
        try {
            // Tentar acessar o componente interno do ComboBox via reflection
            for (int i = 0; i < combo.getComponentCount(); i++) {
                Component c = combo.getComponent(i);
                c.setBackground(fundo);
                c.setForeground(texto);
                
                // Se for um container, processar filhos
                if (c instanceof Container) {
                    for (Component child : ((Container) c).getComponents()) {
                        child.setBackground(fundo);
                        child.setForeground(texto);
                    }
                }
            }
        } catch (Exception e) {
            // Ignorar erros de reflection
        }

        // Renderer personalizado
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

                if (!combo.isEnabled()) {
                    // ComboBox desabilitado
                    label.setBackground(getFundoDesabilitado(tema));
                    label.setForeground(getTextoDesabilitado(tema));
                } else if (isSelected) {
                    label.setBackground(tema.getBackground().darker());
                    label.setForeground(tema.getForeground());
                } else {
                    label.setBackground(tema.getBackground());
                    label.setForeground(tema.getForeground());
                }

                return label;
            }
        });

        // Editor do ComboBox
        Component editor = combo.getEditor().getEditorComponent();
        if (editor != null) {
            editor.setBackground(fundo);
            editor.setForeground(texto);
            
            if (editor instanceof JTextField) {
                ((JTextField) editor).setCaretColor(texto);
            }
            
            // Processar componentes do editor
            if (editor instanceof Container) {
                for (Component child : ((Container) editor).getComponents()) {
                    child.setBackground(fundo);
                    child.setForeground(texto);
                }
            }
        }

        // Forçar atualização completa
        combo.revalidate();
        combo.repaint();
        
        // Pequeno truque para forçar a repaint
        SwingUtilities.invokeLater(() -> {
            combo.revalidate();
            combo.repaint();
        });
    }

    private static void aplicarTemaBotao(JButton botao, TemaEnum tema) {
        if (!botao.isEnabled()) {
            botao.setBackground(new Color(200, 200, 200));
            botao.setForeground(new Color(120, 120, 120));
        } else {
            botao.setBackground(tema.getBackground().darker());
            botao.setForeground(tema.getForeground());
        }
    }

    private static void aplicarTemaLabel(JLabel label, TemaEnum tema) {
        if (!label.isEnabled()) {
            label.setForeground(getTextoDesabilitado(tema));
        } else {
            label.setForeground(tema.getForeground());
        }
    }

    private static Color getFundoDesabilitado(TemaEnum tema) {
        switch (tema) {
            case ESCURO:
                return FUNDO_DESABILITADO_ESCURO;
            case VERDE:
                return FUNDO_DESABILITADO_VERDE;
            case CLARO:
            default:
                return FUNDO_DESABILITADO_CLARO;
        }
    }

    private static Color getTextoDesabilitado(TemaEnum tema) {
        switch (tema) {
            case ESCURO:
                return TEXTO_DESABILITADO_ESCURO;
            case VERDE:
                return TEXTO_DESABILITADO_VERDE;
            case CLARO:
            default:
                return TEXTO_DESABILITADO_CLARO;
        }
    }

    public static void reaplicarTema(Component comp, TemaEnum tema) {
        if (comp != null && tema != null) {
            aplicarTema(comp, tema);
        }
    }

    public static void aplicarTemaCombo(JComboBox<?> combo, TemaEnum tema) {
        if (combo != null && tema != null) {
            aplicarTemaComboBox(combo, tema);
        }
    }
}