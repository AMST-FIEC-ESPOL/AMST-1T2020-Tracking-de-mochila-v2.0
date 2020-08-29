package com.example.g_bag.ui.mochila;

public class Mochila {

    String id_dispositivo,alias,encd_apagado,bateria;
    String latitud,longitud;
    String modo;

    public Mochila(String id_dispositivo) {
        this.id_dispositivo = id_dispositivo;
        this.alias="";
        this.latitud="0.00";
        this.longitud="0.00";
        this.encd_apagado="off";
        this.bateria="";
        this.modo = "REAL";

    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getEncd_apagado() {
        return encd_apagado;
    }

    public void setEncd_apagado(String encd_apagado) {
        this.encd_apagado = encd_apagado;
    }

    public String getBateria() {
        return bateria;
    }

    public void setBateria(String bateria) {
        this.bateria = bateria;
    }

    public String getLatitud() {
        return latitud;
    }

    public void setLatitud(String latitud) {
        this.latitud = latitud;
    }

    public String getLongitud() {
        return longitud;
    }

    public void setLongitud(String longitud) {
        this.longitud = longitud;
    }

    public String getId_dispositivo() {
        return id_dispositivo;
    }

    public void setId_dispositivo(String id_dispositivo) {
        this.id_dispositivo = id_dispositivo;
    }

    public String getModo() {
        return modo;
    }

    public void setModo(String modo) {
        this.modo = modo;
    }
}
