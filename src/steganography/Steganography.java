
package steganography;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import javax.imageio.ImageIO;
/**
 *
 * @author noahbragg
 */
public class Steganography {
    
    private BufferedImage openImage(String image) {
        
        try {   

            BufferedImage in = ImageIO.read(new File(image));
            BufferedImage newImage = new BufferedImage(in.getWidth(), in.getHeight(), BufferedImage.TYPE_INT_ARGB);

            Graphics2D g = newImage.createGraphics();
            g.drawImage(in, 0, 0, null);
            g.dispose();
            
            return newImage;
        } catch(Exception ex) {
            ex.printStackTrace(); 
        }
        System.out.println("Failed to open image");
        return null;
    }
    
    private void writeToImage(BufferedImage img, String image) {
        
        try {   
            File outputfile = new File(image);
            ImageIO.write(img, "png", outputfile);
        } catch(Exception ex) {
            ex.printStackTrace();
            System.out.println("Failed to write to image");
        }
    }
    
    //parses the input
    private String parseInput(String destination) {
        try {
            Scanner myScan = new Scanner(new File(destination));             //create the scanner
            String secretMessage = "";

            while(myScan.hasNext()) {
                secretMessage += myScan.next() + " ";
            }
            myScan.close();
            return secretMessage;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Could not open text file");
        }
        return null;
    }
    
    //print the secret message to the text file
    private void printToFile(String secretMessage, String destination) {
        try {
            PrintWriter printWriter = new PrintWriter(new File(destination));
            printWriter.println(secretMessage);
            printWriter.close (); 
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Could not open text file");
        }
    }
    
    //embeds the message into the image
    private BufferedImage embedMessage(BufferedImage img, String mess) {
        int messageLength = mess.length();
        int imageWidth = img.getWidth();
        int imageHeight = img.getHeight();
        int imageSize = imageWidth * imageHeight;
        if(messageLength * 2 + 8 > imageSize) {
            System.out.println("The message is too big");
            return null;
        }
        embedInteger(img, messageLength, 0);

        byte b[] = mess.getBytes();
        for(int i=0; i<b.length; i++) {
            embedByte(img, b[i], i*2+8);
        }
        return img;
    }
    
    //used for embeding the message
    private void embedInteger(BufferedImage img, int n, int start) {
        int maxX = img.getWidth();
        int maxY = img.getHeight();
        int startX = start/maxY;
        int startY = start - startX*maxY;
        int count = 0;
        for(int i=startX; i<maxX && count<32; i++) {
           for(int j=startY; j<maxY && count<32; j++) {
                int rgb = img.getRGB(i, j);
                int bit = getBitValue(n, count);
                rgb = setBitValue(rgb, 0, bit);
                bit = getBitValue(n, count+1); rgb = setBitValue(rgb, 8, bit);
                bit = getBitValue(n, count+2); rgb = setBitValue(rgb, 16, bit);
                bit = getBitValue(n, count+3); rgb = setBitValue(rgb, 24, bit);
                img.setRGB(i, j, rgb); 
                count = count+4;
            }
        }
    }
    
    //used to embed the bytes into the message
    private void embedByte(BufferedImage img, byte b, int start) {
        int maxX = img.getWidth(), maxY = img.getHeight(), 
           startX = start/maxY, startY = start - startX*maxY, count=0;
        for(int i = startX; i < maxX && count < 8; i++) {
            for(int j = startY; j < maxY && count < 8; j++) {
                if(j == maxY - 1) {
                    startY = 0;
                }
                int rgb = img.getRGB(i, j);
                int bit = getBitValue(b, count);
                rgb = setBitValue(rgb, 0, bit);
                bit = getBitValue(b, count+1); 
                rgb = setBitValue(rgb, 8, bit);
                bit = getBitValue(b, count+2); 
                rgb = setBitValue(rgb, 16, bit);
                bit = getBitValue(b, count+3); 
                rgb = setBitValue(rgb, 24, bit);
                img.setRGB(i, j, rgb);
                count = count + 4;
              
           }
        }
    }
    
