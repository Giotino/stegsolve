/*
 * Extract.java
 *
 * Created on 20-Apr-2011, 12:36:17
 */

package stegsolve;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.*;
import javax.swing.*;
import java.io.*;

/**
 * The data extraction form
 * @author Caesum
 */
public class Extract extends javax.swing.JFrame {
    /**
     * The image data is being extracted from
     */
    private BufferedImage bi = null;
    /**
     * The bytes being extracted
     */
    private byte [] extract = null;
    /**
     * A mask of the bits selected for extraction
     */
    private int mask = 0;
    /**
     * Number of bits selected for extraction
     */
    private int maskbits = 0;
    /**
     * Boolean option - LSB first or MSB first?
     */
    private boolean lsbFirst = false;
    /**
     * Boolean option - row by row or column by column?
     */
    private boolean rowFirst = true;
    /**
     * Variable indicating the order of red, blue and green
     * components to examine
     */
    private int rgbOrder = 0;
    /**
     * Bit position in the current extract byte
     */
    private int extractBitPos=0;
    /**
     * Byte position in the current extract
     */
    private int extractBytePos=0;
    /**
     * Text that goes into the preview box
     */
    private StringBuilder prev = null;
    /**
     * A JFileChooser object for choosing where to save the data
     */
    private JFileChooser fileChooser;
    /**
     * Line separator for text files
     */
    private String ls = System.getProperty("line.separator");

    // TODO - inversion option
    // TODO - optional offset to start
    
    /** 
     * Creates new form Extract
     */
    public Extract(BufferedImage b) {
        bi = b; 
        initComponents();
    }

    /**
     * Retrieves the mask from the bits selected on the form
     */
    private void getMask()
    {
        mask = 0;
        maskbits = 0;
        if(ab7.isSelected()) { mask += 1<<31; maskbits++;}
        if(ab6.isSelected()) { mask += 1<<30; maskbits++;}
        if(ab5.isSelected()) { mask += 1<<29; maskbits++;}
        if(ab4.isSelected()) { mask += 1<<28; maskbits++;}
        if(ab3.isSelected()) { mask += 1<<27; maskbits++;}
        if(ab2.isSelected()) { mask += 1<<26; maskbits++;}
        if(ab1.isSelected()) { mask += 1<<25; maskbits++;}
        if(ab0.isSelected()) { mask += 1<<24; maskbits++;}
        if(rb7.isSelected()) { mask += 1<<23; maskbits++;}
        if(rb6.isSelected()) { mask += 1<<22; maskbits++;}
        if(rb5.isSelected()) { mask += 1<<21; maskbits++;}
        if(rb4.isSelected()) { mask += 1<<20; maskbits++;}
        if(rb3.isSelected()) { mask += 1<<19; maskbits++;}
        if(rb2.isSelected()) { mask += 1<<18; maskbits++;}
        if(rb1.isSelected()) { mask += 1<<17; maskbits++;}
        if(rb0.isSelected()) { mask += 1<<16; maskbits++;}
        if(gb7.isSelected()) { mask += 1<<15; maskbits++;}
        if(gb6.isSelected()) { mask += 1<<14; maskbits++;}
        if(gb5.isSelected()) { mask += 1<<13; maskbits++;}
        if(gb4.isSelected()) { mask += 1<<12; maskbits++;}
        if(gb3.isSelected()) { mask += 1<<11; maskbits++;}
        if(gb2.isSelected()) { mask += 1<<10; maskbits++;}
        if(gb1.isSelected()) { mask += 1<<9; maskbits++;}
        if(gb0.isSelected()) { mask += 1<<8; maskbits++;}
        if(bb7.isSelected()) { mask += 1<<7; maskbits++;}
        if(bb6.isSelected()) { mask += 1<<6; maskbits++;}
        if(bb5.isSelected()) { mask += 1<<5; maskbits++;}
        if(bb4.isSelected()) { mask += 1<<4; maskbits++;}
        if(bb3.isSelected()) { mask += 1<<3; maskbits++;}
        if(bb2.isSelected()) { mask += 1<<2; maskbits++;}
        if(bb1.isSelected()) { mask += 1<<1; maskbits++;}
        if(bb0.isSelected()) { mask += 1; maskbits++;}
    }

