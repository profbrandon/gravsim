import javax.swing.SwingUtilities;
import javax.swing.AbstractAction;
import javax.swing.Timer;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.GroupLayout;

import javax.imageio.ImageIO;

import java.io.File;

import java.awt.Color;
import java.awt.BorderLayout;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

import java.awt.image.BufferedImage;


class GravSim extends JFrame implements ActionListener, MouseListener, KeyListener {

    private Simulator sim;
    private SimPanel  simPanel;
    private IdPanel   idPanel;
    private Timer     timer;




    private JButton toggleTopTenButton = new SimButton (new AbstractAction ("Toggle Top 10") {
        public void actionPerformed (ActionEvent e) {
            idPanel.setVisible (!idPanel.isVisible ());
            //sim.setSortEnabled (idPanel.isVisible ());
        }
    });

    private JButton toggleTrailsButton = new SimButton (new AbstractAction ("Toggle Trails") {
        public void actionPerformed (ActionEvent e) {
            simPanel.toggleTrails ();
            repaint ();
        }
    });

    private JSlider trailLengthSlider = new SimSlider (0, SimPanel.MAX_TRAIL_LEN, 0, (Integer value) -> {
            simPanel.changeTrailLength (value);
            repaint ();
        }, (Integer value) -> {
            return "Trail Length: " + value; 
    });

    private JButton toggleLabelsButton = new SimButton (new AbstractAction ("Toggle Identifiers") {
        public void actionPerformed (ActionEvent e) {
            simPanel.toggleLabels ();
            repaint ();
        }
    });

    private JButton startPlayPauseButton = new SimButton (new AbstractAction ("Start") {
        public void actionPerformed (ActionEvent e) {
            if (timer.isRunning ()) {
                startPlayPauseButton.setText ("Play");
                timer.stop ();
            }
            else {
                startPlayPauseButton.setText ("Pause");
                timer.start ();
            }
            repaint ();
        }
    });

    private JSlider magnificationSlider = new SimSlider (-30, 30, 0, (Integer value) -> {
            simPanel.setScale (((double) value) / 10.0);
            repaint ();
        }, 
        (Integer value) -> { 
            return String.format ("Mag: %fx", Math.pow (10, ((double) value) / 10.0)); 
    });

    private JButton toggleDataButton = new SimButton (new AbstractAction ("Toggle Data") {
        public void actionPerformed (ActionEvent e) {
            simPanel.toggleData ();
            repaint ();
        }
    });

    private JButton setRefButton = new SimButton (new AbstractAction ("Set Relative") {
        public void actionPerformed (ActionEvent e) {
            simPanel.setRef ();
            repaint ();
        }
    });

    private JTextField searchByIdTextField = new SimTextField ("BODY ID", new AbstractAction ("BODY ID") {
        public void actionPerformed (ActionEvent e) {
            for (Body b : sim.getAlive ()) {
                if (b.getId ().equals (searchByIdTextField.getText ().toUpperCase ())) {
                    simPanel.setSelected (b);
                    repaint ();
                    return;
                }
            }
            
            searchByIdTextField.setText ("");
            repaint ();
            //requestFocus ();
        }
    });



