package com.example.printersetup;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.BitSet;

public class BluetoothPrinterActivity extends AppCompatActivity {

    BluetoothAdapter mBTAdapter;
    BluetoothSocket mBTSocket = null;
    Dialog dialogProgress;
    String BILL, TRANS_ID;
    String PRINTER_MAC_ID = "00:01:90:85:05:66";
    final String ERROR_MESSAGE = "There has been an error in printing the bill.";
    BitSet dots;
    public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        String temp= Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    private void convertArgbToGrayscale(Bitmap bmpOriginal, int width,
                                        int height) {
        int pixel;
        int k = 0;
        int B = 0, G = 0, R = 0;
        dots = new BitSet();
        try {

            for (int x = 0; x < height; x++) {
                for (int y = 0; y < width; y++) {
                    // get one pixel color
                    pixel = bmpOriginal.getPixel(y, x);

                    // retrieve color of all channels
                    R = Color.red(pixel);
                    G = Color.green(pixel);
                    B = Color.blue(pixel);
                    // take conversion up to one single value by calculating
                    // pixel intensity.
                    R = G = B = (int) (0.299 * R + 0.587 * G + 0.114 * B);
                    // set bit into bitset, by calculating the pixel's luma
                    if (R < 55) {
                        dots.set(k);//this is the bitset that i'm printing
                    }
                    k++;

                }


            }


        } catch (Exception e) {
            // TODO: handle exception
            Log.e("TAG", e.toString());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {

            Bitmap icon = BitmapFactory.decodeResource(getResources(),
                    R.drawable.download);


            BILL = "\nSale Slip No: 12345678" + " " + "04-08-2011\n";
            BILL = BILL + "----------------------------------------";
            BILL = BILL + "\n";
            BILL = BILL + "Total Qty:" + " " + "2.0\n";
            BILL = BILL + "Total Value:" + " " + "17625.0\n";
            BILL = BILL + "-----------------------------------------";
            BILL = BILL + BitMapToString(icon);
            BILL = BILL + "\n\n";
            mBTAdapter = BluetoothAdapter.getDefaultAdapter();

            if (mBTAdapter == null) {
                Toast.makeText(this, "Device has no bluetooth capability",Toast.LENGTH_LONG).show();
                finish();
            } else {
                if (!mBTAdapter.isEnabled()) {
                    Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(i, 0);
                }

                // Register the BroadcastReceiver
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

                dialogProgress = new Dialog(BluetoothPrinterActivity.this);
                dialogProgress.setTitle("Finding printer...");
                dialogProgress.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        dialog.dismiss();
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                });
                dialogProgress.show();

            }

            if (mBTAdapter.isDiscovering())
                mBTAdapter.cancelDiscovery();
            else
                mBTAdapter.startDiscovery();

            System.out.println("BT Searching status :" + mBTAdapter.isDiscovering());

        } catch (Exception e) {
            Log.e("Class ", "My Exe ", e);
        }
    }


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    System.out.println("***" + device.getName() + " : "+ device.getAddress());

                    if (device.getAddress().equalsIgnoreCase(PRINTER_MAC_ID)) {
                        mBTAdapter.cancelDiscovery();
                        dialogProgress.dismiss();
                        Toast.makeText(BluetoothPrinterActivity.this,device.getName() + " Printing data",Toast.LENGTH_LONG).show();
                        printBillToDevice(PRINTER_MAC_ID);
                        Toast.makeText(BluetoothPrinterActivity.this,device.getName() + " found", Toast.LENGTH_LONG).show();
                    }
                }
            } catch (Exception e) {
                Log.e("Class ", "My Exe ", e);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (dialogProgress != null)
                dialogProgress.dismiss();
            if (mBTAdapter != null)
                mBTAdapter.cancelDiscovery();
            this.unregisterReceiver(mReceiver);
        } catch (Exception e) {
            Log.e("Class ", "My Exe ", e);
        }
    }

    @Override
    public void onBackPressed() {
        try {
            if (mBTAdapter != null)
                mBTAdapter.cancelDiscovery();
            this.unregisterReceiver(mReceiver);
        } catch (Exception e) {
            Log.e("Class ", "My Exe ", e);
        }
        setResult(RESULT_CANCELED);
        finish();
    }


    public void printBillToDevice(final String address) {
        new Thread(new Runnable() {
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        dialogProgress.setTitle("Connecting...");
                        dialogProgress.show();
                    }

                });

                mBTAdapter.cancelDiscovery();

                try {
                    System.out.println("**************************#****connecting");
                    BluetoothDevice mdevice = mBTAdapter.getRemoteDevice(address);
                    Method m = mdevice.getClass().getMethod("createRfcommSocket", new Class[] { int.class });
                    mBTSocket = (BluetoothSocket) m.invoke(mdevice, 1);

                    mBTSocket.connect();
                    OutputStream os = mBTSocket.getOutputStream();
                    os.flush();

                    os.write(BILL.getBytes());
                    System.out.println(BILL);

                    setResult(RESULT_OK);
                    finish();
                } catch (Exception e) {
                    Log.e("Class ", "My Exe ", e);
                    e.printStackTrace();
                    setResult(RESULT_CANCELED);
                    finish();

                }

                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            dialogProgress.dismiss();
                        } catch (Exception e) {
                            Log.e("Class ", "My Exe ", e);
                        }
                    }

                });

            }

        }).start();
    }

}
