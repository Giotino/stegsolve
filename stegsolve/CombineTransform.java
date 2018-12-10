/*
 * CombineTransform.java
 */

package stegsolve;

import java.awt.image.*;

/**
 * Represents combination of two images
 * @author Caesum
 */
public class CombineTransform {

    /**
     * Original images
     */
    private BufferedImage im1, im2;
    /**
     * Transformed image
     */
    private BufferedImage transform;
    /**
     * Number of transformation
     */
    private int transNum;
    private final int NUMTRANS = 13;

    /**
     * Create a new transformation
     * @param bi Image 1
     * @param bi2 Image 2
     */
    CombineTransform(BufferedImage bi, BufferedImage  bi2)
    {
        im1 = bi;
        im2 = bi2;
        transNum=0;
        calcTrans();
    }

    /**
     * interlaces the images
     */
    private void calcInterlace()
    {
        int width = im1.getWidth();
        if(im2.getWidth()<width) width = im2.getWidth();
        int height = im1.getHeight();
        if(im2.getHeight()<height) height = im2.getHeight();
        if(transNum==11)
        {
           transform = new BufferedImage(width, 2*height, BufferedImage.TYPE_INT_RGB);
           for(int i=0;i<width;i++)
              for(int j=0;j<height;j++)
              {
                 transform.setRGB(i, j*2, im1.getRGB(i,j));
                 transform.setRGB(i, j*2+1, im2.getRGB(i,j));
              }
        }
        else if(transNum == 12)
        {
           transform = new BufferedImage(2*width, height, BufferedImage.TYPE_INT_RGB);
           for(int i=0;i<width;i++)
              for(int j=0;j<height;j++)
              {
                 transform.setRGB(i*2, j, im1.getRGB(i,j));
                 transform.setRGB(i*2+1, j, im2.getRGB(i,j));
              }
        }
    }

    /**
     * Combine the images
     */
    private void calcTrans()
    {
        if(transNum==11||transNum==12)
        {
            calcInterlace();
            return;
        }
        int width = im1.getWidth();
        if(im2.getWidth()>width) width = im2.getWidth();
        int height = im1.getHeight();
        if(im2.getHeight()>height) height = im2.getHeight();
        transform = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for(int i=0;i<width;i++)
            for(int j=0;j<height;j++)
            {
                int col1=0;
                int col2=0;
                if(i<im1.getWidth()&&j<im1.getHeight())
                    col1 = im1.getRGB(i,j);
                if(i<im2.getWidth()&&j<im2.getHeight())
                    col2 = im2.getRGB(i,j);
                int col = comb(col1,col2);
                transform.setRGB(i, j, col);
             }
    }

    /**
     * Reduce the offset and try to solve
     */
    public void back()
    {
        transNum--;
        if(transNum<0) transNum=NUMTRANS-1;
        calcTrans();
    }

    /**
     * Increase the offset and try to solve
     */
    public void forward()
    {
        transNum++;
        if(transNum>=NUMTRANS) transNum=0;
        calcTrans();
    }

    /**
     * Combines two colours according to transform
     * @param c1 colour 1
     * @param c2 colour 2
     * @return combined colour
     */
    private int comb(int c1, int c2)
    {
        int r,g,b;
        switch(transNum)
        {
            case 0:
              return c1^c2;
            case 1:
              return c1|c2;
            case 2:
              return c1&c2;
            case 3:
              return c1+c2;
            case 4:
              r = ((c1&0xff0000)+(c2&0xff0000))&0xff0000;
              g = ((c1&0xff00)+(c2&0xff00))&0xff00;
              b = ((c1&0xff)+(c2&0xff))&0xff;
              return r|g|b;
            case 5:
              return c1-c2;
            case 6:
              r = ((c1&0xff0000)-(c2&0xff0000))&0xff0000;
              g = ((c1&0xff00)-(c2&0xff00))&0xff00;
              b = ((c1&0xff)-(c2&0xff))&0xff;
              return r|g|b;
            case 7:
              return c1*c2;
            case 8:
              r = ((((c1&0xff0000)>>16) * ((c2&0xff0000)>> 16))&0xff) << 16;
              g = ((((c1&0xff00)>>8) * ((c2&0xff00)>> 8))&0xff) << 8;
              b = ((c1&0xff) * (c2&0xff))&0xff;
              return r|g|b;
            case 9:
              if((c1&0xff0000)>(c2&0xff0000))
                  r=(c1&0xff0000);
              else r=(c2&0xff0000);
              if((c1&0xff00)>(c2&0xff00))
                  g=(c1&0xff00);
              else g=(c2&0xff00);
              if((c1&0xff)>(c2&0xff))
                  b=(c1&0xff);
              else b=(c2&0xff);
              return r|g|b;
            case 10:
              if((c1&0xff0000)<(c2&0xff0000))
                  r=(c1&0xff0000);
              else r=(c2&0xff0000);
              if((c1&0xff00)<(c2&0xff00))
                  g=(c1&0xff00);
              else g=(c2&0xff00);
              if((c1&0xff)<(c2&0xff))
                  b=(c1&0xff);
              else b=(c2&0xff);
              return r|g|b;
        }
        return 0;
    }

    /**
     * Text description  for combination
     * @return string text description of offset
     */
    public String getText()
    {
        switch(transNum)
        {
            case 0:
              return "XOR";
            case 1:
              return "OR";
            case 2:
              return "AND";
            case 3:
              return "ADD";
            case 4:
              return "ADD (R,G,B separate)";
            case 5:
              return "SUB";
            case 6:
              return "SUB (R,G,B separate)";
            case 7:
              return "MUL";
            case 8:
              return "MUL (R,G,B separate)";
            case 9:
              return "Lightest (R, G, B separate)";
            case 10:
              return "Darkest (R, G, B separate)";
            case 11:
              return "Horizontal Interlace";
            case 12:
              return "Vertical Interlace";
        }
        return "???";
    }

    /**
     * The combined image
     * @return buffered image of the combined image
     */
    public BufferedImage getImage()
    {
         return transform;
    }
}