    /**
     * Retrieve the ordering options from the form
     */
    private void getBitOrderOptions()
    {
        if(byRowButton.isSelected()) rowFirst = true;
        else rowFirst = false;
        if(LSBButton.isSelected()) lsbFirst = true;
        else lsbFirst = false;
        if(RGBButton.isSelected()) rgbOrder = 1;
        else if (RBGButton.isSelected()) rgbOrder = 2;
        else if (GRBButton.isSelected()) rgbOrder = 3;
        else if (GBRButton.isSelected()) rgbOrder = 4;
        else if (BRGButton.isSelected()) rgbOrder = 5;
        else rgbOrder = 6;
    }
    
    /**
     * Adds another bit to the extract
     * @param num Non-zero if adding a 1-bit
     */
    private void addBit(int num)
    {
        if(num!=0)
        {
           extract[extractBytePos]+=extractBitPos;
        }
        extractBitPos>>=1;
        if(extractBitPos>=1)
            return;
        extractBitPos=128;
        extractBytePos++;
        if(extractBytePos<extract.length)
            extract[extractBytePos]=0;
    }

    /**
     * Examine 8 bits and check them against the mask to
     * see if any should be extracted
     * @param nextByte The byte to be examined
     * @param bitMask The bitmask to be applied
     */
    private void extract8Bits(int nextByte, int bitMask)
    {
        for(int i=0;i<8;i++)
        {
            if((mask&bitMask)!=0)
            {
                //System.out.println("call "+ mask+" "+bitMask+" "+nextByte);
                addBit(nextByte & bitMask);
            }
            if(lsbFirst)
               bitMask<<=1;
            else
               bitMask>>>=1;
        }
    }

    /**
     * Extract bits from the given byte taking account of
     * the options selected
     * @param nextByte the byte to extract bits from
     */
    private void extractBits(int nextByte)
    {
        if(lsbFirst)
        {
            extract8Bits(nextByte,1<<24);
            switch(rgbOrder)
            {
                case 1: //rgb
                    extract8Bits(nextByte,1<<16);
                    extract8Bits(nextByte,1<<8);
                    extract8Bits(nextByte,1);
                    break;
                case 2: //rbg
                    extract8Bits(nextByte,1<<16);
                    extract8Bits(nextByte,1);
                    extract8Bits(nextByte,1<<8);
                    break;
                case 3: //grb
                    extract8Bits(nextByte,1<<8);
                    extract8Bits(nextByte,1<<16);
                    extract8Bits(nextByte,1);
                    break;
                case 4: //gbr
                    extract8Bits(nextByte,1<<8);
                    extract8Bits(nextByte,1);
                    extract8Bits(nextByte,1<<16);
                    break;
                case 5: //brg
                    extract8Bits(nextByte,1);
                    extract8Bits(nextByte,1<<16);
                    extract8Bits(nextByte,1<<8);
                    break;
                case 6: //bgr
                    extract8Bits(nextByte,1);
                    extract8Bits(nextByte,1<<8);
                    extract8Bits(nextByte,1<<16);
                    break;
            }
        }
        else
        {
            extract8Bits(nextByte,1<<31);
            switch(rgbOrder)
            {
                case 1: //rgb
                    extract8Bits(nextByte,1<<23);
                    extract8Bits(nextByte,1<<15);
                    extract8Bits(nextByte,1<<7);
                    break;
                case 2: //rbg
                    extract8Bits(nextByte,1<<23);
                    extract8Bits(nextByte,1<<7);
                    extract8Bits(nextByte,1<<15);
                    break;
                case 3: //grb
                    extract8Bits(nextByte,1<<15);
                    extract8Bits(nextByte,1<<23);
                    extract8Bits(nextByte,1<<7);
                    break;
                case 4: //gbr
                    extract8Bits(nextByte,1<<15);
                    extract8Bits(nextByte,1<<7);
                    extract8Bits(nextByte,1<<23);
                    break;
                case 5: //brg
                    extract8Bits(nextByte,1<<7);
                    extract8Bits(nextByte,1<<23);
                    extract8Bits(nextByte,1<<15);
                    break;
                case 6: //bgr
                    extract8Bits(nextByte,1<<7);
                    extract8Bits(nextByte,1<<15);
                    extract8Bits(nextByte,1<<23);
                    break;
            }
        }
    }

