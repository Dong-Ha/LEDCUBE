package com.example.ledcube.maze;

import static java.lang.Thread.sleep;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.ledcube.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.UUID;

public class MazeActivity extends AppCompatActivity {

    final int handlerState = 0;
    Handler bluetoothIn;

    private static final String MAC_ADDRESS = "98:DA:60:01:C3:10";
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter btAdapter = null;
    private StringBuilder recDataString = new StringBuilder();

    private ConnectingThread mConnectingThread;
    private ConnectedThread mConnectedThread;

    String s;
    public static String calculating_path = "";
    public static String path = "";

    public static int xx= 0;
    public static int yy= 0;
    public static int zz= 0;

    public static int tmp_xx= 0;
    public static int tmp_yy= 0;
    public static int tmp_zz= 0;

    public static int rows = 5;
    public static int cols = 5;
    public static int zs = 5;
    public static int[][][] map = new int[5][5][5];
    public static int[][][] map1 = {
            {
                    {1, 0, 0, 0, 0},
                    {1, 1, 1, 1, 0},
                    {1, 0, 0, 0, 0},
                    {1, 0, 0, 0, 0},
                    {0, 0, 0, 0, 0}
            },
            {
                    {0, 0, 0, 0, 0},
                    {0, 0, 0, 0, 0},
                    {0, 0, 1, 1, 1},
                    {1, 0, 1, 0, 0},
                    {1, 1, 1, 1, 0}
            },
            {
                    {0, 0, 1, 0, 0},
                    {1, 1, 1, 0, 0},
                    {1, 0, 1, 0, 0},
                    {0, 0, 0, 1, 0},
                    {0, 0, 0, 1, 0}
            },
            {
                    {1, 1, 1, 1, 1},
                    {0, 0, 0, 0, 1},
                    {1, 0, 0, 0, 0},
                    {1, 0, 0, 0, 0},
                    {1, 0, 0, 0, 0}
            },
            {
                    {0, 0, 0, 0, 0},
                    {0, 0, 0, 0, 1},
                    {0, 0, 1, 1, 1},
                    {0, 1, 1, 0, 1},
                    {0, 1, 0, 0, 1}
            }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maze);

        Button gmstart = findViewById(R.id.button1);
        Button go = findViewById(R.id.button2);
        Button back = findViewById(R.id.button5);
        Button right = findViewById(R.id.button3);
        Button left = findViewById(R.id.button4);
        Button up = findViewById(R.id.button6);
        Button down = findViewById(R.id.button7);
        Button hint = findViewById(R.id.button8);
        Button restart = findViewById(R.id.button9);

        gmstart.setOnClickListener(view -> {
            btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
            checkBTState();
            gmstart.setVisibility(View.INVISIBLE);
            go.setVisibility(View.VISIBLE);
            back.setVisibility(View.VISIBLE);
            right.setVisibility(View.VISIBLE);
            left.setVisibility(View.VISIBLE);
            up.setVisibility(View.VISIBLE);
            down.setVisibility(View.VISIBLE);
            hint.setVisibility(View.VISIBLE);
            restart.setVisibility(View.VISIBLE);
        });



        Button.OnClickListener onClickListener = new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.button2:
                        mConnectedThread.write("g");
                        //mConnectedThread.write("g2g");
                        break;
                    case R.id.button5:
                        mConnectedThread.write("b");
                        //mConnectedThread.write("g2b");
                        break;
                    case R.id.button3:
                        mConnectedThread.write("r");
                        //mConnectedThread.write("g2r");
                        break;
                    case R.id.button4:
                        mConnectedThread.write("l");
                        //mConnectedThread.write("g2l");
                        break;
                    case R.id.button6:
                        mConnectedThread.write("u");
                        //mConnectedThread.write("g2u");
                        break;
                    case R.id.button7:
                        mConnectedThread.write("d");
                        //mConnectedThread.write("g2d");
                        break;
                    case R.id.button8: {

                        path = "";

                        for(int i = 0; i < 5; i++){
                            for(int j = 0; j < 5; j++){
                                for(int k = 0; k < 5; k++){
                                    map[i][j][k] = map1[i][j][k];
                                }
                            }
                        }
                        tmp_zz = zz;
                        tmp_xx = xx;
                        tmp_yy = yy;

                        //bfs(zz,xx,yy);

                        zz = tmp_zz;
                        xx = tmp_xx;
                        yy = tmp_yy;

                        mConnectedThread.write("s"); // 출발점 복귀를 위한 정보 저장
                        try{ Thread.sleep(500);}
                        catch(InterruptedException e) {}

                        for(int i = 1; i < path.length(); i++) {
                            mConnectedThread.write( String.valueOf(path.charAt(i)) );
                            try{ Thread.sleep(500);}
                            catch(InterruptedException e) {}
                        }

                        mConnectedThread.write("n");

                        break;
                    }
                    case R.id.button9: {
                        mConnectedThread.write("h");
                        xx = 0; yy = 0; zz = 0;
                        break;
                    }
                    default:
                        break;

                }
            }
        };

        go.setOnClickListener(onClickListener);
        back.setOnClickListener(onClickListener);
        right.setOnClickListener(onClickListener);
        left.setOnClickListener(onClickListener);
        up.setOnClickListener(onClickListener);
        down.setOnClickListener(onClickListener);
        hint.setOnClickListener(onClickListener);
        restart.setOnClickListener(onClickListener);




        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {

                if (msg.what == handlerState) {
                    //if message is what we want
                    String readMessage = (String) msg.obj;
                    // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage);

                //    rrr.setText(recDataString);  원하는 텍스트 설정 부분
                    switch (recDataString.charAt(0)){
                        case 'r':{
                            xx++;
                            Log.d("MAZE", "xx increase");
                            break;
                        }
                        case 'l':{
                            xx--;
                            Log.d("MAZE", "xx decrease");
                            break;}
                        case 'g':{
                            yy--;
                            Log.d("MAZE", "yy decrease");
                            break;
                        }
                        case 'b':{
                            yy++;
                            Log.d("MAZE", "yy increase");
                            break;
                        }
                        case 'u':
                            zz++;
                            break;
                        case 'd':
                            zz--;
                            break;

                        default:
                            break;
                    }

                    // Do stuff here with your data, like adding it to the database
                }
                //clear all string data
                recDataString.delete(0, recDataString.length());
            }
        };

    }

    @Override
    protected void onPause(){
        super.onPause();
        mConnectedThread.write("0");
        mConnectedThread.closeStreams();
        mConnectingThread.closeSocket();

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
                mConnectedThread.write("1"); // 미로 게임 시작 부분
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





    ///////////////////////////////////////////////////////////////////////








}