/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemacadastrodecliente.util;

import java.time.LocalDate;

/**
 *
 * @author diomar.alexandrino
 */
public class ValidarIdade {
    
    public static boolean validarIdade(LocalDate dataNascimento) {
        return validarIdadeComMotivo(dataNascimento) == null;
    }
    
    public static String validarIdadeComMotivo(LocalDate dataNascimento) {
        if (dataNascimento == null) {
            return "Data de nascimento não informada";
        }

        LocalDate hoje = LocalDate.now();
        int idade = hoje.getYear() - dataNascimento.getYear();

        // Ajusta idade se ainda não fez aniversário este ano
        if (hoje.getMonthValue() < dataNascimento.getMonthValue()
                || (hoje.getMonthValue() == dataNascimento.getMonthValue()
                && hoje.getDayOfMonth() < dataNascimento.getDayOfMonth())) {
            idade--;
        }

        // Verifica idade mínima (12 anos)
        if (idade < 12) {
            return "Idade mínima de 12 anos não atingida (idade atual: " + idade + " anos)";
        }
        
        // Verifica idade máxima (150 anos)
        if (idade > 150) {
            return "Idade máxima de 150 anos excedida (idade atual: " + idade + " anos)";
        }

        return null; // idade válida
    }
}
    