    /**
     * Generates the extract from the selected options
     */
    private void generateExtract()
    {
        getMask();
        getBitOrderOptions();
        int len = bi.getHeight() * bi.getWidth();
        len = len * maskbits; // number of bits to be extracted
        len = (len +7)/8; // bytes to be extracted
        extract = new byte[len];
        extractBitPos = 128;
        extractBytePos = 0;
        //System.out.println(bi.getHeight()+" "+bi.getWidth()+" "+len+" "+mask);
        if(rowFirst)
        {
           for(int j=0;j<bi.getHeight();j++)
              for(int i=0;i<bi.getWidth();i++)
              {
                  //System.out.println(i+" "+j+" "+extractBytePos);
                  extractBits(bi.getRGB(i, j));
              }
        }
        else
        {
           for(int i=0;i<bi.getWidth();i++)
              for(int j=0;j<bi.getHeight();j++)
                 extractBits(bi.getRGB(i, j));
        }
    }

    /**
     * Generates the preview from the selected options
     * and given the extract has already been generated
     */
    private void generatePreview()
    {
        boolean hexDump = hdInclude.isSelected();
        prev = new StringBuilder();
        for(int i=0;i<extract.length;i+=16)
        {
            if(hexDump)
            {
              for(int j=0;j<16&&i+j<extract.length;j++)
              {
                prev.append(m2(Integer.toHexString(((int)extract[i+j])&0xff)));
                if(j==7)
                  prev.append(' ');
              }
              prev.append("  ");
            }
            for(int j=0;j<16&&i+j<extract.length;j++)
            {
               char c = (char)extract[i+j];
               if(c>=32 && c<=128)
                   prev.append(c);
               else
                   prev.append('.');
               if(j==7) prev.append(' ');
            }
            prev.append(ls);
        }
        jPreview.setText(prev.toString());
    }

    /**
     * Save the preview to a text file.
     */
    private void savePreview() // to file
    {
        fileChooser = new JFileChooser(System.getProperty("user.dir"));
        int rVal = fileChooser.showSaveDialog(this);
        System.setProperty("user.dir", fileChooser.getCurrentDirectory().getAbsolutePath());
        File sfile = null;
        if(rVal == JFileChooser.APPROVE_OPTION)
        {
            sfile = fileChooser.getSelectedFile();
            try
            {
                FileWriter fw = new FileWriter(sfile);
                fw.write(jPreview.getText());
                fw.close();
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(this, "Failed to write file: "+e.toString());
            }
        }
    }

    /**
     * Save the extract to a binary file
     */
    private void saveExtract() // bin to file
    {
        fileChooser = new JFileChooser(System.getProperty("user.dir"));
        int rVal = fileChooser.showSaveDialog(this);
        System.setProperty("user.dir", fileChooser.getCurrentDirectory().getAbsolutePath());
        File sfile = null;
        if(rVal == JFileChooser.APPROVE_OPTION)
        {
            sfile = fileChooser.getSelectedFile();
            try
            {
                FileOutputStream fw = new FileOutputStream(sfile);
                fw.write(extract);
                fw.close();
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(this, "Failed to write file: "+e.toString());
            }
        }
    }

