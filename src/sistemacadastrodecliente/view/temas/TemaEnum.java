package sistemacadastrodecliente.view.temas;

/**
 *
 * @author diomar.alexandrino
 */
import java.awt.Color;

public enum TemaEnum {
    CLARO(new Color(245, 245, 245), Color.BLACK),
    ESCURO(new Color(45, 45, 45), Color.WHITE),
    VERDE(new Color(200, 255, 200), Color.DARK_GRAY);

    private final Color background;
    private final Color foreground;

    TemaEnum(Color bg, Color fg) {
        this.background = bg;
        this.foreground = fg;
    }

    public Color getBackground() {
        return background;
    }

    public Color getForeground() {
        return foreground;
    }
}
