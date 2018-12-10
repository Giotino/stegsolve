/*
 * FileAnalysis.java
 *
 * Created on 27-Apr-2011, 11:14:48
 */

package stegsolve;

import java.io.*;
import java.util.zip.CRC32;

/**
 * File Analysis by examining the file format
 * @author Caesum
 */
public class FileAnalysis extends javax.swing.JFrame {

    /**
     * String containing the file analysis report
     */
    private StringBuilder rep;
    /**
     * The file in bytes
     */
    private byte [] f = null;

    /** Creates new form FileAnalysis */
    public FileAnalysis(File ifile) {
        initComponents();
        analyse_file(ifile);
    }

    /**
     * Reads the data from the file and compiles
     * the analysis report
     * @param ifile File to analyse
     */
    private void analyse_file(File ifile)
    {
        rep = new StringBuilder();
        rep.append("<html><center><b>");
        rep.append("File format report");
        rep.append("</b></center>");
        rep.append(("<br>File: "+ifile.getName()));
        try{
          FileInputStream fis = new FileInputStream(ifile);
          f = new byte[(int)ifile.length()];
          fis.read(f);
          rep.append(("<br>Read "+Integer.toHexString(f.length)+" bytes"));
          analyse();
        }
        catch(Exception e)
        {
           rep.append(("Error reading file: "+e.toString()));
        }
        rep.append("</html>");
        report.setText(rep.toString());
    }

    /**
     * Checks the header and chooses the correct subtype
     * for analysis
     */
    private void analyse()
    {
        // analyse f, write report to rep
        if(f.length<4)
        {
           rep.append("<br>file too short?");
           return;
        }
        if(f[0] == 'B' && f[1] == 'M')
           analyse_bmp();
        else if(f[0]==(byte)137 && f[1]==(byte)80 && f[2]==78 && f[3]==71)
           analyse_png();
        else if(f.length>=6 && f[0]=='G' && f[1]=='I' && f[2]=='F' && f[3]=='8' && (f[4]=='7' ||
                f[4]=='9') && f[5]=='a')
           analyse_gif();
        else if(f[0]==(byte)0xff && f[1]==(byte)0xd8)
            analyse_jpg();
        else rep.append("<br>File format analysis code not done yet!");
    }

    /**
     * Analysis particular to a jpeg/jpg image
     */
    private void analyse_jpg()
    {
        int cpos = 0;
        cpos = analyse_jpg_sections(cpos);
        if(cpos<f.length)
        {
            rep.append(("<br>Additional bytes at end of file = " +(f.length-cpos)));
            rep.append("<br>Dump of additional bytes:");
            fdump(cpos, f.length);
        }
    }