    /**
     * Ensures a hex string is 2 bytes long, adding a leading zero if it is not
     * @param hx hex string
     * @return
     */
    private String m2(String hx)
    {
        if(hx.length()<2)
            return "0" + hx;
        return hx;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        byGroup = new javax.swing.ButtonGroup();
        bitGroup = new javax.swing.ButtonGroup();
        planeGroup = new javax.swing.ButtonGroup();
        optionsPanel = new javax.swing.JPanel();
        lhSettingsPanel = new javax.swing.JPanel();
        bitPlanesPanel = new javax.swing.JPanel();
        alphaBitPanel = new javax.swing.JPanel();
        alphaLabel = new javax.swing.JLabel();
        aba = new javax.swing.JCheckBox();
        ab7 = new javax.swing.JCheckBox();
        ab6 = new javax.swing.JCheckBox();
        ab5 = new javax.swing.JCheckBox();
        ab4 = new javax.swing.JCheckBox();
        ab3 = new javax.swing.JCheckBox();
        ab2 = new javax.swing.JCheckBox();
        ab1 = new javax.swing.JCheckBox();
        ab0 = new javax.swing.JCheckBox();
        redBitPanel = new javax.swing.JPanel();
        redLabel = new javax.swing.JLabel();
        rba = new javax.swing.JCheckBox();
        rb7 = new javax.swing.JCheckBox();
        rb6 = new javax.swing.JCheckBox();
        rb5 = new javax.swing.JCheckBox();
        rb4 = new javax.swing.JCheckBox();
        rb3 = new javax.swing.JCheckBox();
        rb2 = new javax.swing.JCheckBox();
        rb1 = new javax.swing.JCheckBox();
        rb0 = new javax.swing.JCheckBox();
        greenBitPanel = new javax.swing.JPanel();
        greenLabel = new javax.swing.JLabel();
        gba = new javax.swing.JCheckBox();
        gb7 = new javax.swing.JCheckBox();
        gb6 = new javax.swing.JCheckBox();
        gb5 = new javax.swing.JCheckBox();
        gb4 = new javax.swing.JCheckBox();
        gb3 = new javax.swing.JCheckBox();
        gb2 = new javax.swing.JCheckBox();
        gb1 = new javax.swing.JCheckBox();
        gb0 = new javax.swing.JCheckBox();
        blueBitPanel = new javax.swing.JPanel();
        blueLabel = new javax.swing.JLabel();
        bba = new javax.swing.JCheckBox();
        bb7 = new javax.swing.JCheckBox();
        bb6 = new javax.swing.JCheckBox();
        bb5 = new javax.swing.JCheckBox();
        bb4 = new javax.swing.JCheckBox();
        bb3 = new javax.swing.JCheckBox();
        bb2 = new javax.swing.JCheckBox();
        bb1 = new javax.swing.JCheckBox();
        bb0 = new javax.swing.JCheckBox();
        prevSettingsPanel = new javax.swing.JPanel();
        hdLabel = new javax.swing.JLabel();
        hdInclude = new javax.swing.JCheckBox();
        rhSettingsPanel = new javax.swing.JPanel();
        orderSettingsPanel = new javax.swing.JPanel();
        extractByPanel = new javax.swing.JPanel();
        extractByLabel = new javax.swing.JLabel();
        byRowButton = new javax.swing.JRadioButton();
        byColumnButton = new javax.swing.JRadioButton();
        bitOrderPanel = new javax.swing.JPanel();
        bitOrderLabel = new javax.swing.JLabel();
        MSBButton = new javax.swing.JRadioButton();
        LSBButton = new javax.swing.JRadioButton();
        bitPlaneOrderPanel = new javax.swing.JPanel();
        bitPlaneOrderLabel = new javax.swing.JLabel();
        RGBButton = new javax.swing.JRadioButton();
        RBGButton = new javax.swing.JRadioButton();
        GBRButton = new javax.swing.JRadioButton();
        GRBButton = new javax.swing.JRadioButton();
        BRGButton = new javax.swing.JRadioButton();
        BGRButton = new javax.swing.JRadioButton();
        exPreviewPanel = new javax.swing.JPanel();
        exPreviewLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPreview = new javax.swing.JTextArea();
        buttonsPanel = new javax.swing.JPanel();
        previewButton = new javax.swing.JButton();
        saveTextButton = new javax.swing.JButton();
        saveBinButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(790, 560));
        getContentPane().setLayout(new java.awt.BorderLayout(5, 5));

        optionsPanel.setMinimumSize(new java.awt.Dimension(720, 280));
        optionsPanel.setPreferredSize(new java.awt.Dimension(720, 280));
        optionsPanel.setLayout(new java.awt.BorderLayout());

        lhSettingsPanel.setMinimumSize(new java.awt.Dimension(360, 280));
        lhSettingsPanel.setPreferredSize(new java.awt.Dimension(360, 280));

        int bitPlanesWidth = 480;

        bitPlanesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Bit Planes"));
        bitPlanesPanel.setMinimumSize(new java.awt.Dimension(bitPlanesWidth, 200));
        bitPlanesPanel.setPreferredSize(new java.awt.Dimension(bitPlanesWidth, 200));

