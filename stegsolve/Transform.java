/*
 * Transform.java
 */

/*
 * transforms
 * 0 - none
 * 1 - inversion
 * 2-9 - alpha planes
 * 10-17 - r planes
 * 18-25 - g planes
 * 26-33 - b planes
 * 34 full alpha
 * 35 full red
 * 36 full green
 * 37 full blue
 * 38 random color1
 * 39 random color2
 * 40 random color3
 * 41 gray bits
 */

package stegsolve;

import java.awt.image.*;
import java.util.Random;

/**
 * A transform represents a change to the image such as a bit plane
 * or recolouring etc
 * @author Caesum
 */
public class Transform {

    /**
     * Original image
     */
    private BufferedImage originalImage;
    /**
     * Transformed image
     */
    private BufferedImage transform;
    /**
     * The number of this transformation
     */
    private int transNum;
    /**
     * The number of transformations supported
     */
    private static final int MAXTRANS = 41;

    /**
     * Create a new transformation
     * @param bi the image
     */
    Transform(BufferedImage bi)
    {
        originalImage = bi;
        transform = originalImage;
        transNum=0;
    }

    /**
     * Makes an image from a bit plane
     * @param d bit to use
     */
    private void transfrombit(int d)
    {
        transform = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        for(int i=0;i<originalImage.getWidth();i++)
            for(int j=0;j<originalImage.getHeight();j++)
            {
                int col=0;
                int fcol = originalImage.getRGB(i,j);
                if(((fcol>>>d)&1)>0)
                   col=0xffffff;
                transform.setRGB(i, j, col);
             }
    }

    /**
     * Makes an image given a mask of bit planes
     * @param mask bit mask to use
     */
    private void transmask(int mask)
    {
        transform = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        for(int i=0;i<originalImage.getWidth();i++)
            for(int j=0;j<originalImage.getHeight();j++)
            {
                int col=0;
                int fcol = originalImage.getRGB(i,j);
                col=fcol&mask;
                if(col>0xffffff||col<0)
                    col=col>>>8;
                transform.setRGB(i, j, col);
             }
    }

    /**
     * Inverts the colours of the image
     */
    private void inversion()
    {
        transform = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        for(int i=0;i<originalImage.getWidth();i++)
            for(int j=0;j<originalImage.getHeight();j++)
            {
                int col=0;
                int fcol = originalImage.getRGB(i,j);
                col=fcol^0xffffff;
                transform.setRGB(i, j, col);
             }
    }
    
    /**
     * Highlights just the pixels for which r=g=b
     */
    private void graybits()
    {
        transform = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        for(int i=0;i<originalImage.getWidth();i++)
            for(int j=0;j<originalImage.getHeight();j++)
            {
                int col=0;
                int fcol = originalImage.getRGB(i,j);
                if((fcol&0xff)==((fcol&0xff00)>>8)&&(fcol&0xff)==((fcol&0xff0000)>>16))
                col=0xffffff;
                transform.setRGB(i, j, col);
             }
    }

    /**
     * Randomises the colours of the image for a truecolour image
     */
    private void random_colormap()
    {
        // straight random mapping
        Random rnd = new Random();
        int bm = rnd.nextInt(256),ba = rnd.nextInt(256),bx = rnd.nextInt(256);
        int gm = rnd.nextInt(256),ga = rnd.nextInt(256),gx = rnd.nextInt(256);
        int rm = rnd.nextInt(256),ra = rnd.nextInt(256),rx = rnd.nextInt(256);
        transform = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        for(int i=0;i<originalImage.getWidth();i++)
            for(int j=0;j<originalImage.getHeight();j++)
            {
                int col=0;
                int fcol = originalImage.getRGB(i,j);
                int b = (fcol & 0xff) * bm;
                b = ((b*bm)^bx)+ba;
                int g = ((fcol & 0xff00)>>8) * gm;
                g = ((g*gm)^gx)+ga;
                int r = ((fcol & 0xff0000)>>16) * rm;
                r = ((r*rm)^rx)+ra;
                col = (r<<16)+(g<<8)+b+(fcol&0xff000000);
                transform.setRGB(i, j, col);
             }
    }
    
