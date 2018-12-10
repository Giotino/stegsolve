/*
 * StereoTransform.java
 */

package stegsolve;

import java.awt.image.*;

/**
 * Represents solving a stereogram
 * @author Caesum
 */
public class StereoTransform {

    /**
     * Original image
     */
    private BufferedImage originalImage;
    /**
     * Transformed image
     */
    private BufferedImage transform;
    /**
     * Offset of transformation
     */
    private int transNum;

    /**
     * Create a new transformation
     * @param bi Image
     */
    StereoTransform(BufferedImage bi)
    {
        originalImage = bi;
        transNum=0;
        calcTrans();
    }

    /**
     * Solve the stereogram given the offset
     */
    private void calcTrans()
    {
        transform = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        for(int i=0;i<originalImage.getWidth();i++)
            for(int j=0;j<originalImage.getHeight();j++)
            {
                int col=0;
                int fcol = originalImage.getRGB(i,j);
                col=fcol^(originalImage.getRGB((i+transNum)%originalImage.getWidth(), j)&0x00ffffff);
                transform.setRGB(i, j, col);
             }
    }

    /**
     * Reduce the offset and try to solve
     */
    public void back()
    {
        transNum--;
        if(transNum<0) transNum=originalImage.getWidth()-1;
        calcTrans();
    }

    /**
     * Increase the offset and try to solve
     */
    public void forward()
    {
        transNum++;
        if(transNum>=originalImage.getWidth()) transNum=0;
        calcTrans();
    }

    /**
     * Text description  for offset
     * @return string text description of offset
     */
    public String getText()
    {
        return "Offset: "+transNum;
    }

    /**
     * The transformed image
     * @return buffered image of the stereogram with transformation
     */
    public BufferedImage getImage()
    {
         return transform;
    }
}
