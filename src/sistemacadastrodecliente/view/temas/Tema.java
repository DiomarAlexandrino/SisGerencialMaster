package sistemacadastrodecliente.view.temas;


import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import sistemacadastrodecliente.view.temas.TemaEnum;

public class Tema {

    public static void aplicarTema(Component comp, TemaEnum tema) {
        if (comp instanceof JPanel || comp instanceof JScrollPane) {
            comp.setBackground(tema.getBackground());
            comp.setForeground(tema.getForeground());
        } else if (comp instanceof JTextField || comp instanceof JFormattedTextField || comp instanceof JTextArea) {
            comp.setBackground(Color.WHITE);
            comp.setForeground(Color.BLACK);
        } else if (comp instanceof JComboBox) {
            comp.setBackground(Color.WHITE);
            comp.setForeground(Color.BLACK);
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
}