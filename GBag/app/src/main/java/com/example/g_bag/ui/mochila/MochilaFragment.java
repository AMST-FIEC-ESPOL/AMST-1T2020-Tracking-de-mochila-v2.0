package com.example.g_bag.ui.mochila;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.g_bag.Preferences;
import com.example.g_bag.R;
import com.example.g_bag.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class MochilaFragment extends Fragment {


    private RecyclerView recyclerView;
    Usuario usuario;
    ArrayList<String> listaDatos;
    AdapterDatos adapter;
    private FirebaseFirestore db;
    Button BtnAgregarmochilas,BtncambiarModo;
    ViewGroup viewGroup;
    ArrayList<String> ids;

    public MochilaFragment() {
    }
    public void setListaDatos(String s) {
        this.listaDatos.add(s);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_mochila, container, false);
        this.viewGroup = container;
        recyclerView = (RecyclerView) root.findViewById(R.id.recyclerMochilas);
        BtnAgregarmochilas = root.findViewById(R.id.btnAgregarMochila);
        BtncambiarModo = root.findViewById(R.id.btnCambiarModo);
        BtnAgregarmochilas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),AddMochilas.class);
                startActivity(intent);
            }
        });
        BtncambiarModo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Se mueve a la actividad Modo Mochila
                Intent intent = new Intent(getActivity(),ModoMochila.class);
                startActivity(intent);

            }
        });


        return root;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        usuario = Preferences.getUsuario(getActivity().getApplicationContext(),"obusuario");
        ids = new ArrayList<>();
        if(usuario==null){
            usuario = new Usuario();
            String email = Preferences.ObtenerCredenciales(getActivity().getApplicationContext(),"email","null");
            if(!email.equals("null")){
                usuario.setCorreo(email);
            }
        }else{
            for(Mochila mochila: usuario.getMochilas()){
                ids.add(mochila.getId_dispositivo());
                mochila.setRango("neutral");
            }
            Preferences.save(getActivity().getApplicationContext(),usuario,"obusuario");
        }

        listaDatos = new ArrayList<>();
        if(verConexioInternet()){
            db =  FirebaseFirestore.getInstance();
            db.collection("dispositivos").whereEqualTo("pertenece",usuario.getCorreo()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if(usuario.getMochilas().isEmpty()){
                                Mochila mochila = new Mochila(document.getId());
                                mochila.setAlias((String) document.get("alias"));
                                usuario.setMochilas(mochila);

                            }else{
                                if(!ids.isEmpty()){
                                    if(!ids.contains(document.getId())){
                                        Mochila mochila = new Mochila(document.getId());
                                        mochila.setAlias((String) document.get("alias"));
                                        usuario.setMochilas(mochila);
                                    }
                                }
                            }
                            if(TextUtils.isEmpty((String) document.get("alias"))){
                                setListaDatos(document.getId());
                            }else{
                                setListaDatos((String) document.get("alias"));
                            }
                        }
                    }

                    Preferences.save(getActivity().getApplicationContext(),usuario,"obusuario");
                    adapter = new AdapterDatos(listaDatos,getContext(),getActivity());
                    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                    recyclerView.setAdapter(adapter);
                    if(usuario.getMochilas().size()==0){
                        Toast.makeText(getActivity(),"Registre al menos una mochila",Toast.LENGTH_SHORT).show();
                    }
                }
            });



        }
    }

    public boolean verConexioInternet(){
        try {
            ConnectivityManager con = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            assert con != null;
            NetworkInfo networkInfo = con.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                return true;
            }else{
                Toast.makeText(getActivity(),"Verifique su conexion de Internet",Toast.LENGTH_SHORT).show();
                return false;
            }
        }catch (NullPointerException n){
            return false;
        }
    }
}