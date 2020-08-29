package com.example.g_bag.ui.home;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.g_bag.Preferences;
import com.example.g_bag.R;
import com.example.g_bag.Usuario;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class ModoMochila extends AppCompatActivity {
    private static  final int REQUEST_ENABLE_BT=0;
    private static  final int REQUEST_DISCOVER_BT=1;
    ArrayList<String> alias_id = new ArrayList<>();
    ArrayList<String> mochilas_activas = new ArrayList<>();
    Spinner mochilas_spinner;
    Button botonEncenderBt,botonApagarBt,btnconectarBlue,btnActivarModo,btnModifTelefono,btnEnviarBlue;
    BluetoothAdapter mbluetoothAdapter = null;
    Usuario usuario;
    Switch estado_modo;
    ImageView bluetothView;
    TextView nom_bluetooth_Ref,con_nom_bluetooh,txMensaje;
    String nom_bluetooth;
    private static String adress_bluetooth;
    EditText edtxtelefonaenviar;

    Handler bluetoothIn;
    final int handlerState = 0;
    private BluetoothSocket btSocket = null;
    private StringBuilder DataStringIN = new StringBuilder();
    private ConnectedThread MyConexionBT;
    // Identificador unico de servicio - SPP UUID
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // String para la direccion MAC

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                bluetothView.setImageResource(R.drawable.ic_bluetooth_on);
                Toast.makeText(getApplicationContext(), "Bluetooth esta encendido", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "No se pudo encender Bluetooth", Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modo_mochila);
        estado_modo = findViewById(R.id.switchModo);
        bluetothView = findViewById(R.id.bluetoothView);
        mochilas_spinner = findViewById(R.id.mochilas_act_spinner);
        botonEncenderBt = findViewById(R.id.btnModoEncender);
        botonApagarBt = findViewById(R.id.btnModoApagar);
        nom_bluetooth_Ref = findViewById(R.id.RefBluetooh);
        con_nom_bluetooh = findViewById(R.id.NombBluetooh);
        btnconectarBlue = findViewById(R.id.btnConectarBlue);
        txMensaje = findViewById(R.id.textMensaje);
        btnActivarModo = findViewById(R.id.btnActivarModo);
        edtxtelefonaenviar = findViewById(R.id.EdtxTelefonEnviar);
        btnModifTelefono = findViewById(R.id.btnModificarTelf);
        btnEnviarBlue = findViewById(R.id.btnEnviarBlue);
        txMensaje.setVisibility(View.GONE);
        usuario = Preferences.getUsuario(getApplicationContext(),"obusuario");

        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {

                    //Interacción con los datos de ingreso
                }
            }
        };

        verificadorEstadoBt();
        mbluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        if(mbluetoothAdapter.isEnabled()){
            bluetothView.setImageResource(R.drawable.ic_bluetooth_on);
            Toast.makeText(this,"Bluetooh activado",Toast.LENGTH_SHORT).show();
        }else{
            bluetothView.setImageResource(R.drawable.ic_bluetooth_off);
            Toast.makeText(this,"Bluetooh desactivado",Toast.LENGTH_SHORT).show();
        }


        FloatingActionButton fabreturn = findViewById(R.id.fabReturn2);
        fabreturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                finish();
            }
        });

        if(usuario!=null) {
            String telefono = Preferences.ObtenerCredenciales(this,"telefono", (String) null);
            if(telefono!=null){
                edtxtelefonaenviar.setText(telefono);
                edtxtelefonaenviar.setEnabled(false);
            }
            for (Mochila m : usuario.getMochilas()) {
                if (!m.getAlias().isEmpty() && !m.getEncd_apagado().equals("off")) {
                    mochilas_activas.add(m.getAlias());
                    alias_id.add("alias");
                } else {
                    if(!m.getEncd_apagado().equals("off")){
                        mochilas_activas.add(m.getId_dispositivo());
                        alias_id.add("id");
                    }
                }
            }
        }

        if(TextUtils.isEmpty(edtxtelefonaenviar.getText().toString())){
            btnModifTelefono.setText("ACEPTAR");
            edtxtelefonaenviar.setEnabled(true);
        }else{
            btnModifTelefono.setText("MODIFICAR");
        }

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter(this,android.R.layout.simple_spinner_item,mochilas_activas);
        mochilas_spinner.setAdapter(adapter);
        botonEncenderBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mbluetoothAdapter.isEnabled()){
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent,REQUEST_ENABLE_BT);
                }else{
                    Toast.makeText(getApplication(),"Bluetooth ya esta encendido",Toast.LENGTH_SHORT).show();
                }
            }
        });

        botonApagarBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mbluetoothAdapter.isEnabled()){
                    mbluetoothAdapter.disable();
                    Toast.makeText(getApplication(),"Apagando Bluetooth",Toast.LENGTH_SHORT).show();
                    bluetothView.setImageResource(R.drawable.ic_bluetooth_off);
                    btnActivarModo.setEnabled(false);
                    estado_modo.setEnabled(false);
                    con_nom_bluetooh.setText("");
                }else{
                    Toast.makeText(getApplication(),"Bluetooth ya esta apagado",Toast.LENGTH_SHORT).show();
                }

                if (btSocket!=null)
                {
                    try {btSocket.close();}
                    catch (IOException e)
                    { Toast.makeText(getApplication(), "Error", Toast.LENGTH_SHORT).show();;}
                }

            }
        });


        estado_modo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(estado_modo.isChecked()){
                    Toast.makeText(getApplication(),"Estatico",Toast.LENGTH_SHORT).show();

                }else{
                    Toast.makeText(getApplication(),"Real",Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnconectarBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txMensaje.setVisibility(View.GONE);
                Set<BluetoothDevice> pairedDevices = mbluetoothAdapter.getBondedDevices();
                if(mbluetoothAdapter.isEnabled()){
                    if(pairedDevices.size()>0){
                        for(BluetoothDevice device: pairedDevices){
                            if(nom_bluetooth_Ref.getText().toString().equalsIgnoreCase(device.getName())){
                                con_nom_bluetooh.setText(device.getName());
                                nom_bluetooth = device.getName();
                                adress_bluetooth = device.getAddress();
                                btnActivarModo.setEnabled(true);

                            }
                        }if(TextUtils.isEmpty(con_nom_bluetooh.getText().toString())){
                            Toast.makeText(getApplicationContext(),"Bluetooth No Vinculado ",Toast.LENGTH_LONG).show();
                            txMensaje.setVisibility(View.VISIBLE);
                            txMensaje.setText("Dispositivo NO VINCULADO\nVaya a configuraciones y vincule el dispositivo\nY vuelva aplastar el boton CONECTAR BLUETOOTH");
                            btnActivarModo.setEnabled(false);
                        }
                    }
                }else{
                    Toast.makeText(getApplicationContext(),"Active el Bluetooth ",Toast.LENGTH_SHORT).show();
                    btnActivarModo.setEnabled(false);
                }

            }
        });

        btnModifTelefono.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(btnModifTelefono.getText().toString().equalsIgnoreCase("MODIFICAR")){
                    edtxtelefonaenviar.setEnabled(true);
                    btnModifTelefono.setText("ACEPTAR");
                }else{
                    if(TextUtils.isEmpty(edtxtelefonaenviar.getText().toString())){
                        edtxtelefonaenviar.setError("Este campo no puede estar vacio");
                    }else{
                        edtxtelefonaenviar.setEnabled(false);
                        btnModifTelefono.setText("MODIFICAR");
                    }

                }
            }
        });

        btnActivarModo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(verficarMochias_activas(mochilas_spinner.getSelectedItemPosition())){
                    if(nom_bluetooth!=null&&adress_bluetooth!=null&&!edtxtelefonaenviar.getText().toString().isEmpty()&&
                            !con_nom_bluetooh.getText().toString().isEmpty()){
                        estado_modo.setEnabled(true);
                    }else{
                        if(nom_bluetooth==null||adress_bluetooth==null){
                            Toast.makeText(getApplicationContext(),"Vincule con el dispositivo Bluetooth ",Toast.LENGTH_SHORT).show();
                        }else if(TextUtils.isEmpty(edtxtelefonaenviar.getText().toString())){
                            edtxtelefonaenviar.setError("Este campo no puede estar vacio");
                        }
                    }
                }
            }
        });

        btnEnviarBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MyConexionBT==null){
                    onResume();
                }else{
                    System.out.println(MyConexionBT);
                    System.out.println(adress_bluetooth);
                    System.out.println(edtxtelefonaenviar.getText().toString().trim());
                    String dataenviar = edtxtelefonaenviar.getText().toString().trim();
                    MyConexionBT.write(dataenviar.getBytes());
                }
            }
        });

    }

    public boolean verficarMochias_activas(int valor){
        if(valor<0){
            Toast.makeText(this,"Active al menos una mochila",Toast.LENGTH_SHORT).show();
            return false;
        }return true;
    }

    public void verificadorEstadoBt(){
        mbluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mbluetoothAdapter==null){
            Toast.makeText(this,"El dispositivo no soporta Bluetooth",Toast.LENGTH_SHORT).show();
        }else{
            if(!mbluetoothAdapter.isEnabled()){
                Intent enableBlue = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBlue,1);
                bluetothView.setImageResource(R.drawable.ic_bluetooth_on);
            }
        }
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException
    {
        //crea un conexion de salida segura para el dispositivo usando el servicio UUID
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if(estado_modo.isEnabled()){
            BluetoothDevice device = mbluetoothAdapter.getRemoteDevice(adress_bluetooth);
            try
            {
                btSocket = createBluetoothSocket(device);
            } catch (IOException e) {
                Toast.makeText(getBaseContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
            }
            // Establece la conexión con el socket Bluetooth.
            try
            {
                btSocket.connect();
            } catch (IOException e) {
                try {
                    btSocket.close();
                } catch (IOException e2) {
                    System.out.println(e2.getMessage());
                }
            }
            MyConexionBT = new ConnectedThread(btSocket);
            MyConexionBT.start();

        }
    }


    @Override
    public void onPause()
    {
        super.onPause();
        try
        { // Cuando se sale de la aplicación esta parte permite que no se deje abierto el socket
            btSocket.close();
        } catch (IOException e2) {
            System.out.println(e2.getMessage());
        }
    }
    // Defines several constants used when transmitting messages between the
    // service and the UI.
    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;

        // ... (Add other message types here as needed.)
    }

    //Crea la clase que permite crear el evento de conexion
    private class ConnectedThread extends Thread
    {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket)
        {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try
            {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run()
        {
            byte[] byte_in = new byte[1024];
            int numBytes; // bytes returned from read()
            // Se mantiene en modo escucha para determinar el ingreso de datos
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    // Send the obtained bytes to the UI activity.
                    Message readMsg = bluetoothIn.obtainMessage(
                            MessageConstants.MESSAGE_READ, numBytes, -1,
                            mmBuffer);
                    readMsg.sendToTarget();
                    /*mmInStream.read(byte_in);
                    char ch = (char) byte_in[0];
                    bluetoothIn.obtainMessage(handlerState, ch).sendToTarget();*/
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    break;
                }
            }
        }

        //Envio de trama
        /*public void write(String input) {
            try {
                mmOutStream.write(input.getBytes());
            }
            catch (IOException e)
            {
                //si no es posible enviar datos se cierra la conexión
                Toast.makeText(getBaseContext(), "La Conexión fallo", Toast.LENGTH_LONG).show();
            }
        }*/
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);

                // Share the sent message with the UI activity.
                Message writtenMsg = bluetoothIn.obtainMessage(
                        MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
                writtenMsg.sendToTarget();
            } catch (IOException e) {
                System.out.println(e.getMessage());

                // Send a failure message back to the activity.
                Message writeErrorMsg =
                        bluetoothIn.obtainMessage(MessageConstants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast", "No puede enviar la data al otro dispositivo");
                writeErrorMsg.setData(bundle);
                bluetoothIn.sendMessage(writeErrorMsg);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

}