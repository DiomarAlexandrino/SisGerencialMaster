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
    public static boolean validarIdadeMinima(LocalDate dataNascimento) {
    if (dataNascimento == null) {
        return false;
    }

    LocalDate hoje = LocalDate.now();
    int idade = hoje.getYear() - dataNascimento.getYear();

    // Ajusta se ainda não fez aniversário no ano atual
    if (hoje.getMonthValue() < dataNascimento.getMonthValue()
            || (hoje.getMonthValue() == dataNascimento.getMonthValue()
            && hoje.getDayOfMonth() < dataNascimento.getDayOfMonth())) {
        idade--;
    }

    return idade >= 12;
}
    
}
