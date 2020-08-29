package com.example.g_bag;

import com.example.g_bag.ui.mochila.Mochila;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Usuario {
    @SerializedName("mochilas")
    final ArrayList<Mochila> mochilas;

    @SerializedName("nom_usuario")
    String nom_usuario;

    @SerializedName("correo")
    String correo;

    public Usuario() {
        mochilas = new ArrayList<>();
        this.correo = "";
    }

    public ArrayList<Mochila> getMochilas() {
        return mochilas;
    }

    public void setMochilas(Mochila mochila) {

        this.mochilas.add(mochila);
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getNom_usuario() {
        return nom_usuario;
    }

    public void setNom_usuario(String nom_usuario) {
        this.nom_usuario = nom_usuario;
    }

    public int cant_mochilas(){
        return this.mochilas.size();
    }

    public String ObteneridMochila(String tipo, String id_alias){
        if(tipo.equals("alias")){
            for(Mochila mochila: getMochilas()){
                if(mochila.getAlias().equals(id_alias)){
                    return mochila.getId_dispositivo();
                }
            }
        }
        return id_alias;
    }

    public void setModoMochila(int position,String modo){
        this.mochilas.get(position).setEncd_apagado(modo);
    }

}
