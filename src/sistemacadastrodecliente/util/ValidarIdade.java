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
        if (dataNascimento == null) {
            return false; // Se não informar data, inválido
        }

        LocalDate hoje = LocalDate.now();
        int idade = hoje.getYear() - dataNascimento.getYear();

        // Ajusta idade se ainda não fez aniversário este ano
        if (hoje.getMonthValue() < dataNascimento.getMonthValue()
                || (hoje.getMonthValue() == dataNascimento.getMonthValue()
                && hoje.getDayOfMonth() < dataNascimento.getDayOfMonth())) {
            idade--;
        }

        // Calcula limites de ano válidos
        LocalDate dataMinima = hoje.minusYears(150); // idade máxima 150
        LocalDate dataMaxima = hoje.minusYears(12);  // idade mínima 12

        // Verifica se a data está dentro do intervalo permitido
        return !dataNascimento.isBefore(dataMinima) && !dataNascimento.isAfter(dataMaxima);
    }
    
}
