package com.apps.kreativeco.kreativecocar;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {
    Context context;
    Button btnU,btnD,btnL,btnR,btnPlay;
    EditText inp_idA, inp_port;

    String msgToServer;
    String msgFromServer;
    String texto="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context=this;

        inp_idA = (EditText) findViewById(R.id.ipAddress);
        inp_port = (EditText) findViewById(R.id.numPort);

        btnU = (Button) findViewById(R.id.btnUp);
        btnD = (Button) findViewById(R.id.btnD);
        btnR = (Button) findViewById(R.id.btnR);
        btnL = (Button) findViewById(R.id.btnL);
        btnPlay= (Button) findViewById(R.id.btnP);

        btnU.setOnClickListener(this);
        btnD.setOnClickListener(this);
        btnR.setOnClickListener(this);
        btnL.setOnClickListener(this);
        btnPlay.setOnClickListener(this);


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //SpecialView myView = new SpecialView(context);
        //setContentView(myView);

        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        inp_idA.setText(ip);

    }

    protected void onPause() {
        super.onPause();
    }

    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.btnUp:
                texto=ejecutaCliente("2");//U
                Log.d("Response Server",texto);
                Toast.makeText(MainActivity.this, "Button Up pressed ", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnR:
                texto = ejecutaCliente("6");//R
                Log.d("Response Server",texto);
                Toast.makeText(MainActivity.this, "Button Right pressed ", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnD:
                texto = ejecutaCliente("8");//B
                Log.d("Response Server",texto);
                Toast.makeText(MainActivity.this, "Button Down pressed ", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnL:
                texto = ejecutaCliente("4");//L
                Log.d("Response Server",texto);
                Toast.makeText(MainActivity.this, "Button Left pressed ", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnP:
                texto = ejecutaCliente("5");//Stop/PLay
                Log.d("Response Server",texto);
                Toast.makeText(MainActivity.this, "IpAddress: "+inp_idA.getText().toString()+" , Port: "+inp_port.getText().toString(), Toast.LENGTH_LONG).show();
                break;
        }
    }

    private String ejecutaCliente(String boton)
    {
        msgToServer = boton;
        try
        {
            Socket sk = new Socket(inp_idA.getText().toString(), Integer.parseInt(inp_port.getText().toString()));
            PrintWriter salida = new PrintWriter(new OutputStreamWriter(sk.getOutputStream()),true);
            BufferedReader entrada = new BufferedReader(new InputStreamReader(sk.getInputStream()));
            salida.println(msgToServer+'\n');//sending the message
            msgFromServer = entrada.readLine();//receiving the answer
            sk.close();
            return msgFromServer;
        }
        catch (Exception e)
        {
            return "error";
        }
    }


}