    /**
     * Analyses one particular jpeg section
     * @param pos Position in the file
     * @return End position in the file
     */
    private int analyse_jpg_sections(int pos)
    {
        if(f[pos]==(byte)0xff && f[pos+1]==(byte)0xd8)
        {
            rep.append("<br>Start of Image");
            pos+=2;
        }
        else if(f[pos] == (byte) 0xff && f[pos + 1] == (byte) 0xd9)
        {
            rep.append("<br><br>End of Image");
            pos+=2;
            return pos;
        }
        else if(f[pos] == (byte) 0xff && 
                (  (f[pos + 1] >= (byte) 0xc0 && f[pos + 1] <= (byte) 0xc3)
                || (f[pos + 1] >= (byte) 0xc5 && f[pos + 1] <= (byte) 0xc7)
                || (f[pos + 1] >= (byte) 0xc9 && f[pos + 1] <= (byte) 0xcb)
                || (f[pos + 1] >= (byte) 0xcd && f[pos + 1] <= (byte) 0xcf)
                || f[pos+1]==(byte)0xde))
        {
            if(f[pos+1]==(byte)0xc0)
              rep.append("<br><br>Start of frame : Baseline DCT");
            else if(f[pos + 1] == (byte) 0xc1)
              rep.append("<br><br>Start of frame : Extended sequential DCT");
            else if(f[pos + 1] == (byte) 0xc2)
              rep.append("<br><br>Start of frame : Progressive DCT");
            else if(f[pos + 1] == (byte) 0xc3)
              rep.append("<br><br>Start of frame : Lossless (sequential)");
            else if(f[pos + 1] == (byte) 0xc5)
              rep.append("<br><br>Start of frame : Differential sequential DCT");
            else if(f[pos + 1] == (byte) 0xc6)
              rep.append("<br><br>Start of frame : Differential progressive DCT");
            else if(f[pos + 1] == (byte) 0xc7)
              rep.append("<br><br>Start of frame : Differential lossless (sequential)");
            else if(f[pos + 1] == (byte) 0xc9)
              rep.append("<br><br>Start of frame : Extended sequential DCT");
            else if(f[pos + 1] == (byte) 0xca)
              rep.append("<br><br>Start of frame : Progressive DCT");
            else if(f[pos + 1] == (byte) 0xcb)
              rep.append("<br><br>Start of frame : Lossless (sequential)");
            else if(f[pos + 1] == (byte) 0xcd)
              rep.append("<br><br>Start of frame : Differential sequential DCT");
            else if(f[pos + 1] == (byte) 0xce)
              rep.append("<br><br>Start of frame : Differential progressive DCT");
            else if(f[pos + 1] == (byte) 0xcf)
              rep.append("<br><br>Start of frame : Differential  lossless (sequential)");
            else if(f[pos + 1] == (byte) 0xde)
              rep.append("<br><br>Start of frame : DHP");
            if(f[pos+1]<(byte)0xc8)
                rep.append(" (Huffman coding)");
            else rep.append(" (arithmetic coding)");
            rep.append(("<br>Header Length: " + Integer.toHexString(png_get_word(pos+2)) + " (" + png_get_word(pos+2) + ")"));
            rep.append(("<br>Precision: " + Integer.toHexString(uf(pos+4))));
            rep.append(("<br>Image lines: " + Integer.toHexString(png_get_word(pos+5)) + " (" + png_get_word(pos+5) + ")"));
            rep.append(("<br>Samples per line: " + Integer.toHexString(png_get_word(pos+7)) + " (" + png_get_word(pos+7) + ")"));
            pos+=2+png_get_word(pos+2);
        }
        else if(f[pos] == (byte) 0xff && f[pos + 1] == (byte) 0xc4)
        {
            rep.append("<br><br>Huffman table");
            rep.append(("<br>Length: " + Integer.toHexString(png_get_word(pos+2)) + " (" + png_get_word(pos+2) + ")"));
            pos+=2+png_get_word(pos+2);
        }
        else if(f[pos] == (byte) 0xff && f[pos + 1] == (byte) 0xcc)
        {
            rep.append("<br><br>Arithmetic coding conditioning");
            rep.append(("<br>Length: " + Integer.toHexString(png_get_word(pos+2)) + " (" + png_get_word(pos+2) + ")"));
            pos+=2+png_get_word(pos+2);
        }
        else if(f[pos] == (byte) 0xff && f[pos + 1] >= (byte) 0xd0 && f[pos + 1] <= (byte) 0xd7)
        {
            rep.append("<br><br>Restart interval");
            pos+=2;
        }
        else if(f[pos] == (byte) 0xff && f[pos + 1] == (byte) 0xda)
        {
            rep.append("<br><br>Start of scan");
            rep.append(("<br>Header Length: " + Integer.toHexString(png_get_word(pos+2)) + " (" + png_get_word(pos+2) + ")"));
            pos+=2+png_get_word(pos+2);
            int ct=pos;
            while(f[ct]!=(byte)0xff || (f[ct]==(byte)0xff && f[ct+1]==0)
                 || (f[ct]==(byte)0xff && f[ct+1]>=(byte)0xd0&&f[ct+1]<=(byte)0xd7))
                ct++;
            rep.append(("<br>Detected " + (ct-pos) + " bytes in scan"));
            pos=ct;
        }
        else if(f[pos] == (byte) 0xff && f[pos + 1] == (byte) 0xdb)
        {
            rep.append("<br><br>Quantisation table");
            rep.append(("<br>Length: " + Integer.toHexString(png_get_word(pos+2)) + " (" + png_get_word(pos+2) + ")"));
            pos+=2+png_get_word(pos+2);
        }
        else if(f[pos] == (byte) 0xff && f[pos + 1] == (byte) 0xdc)
        {
            rep.append("<br><br>Define number of lines");
            rep.append(("<br>Length: " + Integer.toHexString(png_get_word(pos+2)) + " (" + png_get_word(pos+2) + ")"));
            rep.append(" (Should be 4)");
            rep.append(("<br>Number: " + Integer.toHexString(png_get_word(pos+4)) + " (" + png_get_word(pos+4) + ")"));
            pos+=2+png_get_word(pos+2);
        }
        else if(f[pos] == (byte) 0xff && f[pos + 1] == (byte) 0xdd)
        {
            rep.append("<br><br>Define Restart interval");
            rep.append(("<br>Length: " + Integer.toHexString(png_get_word(pos+2)) + " (" + png_get_word(pos+2) + ")"));
            rep.append(" (Should be 4)");
            rep.append(("<br>Interval: " + Integer.toHexString(png_get_word(pos+4)) + " (" + png_get_word(pos+4) + ")"));
            pos+=2+png_get_word(pos+2);
        }
        else if(f[pos] == (byte) 0xff && f[pos + 1] == (byte) 0xdf)
        {
            rep.append("<br><br>Expand reference components");
            rep.append(("<br>Length: " + Integer.toHexString(png_get_word(pos+2)) + " (" + png_get_word(pos+2) + ")"));
            rep.append(" (Should be 3)");
            rep.append(("<br>Value: " + Integer.toHexString(uf(pos+4))));
            pos+=2+png_get_word(pos+2);
        }
        else if(f[pos] == (byte) 0xff && f[pos + 1] >= (byte) 0xe0 &&f[pos + 1] <= (byte) 0xef)
        {
            rep.append("<br><br>Application data");
            rep.append(("<br>Length: " + Integer.toHexString(png_get_word(pos+2)) + " (" + png_get_word(pos+2) + ")"));
            rep.append("<br>Dump of data:");
            fdump(pos+2,pos+2+png_get_word(pos+2)-1);
            pos+=2+png_get_word(pos+2);
        }
        else if(f[pos] == (byte) 0xff && f[pos + 1] == (byte) 0xfe)
        {
            rep.append("<br><br>Comment data");
            rep.append(("<br>Length: " + Integer.toHexString(png_get_word(pos+2)) + " (" + png_get_word(pos+2) + ")"));
            rep.append("<br>Dump of data:");
            fdump(pos+2,pos+2+png_get_word(pos+2)-1);
            pos+=2+png_get_word(pos+2);
        }
        else if(f[pos] == (byte) 0xff && f[pos + 1] == (byte) 0xff)
        {
            pos++;
        }
        else return pos;
        return analyse_jpg_sections(pos);
    }

