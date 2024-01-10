
import javax.swing.JSlider;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.border.LineBorder;

import java.util.function.Consumer;
import java.util.function.Function;

import java.awt.Color;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.Graphics;


class SimSlider extends JSlider implements ChangeListener {

    private final Consumer<Integer> consumer;
    private final Function<Integer,String> converter;

    public SimSlider (int min, int max, int value, Consumer<Integer> consumer, Function<Integer,String> converter) {
        super (JSlider.HORIZONTAL, min, max, value);

        this.setMinimumSize (new Dimension (200,60));
        this.setMaximumSize (new Dimension (200,60));

        this.setBackground (new Color (20,20,20));
        this.setForeground (new Color (100,100,100));
        this.setFont (new Font ("Consolas", Font.PLAIN, 10));
        this.setBorder (new LineBorder (new Color (40,40,40)));

        this.setPaintLabels (true);
        this.setMajorTickSpacing (max - min);

        this.addChangeListener (this);

        this.consumer = consumer;
        this.converter = converter;
    }

    public SimSlider (int min, int max, int value, Consumer<Integer> consumer) {
        this (min, max, value, consumer, null);
    }

    public void stateChanged (ChangeEvent e) {
        this.consumer.accept (this.getValue ());
    }

    @Override
    public void paint (Graphics g) {
        super.paint (g);

        g.setFont (new Font ("Consolas", Font.PLAIN, 10));

        if (converter != null) {
            g.drawString (this.converter.apply (this.getValue ()), 5, 11);
        }
    }
}