package com.example.printersetup;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;



public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice bluetoothDevice;
    BluetoothSocket bluetoothSocket;

    OutputStream outputStream;
    InputStream inputStream;
    Thread thread;
    public static final String LINE_ANALOGICS = "------------------------";

    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;

    TextView lblPrinterName;
    EditText textBox;
    Bitmap bitmap;
    ArrayList<String> printerValues = new ArrayList<>();
    private Bitmap m_bmp_image;

    private ArrayList <DeviceItem>deviceItemList;
    private ListView mListView;
    private ArrayAdapter<DeviceItem> mAdapter;
    ProgressDialog progressDoalog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            new AlertDialog.Builder(this)
                    .setTitle("Not compatible")
                    .setMessage("Your phone does not support Bluetooth")
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, 124);
        }



        Button btnConnect =  findViewById(R.id.btnConnect);
        Button btnDisconnect =  findViewById(R.id.btnDisconnect);
        Button btnPrint =  findViewById(R.id.btnPrint);
        Button btnchoose =  findViewById(R.id.btnchoose);

        textBox =  findViewById(R.id.txtText);

        lblPrinterName =  findViewById(R.id.lblPrinterName);

        bitmap=textToBitmap();

        progressDoalog = new ProgressDialog(MainActivity.this);
        progressDoalog.setMessage("Its loading....");
        progressDoalog.setTitle("ProgressDialog bar example");
        //progressDoalog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDoalog.setCancelable(false);
        progressDoalog.setCanceledOnTouchOutside(false);
        //progressDoalog.show();

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    //Intent intent=new Intent(MainActivity.this,Main2Activity.class);
                    //startActivity(intent);