    /**
     * Analysis particular to a gif image
     */
    private void analyse_gif()
    {
        if(f.length<13)
        {
           rep.append("<br>file too short?");
           return;
        }
        rep.append(("<br>Width: " + get_hword(6) + " (" + get_word(6) + ")"));
        rep.append(("<br>Height: " + get_hword(8) + " (" + get_word(8) + ")"));
        rep.append(("<br>Flags: " + Integer.toHexString(uf(10))));
        int flags = uf(10);
        int gctsize=0;
        if((flags&0x80)>0)
        {
            rep.append(" (Global Colour Table)");
            if((flags&0x10)>0) rep.append(" (Sorted GCT)");
            gctsize = 1<<(((flags&0x07)+1));
            rep.append((" (GCT Size = " + gctsize + ")"));
        }
        rep.append((" (Color Resolution = " + (((flags>>3)&0x03)+1) + ")"));
        rep.append(("<br>Background color index: " + uf(11)));
        rep.append(("<br>Pixel aspect ratio: " + uf(12)));
        int cpos = 13;
        if(f.length<cpos+gctsize*3)
        {
           rep.append("<br>file too short?");
           return;
        }
        if(gctsize>0)
        {
           rep.append("<br><br>Global Colour Table:");
            for(int i=0;i<gctsize;i++)
            {
                rep.append("<br>");
                for(int j=0;j<3;j++)
                {
                    rep.append(m2(Integer.toHexString(uf(cpos + i * 3 + j))));
                    rep.append(" ");
                }
                rep.append("   ");
                for(int j=0;j<3;j++)
                {
                    char c=(char)uf(cpos + i * 3 + j);
                    if(c<32||c>0x7f)c='.';
                    rep.append((""+c));
                }
            }
            cpos+=gctsize*3;
        }
        cpos = check_gif_blocks(cpos);
        if(cpos<f.length)
        {
            rep.append(("<br>Additional bytes at end of file = " +(f.length-cpos)));
            rep.append("<br>Dump of additional bytes:");
            fdump(cpos, f.length);
        }
    }

