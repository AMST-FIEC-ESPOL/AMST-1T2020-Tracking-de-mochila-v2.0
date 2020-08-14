package com.example.g_bag.ui.home;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.g_bag.Preferences;
import com.example.g_bag.R;
import com.example.g_bag.Usuario;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class ModoMochila extends AppCompatActivity {
    ArrayList<String> alias_id = new ArrayList<>();
    ArrayList<String> mochilas_activas = new ArrayList<>();
    Spinner mochilas_spinner;
    Button botonEstatico,botonLive;
    Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modo_mochila);
        mochilas_spinner = findViewById(R.id.mochilas_act_spinner);
        botonEstatico = findViewById(R.id.btnModoEstatico);
        botonLive = findViewById(R.id.btnModoLive);
        FloatingActionButton fabreturn = findViewById(R.id.fabReturn2);
        fabreturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                finish();
            }
        });
        usuario = Preferences.getUsuario(getApplicationContext(),"obusuario");
        if(usuario!=null) {
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
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter(this,android.R.layout.simple_spinner_item,mochilas_activas);
        mochilas_spinner.setAdapter(adapter);
        botonEstatico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(verficarMochias_activas(mochilas_spinner.getSelectedItemPosition())){
                    String id_mochila = usuario.ObteneridMochila(alias_id.get(mochilas_spinner.getSelectedItemPosition()),mochilas_activas.get(mochilas_spinner.getSelectedItemPosition()));
                    System.out.println(id_mochila);
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
}