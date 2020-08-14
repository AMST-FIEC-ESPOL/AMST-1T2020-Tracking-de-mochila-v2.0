package com.example.g_bag;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Set;

public class Preferences {

    public static final String PREFERENCES_CREDENCIALES = "credenciales.usuario";

    public static void SaveCredenciales(Context c, String s, String key){
        SharedPreferences preferences = c.getSharedPreferences(PREFERENCES_CREDENCIALES,c.MODE_PRIVATE);
        preferences.edit().putString(key,s).apply();
    }

    public static String ObtenerCredenciales(Context c, String key,String defecto){
        SharedPreferences preferences = c.getSharedPreferences(PREFERENCES_CREDENCIALES,c.MODE_PRIVATE);
        return preferences.getString(key,defecto);
    }

    public static void SaveCredenciales(Context c, boolean b, String key){
        SharedPreferences preferences = c.getSharedPreferences(PREFERENCES_CREDENCIALES,c.MODE_PRIVATE);
        preferences.edit().putBoolean(key,b).apply();
    }

    public static Boolean ObtenerCredenciales(Context c, String key, Boolean defecto){
        SharedPreferences preferences = c.getSharedPreferences(PREFERENCES_CREDENCIALES,c.MODE_PRIVATE);
        return preferences.getBoolean(key,defecto);
    }
    public static void save(Context c,Usuario usuario,String key) {
        SharedPreferences.Editor edit = c.getSharedPreferences(PREFERENCES_CREDENCIALES,c.MODE_PRIVATE).edit();
        String json = new Gson().toJson(usuario);
        edit.putString(key,json);
        edit.apply();
    }


    public static void LimpiarCredenciales(Context c){
        SharedPreferences preferences = c.getSharedPreferences(PREFERENCES_CREDENCIALES,c.MODE_PRIVATE);
        preferences.edit().clear().apply();
    }

}