        alphaBitPanel.setName("alphaBitPanel"); // NOI18N
        alphaBitPanel.setPreferredSize(new java.awt.Dimension(bitPlanesWidth-10, 34));

        alphaLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        alphaLabel.setText("Alpha");
        alphaLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        alphaLabel.setMaximumSize(new java.awt.Dimension(40, 14));
        alphaLabel.setMinimumSize(new java.awt.Dimension(40, 14));
        alphaLabel.setPreferredSize(new java.awt.Dimension(40, 14));
        alphaBitPanel.add(alphaLabel);

        aba.setText("all");
        alphaBitPanel.add(aba);

        checkAllListener(aba, ab7, ab6, ab5, ab4, ab3, ab2, ab1, ab0);

        ab7.setText("7");
        alphaBitPanel.add(ab7);

        ab6.setText("6");
        alphaBitPanel.add(ab6);

        ab5.setText("5");
        alphaBitPanel.add(ab5);

        ab4.setText("4");
        alphaBitPanel.add(ab4);

        ab3.setText("3");
        alphaBitPanel.add(ab3);

        ab2.setText("2");
        alphaBitPanel.add(ab2);

        ab1.setText("1");
        alphaBitPanel.add(ab1);

        ab0.setText("0");
        alphaBitPanel.add(ab0);

        bitPlanesPanel.add(alphaBitPanel);
        alphaBitPanel.getAccessibleContext().setAccessibleName("alphaBitPanel");

        redBitPanel.setPreferredSize(new java.awt.Dimension(bitPlanesWidth-10, 34));

        redLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        redLabel.setText("Red");
        redLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        redLabel.setMaximumSize(new java.awt.Dimension(40, 14));
        redLabel.setMinimumSize(new java.awt.Dimension(40, 14));
        redLabel.setPreferredSize(new java.awt.Dimension(40, 14));
        redBitPanel.add(redLabel);

        rba.setText("all");
        redBitPanel.add(rba);

        checkAllListener(rba, rb7, rb6, rb5, rb4, rb3, rb2, rb1, rb0);

        rb7.setText("7");
        redBitPanel.add(rb7);

        rb6.setText("6");
        redBitPanel.add(rb6);

        rb5.setText("5");
        redBitPanel.add(rb5);

        rb4.setText("4");
        redBitPanel.add(rb4);

        rb3.setText("3");
        redBitPanel.add(rb3);

        rb2.setText("2");
        redBitPanel.add(rb2);

        rb1.setText("1");
        redBitPanel.add(rb1);

        rb0.setText("0");
        redBitPanel.add(rb0);

        bitPlanesPanel.add(redBitPanel);

        greenBitPanel.setPreferredSize(new java.awt.Dimension(bitPlanesWidth-10, 34));

        greenLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        greenLabel.setText("Green");
        greenLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        greenLabel.setMaximumSize(new java.awt.Dimension(40, 14));
        greenLabel.setMinimumSize(new java.awt.Dimension(40, 14));
        greenLabel.setPreferredSize(new java.awt.Dimension(40, 14));
        greenBitPanel.add(greenLabel);

        gba.setText("all");
        greenBitPanel.add(gba);

        checkAllListener(gba, gb7, gb6, gb5, gb4, gb3, gb2, gb1, gb0);

        gb7.setText("7");
        greenBitPanel.add(gb7);

        gb6.setText("6");
        greenBitPanel.add(gb6);

        gb5.setText("5");
        greenBitPanel.add(gb5);

        gb4.setText("4");
        greenBitPanel.add(gb4);

        gb3.setText("3");
        greenBitPanel.add(gb3);

        gb2.setText("2");
        greenBitPanel.add(gb2);

        gb1.setText("1");
        greenBitPanel.add(gb1);

        gb0.setText("0");
        greenBitPanel.add(gb0);

        bitPlanesPanel.add(greenBitPanel);

        blueBitPanel.setPreferredSize(new java.awt.Dimension(bitPlanesWidth-10, 34));

        blueLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        blueLabel.setText("Blue");
        blueLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        blueLabel.setMaximumSize(new java.awt.Dimension(40, 14));
        blueLabel.setMinimumSize(new java.awt.Dimension(40, 14));
        blueLabel.setPreferredSize(new java.awt.Dimension(40, 14));
        blueBitPanel.add(blueLabel);