    /**
     * Analysis of a gif block
     * @param pos Position in the file
     * @return Position after this block
     */
    private int check_gif_blocks(int pos)
    {  
        if(f[pos]==0x2c)
        {
            if(pos+10>=f.length) return pos;
            rep.append("<br><br>Image Descriptor");
            rep.append(("<br>Left: " + get_hword(pos+1) + " (" + get_word(pos+1) + ")"));
            rep.append(("<br>Top: " + get_hword(pos+3) + " (" + get_word(pos+3) + ")"));
            rep.append(("<br>Width: " + get_hword(pos+5) + " (" + get_word(pos+5) + ")"));
            rep.append(("<br>Height: " + get_hword(pos+7) + " (" + get_word(pos+7) + ")"));
            rep.append(("<br>Flags: " + Integer.toHexString(uf(pos+9))));
            int flags = uf(pos+9);
            int lctsize = 0;
            if((flags&128)>0)
            {
                rep.append(" (Local colour table)");
                lctsize=1<<((flags&0x07)+1);
                rep.append((" (LCT Size = " + lctsize + ""));
            }
            if((flags&64)>0) rep.append(" (Interlace)");
            if((flags&32)>0) rep.append(" (Sort)");
            if((flags&24)>0) rep.append(" (**Reserved flags set **)");
            pos=pos+10;
            if(f.length<pos+lctsize*3)
            {
                rep.append("<br>file too short?");
                return pos;
            }
            if(lctsize>0)
            {
                rep.append("<br><br>Local Colour Table:");
                for(int i=0;i<lctsize;i++)
                {
                    rep.append("<br>");
                    for(int j=0;j<3;j++)
                    {
                        rep.append(m2(Integer.toHexString(uf(pos + i * 3 + j))));
                        rep.append(" ");
                    }
                    rep.append("   ");
                    for(int j=0;j<3;j++)
                    {
                        char c=(char)uf(pos + i * 3 + j);
                        if(c<32||c>0x7f)c='.';
                        rep.append((""+c));
                    }
                }
                pos+=lctsize*3;
            }
            rep.append(("<br>LZW size = " + uf(pos)));
            pos++;
            while(uf(pos)>0) pos+=uf(pos)+1;
            pos++;
        }
        else if(f[pos]==0x21 && uf(pos+1)==0xf9)
        {
            if(pos+8>=f.length) return pos;
            rep.append("<br><br>Graphic control extension");
            rep.append(("<br>Size : " + uf(pos+2) + " (must be 4)"));
            rep.append(("<br>Flags: " + Integer.toHexString(uf(pos+3))));
            int flags = uf(pos+3);
            if((flags&224)>0) rep.append(" (**Reserved flags set **)");
            if((flags&1)>0) rep.append(" (Transparency flag)");
            rep.append(("<br>Delay time: " + get_word(pos+4)));
            rep.append(("<br>Transparent color index: " + uf(6)));
            rep.append(("<br>Terminator: " + Integer.toHexString(uf(pos+7))));
            pos=pos+8;
        }
        else if(f[pos]==0x21 && uf(pos+1)==0xfe)
        {
            if(pos+3>=f.length) return pos;
            rep.append("<br><br>Comment extension");
            rep.append("<br>Dump of data:");
            pos+=2;
            while(uf(pos)>0)
            {
                fdump(pos+1,pos+uf(pos));
                pos += uf(pos) + 1;
            }
            pos++;
        }
        else if(f[pos]==0x21 && uf(pos+1)==0x01)
        {
            if(pos+16>=f.length) return pos;
            rep.append("<br><br>Plain Text extension");
            rep.append(("<br>Size : " + uf(pos+2) + " (must be 12)"));
            rep.append(("<br>Left: " + get_hword(pos+3) + " (" + get_word(pos+3) + ")"));
            rep.append(("<br>Top: " + get_hword(pos+5) + " (" + get_word(pos+5) + ")"));
            rep.append(("<br>Width: " + get_hword(pos+7) + " (" + get_word(pos+7) + ")"));
            rep.append(("<br>Height: " + get_hword(pos+9) + " (" + get_word(pos+9) + ")"));
            rep.append(("<br>Cell Width : " + uf(pos+11)));
            rep.append(("<br>Cell Height : " + uf(pos+12)));
            rep.append(("<br>Foreground Color Index : " + uf(pos+13)));
            rep.append(("<br>Background Color Index : " + uf(pos+14)));
            rep.append("<br>Dump of data:");
            pos+=15;
            while(uf(pos)>0)
            {
                fdump(pos+1,pos+uf(pos));
                pos += uf(pos) + 1;
            }
            pos++;
        }
        else if(f[pos]==0x21 && uf(pos+1)==0xff)
        {
            if(pos+14>=f.length) return pos;
            rep.append("<br><br>Application extension");
            rep.append(("<br>Size : " + uf(pos+2) + " (must be 11)"));
            rep.append("<br>Identifier:");
            fdump(pos+3,pos+10);
            rep.append("<br>Authentication code:");
            fdump(pos+11,pos+13);
            rep.append("<br>Application data:");
            pos+=14;
            while(uf(pos)>0)
            {
                fdump(pos+1,pos+uf(pos));
                pos += uf(pos) + 1;
            }
            pos++;
        }
        else if(f[pos]==0x3b)
        {
            rep.append("<br><br>Trailer block");
            return pos+1;
        }
        else return pos;
        return check_gif_blocks(pos);
    }