    //used in embedding and decoding the message
    private int getBitValue(int n, int location) { //n=messageLength, location=count

        int v = n & (int) Math.round(Math.pow(2, location));
        return v==0?0:1;
    }
    
    //used in embedding and decoding the message
    private int setBitValue(int n, int location, int bit) { 
        int toggle = (int) Math.pow(2, location);
        int bv = getBitValue(n, location); 
        if(bv == bit) {
            return n;
        }
        if(bv == 0 && bit == 1) {
            n |= toggle;
        } else if (bv == 1 && bit == 0){
            n ^= toggle;
        }
        return n;
    }
    
    //used ot decode the message
    private String decodeMessage(BufferedImage img) {
        int length = extractInteger(img, 0);
        byte b[] = new byte[length];
        for(int i = 0; i < length; i++) {
            b[i] = extractByte(img, i*2+8);
        }
        return new String(b);
    }
    
    //Finds the length that we will need for decoding the message
    private int extractInteger(BufferedImage img, int start) {
        int maxX = img.getWidth();
        int maxY = img.getHeight(); 
        int startX = start / maxY;
        int startY = start - startX * maxY;
        int count = 0;
        int length = 0;
        for(int i = startX; i < maxX && count < 32; i++) {
            for(int j = startY; j < maxY && count < 32; j++) {
                int rgb = img.getRGB(i, j);
                int bit = getBitValue(rgb, 0);
                length = setBitValue(length, count, bit);
                bit = getBitValue(rgb, 8);
                length = setBitValue(length, count+1, bit);
                bit = getBitValue(rgb, 16);
                length = setBitValue(length, count+2, bit);
                bit = getBitValue(rgb, 24);
                length = setBitValue(length, count+3, bit);
                count = count + 4;
            }
        }
        return length;
   }
    
    //exctracts bytes from the image
    private byte extractByte(BufferedImage img, int start) {
        int maxX = img.getWidth(), maxY = img.getHeight(), 
           startX = start/maxY, startY = start - startX*maxY, count=0;
        byte b = 0;
        for(int i=startX; i<maxX && count<8; i++) {
            for(int j=startY; j<maxY && count<8; j++) {
                if(j == maxY - 1){
                    startY = 0;
                }
                int rgb = img.getRGB(i, j), bit = getBitValue(rgb, 0);
                b = (byte)setBitValue(b, count, bit);
                bit = getBitValue(rgb, 8); b = (byte)setBitValue(b, count+1, bit);
                bit = getBitValue(rgb, 16); b = (byte)setBitValue(b, count+2, bit);
                bit = getBitValue(rgb, 24); b = (byte)setBitValue(b, count+3, bit);
                count = count + 4;
            }
        }
        return b;
   }
    
    public static void main(String[] args) {
        //create steg object
        Steganography steg = new Steganography();
        
        //evaluate the args
        if(args[0].equals("-e")) {               //encript
            if(args.length < 4) {
                System.out.println("You don't have all the arguments");
            } else {
                String origImgName = args[1];
                String modifiedImgName = args[2];
                String secretFile = args[3];
                String secretMessage = steg.parseInput(secretFile);
                
                //embed it
                BufferedImage img = steg.openImage(origImgName);
                BufferedImage changedImg = steg.embedMessage(img, secretMessage);
                steg.writeToImage(changedImg, modifiedImgName);
                System.out.println("Hid your secret message in " + modifiedImgName);
            }
        } else if (args[0].equals("-d")) {       //decode
            if(args.length < 3) {
                System.out.println("You don't have all the arguments");
            } else {
                String modifiedImgName = args[1];
                String outputFile = args[2];
                
                //decode it
                BufferedImage img = steg.openImage(modifiedImgName);
                String secretMessage = steg.decodeMessage(img);
                steg.printToFile(secretMessage, outputFile);
                System.out.println("The secret message was written to " + outputFile);
            }
        } else {
            System.out.println("That is not a valid argument");
        }
    }

}
    
