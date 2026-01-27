/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemacadastrodecliente.view;

/**
 *
 * @author diomar.alexandrino
 */
// ===== Interface auxiliar para simplificar DocumentListener =====
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

@FunctionalInterface
public interface SimpleDocumentListener extends DocumentListener {

    void update();

    @Override
    default void insertUpdate(DocumentEvent e) {
        update();
    }

    @Override
    default void removeUpdate(DocumentEvent e) {
        update();
    }

    @Override
    default void changedUpdate(DocumentEvent e) {
        update();
    }

    // ✅ Método utilitário para criar listener a partir de lambda
    static SimpleDocumentListener of(Runnable r) {
        return r::run;
    }
}
