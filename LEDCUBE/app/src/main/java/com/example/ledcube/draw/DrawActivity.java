package com.example.ledcube.draw;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.divyanshu.draw.widget.DrawView;
import com.example.ledcube.R;
import com.example.ledcube.tflite.Classifier;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class DrawActivity extends AppCompatActivity {

    final int handlerState = 0;
    Handler bluetoothIn;

    private static final String MAC_ADDRESS = "98:DA:60:01:C3:10";
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter btAdapter = null;
    private StringBuilder recDataString = new StringBuilder();

    private ConnectingThread mConnectingThread;
    private ConnectedThread mConnectedThread;

    public static int target = 0;
    public int cnt = 0;
    public int now = 0;

    String s;

    Classifier cls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        Button gamestart = findViewById(R.id.gmstart);
        DrawView drawView = findViewById(R.id.drawView);
        Button classifyBtn = findViewById(R.id.classifyBtn);
        LinearLayout btnlayout = findViewById(R.id.buttonLayout);
        TextView ttime = findViewById(R.id.currnttime);
        TextView ccnt = findViewById(R.id.currntcount);


        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask(){
            @Override
            public void run(){
                now++;
                int hour = now / 3600;
                int minute = now / 60;
                int second = now % 60;
                String tmp = String.format("%02d", hour) + ":"+ String.format("%02d", minute) + ":" + String.format("%02d", second);
                ttime.setText("플레이 시간 : " + tmp);

            }
        };


        gamestart.setOnClickListener(view -> {
            btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
            checkBTState();
            gamestart.setVisibility(View.INVISIBLE);
            drawView.setVisibility(View.VISIBLE);
            btnlayout.setVisibility(View.VISIBLE);
            ttime.setVisibility(View.VISIBLE);
            ccnt.setVisibility(View.VISIBLE);
            timer.schedule(timerTask, 0, 1000);
        });

        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {

                if (msg.what == handlerState) {
                    //if message is what we want
                    String readMessage = (String) msg.obj;
                    // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage);

                    if(recDataString.charAt(0) >='0' && recDataString.charAt(0) <='7' ){
                        target = (recDataString.charAt(0) - '0') / 2;
                    }

                //rrr.setText(recDataString);   원하는 텍스트 보내는 부분
                    // Do stuff here with your data, like adding it to the database
                }
                //clear all string data
                recDataString.delete(0, recDataString.length());
            }
        };

        ActionBar ac = getSupportActionBar();
        ac.setTitle("도형 인식 부분");

        drawView.setStrokeWidth(60.0f);
        drawView.setBackgroundColor(Color.WHITE);
        drawView.setColor(Color.BLACK);


        classifyBtn.setOnClickListener(v -> {
            Bitmap image = drawView.getBitmap();

            Pair<Integer, Float> res = cls.classify(image);
            String outStr = String.format(Locale.ENGLISH, "%d, %.0f%%", res.first, res.second * 100.0f);

            drawView.clearCanvas();

            if(res.first == target){
                mConnectedThread.write("2");
                cnt++;
                ccnt.setText("맞힌개수 : " + Integer.toString(cnt));
            }

        });

        Button clearBtn = findViewById(R.id.clearBtn);
        clearBtn.setOnClickListener(v -> {
            drawView.clearCanvas();
        });

        cls = new Classifier(this);
        try {
            cls.init();
        } catch(IOException ioe) {
            Log.d("DigitClassifier", "failed to init Classifier", ioe);
        }





    }

    @Override
    protected void onPause(){
        super.onPause();
        mConnectedThread.write("0");
        mConnectedThread.closeStreams();
        mConnectingThread.closeSocket();

    }

    @Override
    protected void onDestroy() {
        cls.finish();
        super.onDestroy();
    }




    private void checkBTState() {
        if (btAdapter == null) {
            Log.d("BT SERVICE", "BLUETOOTH NOT SUPPORTED BY DEVICE, STOPPING SERVICE");
            return ;
        } else {
            if (btAdapter.isEnabled()) {
                try {
                    BluetoothDevice device = btAdapter.getRemoteDevice(MAC_ADDRESS);
                    mConnectingThread = new ConnectingThread(device);
                    mConnectingThread.start();
                } catch (IllegalArgumentException e) {
                    return ;
                }
            } else {
                return ;
            }
        }
    }

    // New Class for Connecting Thread
    private class ConnectingThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        ConnectingThread(BluetoothDevice device) {
            Log.d("DEBUG BT", "IN CONNECTING THREAD");
            mmDevice = device;
            BluetoothSocket temp = null;
            try {
                temp = mmDevice.createRfcommSocketToServiceRecord(BTMODULEUUID);
            } catch (IOException e) {
            }
            mmSocket = temp;
        }

        @Override
        public void run() {
            super.run();
            Log.d("DEBUG BT", "IN CONNECTING THREAD RUN");
            // Establish the Bluetooth socket connection.
            // Cancelling discovery as it may slow down connection
            btAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
                Log.d("DEBUG BT", "BT SOCKET CONNECTED");
                mConnectedThread = new ConnectedThread(mmSocket);
                mConnectedThread.start();
                Log.d("DEBUG BT", "CONNECTED THREAD STARTED");
                //I send a character when resuming.beginning transmission to check device is connected
                //If it is not an exception will be thrown in the write method and finish() will be called
                mConnectedThread.write("2"); // 그림 맞추기 게임 시작 부분



            } catch (IOException e) {
                try {
                    Log.d("DEBUG BT", "SOCKET CONNECTION FAILED : " + e.toString());
                    Log.d("BT SERVICE", "SOCKET CONNECTION FAILED, STOPPING SERVICE");
                    mmSocket.close();
                    //          stopSelf();
                } catch (IOException e2) {
                    Log.d("DEBUG BT", "SOCKET CLOSING FAILED :" + e2.toString());
                    Log.d("BT SERVICE", "SOCKET CLOSING FAILED, STOPPING SERVICE");
                    //           stopSelf();
                    //insert code to deal with this
                }
            } catch (IllegalStateException e) {
                Log.d("DEBUG BT", "CONNECTED THREAD START FAILED : " + e.toString());
                Log.d("BT SERVICE", "CONNECTED THREAD START FAILED, STOPPING SERVICE");
                //        stopSelf();
            }
        }

        void closeSocket() {
            try {
                //Don't leave Bluetooth sockets open when leaving activity
                mmSocket.close();
            } catch (IOException e2) {
                //insert code to deal with this
                Log.d("DEBUG BT", e2.toString());
                Log.d("BT SERVICE", "SOCKET CLOSING FAILED, STOPPING SERVICE");
                // stopSelf();
            }
        }
    }

    // New Class for Connected Thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        ConnectedThread(BluetoothSocket socket) {
            Log.d("DEBUG BT", "IN CONNECTED THREAD");
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.d("DEBUG BT", e.toString());
                Log.d("BT SERVICE", "UNABLE TO READ/WRITE, STOPPING SERVICE");
                //                       stopSelf();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.d("DEBUG BT", "IN CONNECTED THREAD RUN");
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer); //read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    Log.d("DEBUG BT PART", "CONNECTED THREAD " + readMessage);
                    s = readMessage;
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    Log.d("DEBUG BT", e.toString());
                    Log.d("BT SERVICE", "UNABLE TO READ/WRITE, STOPPING SERVICE");
                    //                           stopSelf();
                    break;
                }
            }
        }

        //write method
        void write(String input) {
            byte[] msgBuffer = input.getBytes(); //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer); //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Log.d("DEBUG BT", "UNABLE TO READ/WRITE " + e.toString());
                Log.d("BT SERVICE", "UNABLE TO READ/WRITE, STOPPING SERVICE");
                //                stopSelf();
            }
        }

        void closeStreams() {
            try {
                //Don't leave Bluetooth sockets open when leaving activity
                mmInStream.close();
                mmOutStream.close();
            } catch (IOException e2) {
                //insert code to deal with this
                Log.d("DEBUG BT", e2.toString());
                Log.d("BT SERVICE", "STREAM CLOSING FAILED, STOPPING SERVICE");
                //                          stopSelf();
            }
        }
    }

}
