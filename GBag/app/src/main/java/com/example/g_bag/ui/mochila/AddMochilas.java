package com.example.g_bag.ui.mochila;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.g_bag.Preferences;
import com.example.g_bag.R;
import com.example.g_bag.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddMochilas extends AppCompatActivity {
    private FirebaseFirestore db;
    TextView aliasmochila,idmochila;
    Usuario usuario;
    ProgressDialog progressDialog;
    DatabaseReference db_reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_mochilas);
        db =  FirebaseFirestore.getInstance();
        db_reference = FirebaseDatabase.getInstance().getReference();
        aliasmochila = findViewById(R.id.EdtxtAliasMochila);
        idmochila = findViewById(R.id.EdtxViewIdmochila);
        progressDialog = new ProgressDialog(this);
        usuario = Preferences.getUsuario(getApplicationContext(),"obusuario");
        FloatingActionButton fabreturn = findViewById(R.id.fabReturn);
        fabreturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                finish();
            }
        });

        Button btnregistroMochila =(Button) findViewById(R.id.btnEnviarMochila);
        btnregistroMochila.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(verConexioInternet(view)){
                    if(validar_vacios()){
                        final String alias = aliasmochila.getText().toString().trim();
                        final String id = idmochila.getText().toString().trim();
                        final Map<String, Object> mochila_cloud = new HashMap<>();
                        if(TextUtils.isEmpty(alias)){
                            mochila_cloud.put("alias","");
                        }else{
                            mochila_cloud.put("alias",alias);
                        }
                        if(usuario!=null){
                            mochila_cloud.put("pertenece",usuario.getCorreo());
                        }
                        progressDialog.setMessage("Realizando registro en linea");
                        progressDialog.show();
                        db.collection("dispositivos").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful()){
                                    ArrayList<String> ids_totales = new ArrayList<String>();
                                    for(QueryDocumentSnapshot documentSnapshots: task.getResult()){
                                        System.out.println(documentSnapshots.getId());
                                        ids_totales.add(documentSnapshots.getId());
                                    }
                                    if(ids_totales.contains(id)){
                                        db.collection("dispositivos").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if(task.isSuccessful()){
                                                    DocumentSnapshot documentReference = task.getResult();
                                                    String perteneciente = (String) documentReference.get("pertenece");
                                                    if(TextUtils.isEmpty(perteneciente)){
                                                        db.collection("dispositivos").document(id).set(mochila_cloud).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    DatabaseReference mochila_database = db_reference.child("dispositivos").child(id);
                                                                    mochila_database.child("ubicacion").child("latitud").setValue("");
                                                                    mochila_database.child("ubicacion").child("longitud").setValue("");
                                                                    mochila_database.child("informacion").child("bateria").setValue(100);
                                                                    Toast.makeText(getApplicationContext(),"Dispositivo registrado correctamente",Toast.LENGTH_SHORT).show();
                                                                }else{
                                                                    Toast.makeText(getApplicationContext(),"Hubo un error en el registro. Intentelo nuevamente",Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                    }else{
                                                        idmochila.setError("Ingrese Id no registrado");
                                                        Toast.makeText(getApplicationContext(),"Dispositivo ya registrado",Toast.LENGTH_SHORT).show();
                                                    }
                                                }else{
                                                    Toast.makeText(getApplicationContext(),"Hubo un error en el registro. Intentelo nuevamente",Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });

                                    }else{
                                        Toast.makeText(getApplicationContext(),"Autentificacion de Dispositivo Incorrecta",Toast.LENGTH_SHORT).show();
                                        idmochila.setError("Id dispositivo incorrecto");
                                    }

                                }else{
                                    Toast.makeText(getApplicationContext(),"Fallo de conexión en la nube",Toast.LENGTH_SHORT).show();
                                }
                                progressDialog.dismiss();
                            }
                        });


                    }

                }
            }
        });


    }

    public boolean verConexioInternet(View view){
        try {
            ConnectivityManager con = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
            assert con != null;
            NetworkInfo networkInfo = con.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                return true;
            }else{
                Snackbar.make(view, "Verifique su conexion de Internet", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                return false;
            }
        }catch (NullPointerException n){
            return false;
        }
    }

    public boolean validar_vacios(){
        String id = idmochila.getText().toString().trim();
        if(TextUtils.isEmpty(id)){
            idmochila.setError("Este campo no puede estar vacio");
            return false;
        }
        return true;
    }
}