    /**
     * Randomises the colours of the image for an indexed palette image
     */
    private void random_indexmap()
    {
        // this would be an indexed palette - this is separate
        // because two indexes can have the same color value
        // so the mapping should be done on the palette and not
        // on the image itself
        byte [] r, g, b;
        int bits, size;
        int [] gcs;
        Random rnd = new Random();
        IndexColorModel cm = (IndexColorModel)originalImage.getColorModel();
        gcs = cm.getComponentSize();
        bits = gcs[0];
        if(gcs[1]>bits) bits=gcs[1];
        if(gcs[2]>bits) bits=gcs[2];
        size = cm.getMapSize();
        r = new byte[size];
        g = new byte[size];
        b = new byte[size];
        cm.getReds(r);
        cm.getGreens(g);
        cm.getBlues(b);
        for(int i=0;i<size;i++)
        {
            r[i]=(byte)rnd.nextInt(256);
            g[i]=(byte)rnd.nextInt(256);
            b[i]=(byte)rnd.nextInt(256);
        }
        transform = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_BYTE_INDEXED, new IndexColorModel(bits, size, r, g, b));
        transform.setData(originalImage.getRaster());
    }

    /**
     * Randomises the colours of the image
     * depending on the type of image
     */
    private void randommap()
    {
        ColorModel cm = originalImage.getColorModel();
        if(cm instanceof ComponentColorModel)
            random_colormap();
        else if(cm instanceof IndexColorModel)
            random_indexmap();
        else if(cm instanceof PackedColorModel)
            random_colormap();
    }

    /**
     * Calculates the current image transformation
     */
    private void calcTrans()
    {
        switch(transNum)
        {
            case 0:
              transform = originalImage;
              return;
            case 1:
                inversion();
                return;
            case 2:
                transfrombit(31);
                return;
            case 3:
                transfrombit(30);
                return;
            case 4:
                transfrombit(29);
                return;
            case 5:
                transfrombit(28);
                return;
            case 6:
                transfrombit(27);
                return;
            case 7:
                transfrombit(26);
                return;
            case 8:
                transfrombit(25);
                return;
            case 9:
                transfrombit(24);
                return;
            case 10:
                transfrombit(23);
                return;
            case 11:
                transfrombit(22);
                return;
            case 12:
                transfrombit(21);
                return;
            case 13:
                transfrombit(20);
                return;
            case 14:
                transfrombit(19);
                return;
            case 15:
                transfrombit(18);
                return;
            case 16:
                transfrombit(17);
                return;
            case 17:
                transfrombit(16);
                return;
            case 18:
                transfrombit(15);
                return;
            case 19:
                transfrombit(14);
                return;
            case 20:
                transfrombit(13);
                return;
            case 21:
                transfrombit(12);
                return;
            case 22:
                transfrombit(11);
                return;
            case 23:
                transfrombit(10);
                return;
            case 24:
                transfrombit(9);
                return;
            case 25:
                transfrombit(8);
                return;
            case 26:
                transfrombit(7);
                return;
            case 27:
                transfrombit(6);
                return;
            case 28:
                transfrombit(5);
                return;
            case 29:
                transfrombit(4);
                return;
            case 30:
                transfrombit(3);
                return;
            case 31:
                transfrombit(2);
                return;
            case 32:
                transfrombit(1);
                return;
            case 33:
                transfrombit(0);
                return;
            case 34:
                transmask(0xff000000);
                return;
            case 35:
                transmask(0x00ff0000);
                return;
            case 36:
                transmask(0x0000ff00);
                return;
            case 37:
                transmask(0x000000ff);
                return;
            case 38:
                randommap();
                return;
            case 39:
                randommap();
                return;
            case 40:
                randommap();
                return;
            case 41:
                graybits();
                return;
            default:
               transform = originalImage;
               return;
        }
    }

    /**
     * Select the previous transformation
     */
    public void back()
    {
        transNum--;
        if(transNum<0) transNum=MAXTRANS;
        calcTrans();
    }

    /**
     * Select the next transformation
     */
    public void forward()
    {
        transNum++;
        if(transNum>MAXTRANS) transNum=0;
        calcTrans();
    }

    /**
     * Return a textual description of the transformation
     * @return text description for transformation
     */
    public String getText()
    {
        switch(transNum)
        {
            case 0:
              return "Normal Image";
            case 1:
              return "Colour Inversion (Xor)";
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
              return "Alpha plane " + (9 - transNum);
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
              return "Red plane " + (17 - transNum);
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
              return "Green plane " + (25 - transNum);
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
            case 31:
            case 32:
            case 33:
              return "Blue plane " + (33 - transNum);
            case 34:
              return "Full alpha";
            case 35:
              return "Full red";
            case 36:
              return "Full green";
            case 37:
              return "Full blue";
            case 38:
              return "Random colour map 1";
            case 39:
              return "Random colour map 2";
            case 40:
              return "Random colour map 3";
            case 41:
              return "Gray bits";
            default:
              return "";
        }
    }

    /**
     * Get the transformed image
     * @return transformed image
     */
    public BufferedImage getImage()
    {
         return transform;
    }
}
