/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemacadastrodecliente.app;

import javax.swing.SwingUtilities;
import sistemacadastrodecliente.view.TelaDoCadastro;


/**
 *
 * @author diomar.alexandrino
 */
public class Main {
        public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            new TelaDoCadastro();
        });

    }
}
