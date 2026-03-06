package sistemacadastrodecliente.util;



import java.awt.Color;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import sistemacadastrodecliente.model.enums.UF;
import sistemacadastrodecliente.view.temas.TemaEnum;

public class ComboBoxUF extends JComboBox<UF> {
    
    private TemaEnum temaAtual = TemaEnum.CLARO;
    
    // Cores fixas para cada tema quando desabilitado
    private static final Color FUNDO_DESABILITADO_CLARO = new Color(240, 240, 240);
    private static final Color TEXTO_DESABILITADO_CLARO = Color.BLACK;
    
    private static final Color FUNDO_DESABILITADO_ESCURO = new Color(45, 45, 45);
    private static final Color TEXTO_DESABILITADO_ESCURO = new Color(220, 220, 220);
    
    private static final Color FUNDO_DESABILITADO_VERDE = new Color(35, 55, 35);
    private static final Color TEXTO_DESABILITADO_VERDE = new Color(200, 230, 200);
    
    // Renderer personalizado
    private final ComboBoxRenderer renderer = new ComboBoxRenderer();
    
    public ComboBoxUF() {
        super(UF.values());
        setRenderer(renderer);
        setSelectedItem(UF.UF);
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        
        // Força a atualização visual imediata
        SwingUtilities.invokeLater(() -> {
            atualizarCores();
            revalidate();
            repaint();
            
            // Forçar atualização do popup se estiver aberto
            firePropertyChange("enabled", !enabled, enabled);
        });
    }
    
    public void aplicarTema(TemaEnum tema) {
        this.temaAtual = tema;
        atualizarCores();
        
        // Forçar redesenho do renderer
        renderer.temaAtual = tema;
        renderer.comboEnabled = isEnabled();
    }
    
    private void atualizarCores() {
        if (!isEnabled()) {
            // Desabilitado - usa cores fixas
            setBackground(getFundoDesabilitado());
            setForeground(getTextoDesabilitado());
        } else {
            // Habilitado - usa cores do tema
            setBackground(temaAtual.getBackground());
            setForeground(temaAtual.getForeground());
        }
        
        // Atualizar cores do renderer
        renderer.comboEnabled = isEnabled();
    }
    
    private Color getFundoDesabilitado() {
        switch (temaAtual) {
            case ESCURO:
                return FUNDO_DESABILITADO_ESCURO;
            case VERDE:
                return FUNDO_DESABILITADO_VERDE;
            default:
                return FUNDO_DESABILITADO_CLARO;
        }
    }
    
    private Color getTextoDesabilitado() {
        switch (temaAtual) {
            case ESCURO:
                return TEXTO_DESABILITADO_ESCURO;
            case VERDE:
                return TEXTO_DESABILITADO_VERDE;
            default:
                return TEXTO_DESABILITADO_CLARO;
        }
    }
    
    // Classe interna para o renderer
    private class ComboBoxRenderer extends DefaultListCellRenderer {
        private TemaEnum temaAtual = ComboBoxUF.this.temaAtual;
        private boolean comboEnabled = true;
        
        @Override
        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            
            label.setOpaque(true);
            
            if (!comboEnabled || !isEnabled()) {
                // ComboBox desabilitado - TODOS os itens com cores de desabilitado
                label.setBackground(getFundoDesabilitado());
                label.setForeground(getTextoDesabilitado());
            } else if (isSelected) {
                // Item selecionado em ComboBox habilitado
                label.setBackground(temaAtual.getBackground().darker());
                label.setForeground(temaAtual.getForeground());
            } else {
                // Item normal em ComboBox habilitado
                label.setBackground(temaAtual.getBackground());
                label.setForeground(temaAtual.getForeground());
            }
            
            return label;
        }
    }
    
    // Método auxiliar para selecionar por sigla (mantém sua lógica)
    public void setSelectedUF(String sigla) {
        if (sigla == null || sigla.isEmpty() || sigla.equals("UF")) {
            setSelectedItem(UF.UF);
            return;
        }
        
        for (int i = 0; i < getItemCount(); i++) {
            UF uf = getItemAt(i);
            if (uf.getSigla().equalsIgnoreCase(sigla)) {
                setSelectedItem(uf);
                return;
            }
        }
        setSelectedItem(UF.UF);
    }
}