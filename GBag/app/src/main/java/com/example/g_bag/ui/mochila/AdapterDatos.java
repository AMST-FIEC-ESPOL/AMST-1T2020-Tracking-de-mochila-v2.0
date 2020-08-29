package com.example.g_bag.ui.mochila;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.g_bag.Preferences;
import com.example.g_bag.R;
import com.example.g_bag.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class AdapterDatos extends RecyclerView.Adapter<AdapterDatos.ViewHolderDatos> {

    ArrayList<String> listaDatos ;
    Context context;
    final Usuario usuario;
    View mview;
    FragmentActivity activity;
    DatabaseReference db_reference;
    int indice = 1;

    public AdapterDatos(ArrayList<String> listaDatos, Context context, FragmentActivity activity) {
        this.listaDatos = listaDatos;
        this.context = context;
        this.activity=activity;
        usuario = Preferences.getUsuario(context,"obusuario");
    }


    @NonNull
    @Override
    public ViewHolderDatos onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layout = LayoutInflater.from(context);
        View view = layout.inflate(R.layout.items_list,parent,false);
        return new ViewHolderDatos(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderDatos holder, int position) {
        holder.asignarDato(listaDatos.get(position));

    }

    @Override
    public int getItemCount() {
        return listaDatos.size();
    }

    public class ViewHolderDatos extends RecyclerView.ViewHolder {
        TextView dato ;
        Switch switchon_off;
        Button info;

        public ViewHolderDatos(@NonNull final View itemView) {
            super(itemView);
            dato = (TextView) itemView.findViewById(R.id.idDato);
            switchon_off = (Switch) itemView.findViewById(R.id.switchOnOff);
            info = (Button) itemView.findViewById(R.id.imgButtonInfo);
            //Cambia el modo de la mochila
            indice+=getAdapterPosition();
            if(-1*(indice)>=0){
                if(usuario.getMochilas().get(-1*(indice)).getEncd_apagado().equals("on")){
                    switchon_off.setChecked(true);
                }else if(usuario.getMochilas().get(-1*(indice)).getEncd_apagado().equals("off")){
                    switchon_off.setChecked(false);
                }
            }
            switchon_off.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(usuario!=null){
                        if(switchon_off.isChecked()){
                            usuario.setModoMochila(getAdapterPosition(),"on");
                        }else{
                            usuario.setModoMochila(getAdapterPosition(),"off");
                        }
                        Preferences.save(view.getContext(),usuario,"obusuario");
                    }
                }
            });

            info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    db_reference = FirebaseDatabase.getInstance().getReference();
                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
                    mview = activity.getLayoutInflater().inflate(R.layout.informacion_dipositivo,null);
                    DatabaseReference db_dispositivos = db_reference.child("dispositivos")
                            .child(usuario.getMochilas().get(getAdapterPosition()).getId_dispositivo());
                    TextView dispositivo = (TextView) mview.findViewById(R.id.info_id);
                    final TextView bateria = (TextView) mview.findViewById(R.id.info_bat);
                    TextView modo = (TextView) mview.findViewById(R.id.info_modo);
                    dispositivo.setText(usuario.getMochilas().get(getAdapterPosition()).getId_dispositivo());
                    db_dispositivos.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            bateria.setText(String.valueOf(dataSnapshot.child("informacion").child("bateria").getValue())+"%");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    modo.setText(usuario.getMochilas().get(getAdapterPosition()).getModo());
                    mBuilder.setView(mview);
                    AlertDialog dialog = mBuilder.create();
                    Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.show();


                }
            });

        }


        public void asignarDato(String s) {
            dato.setText(s);
        }
    }
}
