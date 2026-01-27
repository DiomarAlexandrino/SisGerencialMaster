/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemacadastrodecliente.util;

/**
 *
 * @author diomar.alexandrino
 */
public class ValidadorCEP {

    public static boolean validar(String cep) {
        if (cep == null || cep.isEmpty()) return false;

        // Remove espaços em branco
        cep = cep.trim();

        // Regex para validar CEP no formato 12345-678 ou 12345678
        return cep.matches("\\d{5}-?\\d{3}");
    }
}