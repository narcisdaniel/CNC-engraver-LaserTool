package com.example.lasertool;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class ConnectedActivity extends Activity{

    Handler bluetoothIn;
    int[][] newimg;
    ImageView imageView;
    private static final int GALLERY_REQUEST_CODE = 123;
    Button btnPick,btnRsz,btnEng,btnTst,btnSnd,btnSave;
    Uri imageData=null;
    String ImagePath;
    Uri URI;
    Bitmap newBitmap,bitmap1;
    TextView resolution;
    boolean ok;
    EditText fieldWidth,fieldHeight;
    int nWidth,nHeight;


    final int handlerState = 0;        				 //used to identify handler message
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();

    private ConnectedThread mConnectedThread;

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address
    private static String address;

    @SuppressLint("HandlerLeak")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.connected_activity);
        Toast.makeText(getBaseContext(), "Connected!", Toast.LENGTH_LONG).show();



        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.logor);

        resolution=(TextView) findViewById(R.id.textView);

        btnPick = (Button) findViewById(R.id.uploadButton);
        btnRsz = (Button) findViewById(R.id.resizeButton);
        btnEng = (Button) findViewById(R.id.engraveButton);
        btnTst = (Button) findViewById(R.id.testButton);
        btnSnd = (Button) findViewById(R.id.sendButton);
        btnSave = (Button) findViewById(R.id.saveButton);


        btnPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                Intent intent= new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Pick an image"), GALLERY_REQUEST_CODE);

            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(newBitmap!=null){
                    ImagePath = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), newBitmap, "LaserTool_" + System.currentTimeMillis(), "LaserTool_" + System.currentTimeMillis());

                    URI = Uri.parse(ImagePath);
                    Toast.makeText(getBaseContext(), "Saved", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(getBaseContext(), "You need to upload an image.", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnRsz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                dialogResize();

            }
        });

        btnTst.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("T");

            }
        });
        btnEng.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("E");

            }
        });
        btnSnd.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                sendImage();
            }
        });


        bluetoothIn = new Handler() {
            @SuppressLint("HandlerLeak")
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {
                    String readMessage = (String) msg.obj;
                    if(readMessage.substring(0,1).equals("Q"))
                        Toast.makeText(getBaseContext(), "Process failed!", Toast.LENGTH_SHORT).show();
                    if(readMessage.substring(0,1).equals("W"))
                        Toast.makeText(getBaseContext(), "Successfully sent!", Toast.LENGTH_SHORT).show();
                    if(readMessage.substring(0,1).equals("T"))
                        Toast.makeText(getBaseContext(), "Process started!", Toast.LENGTH_SHORT).show();
                    if(readMessage.substring(0,1).equals("E"))
                        Toast.makeText(getBaseContext(), "Engraving started!", Toast.LENGTH_SHORT).show();
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();

    }
    public void sendImage(){
        int toggle=1;
        int delay=0;
        if(nHeight==9 || nWidth==0) {
            Toast.makeText(getBaseContext(), "You need to upload an image.", Toast.LENGTH_LONG).show();
        }
        else{


            mConnectedThread.write("R" + nWidth + ";" + nHeight + ";");

            for(int i=nHeight-1;i>=0;i--){
                if(toggle==1) {
                    for (int j = 0; j < nWidth; j++) {
                        delay++;
                        if (Color.red(newBitmap.getPixel(j, i)) > 127) {
                            mConnectedThread.write("0");
                        } else {
                            mConnectedThread.write("1");
                        }
                        if(delay==25){
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            delay=0;
                        }
                    }
                }
                else{
                    for (int j = nWidth-1; j>= 0; j--) {
                        delay++;
                        if (Color.red(newBitmap.getPixel(j, i)) > 127) {
                            mConnectedThread.write("0");
                        } else {
                            mConnectedThread.write("1");
                        }
                        if(delay==25){
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            delay=0;
                        }
                    }
                }
                if(toggle==1)
                    toggle=0;
                else
                    toggle=1;

            }
            mConnectedThread.write(";");

        }
    }

    public void dialogResize(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);


        final LayoutInflater inflater = getLayoutInflater();
        final View v=inflater.inflate(R.layout.resize_dialog, null);



        builder.setView(v)
                // action buttons
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        nWidth=0;
                        nHeight=0;

                        fieldWidth=(EditText) v.findViewById(R.id.width);
                        fieldHeight=(EditText) v.findViewById(R.id.height);

                        String sW=fieldWidth.getText().toString();
                        String sH=fieldHeight.getText().toString();
                        try {

                            nHeight = Integer.parseInt(sH.replaceAll("[\\D]", ""));
                        }catch (NumberFormatException nfe) {
                            nHeight=0;

                        }
                        try {
                            nWidth = Integer.parseInt(sW.replaceAll("[\\D]", ""));

                        }catch (NumberFormatException nfe) {
                            nWidth=0;

                        }

                        if(bitmap1!=null) {
                            if (nWidth == 0) {
                                if (nHeight == 0) {
                                    Toast.makeText(getBaseContext(), "Invalid input.", Toast.LENGTH_LONG).show();
                                } else {
                                    nWidth = (bitmap1.getWidth() * nHeight) / bitmap1.getHeight();
                                }

                            }else{
                                if(nHeight==0)
                                    nHeight=(bitmap1.getHeight()*nWidth)/bitmap1.getWidth();
                            }
                            if(nHeight!=9 && nWidth!=0) {
                                bitmap1 = Bitmap.createScaledBitmap(bitmap1, nWidth, nHeight, true);

                                newBitmap = dithering(bitmap1);
                                imageView.setImageBitmap(newBitmap);

                                resolution.setText(newBitmap.getWidth() + "x" + newBitmap.getHeight());
                            }
                        }
                        else{
                            Toast.makeText(getBaseContext(), "You need to upload an image.", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // remove the dialog from the screen
                    }
                })
                .show();

    }

    public Bitmap dithering(Bitmap original){
        Bitmap finalImage = Bitmap.createBitmap(original.getWidth(),original.getHeight(),original.getConfig());
        newimg=new int[original.getWidth()][original.getHeight()];
        int colorPixel;
        int width=original.getWidth();
        int height= original.getHeight();
        nWidth=width;
        nHeight=height;
        for (int y=0;y<height;y++) {
            for (int x=0;x<width;x++) {
                colorPixel= original.getPixel(x,y);

                int a= Color.alpha(colorPixel);
                int r = Color.red(colorPixel);
                int g = Color.green(colorPixel);
                int b = Color.blue(colorPixel);
                int gr = (r+g+b)/3;
                newimg[x][y]=gr;
            }
        }

        int newpix,err;

        for (int y=0;y<height-1;y++) {
            for (int x=1;x<width-1;x++) {
                if(newimg[x][y]>127)
                    newpix=255;
                else
                    newpix=0;
                err=newimg[x][y]-newpix;
                newimg[x][y]=newpix;
                newimg[x+1][y]+=(int)(err*7.0/16.0);
                newimg[x-1][y+1]+=(int)(err*3.0/16.0);
                newimg[x][y+1]+=(int)(err*5.0/16.0);
                newimg[x+1][y+1]+=(int)(err*1.0/16.0);
            }
        }
        for (int y=0;y<height;y++) {
            for (int x=0;x<width;x++) {
                if(newimg[x][y]>127)
                    newimg[x][y]=255;
                else
                    newimg[x][y]=0;
            }
        }
        for (int y=0;y<height;y++) {
            for (int x=0;x<width;x++) {
                if(newimg[x][y]>=0 && newimg[x][y]<256) {
                    finalImage.setPixel(x,y, Color.argb(255, newimg[x][y],newimg[x][y], newimg[x][y]));
                }
            }
        }

        return finalImage;
    }

    @SuppressLint("SetTextI18n")
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == GALLERY_REQUEST_CODE && resultCode== RESULT_OK && data!= null ){
            imageData = data.getData();


            try {
                bitmap1 = MediaStore.Images.Media.getBitmap(getContentResolver(), imageData);
            } catch (IOException e) {
                e.printStackTrace();
            }

            newBitmap = dithering(bitmap1);
            imageView.setImageBitmap(newBitmap);
            resolution.setText(newBitmap.getWidth()+"x"+newBitmap.getHeight());
            ok=false;


        }
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    @Override
    public void onResume() {
        super.onResume();

        //Get MAC address from DeviceListActivity via intent
        Intent intent = getIntent();

        //Get the MAC address from the DeviceListActivty via EXTRA
        address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

        //create device and set the MAC address
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try
            {
                btSocket.close();
            } catch (IOException e2)
            {
                //insert code to deal with this
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();


        mConnectedThread.write("x");
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "Device does not support bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }


    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }


        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // looping to listen  received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);        	//read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
                finish();

            }
        }
    }

}
