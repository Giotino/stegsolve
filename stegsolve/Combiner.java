/*
 * Combiner.java
 */

package stegsolve;

import javax.swing.*;
import java.awt.image.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.filechooser.*;
import java.io.*;
import javax.imageio.*;

/**
 * Image combiner
 * @author Caesum
 */
public class Combiner extends JFrame
{
    /**
     * Label showing the current offset
     */
    private JLabel nowShowing;
    /**
     * Panel for buttons
     */
    private JPanel buttonPanel;
    /**
     * Increase offset button
     */
    private JButton forwardButton;
    /**
     * Decrease offset button
     */
    private JButton backwardButton;
    /**
     * Save image button
     */
    private JButton saveButton;
    /**
     * Panel containing image
     */
    private DPanel dp;
    /**
     * Scroll bars for image
     */
    private JScrollPane scrollPane;

    /**
     * The images being combined
     */
    private BufferedImage bi = null, bi2 = null;
    /**
     * The image after transformation
     */
    private CombineTransform transform = null;
    /**
     * The current combination being tried
     */
    private int inum=0;
    /**
     * current width/height
     */
    private int w=0, h=0;

    /**
     * Creates new form Combiner
     * @param b The image to solve
     */
    public Combiner(BufferedImage b, BufferedImage b2)
    {
        bi = b;
        bi2 = b2;
        initComponents();
        transform = new CombineTransform(bi, bi2);
        newImage();
    }

    // <editor-fold defaultstate="collapsed" desc="Initcomponents()">
    private void initComponents() {

        nowShowing = new JLabel();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout());

        this.add(nowShowing, BorderLayout.NORTH);

        buttonPanel = new JPanel();
        backwardButton = new JButton("<");
        backwardButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                backwardButtonActionPerformed(evt);
            }
        });
        forwardButton = new JButton(">");
        forwardButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                forwardButtonActionPerformed(evt);
            }
        });
        saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(backwardButton);
        buttonPanel.add(forwardButton);
        buttonPanel.add(saveButton);

        add(buttonPanel, BorderLayout.SOUTH);

        backwardButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,0), "back");
        backwardButton.getActionMap().put("back", backButtonPress);
        forwardButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,0), "forward");
        forwardButton.getActionMap().put("forward", forwardButtonPress);

        dp = new DPanel();
        scrollPane = new JScrollPane(dp);
        add(scrollPane, BorderLayout.CENTER);

        pack();
        //setResizable(false);
    }// </editor-fold>

    /**
     * This is used to map the left arrow key to the back button
     */
    private Action backButtonPress = new AbstractAction()
    {
        public void actionPerformed(ActionEvent e)
        { backwardButtonActionPerformed(e);}
    };

    /**
     * Move back by one offset
     * @param evt Event
     */
    private void backwardButtonActionPerformed(ActionEvent evt) {
        if(transform == null) return;
        transform.back();
        updateImage();
    }

    /**
     * This is used to map the right arrow key to the forward button
     */
    private Action forwardButtonPress = new AbstractAction()
    {
        public void actionPerformed(ActionEvent e)
        { forwardButtonActionPerformed(e);}
    };

    /**
     * Move forward by one offset
     * @param evt Event
     */
    private void forwardButtonActionPerformed(ActionEvent evt) {
        if(bi == null) return;
        transform.forward();
        updateImage();
    }

    /**
     * Save the current image
     * @param evt Event
     */
    private void saveButtonActionPerformed(ActionEvent evt)
    {
        File sfile = null;
        JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
           "Images", "jpg", "gif", "png", "bmp");
        fileChooser.setFileFilter(filter);
        fileChooser.setSelectedFile(new File("solved.bmp"));
        int rVal = fileChooser.showSaveDialog(this);
        System.setProperty("user.dir", fileChooser.getCurrentDirectory().getAbsolutePath());
        if(rVal == JFileChooser.APPROVE_OPTION)
        {
            sfile = fileChooser.getSelectedFile();
            try
            {
                BufferedImage bbx = transform.getImage();
                int rns = sfile.getName().lastIndexOf(".")+1;
                if(rns==0)
                   ImageIO.write(bbx, "bmp", sfile);
                else
                   ImageIO.write(bbx, sfile.getName().substring(rns), sfile);
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(this, "Failed to write file: "+e.toString());
            }
        }
    }

    /**
     * Update the text description and repaint the image
     */
    private void updateImage()
    {
        if(transform.getImage().getWidth()!=w || transform.getImage().getHeight()!=h)
            newImage();
        nowShowing.setText(transform.getText());
        dp.setImage(transform.getImage());
        repaint();
    }

    /**
     * Show the image and make sure the form looks right
     */
    private void newImage()
    {
        nowShowing.setText(transform.getText());
        dp.setImage(transform.getImage());
        w = transform.getImage().getWidth();
        h = transform.getImage().getHeight();
        dp.setSize(transform.getImage().getWidth(),transform.getImage().getHeight());
        dp.setPreferredSize(new Dimension(transform.getImage().getWidth(),transform.getImage().getHeight()));
        this.setMaximumSize(getToolkit().getScreenSize());
        pack();
        scrollPane.revalidate();
        repaint();
    }

}
