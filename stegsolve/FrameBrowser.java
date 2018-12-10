/*
 * FrameBrowser.java
 */

package stegsolve;

import javax.swing.*;
import java.awt.image.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import javax.imageio.*;
import javax.imageio.stream.*;
import javax.swing.filechooser.*;

// todo: jphide checker/ invisible secrets

/**
 * Frame Browser
 * @author Caesum
 */
public class FrameBrowser extends JFrame
{
    /**
     * Label with the text showing the frame number
     */
    private JLabel nowShowing;
    /**
     * Panel for the buttons
     */
    private JPanel buttonPanel;
    /**
     * Forward button
     */
    private JButton forwardButton;
    /**
     * Backward button
     */
    private JButton backwardButton;
    /**
     * Save button
     */
    private JButton saveButton;
    /**
     * Panel with image on it
     */
    private DPanel dp;
    /**
     * Scroll pane for the image
     */
    private JScrollPane scrollPane;

    /**
     * The image being viewed
     */
    private BufferedImage bi = null;
    /**
     * The individual frames of the image
     */
    private java.util.List<BufferedImage> frames = null;
    /**
     * Number of the current frame
     */
    private int fnum=0;
    /**
     * Number of frames
     */
    private int numframes=0;

    /**
     * Creates a new frame browser
     * @param b The image the view
     * @param f The file of the image
     */
    public FrameBrowser(BufferedImage b, File f)
    {
        BufferedImage bnext;
        bi = b;
        initComponents();
        fnum = 0;
        numframes = 0;
        frames = new ArrayList<BufferedImage>();
        try
        {
            ImageInputStream ii = ImageIO.createImageInputStream(f);
            if(ii==null) System.out.println("Couldn't create input stream");
            ImageReader rr = ImageIO.getImageReaders(ii).next();
            if(rr==null) System.out.println("No image reader");
            rr.setInput(ii);
            int fread = rr.getMinIndex();
            while(true)
            {
                bnext = rr.read(numframes+fread);
                if(bnext==null)
                    break;
                frames.add(bnext);
                numframes++;
            }
            System.out.println("total frames " + numframes);
        }
        catch (IOException e)
        {
            JOptionPane.showMessageDialog(this, "Failed to load file: " +e.toString());
        }
        catch(IndexOutOfBoundsException e)
        {
            // expected for reading too many frames
        }
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
     * Move back by one frame
     * @param evt Event
     */
    private void backwardButtonActionPerformed(ActionEvent evt) {
        fnum--;
        if(fnum<0)fnum=numframes-1;
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
     * Move forward by one frame
     * @param evt Event
     */
    private void forwardButtonActionPerformed(ActionEvent evt) {
        fnum++;
        if(fnum>=numframes)fnum=0;
        updateImage();
    }

    /**
     * Save the current frame
     * @param evt Event
     */
    private void saveButtonActionPerformed(ActionEvent evt)
    {
        File sfile = null;
        JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
           "Images", "jpg", "gif", "png", "bmp");
        fileChooser.setFileFilter(filter);
        fileChooser.setSelectedFile(new File("frame"+(fnum+1)+".bmp"));
        int rVal = fileChooser.showSaveDialog(this);
        System.setProperty("user.dir", fileChooser.getCurrentDirectory().getAbsolutePath());
        if(rVal == JFileChooser.APPROVE_OPTION)
        {
            sfile = fileChooser.getSelectedFile();
            try
            {
                BufferedImage bbx = frames.get(fnum);
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
        nowShowing.setText("Frame : " + (fnum+1) + " of " + numframes);
        if(numframes==0) return;
        dp.setImage(frames.get(fnum));
        repaint();
    }

    /**
     * Show the image and make sure the frame browser looks right
     */
    private void newImage()
    {
        nowShowing.setText("Frame : " + (fnum+1) + " of " + numframes);
        if(numframes==0) return;
        dp.setImage(frames.get(fnum));
        dp.setSize(bi.getWidth(),bi.getHeight());
        dp.setPreferredSize(new Dimension(bi.getWidth(),bi.getHeight()));
        this.setMaximumSize(getToolkit().getScreenSize());
        pack();
        scrollPane.revalidate();
        repaint();
    }

}
