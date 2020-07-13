package com.example.printersetup;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import java.util.Objects;

 class ParseBitmap {
    private static final String TAG = "ParseBitmap";

     public static Bitmap scaleDown(Bitmap realImage, float maxImageSize, boolean filter) {
         //float ratio = Math.min(maxImageSize / realImage.getWidth(), maxImageSize / realImage.getHeight());
         int width=realImage.getHeight();
         if (realImage.getHeight()>300){
             width = 300;
         }
       //  int height = Math.round( ratio * realImage.getHeight());
         return Bitmap.createScaledBitmap(realImage, width, realImage.getHeight(), filter);
     }

   static String ExtractGraphicsDataForCPCL(Bitmap m_bmp_image){

        StringBuilder m_data = new StringBuilder();
        int color,bit,currentValue,redValue, blueValue, greenValue;
        try{
            Log.d(TAG,"Height:"+ m_bmp_image.getHeight());
            Log.d(TAG,"Widht:"+ m_bmp_image.getWidth());
            //Make sure the width is divisible by 8
            int loopWidth = 8 - (m_bmp_image.getWidth() % 8);
            if (loopWidth == 8)
                loopWidth = m_bmp_image.getWidth();
            else
                loopWidth += m_bmp_image.getWidth();

            m_data = new StringBuilder("EG" + " " +
                    loopWidth / 8 + " " +
                    m_bmp_image.getHeight() + " " +
                    0 + " " +
                    0 + " ");

            for (int y = 0; y < m_bmp_image.getHeight(); y++)
            {
                bit = 128;
                currentValue = 0;
                for (int x = 0; x < loopWidth; x++)
                {
                    int intensity;

                    if (x < m_bmp_image.getWidth())
                    {
                        color = m_bmp_image.getPixel(x, y);

                        redValue = Color.red(color);
                        blueValue = Color.blue(color);
                        greenValue = Color.green(color);

                        intensity = 255 - ((redValue + greenValue + blueValue) / 3);
                    }
                    else
                        intensity = 0;


                    if (intensity >= 128)
                        currentValue |= bit;
                    bit = bit >> 1;
                    if (bit == 0)
                    {
                        String hex = Integer.toHexString(currentValue);
                        hex = LeftPad(hex);
                        m_data.append(hex.toUpperCase());
                        bit = 128;
                        currentValue = 0;

                        /*
                         String dbg = "x,y" + "-"+ Integer.toString(x) + "," + Integer.toString(y) + "-" +
                         "Col:" + Integer.toString(color) + "-" +
                         "Red: " +  Integer.toString(redValue) + "-" +
                         "Blue: " +  Integer.toString(blueValue) + "-" +
                         "Green: " +  Integer.toString(greenValue) + "-" +
                         "Hex: " + hex;

                         Log.d(TAG,dbg);
                         */

                    }
                }//x
            }//y
            m_data.append("\r\n");

        }catch(Exception e){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                m_data = new StringBuilder(Objects.requireNonNull(e.getMessage()));
            }
            return m_data.toString();
        }

        return m_data.toString();
    }

    private static String LeftPad(String _num){

        String str = _num;

        if (_num.length() == 1)
        {
            str = "0" + _num;
        }

        return str;
    }
}