    /**
     * Analysis particular to a png file
     */
    private void analyse_png()
    {
        if(f.length<8)
        {
           rep.append("<br>file too short?");
           return;
        }
        if(f[4]!=13||f[5]!=10||f[6]!=26||f[7]!=10)
        {
            rep.append("<br>Error in header, bytes 5-8 should be 0d 0a 1a 0a");
        }
        int cpos = check_png_chunks(8);
        if(cpos<f.length)
        {
            rep.append(("<br>Additional bytes at end of file = " +(f.length-cpos)));
            rep.append("<br>Dump of additional bytes:");
            fdump(cpos, f.length);
        }
    }

    /**
     * Analyse png chunks
     * @param start Position in the file
     * @return Position after the chunks
     */
    private int check_png_chunks(int start)
    {
        if(start+12>f.length)
        {
            rep.append("<br>Premature end to file?");
            return start;
        }
        int length = png_get_dword(start);
        rep.append("<br><br>Chunk: ");
        if((f[4]&64)>0)
        {
            rep.append("<br>Ancillary - provides additional information");
        }
        else
        {
            rep.append("<br>Critical - necessary for display of image MUST BE recognized to proceed");
        }
        if((f[5]&64)>0)
        {
            rep.append("<br>Private, investigate this!!");
        }
        else
        {
            rep.append("<br>Public");
        }
        if((f[6]&64)>0)
        {
            rep.append("<br>Chunk has a reserved flag set, **investigate this**!");
        }
        if((f[7]&64)>0)
        {
            rep.append("<br>Safe to copy, chunk may propagate to other files");
        }
        else
        {
            rep.append("<br>Unsafe to copy unless known to software");
        }
        fdump(start+4,start+7);
        rep.append(("<br>Data length = "+length + " bytes"));
        rep.append(("<br>CRC = "+png_get_hdword(start+8+length)));
        CRC32 cc = new CRC32();
        cc.reset();
        cc.update(f,start+4,4+length);
        if((int)cc.getValue()!=png_get_dword(start+8+length))
        {
           rep.append("<br>Calculated CRC = " + Integer.toHexString((int)cc.getValue()));
        }
        if(start+12+length>f.length)
        {
            rep.append("<br>Not enough room in file for data?");
            return start;
        }
        int typ = png_get_dword(start+4);
        if(typ==0x49454E44) // IEND
            return start + 12;
        else if(typ == 0x49484452) // IHDR
        {
            rep.append(("<br>Width: " + png_get_hdword(start+8) + " ("+png_get_dword(start+8)+")"));
            rep.append(("<br>Height: " + png_get_hdword(start+12) + " ("+png_get_dword(start+12)+")"));
            rep.append(("<br>Bit Depth: " + f[start+16]));
            rep.append(("<br>Color Type: " + f[start+17]));
            if(f[start+17]==0) rep.append(" (Grayscale)");
            if(f[start+17]==2) rep.append(" (RGB Triples)");
            if(f[start+17]==3) rep.append(" (Palette Indexed)");
            if(f[start+17]==4) rep.append(" (Grayscale + Alpha)");
            if(f[start+17]==6) rep.append(" (RGB + Alpha)");
            rep.append(("<br>Compression Method: " + f[start+18]));
            if(f[start+18]==0) rep.append(" (deflate)");
            else rep.append(" (unknown)");
            rep.append(("<br>Filter Method: " + f[start+19]));
            if(f[start+19]==0) rep.append(" (adaptive)");
            else rep.append(" (unknown)");
            rep.append(("<br>Interlace Method: " + f[start+20]));
            if(f[start+20]==0) rep.append(" (none)");
            else if(f[start + 20] == 1) rep.append(" (adam7)");
            else rep.append(" (unknown)");
        }
        else if(typ == 0x504C5445) //PLTE
        {
            rep.append(("<br>Palette contains " + (length/3) +
                    " RGB entries, NB if colortype is 2 or 6 this could be hiding something."));
            rep.append("<br>Dump of palette:");
            for(int i=0;i<length/3;i++)
            {
                rep.append("<br>");
                for(int j=0;j<4;j++)
                {
                    rep.append(m2(Integer.toHexString(uf(start+8 + i * 3 + j))));
                    rep.append(" ");
                }
                rep.append("   ");
                for(int j=0;j<3;j++)
                {
                    char c=(char)uf(start+8 + i * 3 + j);
                    if(c<32||c>0x7f)c='.';
                    rep.append((""+c));
                }
            }
        }
        else if(typ == 0x49444154) //IDAT
        {
            rep.append("<br>Image data, compressed");
        }
        else if(typ == 0x624B4744) //bKGD
        {
            rep.append("<br>Background data, optional");
            rep.append("<br>NB: should be after PLTE chunk and before first IDAT");
            if(length!=1&&length!=2&&length!=6)
                rep.append("<br>Alert: Length should be 1, 2 or 6 bytes");
            rep.append("<br>dump of data:");
            fdump(start+8,start+8+length-1);
        }
        else if(typ == 0x6348524D) //cHRM
        {
            rep.append("<br>Primary chromaticities and white point");
            rep.append("<br>NB data represents 4 points for white,red,green,blue");
            rep.append("<br>x coordinates and y coordinates");
            rep.append("<br>dump of data:");
            fdump(start+8,start+8+length-1);
        }
        else if(typ == 0x67414D41) //gAMA
        {
            rep.append("<br>Image gamma value");
            rep.append(("<br>"+get_dword(start+8)));
        }
        else if(typ == 0x68495354) //hIST
        {
            rep.append("<br>Image histogram");
            rep.append("<br>Should be 2 bytes for each entry in PLTE");
            rep.append(("<br>Contains " + (length/2) + " entries"));
            rep.append("<br>dump of data:");
            fdump(start+8,start+8+length-1);
        }
        else if(typ == 0x70485973) //pHYs
        {
            rep.append("<br>Physical pixel dimensions");
            rep.append("<br>Should be 9 bytes");
            rep.append("<br>dump of data:");
            fdump(start+8,start+8+length-1);
        }
        else if(typ == 0x73424954) //sBIT
        {
            rep.append("<br>Significant bits");
            rep.append("<br>Should be 4 bytes or less");
            rep.append("<br>dump of data:");
            fdump(start+8,start+8+length-1);
        }
        else if(typ == 0x74455874) //tEXt
        {
            rep.append("<br>Text");
            rep.append("<br>Consists of keyword and value separated by null byte");
            rep.append("<br>dump of data:");
            fdump(start+8,start+8+length-1);
        }
        else if(typ == 0x74494D45) //tIME
        {
            rep.append("<br>Last modification time");
            rep.append(("<br>Year  : "+png_get_word(start+8)));
            rep.append(("<br>Month : "+uf(start+10)));
            rep.append(("<br>Day   : "+uf(start+11)));
            rep.append(("<br>Hour  : "+uf(start+12)));
            rep.append(("<br>Minute: "+uf(start+13)));
            rep.append(("<br>Second: "+uf(start+14)));
        }
        else if(typ == 0x74524E53) //tRNS
        {
            rep.append("<br>Transparency");
            rep.append("<br>Alpha values, either for palette");
            rep.append("<br> or single value for image");
            rep.append("<br>dump of data:");
            fdump(start+8,start+8+length-1);
        }
        else if(typ == 0x7a545874) //zTXt
        {
            rep.append("<br>Text, compressed");
            rep.append("<br>Keyword is not compressed, value is compressed");
            rep.append("<br>dump of data:");
            fdump(start+8,start+8+length-1);
        }
        else
        {
            rep.append("<br>Unknown chunk type");
            rep.append("<br>dump of data:");
            fdump(start+8,start+8+length-1);
        }
        return check_png_chunks(start+12+length);
    }

