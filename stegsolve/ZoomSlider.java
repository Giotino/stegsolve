package stegsolve;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class ZoomSlider extends JPanel {

    private JSlider slider;
    private JTextField textBox;
    private int value;
    
    public int getValue () { return value; }

    void setValue(int value) {
        slider.setValue(value);
        textBox.setText(String.valueOf(value));
        this.value = value;
        for (SliderChangeListener scl : changeListeners)
            scl.change(value);
    }

    private List<SliderChangeListener> changeListeners = new ArrayList<>();

    ZoomSlider(int min, int max, int defaultValue) {
        JLabel tip = new JLabel("Zoom:");

        add(tip);

        slider = new JSlider(min, max, defaultValue);

        slider.addChangeListener(e -> {
            setValue(slider.getValue());
        });

        add(slider);

        textBox = new JTextField(String.valueOf(defaultValue), 5);

        textBox.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER){
                    setValue(Integer.parseInt(textBox.getText()));
                }
            }
        });

        slider.setMaximumSize(new Dimension(500, 25));

        add(textBox);

        value = defaultValue;
    }

    void addChangeListener(SliderChangeListener listener) {
        changeListeners.add(listener);
    }
}

interface SliderChangeListener {
    void change(int v);
}