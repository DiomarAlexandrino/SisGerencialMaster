package sistemacadastrodecliente.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public class ValidadorData {

    private static final DateTimeFormatter FORMATADOR =
            DateTimeFormatter.ofPattern("dd/MM/uuuu")
                    .withResolverStyle(ResolverStyle.STRICT);

    public static LocalDate parse(String texto) {
        try {
            return LocalDate.parse(texto, FORMATADOR);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static boolean dataValida(String texto) {
        return parse(texto) != null;
    }
}