    /**
     * Analysis particular to a bmp file
     */
    private void analyse_bmp()
    {
        int fsz=get_dword(2);
        int offbits=get_dword(10);
        if(f.length<54)
        {
            rep.append("<br>File is too short to contain headers");
            return;
        }
        rep.append("<br>File Header info:");
        rep.append(("<br>File size: "+Integer.toHexString(fsz)));
        rep.append(("<br>Reserved1: " + get_hword(6)));
        rep.append(("<br>Reserved2: " + get_hword(8)));
        if(f.length>fsz)
        {
            rep.append(("<br>Additional bytes at end of file = "+(f.length-fsz)));
            rep.append(("<br>Dump of additional bytes:"));
            fdump(fsz,f.length-1);
            rep.append("<br>NB: Additional bytes may indicate that the height is set incorrectly" +
                       " in the header, try increasing the height to see if it is hiding image data");
        }
        rep.append(("<br>Bitmap data starts at: "+Integer.toHexString(offbits)));
        rep.append(("<br>Info Header:"));
        int bisize=get_dword(14);
        rep.append(("<br>Info Size: " + Integer.toHexString(bisize)));
        rep.append(("<br>Width: " + get_hdword(18) + " (" + get_dword(18) + ")"));
        rep.append(("<br>Height: " + get_hdword(22) + " (" + get_dword(22) + ")"));
        rep.append(("<br>Planes: " + get_hword(26) + " (must be 1)"));
        rep.append(("<br>BitCount: " + get_hword(28)+ " bits per pixel"));
        int compress = get_dword(30);
        rep.append(("<br>Compression: " + get_hdword(30)));
        if(compress==0x00||compress==0x32424752)
            rep.append(" (No compression)");
        else if(compress==0x01||compress==0x38454c52)
            rep.append(" (RLE for 8bpp RGB)");
        else if(compress==0x02||compress==0x34454c52)
            rep.append(" (RLE for 4bpp RGB)");
        else if(compress==0x03)
            rep.append(" (Raw RGB with packing)");
        else if(compress==0x32776173)
            rep.append(" (Raw RGB)");
        else if(compress==0x41424752)
            rep.append(" (Raw RGB with alpha)");
        else if(compress==0x54424752)
            rep.append(" (Raw RGB with transparency)");
        rep.append(("<br>Size Image: " + get_hdword(34) + " (can be zero if bitmap is uncompressed)"));
        rep.append(("<br>XPelsPerMeter: " + get_hdword(38) + " (pixels per meter for target device)"));
        rep.append(("<br>YPelsPerMeter: " + get_hdword(42) +  " (pixels per meter for target device)"));
        rep.append(("<br>ClrUsed: " + get_hdword(46) + " (color indexes in use, 0=max)"));
        rep.append(("<br>ClrImportant: " + get_hdword(50) + "(0 if all colors are important)"));
        int ctstart = 14+bisize;
        rep.append(("<br>Color table computed start: "+Integer.toHexString(ctstart) + "(normally 0x36, if not then possible hidden data at that point)"));
        if(ctstart>0x36)
        {
            rep.append("<br>Dump of gap between header and start of color table:");
            fdump(0x36, ctstart-1);
        }
        if(get_word(28)<=8)
        {
            rep.append("<br>Color Index Table (NB fourth entry of each index should be zero, order is b,g,r,a):");
            int ncols=get_dword(46);
            if(ncols==0)
            {
               if(get_word(28)==1)ncols=2;
               else if(get_word(28)==4)ncols=16;
               else if(get_word(28)==8)ncols=256;
            }
            for(int i=0;i<ncols;i++)
            {
                rep.append("<br>");
                for(int j=0;j<4;j++)
                {
                    rep.append(m2(Integer.toHexString(uf(ctstart + i * 4 + j))));
                    rep.append(" ");
                }
                rep.append("   ");
                for(int j=0;j<4;j++)
                {
                    char c=(char)uf(ctstart + i * 4 + j);
                    if(c<32||c>0x7f)c='.';
                    rep.append((""+c));
                }
            }
            ctstart+=ncols*4;
        }
        if(ctstart!=offbits)
        {
            rep.append(("<br>Color table finishes at " + Integer.toHexString(ctstart) +
                    " but data bits start at " + Integer.toHexString(offbits)));
        }
        if(ctstart<offbits)
        {
            rep.append("<br>Dump of gap between color table and image:");
            fdump(ctstart, offbits-1);
        }
        if(compress!=0x00&&compress!=0x32424752)
        {
            rep.append("<br>Further checking is only done when there is no compression");
            return;
        }
        rep.append("<br>Row filler bytes dump:");
        // width * bits per pixel
        int bitsPerRow=get_dword(18)*get_word(28);
        int bytesPerRow = (bitsPerRow+7)/8;
        int fgap = (bytesPerRow+3)/4;
        fgap = fgap * 4 - bytesPerRow;
        ctstart=offbits;
        for(int i=0;i<get_dword(22);i++)
        {
            ctstart+=bytesPerRow;
            int ctend=ctstart+fgap;
            if(ctend!=ctstart)
                fdump(ctstart,ctend-1);
            ctstart=ctend;
        }
    }

