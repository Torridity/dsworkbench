package de.tor.tribes.ui.renderer;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import org.jdesktop.swingx.renderer.DefaultTableRenderer;

import de.tor.tribes.types.FarmInformation;

public class SiegeWeaponsOnWayRenderer extends DefaultTableRenderer{
	private ImageIcon BOTH_onWay = null;
	private ImageIcon CATA_onWay = null;
	private ImageIcon RAM_onWay = null;
    private ImageIcon atHome = null;
    private ImageIcon Not_initiated = null;
    private ImageIcon final_farm = null;
    
    public SiegeWeaponsOnWayRenderer() {
        super();
        try {
        	BOTH_onWay = new ImageIcon(FarmStatusCellRenderer.class.getResource("/res/ui/both.png"));
        	CATA_onWay = new ImageIcon(FarmStatusCellRenderer.class.getResource("/res/unit_catapult.png"));
            RAM_onWay = new ImageIcon(FarmStatusCellRenderer.class.getResource("/res/ui/ram.png"));
            atHome = new ImageIcon(FarmStatusCellRenderer.class.getResource("/res/checkbox.png"));
            Not_initiated = new ImageIcon(FarmStatusCellRenderer.class.getResource("/res/ui/spy_needed.png"));
            final_farm = new ImageIcon(FarmStatusCellRenderer.class.getResource("/res/final_farm.png"));
            } catch (Exception ignored) {
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel label = ((JLabel) c);
        
        try {
            label.setText("");

            label.setHorizontalAlignment(SwingConstants.CENTER);
            
            FarmInformation.Siege_STATUS siege_status = (FarmInformation.Siege_STATUS) value;
            switch (siege_status) {  
                case BOTH_onWay:
                    label.setIcon(BOTH_onWay);
                    break;  
                case CATA_onWay:
                    label.setIcon(CATA_onWay);
                    break;  
                case RAM_onWay:
                    label.setIcon(RAM_onWay);
                    break;  
                case Not_initiated:
                	label.setIcon(Not_initiated);
                	break;
                case final_farm:
                	label.setIcon(final_farm);
                	break;
                default:
                    label.setIcon(atHome);
            }
        } catch (Exception e) {
            label.setText("?");
            label.setIcon(null);
        }
        
        return label;
    }
}