    private GravSim () {
        super ("2D Gravity Simulator");

        this.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
        this.setVisible (true);
        this.setLayout (new BorderLayout ());

        try {
            BufferedImage icon = ImageIO.read (new File ("resources/gravity-icon.png"));

            this.setIconImage(icon);
        }
        catch (Exception e) {
            System.out.println (e);
        }

        this.sim = new Simulator ();

        /*  
        this.sim.addBody (new Body (20.0, new Color (160, 50, 0), "0", new Vec (-6.0, 0.0), Vec.ZERO, 0.0));
        this.sim.addBody (new Body (1.0, new Color (100, 100, 100), "1", new Vec (16.0, 0.0), new Vec (0.0, 0.2), 0.0));
        */
/*
        this.sim.addBody (new Body (100.0, new Color (220, 240, 180), "0", Vec.ZERO, Vec.ZERO, 0.0));
        this.sim.addBody (new Body (1.0, new Color (150, 20, 20), "1", new Vec (100.0, 0.0), new Vec (0.0, 0.316), 0.0));
        this.sim.addBody (new Body (0.01, new Color (100,100,100), "T0", new Vec (50.0, 86.6), new Vec (-0.274, 0.158), 0.0));
        this.sim.addBody (new Body (0.01, new Color (80, 80, 80), "T1", new Vec (50.0, -86.6), new Vec (0.274, 0.158), 0.0));
        this.sim.addBody (new Body (0.01, new Color (120,120,120), "T2", new Vec (71.6, 0.0), new Vec (0.0, 0.235), 0.0));
*/
        
        for (int i = 0; i < 3000; ++i) {
            this.sim.addRandomBody (0.001, 0.001, Integer.toHexString (i).toUpperCase ());
        }

        this.sim.addBody (new Body (20.0, new Color (255,255,100), "STAR", Vec.ZERO, Vec.ZERO, 0.0));
        
        this.sim.addBody (new Body (1.0, new Color (120,120,80), "MUSHIE", new Vec (500.0,0.0), new Vec (0.0,0.063), 0.0));
        

        /*
        this.sim.addBody (new Body (5.0, new Color (255,0,0), "P0", new Vec (0.0,0.0), new Vec (-0.11,-0.10), 0.0));
        this.sim.addBody (new Body (5.0, new Color (0,255,255), "P1", new Vec (-33.0,10.0), new Vec (0.055,0.05), 0.0));
        this.sim.addBody (new Body (5.0, new Color (255,255,255), "P2", new Vec (33.0,-10.0), new Vec (0.055,0.05), 0.0));
        */
        
        /*
        this.sim.addBody (new Body (1000.0, new Color (50,150,255), "a", new Vec (0.0,0.0), new Vec (0.0,0.0), 0.5));
        this.sim.addBody (new Body (1.0, new Color (255,100,0), "b", new Vec (0.0,-120.0), new Vec (0.4,0.0), 2.0));
        this.sim.addBody (new Body (10.0, new Color (255,0,0), "c", new Vec (0.0,-160.0), new Vec (0.8,0.0), 0.1));
        this.sim.addBody (new Body (0.1, new Color (255,255,255), "d", new Vec (0.0,-165.0), new Vec (0.35,0.0), 2.0));
        */
        this.sim.init ();

        this.simPanel = new SimPanel (this.sim);
        this.add (this.simPanel, BorderLayout.CENTER);
        
        // Button Panel
        JPanel buttonPanel = new JPanel ();

        buttonPanel.setBackground (new Color (20,20,20));

        GroupLayout layout = new GroupLayout (buttonPanel);
        buttonPanel.setLayout (layout);

        layout.setAutoCreateGaps (true);
        layout.setAutoCreateContainerGaps (true);

        this.idPanel = new IdPanel (this, this.sim, this.simPanel);

        this.addMouseListener (this.idPanel);
        this.addMouseListener (this);

        this.addKeyListener (this.simPanel);
        this.addKeyListener (this);

        buttonPanel.add (this.idPanel);
        buttonPanel.add (this.searchByIdTextField);
        buttonPanel.add (this.toggleTopTenButton);
        buttonPanel.add (this.toggleTrailsButton);
        buttonPanel.add (this.trailLengthSlider);
        buttonPanel.add (this.toggleLabelsButton);
        buttonPanel.add (this.magnificationSlider);
        buttonPanel.add (this.toggleDataButton);
        buttonPanel.add (this.setRefButton);
        buttonPanel.add (this.startPlayPauseButton);

        layout.setHorizontalGroup (layout.createSequentialGroup ()
            .addGroup (layout.createParallelGroup (GroupLayout.Alignment.LEADING)
                .addComponent (this.idPanel)
                .addComponent (this.searchByIdTextField)
                .addComponent (this.toggleTopTenButton)
                .addComponent (this.toggleTrailsButton)
                .addComponent (this.trailLengthSlider)
                .addComponent (this.toggleLabelsButton)
                .addComponent (this.magnificationSlider)
                .addComponent (this.toggleDataButton)
                .addComponent (this.setRefButton)
                .addComponent (this.startPlayPauseButton)
            )
        );

        layout.setVerticalGroup (layout.createSequentialGroup ()
            .addComponent (this.idPanel)
            .addComponent (this.searchByIdTextField)
            .addComponent (this.toggleTopTenButton)
            .addComponent (this.toggleTrailsButton)
            .addComponent (this.trailLengthSlider)
            .addComponent (this.toggleLabelsButton)
            .addComponent (this.magnificationSlider)
            .addComponent (this.toggleDataButton)
            .addComponent (this.setRefButton)
            .addComponent (this.startPlayPauseButton)
        );

        this.add (buttonPanel, BorderLayout.EAST);

        this.pack ();
        this.repaint ();
        this.requestFocus ();

        this.timer = new Timer (10, this);
    }

    @Override
    public void actionPerformed (ActionEvent e) {
        this.sim.update ();
        this.repaint ();
    }

    @Override
    public void mousePressed (MouseEvent e) {}

    @Override
    public void mouseReleased (MouseEvent e) {}

    @Override
    public void mouseClicked (MouseEvent e) {
        if (e.getX () < this.simPanel.getWidth ()) {
            this.requestFocus ();
        }
    }

    @Override
    public void mouseEntered (MouseEvent e) {

    }

    @Override
    public void mouseExited (MouseEvent e) {

    }

    @Override
    public void keyPressed (KeyEvent e) {
        this.repaint ();
    }

    @Override
    public void keyReleased (KeyEvent e) {
        this.repaint ();
    }

    @Override
    public void keyTyped (KeyEvent e) {

    }

    public static void main (String[] args) {
        SwingUtilities.invokeLater (() -> {
            new GravSim ();
        });
    }
}