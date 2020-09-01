package com.example.g_bag.ui.mochila;

public class Mochila {

    String id_dispositivo,alias,encd_apagado,bateria;
    double latitud,longitud;
    String modo,rango;

    public Mochila(String id_dispositivo) {
        this.id_dispositivo = id_dispositivo;
        this.alias="";
        this.latitud=0.0;
        this.longitud=0.0;
        this.encd_apagado="off";
        this.bateria="";
        this.modo = "REAL";
        this.rango ="neutral";

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

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
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

    public String getRango() {
        return rango;
    }

    public void setRango(String rango) {
        this.rango = rango;
    }
}