//                    progressDoalog = new ProgressDialog(MainActivity.this);
//                    progressDoalog.setMessage("Its loading....");
//                    progressDoalog.setTitle("ProgressDialog bar example");
//                    //progressDoalog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//                    progressDoalog.setCancelable(false);
//                    progressDoalog.setCanceledOnTouchOutside(false);
                    //if (bluetoothAdapter.isDiscovering()) {
                        // the button is pressed when it discovers, so cancel the discovery
                      //  bluetoothAdapter.cancelDiscovery();
                   // }

                    //deviceItemList.clear();
                    //bluetoothAdapter.startDiscovery();
                    FindBluetoothDevice();
                    openBluetoothPrinter();

                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });
        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    disconnectBT();
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });
        btnPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    /*Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("application/pdf");
                    startActivityForResult(intent,123);*/  //for select pdf files from mobiles

                    //sendData();
                    printData();
                    //senddatatodevice();
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });

        btnchoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    if (isStoragePermissionGranted()) {
                        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                        photoPickerIntent.setType("image/*");
                        startActivityForResult(photoPickerIntent, 123);
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });
        /*BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inDither = false;
        m_bmp = BitmapFactory.decodeResource(getResources(),R.drawable.printerlogo,options);
        m_BmpParser = new ParseBitmap(m_bmp);*/



        mListView =  findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);
        printerLSit();
        mAdapter.clear();
        //IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        //registerReceiver(bReciever, filter);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mReceiver, filter);
        bluetoothAdapter.startDiscovery();

    }

    @SuppressLint("HandlerLeak")
    Handler handle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            progressDoalog.incrementProgressBy(1);
        }
    };

    private void printerLSit() {
        deviceItemList = new ArrayList<>();

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                DeviceItem newDevice= new DeviceItem(device.getName(),device.getAddress(),"false");
                deviceItemList.add(newDevice);
            }
        }

        // If there are no devices, add an item that states so. It will be handled in the view.
        if(deviceItemList.size() == 0) {
            deviceItemList.add(new DeviceItem("No Devices", "", "false"));
        }

        Log.d("DEVICELIST", "DeviceList populated\n: "+deviceItemList.size());

        mAdapter = new DeviceListAdapter(MainActivity.this, deviceItemList, bluetoothAdapter);

        Log.d("DEVICELIST", "Adapter created\n");
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            switch (action) {
                case BluetoothDevice.ACTION_FOUND:
                    //Device found
                     System.out.println("CHECK_BLE_DEVICE: FOUND"+device.getName());
                    break;
                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    //Device is now connected
                    System.out.println("CHECK_BLE_DEVICE: ACTION_ACL_CONNECTED"+device.getName());

                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    //Done searching
                    System.out.println("CHECK_BLE_DEVICE: ACTION_DISCOVERY_FINISHED"+device.getName());
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED:
                    //Device is about to disconnect
                    System.out.println("CHECK_BLE_DEVICE: ACTION_ACL_DISCONNECT_REQUESTED"+device.getName());
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    //Device has disconnected
                    System.out.println("CHECK_BLE_DEVICE: ACTION_ACL_DISCONNECTED"+device.getName());
                    break;
            }
        }
    };

    void FindBluetoothDevice(){
        try{
            if(bluetoothAdapter==null){
                lblPrinterName.setText("No Bluetooth Adapter found");
            }
            if(!bluetoothAdapter.isEnabled()){
                Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBT,0);
            }
            Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();
            if(pairedDevice.size()>0){
                for(BluetoothDevice pairedDev:pairedDevice){
                    System.out.println("CHECK_PAIRED_DEVICES: "+pairedDev.getName());
                    System.out.println("CHECK_PAIRED_DEVICES 1: "+pairedDev.getAddress());
                    System.out.println("CHECK_PAIRED_DEVICES 2: " + pairedDev.getUuids()[0]);

                    // My Bluetoth printer name is BTP_F09F1A
                    if(pairedDev.getName().equals("XXZSJ192100207")){
                        System.out.println("CHECK_PAIRED_DEVICES 31: "+pairedDev.getName());
                        System.out.println("CHECK_PAIRED_DEVICES 32: "+pairedDev.getAddress());
                        System.out.println("CHECK_PAIRED_DEVICES 33: "+pairedDev.getType());
                        System.out.println("CHECK_PAIRED_DEVICES 34: "+pairedDev.getBluetoothClass().toString());
                        System.out.println("CHECK_PAIRED_DEVICES 35: "+pairedDev.getBondState());
                        System.out.println("CHECK_PAIRED_DEVICES 36: " + pairedDev.getUuids()[0]);
                        /*for (ParcelUuid uuuuu : pairedDev.getUuids()) {
                            System.out.println("CHECK_PAIRED_DEVICES 37: " + uuuuu);
                        }*/
                        bluetoothDevice=pairedDev;
                        lblPrinterName.setText("Bluetooth Printer Attached: "+pairedDev.getName());
                        break;
                    }
                    /*if(pairedDev.getName().equals("EZ320")){
                        System.out.println("CHECK_PAIRED_DEVICES 3: " + pairedDev.getUuids()[0]);
                        bluetoothDevice=pairedDev;
                        lblPrinterName.setText("Bluetooth Printer Attached: "+pairedDev.getName());
                        break;
                    }*/

                 /*   if (pairedDev.getName().equals("TM-P20_013499")){
                        System.out.println("CHECK_PAIRED_DEVICES 1: "+pairedDev.getUuids().length);
                        System.out.println("CHECK_PAIRED_DEVICES 2: "+pairedDev.getUuids()[0]);
                        System.out.println("CHECK_PAIRED_DEVICES 3: "+pairedDev.getUuids()[1]);
                        bluetoothDevice=pairedDev;
                        lblPrinterName.setText("Bluetooth Printer Attached: "+pairedDev.getName());
                        break;
                    }*/
                }


            }else {
                System.out.println("CHECK_PAIRED_DEVICES 38");
                lblPrinterName.setText("Bluetooth Printer Attached");
            }
        }catch(Exception ex){
            System.out.println("CHECK_PAIRED_DEVICES 39"+ex.toString());
            ex.printStackTrace();
        }
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED&&checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("TAG","Permission is granted");
                return true;
            } else {

                Log.v("TAG","Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("TAG","Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==124){
            System.out.println("CHECK_PERMISSION: ");
            printerLSit();
        }else {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.v("TAG", "Permission: " + permissions[0] + "was " + grantResults[0]);
                //resume tasks needing this permission
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("CHECK_PAIRED_DEVICES: REQUESTCODE:"+requestCode);
        if (requestCode==0){
            try {
                Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();
                if (pairedDevice.size() > 0) {
                    for (BluetoothDevice pairedDev : pairedDevice) {
                        System.out.println("CHECK_PAIRED_DEVICES 0: " + pairedDev.getType());

                        System.out.println("CHECK_PAIRED_DEVICES: " + pairedDev.getName());
                        System.out.println("CHECK_PAIRED_DEVICES 1: " + pairedDev.getUuids()[0]);

                        //00:22:58:39:CB:EB
                        // My Bluetoth printer name is BTP_F09F1A

                       /* if(pairedDev.getName().equals("EZ320")){
                            bluetoothDevice=pairedDev;
                            lblPrinterName.setText("Bluetooth Printer Attached: "+pairedDev.getName());
                            break;
                        }*/

                         if(pairedDev.getName().equals("XXZSJ192100207")){
                            bluetoothDevice=pairedDev;
                            lblPrinterName.setText("Bluetooth Printer Attached: "+pairedDev.getName());
                            break;
                        }
                        /*if (pairedDev.getName().equals("TM-P20_013499")) {
                            System.out.println("CHECK_PAIRED_DEVICES 1: " + pairedDev.getUuids().length);
                            System.out.println("CHECK_PAIRED_DEVICES 2: " + pairedDev.getUuids()[0]);
                            System.out.println("CHECK_PAIRED_DEVICES 3: " + pairedDev.getUuids()[1]);
                            bluetoothDevice = pairedDev;
                            lblPrinterName.setText("Bluetooth Printer Attached: " + pairedDev.getName());
                            break;
                        }*/
                    }


                } else {
                    lblPrinterName.setText("Bluetooth Printer Attached");
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }else if (requestCode==123){
            if (resultCode == RESULT_OK) {
                try {
                    if (data!=null){
                        System.out.println("CHECK_BITMAP1");
                        if (data.getData()!=null) {
                            System.out.println("CHECK_BITMAP2: " + data.getData());
                            final Uri imageUri = data.getData();
                            System.out.println("CHECK_BITMAP3: " + imageUri);
                            final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bmpp = BitmapFactory.decodeStream(imageStream);

                            /*BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inScaled = false;
                            options.inDither = false;*/
                            // m_BmpParser = new ParseBitmap(selectedImage);
                            System.out.println("CHECK_BITMAP4: 1 WIDTH: "+ bmpp.getWidth() + " HEIGHT: " + bmpp.getHeight());
                            m_bmp_image= scaleDown(bmpp,300,true);
                            System.out.println("CHECK_BITMAP4: WIDTH: "+ m_bmp_image.getWidth() + " HEIGHT: " + m_bmp_image.getHeight());

                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                }

            }else {
                Toast.makeText(MainActivity.this, "You haven't picked Image",Toast.LENGTH_LONG).show();
            }
        }else if (requestCode==124){
            System.out.println("CHECK_BITMAP1: 122");
            printerLSit();
        }
    }

    public Bitmap scaleDown(Bitmap realImage, float maxImageSize, boolean filter) {
        float ratio = Math.min(maxImageSize / realImage.getWidth(), maxImageSize / realImage.getHeight());
        int width=realImage.getHeight();
        if (realImage.getHeight()>300){
            width = 300;
        }
        int height = Math.round( ratio * realImage.getHeight());
        return Bitmap.createScaledBitmap(realImage, width, realImage.getHeight(), filter);
    }

    void openBluetoothPrinter() {
        try{
            System.out.println("CHECK_PAIRED_DEVICES 39");

            //Standard uuid from string //    00001101-0000-1000-8000-00805f9b34fb
            //UUID uuidSting = UUID.fromString("00000000-deca-fade-deca-deafdecacaff");
            UUID uuidSting = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            bluetoothSocket=bluetoothDevice.createRfcommSocketToServiceRecord(uuidSting);
            bluetoothSocket.connect();
            outputStream=bluetoothSocket.getOutputStream();
            inputStream=bluetoothSocket.getInputStream();
            //beginListenData();
        }catch (Exception ex){
            System.out.println("CHECK_PAIRED_DEVICES 39"+ex.toString());
            ex.printStackTrace();
        }
    }

    void beginListenData(){
        try{
            System.out.println("CHECK_PAIRED_DEVICES 71");
            final Handler handler =new Handler();
            final byte delimiter=10;
            stopWorker =false;
            readBufferPosition=0;
            readBuffer = new byte[1024];

            thread=new Thread(new Runnable() {
                @Override
                public void run() {
                    //System.out.println("CHECK_PAIRED_DEVICES 72");
                    while (!Thread.currentThread().isInterrupted() && !stopWorker){
                       // System.out.println("CHECK_PAIRED_DEVICES 73");
                        try{
                           // System.out.println("CHECK_PAIRED_DEVICES 74");
                            readFully(inputStream,readBuffer,readBufferPosition,readBufferPosition);
                            int byteAvailable = inputStream.available();
                            if(byteAvailable>0){
                                System.out.println("CHECK_PAIRED_DEVICES 75");
                                byte[] packetByte = new byte[byteAvailable];
                                inputStream.read(packetByte);
                                for(int i=0; i<byteAvailable; i++){
                                   // System.out.println("CHECK_PAIRED_DEVICES 76");
                                    byte b = packetByte[i];
                                    if(b==delimiter){
                                       // System.out.println("CHECK_PAIRED_DEVICES 77");
                                        byte[] encodedByte = new byte[readBufferPosition];
                                        System.arraycopy(
                                                readBuffer,0,
                                                encodedByte,0,
                                                encodedByte.length
                                        );
                                        String data = "";
                                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                                            data = new String(encodedByte, StandardCharsets.US_ASCII);
                                        }
                                        readBufferPosition=0;
                                        final String finalData = data;
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                System.out.println("CHECK_PAIRED_DEVICES 78");
                                                lblPrinterName.setText("success");
                                             }
                                        });
                                    }else{
                                        System.out.println("CHECK_PAIRED_DEVICES 79");
                                        readBuffer[readBufferPosition++]=b;
                                    }
                                }
                            }
                        }catch(Exception ex){
                            System.out.println("CHECK_PAIRED_DEVICES 80"+ex.toString());
                            stopWorker=true;
                        }
                    }

                }
            });

            thread.start();
        }catch (Exception ex){
            System.out.println("CHECK_PAIRED_DEVICES 81"+ex.toString());
            ex.printStackTrace();
        }
    }
    public static void readFully(InputStream in, byte[] b, int off, int len) throws IOException {
        if (len < 0) {
            throw new IndexOutOfBoundsException();
        }
        int n = 0;
        while (n < len) {
            final int count = in.read(b, off + n, len - n);
            if (count < 0) {
                throw new EOFException();
            }
            n += count;
        }
    }

    public static int readByte (byte [] bytes, int offset) {
        return ((int) bytes [offset]) & 0xFF;
    }

    public static short readShort(byte [] bytes, int offset) {
        return (short)
                (readByte(bytes, offset) << 8 |
                        readByte(bytes, offset + 1));
    }


    void printData() {
        try{
            /*Intent  intent=new Intent(MainActivity.this,BluetoothPrinterActivity.class);
            startActivity(intent);*/

            if (textBox.getText().toString().length()>0) {
                System.out.println("CHECK_PAIRED_DEVICES 44:"+textBox.getText().toString());
                //Drawable d = getResources().getDrawable(R.drawable.download);   // for images print
                //Bitmap bitmap = ((BitmapDrawable)d).getBitmap();// for images print

                String msg = textBox.getText().toString();
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {  //for text
                    msg = System.lineSeparator()+msg+System.lineSeparator()+System.lineSeparator()+System.lineSeparator();
                }
                /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {  // for images print   // other printers
                    msg = BitMapToString(bitmap)+System.lineSeparator()+System.lineSeparator()+System.lineSeparator()+System.lineSeparator();
                }*/
               // System.out.println("CHECK_PAIRED_DEVICES 45: "+msg); // other printers

                progressDoalog.show();

               String str = ParseBitmap.ExtractGraphicsDataForCPCL(m_bmp_image); //for CPCL Programming college
               msg+= "\n"+"! 0 100 "+(m_bmp_image.getWidth()+50)+" "+(m_bmp_image.getHeight()+50)+" 1\r\n" + str+"\n"+ "\n"+ "PRINT\r\n";  //for CPCL Programming college
               //msg+=str;  // other printerss

                beginListenData();
                outputStream.write(msg.getBytes());

                new Thread(() -> {
                    try {
                        long timeout=6000;
                        if (m_bmp_image.getHeight()>700){
                            timeout=12000;
                        }
                        Thread.sleep(timeout);
                        progressDoalog.dismiss();
                    } catch (Exception e) {
                        progressDoalog.dismiss();
                        e.printStackTrace();
                    }
                }).start();

                /*Timer timerObj = new Timer();
                final String finalMsg = msg;
                TimerTask timerTaskObj = new TimerTask() {
                    public void run() {
                        if(finalMsg.getBytes().length>0){
                            System.out.println("PRINTING HAS VALUES 1");
                            //Toast.makeText(MainActivity.this, "Printing is not compeleted", Toast.LENGTH_SHORT).show();
                        }else {
                            System.out.println("PRINTING HAS VALUES 2");
                            //Toast.makeText(MainActivity.this, "Printing is compeleted", Toast.LENGTH_SHORT).show();
                        }
                    }
                };
                timerObj.schedule(timerTaskObj, 0, 1000);*/

                //for sample reciept
                /*String BILL = "";

                BILL = "\nInvoice No: ABCDEF28060000005" + "    "
                        + "04-08-2011\n";
                BILL = BILL
                        + "-----------------------------------------";
                BILL = BILL + "\n\n";
                BILL = BILL + "Total Qty:" + "      " + "2.0\n";
                BILL = BILL + "Total Value:" + "     "
                        + "17625.0\n";
                BILL = BILL
                        + "-----------------------------------------\n";
                outputStream.write(BILL.getBytes());
                //This is printer specific code you can comment ==== > Start

                // Setting height
                int gs = 29;
                outputStream.write(intToByteArray(gs));
                int h = 104;
                outputStream.write(intToByteArray(h));
                int n = 162;
                outputStream.write(intToByteArray(n));

                // Setting Width
                int gs_width = 29;
                outputStream.write(intToByteArray(gs_width));
                int w = 119;
                outputStream.write(intToByteArray(w));
                int n_width = 2;
                outputStream.write(intToByteArray(n_width));

                // Print BarCode
                int gs1 = 29;
                outputStream.write(intToByteArray(gs1));
                int k = 107;
                outputStream.write(intToByteArray(k));
                int m = 73;
                outputStream.write(intToByteArray(m));

                String barCodeVal = "ASDFC028060000005";// "HELLO12345678912345012";
                System.out.println("Barcode Length : "
                        + barCodeVal.length());
                int n1 = barCodeVal.length();
                outputStream.write(intToByteArray(n1));

                for (int i = 0; i < barCodeVal.length(); i++) {
                    outputStream.write((barCodeVal.charAt(i) + "").getBytes());
                }
*/
                lblPrinterName.setText("Printing Text...");
                textBox.setText("");
            }else {
                System.out.println("CHECK_PAIRED_DEVICES 46: ");
                Toast.makeText(this, "Please enter the text to print", Toast.LENGTH_SHORT).show();
            }

            /*int headerLn =0 ;
            String headerLabel = "^XA^POI^LH250";
            int sizeoflatestContent = printerValues.size();
            printerValues.add(headerLabel);
            headerLn = headerLn+30;
            String LOGOS="^FO50,50^GFA,4050,4050,27,,:::::::::::::::::X0E,W038,W0E,V03801,V0F0038,U03E007C,U07C00FC,T01FI0FC,T03EI0FE,T0FC001FE,S01F8001FE,S03FI03FE,S0FEI03FE,R01FCI03FF,R03F8I07FF,R07FJ07FF,R0FEJ07FF,Q03FCJ07FF,Q07F8J0IF,Q0FFK0IF8,P01FEK0IF8,P03FCK0IF8,P07F8K07FF8,:P0FFL07FF8,O01FEL07FF8,O03FEL07FF8,O07FCL07FF8,O07F8L03FF8,O0FF8L03FF8,N01FFM03FF8,N03FEM03FF8,N03FEM01FF8,N07FCM01FF8,N0FFCM01FF8,N0FF8M01FF8,M01FF8M01FF8,M01FFO0FF8,M03FFO0FF8,:M07FEO0FF8,M07FEO0FFL0IF,M0FFCO0FFK0KF07IFCK07FFE,M0FFCO0FFJ03KF87JF8I01JF,L01FFCO0FFJ0LF83JFEI07JF,L01FF8O0FFI03FF007F83KFI0KF,L01FF8O0FFI07F8I0783F00FFC01FE01F,L03FF8O0FE001FEJ0183F001FC03F8007,L03FF8O0FE003F8L03FI0FE03F,L03FF8O0FE007FM03FI07F07E,L07FFP0FE00FEM03FI07F07E,L07FFP0FC00FCM03FI03F0FC,L07FFP0FC01F8M03FI03F8FC,L0IFP0FC03F8M03FI03F8FC,L0IFP0FC03FN03FI03F8FC,L0IFP0F807EN03FI03F8FE,L0IFO01F807EN03FI03F07E,L0IFM0601F80FEN03FI03F07F,K01IFM0E01F00FCN03FI07F07F8,K01IFL01E01F00FCN03FI07E03FC,K01IFL07C03E00FCN03FI0FE01FE,K01IFK01FC03E01FCN03F001FC00FF,K01IFJ01FF803C01FCN03F007F8007FC,K01IFJ0IF807C01FCN03F01FFI03FF,K01IFI03IF007801FCN03F1FFCI01FF8,K01IFI07IF00F801FCN03JF8J0FFE,K01IF001JF00F001FCN03IFCK03FF,K01IF003IFE01F001FCN03IF8L0FFC,K01IF003IFC01E001FCN03F7F8L03FE,K01IF007IFC03C001FCN03F3FCL01FF,K01IF00JF803C001FCN03F3FCM07F,K01IF80JF8078I0FCN03F1FEM03F8,K01IF81JF00FJ0FEN03F0FFM01F8,K01IF81IFE01FJ0FEN03F07F8L01FC,L0IFC1IFC03EJ0FEN03F03F8M0FC,L0IFC1IFC07CJ07FN03F03FCM0FC,L0IFC1IF80F8J07F8M03F01FEM0FC,L0IFE1IF01FK03F8M03F00FFM0FC,L07IF0FFC03EK03FCM03F007F8L0FC,L07IF07F80FCK01FEM03F003FCL0FC,L03IF81C01F8L0FFM03F001FEK01F8,L01IFCI07FM07F8L03FI0FFK01F8,L01JF003FEM07FCK083FI07F8J03F,M0JFE7FFCM01FFJ0383FI03FEJ07F,M07MFO0FFE001F83FI01FFJ0FE,M03LFEO07IF7FF83FJ0FFC003FC,M01LF8O01LF87FJ03FFC1FF98,N0KFEQ07KF87F8J0KFE1C,N03JF8R0JFC04L03JF818,O0IFCT01CQ07FFC01C,,:::::L018J0800600103S0CK01030018,L04C113C82198810CC0B19204C99034C9B6090C9224,L08030208220C811801990204C18020C1921930104,K0100382082404811I0890204C18020C11A1120104,K0100682082406813001990204C18030C19I120102,K01004C20FE406812001F1E204F9B018F9E121201E18,K01007C2082406813I0E10204C1800CC1E12120100C,K0100862082604813I0B10204C18006C1A0E1201004,K018082208260C811801990204C18004C1B0C1301004,L0C5832082318C90CC1899324C1802CC188C118914C,L0390320820E0F9078085F3C4790038F9I01071E7,,::::::::::::::::::::::::^FS";
            headerLabel = "^CI28^FO100,%d" + "\r\n"+ "^A0,20,20" + "\r\n" + "^FH^FD%s^FS" + "\r\n";
            printerValues.add(String.format(headerLabel,headerLn,formatCenterAlignString(LOGOS,90)));

            //headerLn = headerLn+250;
            //headerLabel = "^CI28^FO100,%d" + "\r\n"+ "^A0,20,20" + "\r\n" + "^FH^FD%s^FS" + "\r\n";
            //printerValues.add(String.format(headerLabel,headerLn,formatCenterAlignString(OfflinePlugin.printerValSet.getDistribution(),30)));
            headerLn = headerLn+30;
            headerLabel =  "^CI28^FO25,%d" + "\r\n"+ "^A0,20,20" + "\r\n" + "^FH^FD%s^FS" + "\r\n";
            printerValues.add(String.format(headerLabel,headerLn,""));
            headerLabel =  "^XZ";
            printerValues.add(headerLabel);
            printerValues.set(sizeoflatestContent, String.format("^XA^POI^LL%d", headerLn));

            run();*/
        }catch (Exception ex){
            System.out.println("CHECK_PAIRED_DEVICES 47: "+ex.toString());
            ex.printStackTrace();
        }
    }
    public static byte intToByteArray(int value) {
        byte[] b = ByteBuffer.allocate(4).putInt(value).array();

        for (int k = 0; k < b.length; k++) {
            System.out.println("Selva  [" + k + "] = " + "0x"
                    + UnicodeFormatter.byteToHex(b[k]));
        }

        return b[3];
    }
/*
    private void printImageReciept() throws Exception{
        String pathName = "/storage/emulated/0/AppFiles/print.jpg";
        int width = 0;
        int level =50;
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = false;
        opts.inSampleSize = 1;
        opts.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeFile(pathName, opts);
        try {
            // sleep(1000);
            if (bitmap == null) {
                return;
            }
            int MAX_WIDTH = 500;
            if (width == -1) {
                width = MAX_WIDTH;
            } else if ((width == 0) || (width < 0)) {
                width = bitmap.getWidth();
            }
            if (width > MAX_WIDTH) {
                width = MAX_WIDTH;
            }
            byte[] printerData = null;

            printerData = BitmapManager.bitmap2printerData(bitmap, width, level, 0);
            int height = BitmapManager.getBitmapHeight(bitmap, width);

            int capacity = Command.ALIGNMENT_LEFT.length + Command.RASTER_BIT_IMAGE_NORMAL.length + 4 + printerData.length;
            final ByteBuffer buffer = ByteBuffer.allocate(capacity);
            buffer.put(Command.ALIGNMENT_LEFT);
            buffer.put(Command.RASTER_BIT_IMAGE_NORMAL);
            int widthBytes = BitmapManager.bytesOfWidth(width);
            buffer.put((byte)(widthBytes % 256));
            buffer.put((byte)(widthBytes / 256));
            buffer.put((byte)(height % 256));
            buffer.put((byte)(height / 256));
            buffer.put(printerData);

            Toast.makeText(getApplicationContext(), "Invoice Sent to Printer. Please wait...", Toast.LENGTH_LONG).show();
            Thread connectNewThread = new Thread(new Runnable() {

                public void run() {
                    try {

                        mbtOutputStream.write(buffer.array());
                        mbtOutputStream.flush();
                    } catch(IOException ex) {

                        try {
                            if(mbtSocket!=null){
                                mbtSocket.close();
                                mbtSocket = null;
                                StartBluetoothConnection(totalInvoiceVO.toString());
                            }
                        } catch (IOException e) {
                            // TODO Auto-generated catch block

                            e.printStackTrace();
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    } finally {
                        runOnUiThread(new Runnable() {

                            public void run() {
                            }
                        });
                    }
                }
            });

            connectNewThread.start();

        } catch (Exception ioe) {

            throw ioe;

        }
    }
*/

    private Bitmap textToBitmap() {
        QRCodeWriter writer = new QRCodeWriter();
        Bitmap bmp = null;
        try {
            BitMatrix bitMatrix = writer.encode("ZEBRA PRINTER", BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            /*Path path = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
              //  path = FileSystems.getDefault().getPath(filePath);
            }
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);*/
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return bmp;
    }

    // Disconnect Printer //
    void disconnectBT() {
        try {
            stopWorker=true;
            outputStream.close();
            inputStream.close();
            bluetoothSocket.close();
            lblPrinterName.setText("Printer Disconnected.");
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }


    public void printPhoto(Context context) {
        try {
            Bitmap bmp = ((BitmapDrawable)getResources().getDrawable(R.drawable.download)).getBitmap();
            //Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.download);
            if(bmp!=null){
                byte[] command = Utils.decodeBitmap(bmp);
                outputStream.write(PrinterCommands.FEED_LINE);
                printText(command);
            }else{
                Log.e("Print Photo error", "the file isn't exists");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PrintTools", "the file isn't exists");
        }
    }

    //print byte[]
    private void printText(byte[] msg) {
        try {
            // Print normal text
            outputStream.write(msg);
            printNewLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //print new line
    private void printNewLine() {
        try {
            outputStream.write(PrinterCommands.FEED_LINE);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void run() {

        // for new line
        byte[] rf_newLine = new byte[1];
        rf_newLine[0] = 0x0A;

        // for normal font
        byte[] rf_normalFont = new byte[1];
        rf_normalFont[0] = 0x13;

        // for Double Height & Width
        byte[] rf_DbleHght_DblWidth = new byte[1];
        rf_DbleHght_DblWidth[0] = 0x12;

        // for Double Height
        byte[] rf_DblHght = new byte[1];
        rf_DblHght[0] = 0x1C;


        try {
            ArrayList<String> datas = new ArrayList<String>();
            datas =printerValues;

            outputStream.flush();

            outputStream.write(rf_DbleHght_DblWidth);
            outputStream.flush();

            //outputStream.write(datas.get(0).getBytes());
            outputStream.write(datas.get(1).getBytes());
            //outputStream.write(datas.get(2).getBytes());
            outputStream.write("\n\n".getBytes());
            outputStream.flush();

            //outputStream.write(rf_normalFont);
            //outputStream.flush();

            //outputStream.write(rf_newLine);
            //outputStream.flush();

            outputStream.write(rf_DblHght);
            outputStream.flush();

            outputStream.write(datas.get(3).getBytes());
            outputStream.write(rf_newLine);
            outputStream.flush();

            //outputStream.write(rf_newLine);
            //outputStream.flush();

            outputStream.write(rf_normalFont);
            outputStream.flush();

            outputStream.write(rf_newLine);
            outputStream.flush();

            int count = datas.size();
            for(int i = 4; i < count; i++) {
                outputStream.write(datas.get(i).getBytes());

                if(!datas.get(i).equalsIgnoreCase(LINE_ANALOGICS)) {
                    outputStream.write(rf_newLine);
                }

                outputStream.flush();
            }

            outputStream.write(rf_newLine);
            outputStream.flush();

            outputStream.write(rf_newLine);
            outputStream.flush();


        } catch (IOException e) {
            e.printStackTrace();
        }
        finish();
    }


    public String convertBitmap(Bitmap inputBitmap) {

        String mStatus="";
        int mWidth=100;
        int mHeight=100;
        mWidth = inputBitmap.getWidth();
        mHeight = inputBitmap.getHeight();

        convertArgbToGrayscale(inputBitmap, mWidth, mHeight);
        mStatus = "ok";

        return mStatus;

    }

    public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    private void convertArgbToGrayscale(Bitmap bmpOriginal, int width,
                                        int height) {
        int pixel;
        int k = 0;
        int B = 0, G = 0, R = 0;
        BitSet dots = new BitSet();
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d("DEVICELIST", "onItemClick position: " + position +
                " id: " + id + " name: " + deviceItemList.get(position).getDeviceName() + "\n");
    }
}
