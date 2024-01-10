
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;


class IdPanel extends JPanel implements MouseListener {

    private final Simulator sim;
    private final SimPanel simPanel;
    private final GravSim parent;

    public IdPanel (GravSim parent, Simulator sim, SimPanel simPanel) {
        super ();

        this.parent = parent;
        this.sim = sim;
        this.simPanel = simPanel;

        this.setMaximumSize (new Dimension (200,200));
        this.setMinimumSize (new Dimension (200,200));

        this.setBackground (new Color (10,10,10));
        this.setBorder (new LineBorder (new Color (40,40,40)));
    }

    @Override
    public void paint (Graphics g) {
        super.paint (g);

        g.setFont (new Font ("Consolas", Font.PLAIN, 10));

        g.setColor (new Color (100,100,100));

        g.drawString ("TOP 10 BODIES BY MASS", 5, 15);
        g.drawLine (4,17, this.getWidth () - 4,17);
        g.drawString ("CLICK TO DESELECT", 5, 30);

        for (int i = 0; i < Math.min (this.sim.getAlive ().size (), 10); ++i) {
            Body b = this.sim.getAlive ().get (i);

            g.setColor (b.getColor ());
            g.drawString (b.toString (), 5, 15 * (i + 3));
        }
    }

    @Override
    public void mouseClicked (MouseEvent e) {
        if (this.contains (e.getX () - simPanel.getWidth () - this.getX () - 8, e.getY () - this.getY () - 31)) {
            int i = (int) ((e.getY () - this.getY () - 35) / 15 - 2);

            if (0 <= i && i < Math.min (this.sim.getAlive ().size (), 10)) {
                this.simPanel.setSelected (this.sim.getAlive ().get (i));
            }
            else if (i == -1) {
                this.simPanel.setSelected (null);
                this.simPanel.setRef ();
            }

            this.parent.repaint ();
        }
    }

    @Override
    public void mouseEntered (MouseEvent e) {

    }

    @Override
    public void mouseExited (MouseEvent e) {
        
    }

    @Override
    public void mousePressed (MouseEvent e) {

    }

    @Override
    public void mouseReleased (MouseEvent e) {

    }
}