
import javax.swing.JTextField;
import javax.swing.Action;
import javax.swing.border.LineBorder;

import java.awt.Font;
import java.awt.Dimension;
import java.awt.Color;


class SimTextField extends JTextField {

    public SimTextField (String text, Action a) {
        super ();

        this.setMinimumSize (new Dimension (200, 20));
        this.setMaximumSize (new Dimension (200, 20));

        this.setBackground (new Color (10,10,10));
        this.setForeground (new Color (100,100,100));
        this.setBorder (new LineBorder (new Color (40,40,40)));

        this.setFont (new Font ("Consolas", Font.PLAIN, 10));
        this.setText (text);
        this.setHorizontalAlignment (JTextField.CENTER);

        this.setAction (a);
    }
}