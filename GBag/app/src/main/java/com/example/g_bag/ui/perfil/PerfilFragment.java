package com.example.g_bag.ui.perfil;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.g_bag.Login;
import com.example.g_bag.Preferences;
import com.example.g_bag.R;
import com.example.g_bag.Usuario;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class PerfilFragment extends Fragment {

    Button btnCerrarSesion;
    CircleImageView photo_user;
    View root;
    TextView numMochilas,nombreUsuario,textviewtelefono,textViewcorreo;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_notifications, container, false);
        //Inicializando Variables
        btnCerrarSesion = root.findViewById(R.id.btnCerrarSesion);
        photo_user = root.findViewById(R.id.imgViewPhoto);
        numMochilas = root.findViewById(R.id.TextViewMochilas);
        nombreUsuario = root.findViewById(R.id.txtUserPerfil);
        textviewtelefono = root.findViewById(R.id.TextView_telefono);
        textViewcorreo = root.findViewById(R.id.TextView_mail);

        //Se borra las preferencias guardadas y se regresa a la pagina de Login
        btnCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Preferences.LimpiarCredenciales(getContext());
                Intent intent = new Intent(getActivity(), Login.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        //Obteniendo preferencias
        Usuario usuario = Preferences.getUsuario(getActivity().getApplicationContext(),"obusuario");
        String photo = Preferences.ObtenerCredenciales(getActivity().getApplicationContext(),"photo_user","null");
        String email = Preferences.ObtenerCredenciales(getActivity().getApplicationContext(),"email","null");
        String nomuser = Preferences.ObtenerCredenciales(getActivity().getApplicationContext(),"nom_usuario","null");
        String telefono = Preferences.ObtenerCredenciales(getActivity().getApplicationContext(),"telefono","null");

        //Modificando parametros no nulos
        if(usuario!=null){
            numMochilas.setText(String.valueOf(usuario.getMochilas().size()));
        }
        if(!photo.equals("null")){
            Picasso.with(getActivity().getApplicationContext()).load(photo).into(photo_user);
        }if(!nomuser.equals("null")){
            nombreUsuario.setText(nomuser);
        }if(!email.equals("null")){
            textViewcorreo.setText(email);
        }if(!telefono.equals("null")){
            textviewtelefono.setText(telefono);
        }

        return root;
    }
}