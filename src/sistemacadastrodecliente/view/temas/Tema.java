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
import javax.swing.text.JTextComponent;
import sistemacadastrodecliente.util.CampoDataComCalendario;
import sistemacadastrodecliente.view.temas.TemaEnum;

public class Tema {

    private static CampoDataComCalendario calendario;

    public static void aplicarTema(Component comp, TemaEnum tema) {

        if (calendario != null) {
            calendario.aplicarTema(tema);
        }

        if (comp instanceof JPanel || comp instanceof JScrollPane) {
            comp.setBackground(tema.getBackground());
            comp.setForeground(tema.getForeground());

        } else if (comp instanceof JTextField
                || comp instanceof JFormattedTextField
                || comp instanceof JTextArea) {

            // Usa o tema atual (verde, claro ou escuro)
            comp.setBackground(tema.getBackground());
            comp.setForeground(tema.getForeground());

            // Cursor visível em tema escuro
            if (comp instanceof JTextComponent textComponent) {
                textComponent.setCaretColor(tema.getForeground());
            }

        } else if (comp instanceof JComboBox) {
            comp.setBackground(tema.getBackground());
            comp.setForeground(tema.getForeground());

        } else if (comp instanceof JButton) {
            comp.setBackground(tema.getBackground().darker());
            comp.setForeground(tema.getForeground());

        } else if (comp instanceof JLabel) {
            comp.setForeground(tema.getForeground());
        }

        if (comp instanceof Container container) {
            for (Component child : container.getComponents()) {
                aplicarTema(child, tema);
            }
        }
    }

    public static void aplicarTemaCombo(JComboBox<?> combo, TemaEnum tema) {

        combo.setBackground(tema.getBackground());
        combo.setForeground(tema.getForeground());
        combo.setOpaque(true);

        // 🔹 Renderer da lista (dropdown)
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {

                JLabel lbl = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);

                lbl.setOpaque(true);

                if (isSelected) {
                    lbl.setBackground(tema.getBackground().darker());
                    lbl.setForeground(tema.getForeground());
                } else {
                    lbl.setBackground(tema.getBackground());
                    lbl.setForeground(tema.getForeground());
                }

                return lbl;
            }
        });

        // 🔹 FORÇA O EDITOR / BOTÃO INTERNO
        Component editor = combo.getEditor().getEditorComponent();
        editor.setBackground(tema.getBackground());
        editor.setForeground(tema.getForeground());

        // 🔹 Força atualização visual (Swing é teimoso)
        combo.updateUI();
        combo.repaint();
    }

}
