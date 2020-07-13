package com.example.printersetup;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.BitSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ESCPOSApi {
    private final byte[] INITIALIZE_PRINTER = new byte[]{0x1B,0x40};

    private final byte[] PRINT_AND_FEED_PAPER = new byte[]{0x0A};

    private final byte[] SELECT_BIT_IMAGE_MODE = new byte[]{(byte)0x1B, (byte)0x2A};
    private final byte[] SET_LINE_SPACING = new byte[]{0x1B, 0x33};

    private FileOutputStream printOutput;

    public int maxBitsWidth = 255;

    public ESCPOSApi(String device) {
        try {
            printOutput = new FileOutputStream(device);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ESCPOSApi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private byte[] buildPOSCommand(byte[] command, byte... args) {
        byte[] posCommand = new byte[command.length + args.length];

        System.arraycopy(command, 0, posCommand, 0, command.length);
        System.arraycopy(args, 0, posCommand, command.length, args.length);

        return posCommand;
    }

/*
    private BitSet getBitsImageData(BufferedImage image) {
        int threshold = 127;
        int index = 0;
        int dimenssions = image.getWidth() * image.getHeight();
        BitSet imageBitsData = new BitSet(dimenssions);

        for (int y = 0; y < image.getHeight(); y++)
        {
            for (int x = 0; x < image.getWidth(); x++)
            {
                int color = image.getRGB(x, y);
                int  red = (color & 0x00ff0000) >> 16;
                int  green = (color & 0x0000ff00) >> 8;
                int  blue = color & 0x000000ff;
                int luminance = (int)(red * 0.3 + green * 0.59 + blue * 0.11);
                //dots[index] = (luminance < threshold);
                imageBitsData.set(index, (luminance < threshold));
                index++;
            }
        }

        return imageBitsData;
    }
*/

/*
    public void printImage(BufferedImage image) {
        try {
            BitSet imageBits = getBitsImageData(image);

            byte widthLSB = (byte)(image.getWidth() & 0xFF);
            byte widthMSB = (byte)((image.getWidth() >> 16) & 0xFF);

            // COMMANDS
            byte[] selectBitImageModeCommand = buildPOSCommand(SELECT_BIT_IMAGE_MODE, (byte) 33, widthLSB, widthMSB);
            byte[] setLineSpacing24Dots = buildPOSCommand(SET_LINE_SPACING, (byte) 24);
            byte[] setLineSpacing30Dots = buildPOSCommand(SET_LINE_SPACING, (byte) 30);


            printOutput.write(INITIALIZE_PRINTER);
            printOutput.write(setLineSpacing24Dots);

            int offset = 0;
            while (offset < image.getHeight()) {
                printOutput.write(selectBitImageModeCommand);

                int imageDataLineIndex = 0;
                byte[] imageDataLine = new byte[3 * image.getWidth()];

                for (int x = 0; x < image.getWidth(); ++x) {

                    // Remember, 24 dots = 24 bits = 3 bytes.
                    // The 'k' variable keeps track of which of those
                    // three bytes that we're currently scribbling into.
                    for (int k = 0; k < 3; ++k) {
                        byte slice = 0;

                        // A byte is 8 bits. The 'b' variable keeps track
                        // of which bit in the byte we're recording.
                        for (int b = 0; b < 8; ++b) {
                            // Calculate the y position that we're currently
                            // trying to draw. We take our offset, divide it
                            // by 8 so we're talking about the y offset in
                            // terms of bytes, add our current 'k' byte
                            // offset to that, multiple by 8 to get it in terms
                            // of bits again, and add our bit offset to it.
                            int y = (((offset / 8) + k) * 8) + b;

                            // Calculate the location of the pixel we want in the bit array.
                            // It'll be at (y * width) + x.
                            int i = (y * image.getWidth()) + x;

                            // If the image (or this stripe of the image)
                            // is shorter than 24 dots, pad with zero.
                            boolean v = false;
                            if (i < imageBits.length()) {
                                v = imageBits.get(i);
                            }
                            // Finally, store our bit in the byte that we're currently
                            // scribbling to. Our current 'b' is actually the exact
                            // opposite of where we want it to be in the byte, so
                            // subtract it from 7, shift our bit into place in a temp
                            // byte, and OR it with the target byte to get it into there.
                            slice |= (byte) ((v ? 1 : 0) << (7 - b));
                        }

                        imageDataLine[imageDataLineIndex + k] = slice;

                        // Phew! Write the damn byte to the buffer
                        //printOutput.write(slice);
                    }

                    imageDataLineIndex += 3;
                }

                printOutput.write(imageDataLine);
                offset += 24;
                printOutput.write(PRINT_AND_FEED_PAPER);
            }


            printOutput.write(setLineSpacing30Dots);
        } catch (IOException ex) {
            Logger.getLogger(ESCPOSApi.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

}*/
}