        bba.setText("all");
        blueBitPanel.add(bba);

        checkAllListener(bba, bb7, bb6, bb5, bb4, bb3, bb2, bb1, bb0);

        bb7.setText("7");
        blueBitPanel.add(bb7);

        bb6.setText("6");
        blueBitPanel.add(bb6);

        bb5.setText("5");
        blueBitPanel.add(bb5);

        bb4.setText("4");
        blueBitPanel.add(bb4);

        bb3.setText("3");
        blueBitPanel.add(bb3);

        bb2.setText("2");
        blueBitPanel.add(bb2);

        bb1.setText("1");
        blueBitPanel.add(bb1);

        bb0.setText("0");
        blueBitPanel.add(bb0);

        bitPlanesPanel.add(blueBitPanel);

        lhSettingsPanel.add(bitPlanesPanel);

        prevSettingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Preview Settings"));
        prevSettingsPanel.setMinimumSize(new java.awt.Dimension(360, 50));
        prevSettingsPanel.setPreferredSize(new java.awt.Dimension(360, 50));
        prevSettingsPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 0));

        hdLabel.setText("Include Hex Dump In Preview");
        prevSettingsPanel.add(hdLabel);

        hdInclude.setSelected(true);
        prevSettingsPanel.add(hdInclude);

        lhSettingsPanel.add(prevSettingsPanel);

        optionsPanel.add(lhSettingsPanel, java.awt.BorderLayout.CENTER);

        rhSettingsPanel.setMinimumSize(new java.awt.Dimension(300, 280));
        rhSettingsPanel.setPreferredSize(new java.awt.Dimension(300, 280));
        rhSettingsPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 5));

        orderSettingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Order settings"));
        orderSettingsPanel.setPreferredSize(new java.awt.Dimension(280, 260));
        orderSettingsPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        extractByLabel.setText("Extract By");
        extractByPanel.add(extractByLabel);

        byGroup.add(byRowButton);
        byRowButton.setSelected(true);
        byRowButton.setText("Row");
        extractByPanel.add(byRowButton);

        byGroup.add(byColumnButton);
        byColumnButton.setText("Column");
        extractByPanel.add(byColumnButton);

        orderSettingsPanel.add(extractByPanel);

        bitOrderPanel.setPreferredSize(new java.awt.Dimension(250, 41));
        bitOrderPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        bitOrderLabel.setText("Bit Order");
        bitOrderPanel.add(bitOrderLabel);

        bitGroup.add(MSBButton);
        MSBButton.setSelected(true);
        MSBButton.setText("MSB First");
        bitOrderPanel.add(MSBButton);

        bitGroup.add(LSBButton);
        LSBButton.setText("LSB First");
        bitOrderPanel.add(LSBButton);

        orderSettingsPanel.add(bitOrderPanel);

        bitPlaneOrderPanel.setPreferredSize(new java.awt.Dimension(250, 130));

        bitPlaneOrderLabel.setText("Bit Plane Order");

        planeGroup.add(RGBButton);
        RGBButton.setSelected(true);
        RGBButton.setText("RGB");

        planeGroup.add(RBGButton);
        RBGButton.setText("RBG");

        planeGroup.add(GBRButton);
        GBRButton.setText("GBR");

        planeGroup.add(GRBButton);
        GRBButton.setText("GRB");

        planeGroup.add(BRGButton);
        BRGButton.setText("BRG");

        planeGroup.add(BGRButton);
        BGRButton.setText("BGR");

        javax.swing.GroupLayout bitPlaneOrderPanelLayout = new javax.swing.GroupLayout(bitPlaneOrderPanel);
        bitPlaneOrderPanel.setLayout(bitPlaneOrderPanelLayout);
        bitPlaneOrderPanelLayout.setHorizontalGroup(
            bitPlaneOrderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bitPlaneOrderPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(bitPlaneOrderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bitPlaneOrderLabel)
                    .addGroup(bitPlaneOrderPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(bitPlaneOrderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(bitPlaneOrderPanelLayout.createSequentialGroup()
                                .addComponent(RBGButton)
                                .addGap(18, 18, 18)
                                .addComponent(BRGButton))
                            .addGroup(bitPlaneOrderPanelLayout.createSequentialGroup()
                                .addComponent(RGBButton)
                                .addGap(18, 18, 18)
                                .addComponent(GRBButton))
                            .addGroup(bitPlaneOrderPanelLayout.createSequentialGroup()
                                .addComponent(GBRButton)
                                .addGap(18, 18, 18)
                                .addComponent(BGRButton)))))
                .addContainerGap(72, Short.MAX_VALUE))
        );
        bitPlaneOrderPanelLayout.setVerticalGroup(
            bitPlaneOrderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bitPlaneOrderPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(bitPlaneOrderLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(bitPlaneOrderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(RGBButton)
                    .addComponent(GRBButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(bitPlaneOrderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(RBGButton)
                    .addComponent(BRGButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(bitPlaneOrderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(GBRButton)
                    .addComponent(BGRButton))
                .addContainerGap(13, Short.MAX_VALUE))
        );

        orderSettingsPanel.add(bitPlaneOrderPanel);

        rhSettingsPanel.add(orderSettingsPanel);

        optionsPanel.add(rhSettingsPanel, java.awt.BorderLayout.EAST);

        getContentPane().add(optionsPanel, java.awt.BorderLayout.CENTER);
        optionsPanel.getAccessibleContext().setAccessibleName("saveText");

        exPreviewPanel.setLayout(new java.awt.BorderLayout());

        exPreviewLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        exPreviewLabel.setText("Extract Preview");
        exPreviewLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        exPreviewLabel.setMinimumSize(new java.awt.Dimension(20, 14));
        exPreviewPanel.add(exPreviewLabel, java.awt.BorderLayout.CENTER);
        exPreviewLabel.getAccessibleContext().setAccessibleName("previewLabel");

        jPreview.setColumns(20);
        jPreview.setEditable(false);
        jPreview.setFont(new java.awt.Font("Courier New", 0, 14));
        jPreview.setRows(10);
        jPreview.setMargin(new java.awt.Insets(5, 5, 5, 5));
        jScrollPane1.setViewportView(jPreview);

        exPreviewPanel.add(jScrollPane1, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(exPreviewPanel, java.awt.BorderLayout.NORTH);

        previewButton.setText("Preview");
        previewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previewButtonActionPerformed(evt);
            }
        });
        buttonsPanel.add(previewButton);
        previewButton.getAccessibleContext().setAccessibleName("previewButton");

        saveTextButton.setText("Save Text");
        saveTextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveTextButtonActionPerformed(evt);
            }
        });
        buttonsPanel.add(saveTextButton);
        saveTextButton.getAccessibleContext().setAccessibleName("saveTextButton");

        saveBinButton.setText("Save Bin");
        saveBinButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveBinButtonActionPerformed(evt);
            }
        });
        buttonsPanel.add(saveBinButton);
        saveBinButton.getAccessibleContext().setAccessibleName("saveBinButton");

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        buttonsPanel.add(cancelButton);
        cancelButton.getAccessibleContext().setAccessibleName("cancelButton");

        getContentPane().add(buttonsPanel, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void checkAllListener(JCheckBox ca, JCheckBox c7, JCheckBox c6, JCheckBox c5, JCheckBox c4, JCheckBox c3, JCheckBox c2, JCheckBox c1, JCheckBox c0) {
        ca.addItemListener(e -> {
            c7.setSelected(ca.isSelected());
            c6.setSelected(ca.isSelected());
            c5.setSelected(ca.isSelected());
            c4.setSelected(ca.isSelected());
            c3.setSelected(ca.isSelected());
            c2.setSelected(ca.isSelected());
            c1.setSelected(ca.isSelected());
            c0.setSelected(ca.isSelected());
        });
    }

    /**
     * Generate the extract and generate the preview
     * @param evt Event
     */
    private void previewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previewButtonActionPerformed
        generateExtract();
        generatePreview();
    }//GEN-LAST:event_previewButtonActionPerformed

    /**
     * Generate the extract and the preview, and save it as text
     * @param evt Event
     */
    private void saveTextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveTextButtonActionPerformed
        generateExtract();
        generatePreview();
        savePreview();
    }//GEN-LAST:event_saveTextButtonActionPerformed

    /**
     * Generate the extract and save it as binary
     * @param evt Event
     */
    private void saveBinButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveBinButtonActionPerformed
        generateExtract();
        saveExtract();
    }//GEN-LAST:event_saveBinButtonActionPerformed

    /**
     * Close the form
     * @param evt Event
     */
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton BGRButton;
    private javax.swing.JRadioButton BRGButton;
    private javax.swing.JRadioButton GBRButton;
    private javax.swing.JRadioButton GRBButton;
    private javax.swing.JRadioButton LSBButton;
    private javax.swing.JRadioButton MSBButton;
    private javax.swing.JRadioButton RBGButton;
    private javax.swing.JRadioButton RGBButton;
    private javax.swing.JCheckBox ab0;
    private javax.swing.JCheckBox ab1;
    private javax.swing.JCheckBox ab2;
    private javax.swing.JCheckBox ab3;
    private javax.swing.JCheckBox ab4;
    private javax.swing.JCheckBox ab5;
    private javax.swing.JCheckBox ab6;
    private javax.swing.JCheckBox ab7;
    private javax.swing.JCheckBox aba;
    private javax.swing.JPanel alphaBitPanel;
    private javax.swing.JLabel alphaLabel;
    private javax.swing.JCheckBox bb0;
    private javax.swing.JCheckBox bb1;
    private javax.swing.JCheckBox bb2;
    private javax.swing.JCheckBox bb3;
    private javax.swing.JCheckBox bb4;
    private javax.swing.JCheckBox bb5;
    private javax.swing.JCheckBox bb6;
    private javax.swing.JCheckBox bb7;
    private javax.swing.JCheckBox bba;
    private javax.swing.ButtonGroup bitGroup;
    private javax.swing.JLabel bitOrderLabel;
    private javax.swing.JPanel bitOrderPanel;
    private javax.swing.JLabel bitPlaneOrderLabel;
    private javax.swing.JPanel bitPlaneOrderPanel;
    private javax.swing.JPanel bitPlanesPanel;
    private javax.swing.JPanel blueBitPanel;
    private javax.swing.JLabel blueLabel;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JRadioButton byColumnButton;
    private javax.swing.ButtonGroup byGroup;
    private javax.swing.JRadioButton byRowButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel exPreviewLabel;
    private javax.swing.JPanel exPreviewPanel;
    private javax.swing.JLabel extractByLabel;
    private javax.swing.JPanel extractByPanel;
    private javax.swing.JCheckBox gb0;
    private javax.swing.JCheckBox gb1;
    private javax.swing.JCheckBox gb2;
    private javax.swing.JCheckBox gb3;
    private javax.swing.JCheckBox gb4;
    private javax.swing.JCheckBox gb5;
    private javax.swing.JCheckBox gb6;
    private javax.swing.JCheckBox gb7;
    private javax.swing.JCheckBox gba;
    private javax.swing.JPanel greenBitPanel;
    private javax.swing.JLabel greenLabel;
    private javax.swing.JCheckBox hdInclude;
    private javax.swing.JLabel hdLabel;
    private javax.swing.JTextArea jPreview;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel lhSettingsPanel;
    private javax.swing.JPanel optionsPanel;
    private javax.swing.JPanel orderSettingsPanel;
    private javax.swing.ButtonGroup planeGroup;
    private javax.swing.JPanel prevSettingsPanel;
    private javax.swing.JButton previewButton;
    private javax.swing.JCheckBox rb0;
    private javax.swing.JCheckBox rb1;
    private javax.swing.JCheckBox rb2;
    private javax.swing.JCheckBox rb3;
    private javax.swing.JCheckBox rb4;
    private javax.swing.JCheckBox rb5;
    private javax.swing.JCheckBox rb6;
    private javax.swing.JCheckBox rb7;
    private javax.swing.JCheckBox rba;
    private javax.swing.JPanel redBitPanel;
    private javax.swing.JLabel redLabel;
    private javax.swing.JPanel rhSettingsPanel;
    private javax.swing.JButton saveBinButton;
    private javax.swing.JButton saveTextButton;
    // End of variables declaration//GEN-END:variables

}
