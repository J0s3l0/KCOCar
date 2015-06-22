package com.apps.kreativeco.kreativecocar;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

import android.content.Context;
import android.os.PowerManager;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;


public class ControllerB extends Activity implements SensorEventListener,View.OnClickListener {


    private static final String TAG = "bluetooth1";
    protected PowerManager.WakeLock wakelock;
    private SensorManager sensorManager;

    Button avanza, atras,izq, der, centro, salir;

    TextView xCoor; // declare X axis object
    TextView yCoor; // declare Y axis object
    TextView zCoor; // declare Z axis object

    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;

    // SPP UUID service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // MAC-address of Bluetooth module (you must edit this line)
    private static String address = "20:14:10:10:35:28";



    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller_b);

        final PowerManager pm=(PowerManager)getSystemService(Context.POWER_SERVICE);
        this.wakelock=pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "etiqueta");
        wakelock.acquire();

        xCoor=(TextView)findViewById(R.id.xCoor); // create X axis object
        yCoor=(TextView)findViewById(R.id.yCoor); // create Y axis object
        zCoor=(TextView)findViewById(R.id.zCoor); // create Z axis object

        sensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);
        // add listener. The listener will be HelloAndroid (this) class
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);

        avanza = (Button) findViewById(R.id.avanza);
        atras = (Button) findViewById(R.id.atras);
        izq = (Button) findViewById(R.id.izq);
        der = (Button) findViewById(R.id.der);
        centro = (Button) findViewById(R.id.centro);
        //salir = (Button) findViewById(R.id.salir);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();

        avanza.setOnClickListener(this);
        atras.setOnClickListener(this);
        izq.setOnClickListener(this);
        der.setOnClickListener(this);
        centro.setOnClickListener(this);

        /*
        avanza.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                sendData("a");
                //new SendDataAT().execute("a");
                Toast.makeText(getBaseContext(), "Avanza", Toast.LENGTH_SHORT).show();
            }
        });

        atras.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                sendData("r");
                //new SendDataAT().execute("r");
                Toast.makeText(getBaseContext(), "Atras", Toast.LENGTH_SHORT).show();
            }
        });

        der.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                sendData("d");
                //new SendDataAT().execute("d");
                Toast.makeText(getBaseContext(), "Derecha", Toast.LENGTH_SHORT).show();
            }
        });

        izq.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                sendData("i");
                //new SendDataAT().execute("i");
                Toast.makeText(getBaseContext(), "Izquierda", Toast.LENGTH_SHORT).show();
            }
        });*/


    }

    protected void onDestroy(){
        super.onDestroy();

        this.wakelock.release();
    }

    public void onAccuracyChanged(Sensor sensor,int accuracy){

    }

    public void onSensorChanged(SensorEvent event){
        float x=event.values[0];
        float y=event.values[1];
        float z=event.values[2];

        // check sensor type
        if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){

            // assign directions
			/*float x=event.values[0];
			float y=event.values[1];
			float z=event.values[2];*/

            xCoor.setText("X: "+x);
            yCoor.setText("Y: "+y);
            zCoor.setText("Z: "+z);

        }

        // marcha adelante, marcha atras
        if(y > 1 && y < 5) sendData("a");
        if(y > 6) sendData("b");
        if(y < -3) sendData("r");
        if (y > -3 && y < 1 )sendData("p");


        //giro izq y der
        if (x < -3) sendData("d");
        if (x > 3) sendData("i");
        if (x < 3 && x > -3) sendData("c");


    } // onSensorChanged





    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "...onResume - try connect...");

        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.
        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        btAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        Log.d(TAG, "...Connecting...");
        try {
            btSocket.connect();
            Log.d(TAG, "...Connection ok...");
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        // Create a data stream so we can talk to server.
        Log.d(TAG, "...Create Socket...");

        try {
            outStream = btSocket.getOutputStream();
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
        }
    }



    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "...In onPause()...");

        if (outStream != null) {
            try {
                outStream.flush();
            } catch (IOException e) {
                errorExit("Fatal Error", "In onPause() and failed to flush output stream: " + e.getMessage() + ".");
            }
        }

        try     {
            btSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
    }

    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if(btAdapter==null) {
            errorExit("Fatal Error", "Bluetooth not support");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth ON...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(btAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    private void errorExit(String title, String message){
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }

    private void sendData(String message) {
        byte[] msgBuffer = message.getBytes();

        Log.d(TAG, "...Send data: " + message + "...");
        //Log.i(TAG, "...Send data: " + msgBuffer + "...");

        try {
            outStream.write(msgBuffer);
        } catch (IOException e) {
            String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
            if (address.equals("00:00:00:00:00:00"))
                msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 35 in the java code";
            msg = msg +  ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";

            errorExit("Fatal Error", msg);
            finish();
            System.exit(0);

        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.avanza:
                sendData("a");
                break;
            case R.id.atras:
                sendData("r");
                break;
            case R.id.der:
                sendData("d");
                break;
            case R.id.izq:
                sendData("i");
                break;
            case R.id.centro:
                sendData("b");
                break;
        }
    }


    private class SendDataAT extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            sendData(params[0]);
            return "Ok";
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("Result AT", "" + result);
        }
    }
}