    /**
     * Dumps a section of file
     * @param from Dump from this offset
     * @param to Dump to this offset
     */
    private void fdump(int from, int to)
    {
        if(from>=f.length)
            return;
        rep.append("<br>Hex:");
        for(int i=from;i<=to&&i<f.length;i+=16)
        {
           rep.append("<br>");
           for(int j=0;j<16&&i+j<f.length&&i+j<=to;j++)
           {
              rep.append(m2(Integer.toHexString(((int)f[i+j])&0xff)));
              if(j==7)
                 rep.append(' ');
           }
           rep.append("  ");
        }
        rep.append("<br>Ascii:");
        for(int i=from;i<=to&&i<f.length;i+=16)
        {
           rep.append("<br>");
           for(int j=0;j<16&&i+j<f.length&&i+j<=to;j++)
           {
              char c = (char)f[i+j];
              if(c=='<') rep.append("&lt;");
              else if(c=='>') rep.append("&gt;");
              else if(c=='&') rep.append("&amp;");
              else if(c >= 32 && c <= 128)
                  rep.append(c);
              else
                  rep.append('.');
              if(j==7) rep.append(' ');
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

    /**
     * Returns hex of dword at given file offset (lsb...msb)
     * @param offs File offset
     * @return Hex string
     */
    private String get_hdword(int offs)
    {
        return Integer.toHexString(get_dword(offs));
    }

    /**
     * Returns hex of dword at given file offset (msb...lsb)
     * @param offs File offset
     * @return Hex string
     */
    private String png_get_hdword(int offs)
    {
        return Integer.toHexString(png_get_dword(offs));
    }

    /**
     * Returns hex of word at given file offset (lsb...msb)
     * @param offs File offset
     * @return Hex string
     */
    private String get_hword(int offs)
    {
        return Integer.toHexString(get_word(offs));
    }

    /**
     * Returns byte at given file offset
     * @param offs File offset
     * @return byte at file offset, as int
     */
    private int uf(int offs)
    {
        if(offs>=f.length)
            return 0;
        int r = f[offs];
        return r&0xff;
    }

    /**
     * Returns dword at given file offset (lsb...msb)
     * @param offs File offset
     * @return dword at file offset, as int
     */
    private int get_dword(int offs)
    {
        if(offs+3>=f.length)
            return 0;
        return uf(offs)+(uf(offs+1)<<8)+(uf(offs+2)<<16)+(uf(offs+3)<<24);
    }

    /**
     * Returns dword at given file offset (msb...lsb)
     * @param offs File offset
     * @return dword at file offset, as int
     */
    private int png_get_dword(int offs)
    {
        if(offs+3>=f.length)
            return 0;
        return uf(offs+3)+(uf(offs+2)<<8)+(uf(offs+1)<<16)+(uf(offs)<<24);
    }

    /**
     * Returns word at given file offset (lsb...msb)
     * @param offs File offset
     * @return word at file offset, as int
     */
    private int get_word(int offs)
    {
        if(offs+1>=f.length)
            return 0;
        return uf(offs)+(uf(offs+1)<<8);
    }

    /**
     * Returns word at given file offset (msb...lsb)
     * @param offs File offset
     * @return word at file offset, as int
     */
    private int png_get_word(int offs)
    {
        if(offs+1>=f.length)
            return 0;
        return uf(offs+1)+(uf(offs)<<8);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        report = new javax.swing.JEditorPane();
        jPanel2 = new javax.swing.JPanel();
        OKButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("File Format Analysis");

        jPanel1.setMinimumSize(new java.awt.Dimension(400, 300));
        jPanel1.setPreferredSize(new java.awt.Dimension(400, 350));

        jScrollPane1.setMinimumSize(new java.awt.Dimension(400, 260));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(400, 260));

        report.setContentType("text/html");
        report.setFont(new java.awt.Font("Courier New", 0, 14)); // NOI18N
        jScrollPane1.setViewportView(report);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 815, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        jPanel2.setMinimumSize(new java.awt.Dimension(400, 35));
        jPanel2.setPreferredSize(new java.awt.Dimension(400, 35));

        OKButton.setText("OK");
        OKButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        OKButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OKButtonActionPerformed(evt);
            }
        });
        jPanel2.add(OKButton);

        getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Close the form
     * @param evt Event
     */
    private void OKButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OKButtonActionPerformed
        dispose();
    }//GEN-LAST:event_OKButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton OKButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JEditorPane report;
    // End of variables declaration//GEN-END:variables

}
