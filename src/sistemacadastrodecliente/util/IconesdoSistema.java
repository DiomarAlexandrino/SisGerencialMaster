/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemacadastrodecliente.util;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingConstants;

/**
 *
 * @author diomar.alexandrino
 */
public class IconesdoSistema {

    private static Dimension tamanho;

       /**
     * Cria um JButton com texto, ícone, listener e tamanho uniforme.
     */
       /**
     * Cria um JButton com texto, ícone, listener e tamanho uniforme.
     */
    public static JButton criarBotao(String texto, String nomeIcone, ActionListener acao, Dimension tamanho) {
        JButton botao = new JButton(texto);

        ImageIcon icon = criarIcon(nomeIcone, 20, 20);
        if (icon != null) {
            botao.setIcon(icon);
            botao.setHorizontalTextPosition(SwingConstants.RIGHT);
            botao.setVerticalTextPosition(SwingConstants.CENTER);
            botao.setIconTextGap(5);
        }

        botao.setPreferredSize(tamanho);
        botao.setMinimumSize(tamanho);
        botao.setMaximumSize(tamanho);

        botao.addActionListener(acao);
        return botao;
    }

    /**
     * Cria um ImageIcon redimensionado.
     */
    public static ImageIcon criarIcon(String nomeArquivo, int largura, int altura) {
        ImageIcon originalIcon = new ImageIcon("src/sistemacadastrodecliente/view/icons/" + nomeArquivo);
        if (originalIcon.getIconWidth() > 0) {
            Image scaledImage = originalIcon.getImage().getScaledInstance(largura, altura, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImage);
        } else {
            System.out.println("⚠️ Ícone não encontrado: " + nomeArquivo);
            return null;
        }
    }
}