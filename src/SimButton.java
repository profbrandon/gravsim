
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.border.LineBorder;

import java.awt.Dimension;
import java.awt.Color;
import java.awt.Font;

class SimButton extends JButton {

    public SimButton (Action a) {
        super (a);

        this.setMinimumSize (new Dimension (200,20));
        this.setMaximumSize (new Dimension (200,20));

        this.setBackground (new Color (30,30,30));
        this.setForeground (new Color (100,100,100));
        this.setFont (new Font ("Consolas", Font.PLAIN, 10));
        this.setBorder (new LineBorder (new Color (40,40,40)));
    }
}