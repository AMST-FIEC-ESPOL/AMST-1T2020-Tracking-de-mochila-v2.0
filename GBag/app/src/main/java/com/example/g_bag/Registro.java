package com.example.g_bag;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class Registro extends AppCompatActivity {
    TextView txtpasslong,txtpassdig,txtpassmayus,txtpassesp;
    EditText ednom, edapell, edmail, edpass, edpassconf, edphone;
    ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private static final ArrayList<String> correo_permitidos = new ArrayList<>();
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +         //al menos 1 digito
                    //"(?=.*[a-z])" +         //al menos 1 letra minuscula
                    "(?=.*[A-Z])" +         //al menos 1 letra mayuscula
                    "(?=.*[a-zA-Z])" +      //cualquier letra
                    //"(?=.*[@#$%^&+=])" +    //al menos 1 caracter especial
                    "(?=\\S+$)" +           //sin espacios
                    ".{7,}" +               //al menos 7 caracteres
                    "$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        //inicializar variables
        ednom = findViewById(R.id.EdTnombre_registro);
        edapell = findViewById(R.id.EdTapellido_registro);
        edmail = findViewById(R.id.EdTemail_registro);
        edpass = findViewById(R.id.EdTpass_registro);
        edpassconf = findViewById(R.id.EdTpassconfir_registro);
        edphone = findViewById(R.id.Edcelular);
        txtpasslong = findViewById(R.id.Txtpass_longitud);
        txtpassmayus = findViewById(R.id.Txtpass_mayuscula);
        txtpassesp = findViewById(R.id.Txtpass_espacio);
        txtpassdig = findViewById(R.id.Txtpass_digito);

        //agregando dominio permitido
        correo_permitidos.add("gmail.com");
        correo_permitidos.add("outlook.com");
        correo_permitidos.add("outlook.es");
        correo_permitidos.add("hotmail.com");
        correo_permitidos.add("hotmail.es");
        correo_permitidos.add("yahoo.com");

        progressDialog = new ProgressDialog(this);
        firebaseAuth = FirebaseAuth.getInstance();
        db =  FirebaseFirestore.getInstance();

        //Muestra al usuario si el pass cumple con los parametros especificos
        edpass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!PASSWORD_PATTERN.matcher(s).matches()){
                    txtpasslong.setVisibility(View.VISIBLE);
                    txtpassesp.setVisibility(View.VISIBLE);
                    txtpassdig.setVisibility(View.VISIBLE);
                    txtpassmayus.setVisibility(View.VISIBLE);
                    txtpasslong.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_cancel_red_24dp,0,0,0);
                    txtpassesp.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_cancel_red_24dp,0,0,0);
                    txtpassmayus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_cancel_red_24dp,0,0,0);

                }else{
                    txtpassdig.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_green_24dp,0,0,0);
                    txtpasslong.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_green_24dp,0,0,0);
                    txtpassesp.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_green_24dp,0,0,0);
                    txtpassmayus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_green_24dp,0,0,0);

                }

            }
        });


        edpassconf.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(PASSWORD_PATTERN.matcher(edpass.getText().toString()).matches()){
                    txtpasslong.setVisibility(View.GONE);
                    txtpassesp.setVisibility(View.GONE);
                    txtpassdig.setVisibility(View.GONE);
                    txtpassmayus.setVisibility(View.GONE);
                }else{
                    txtpasslong.setVisibility(View.VISIBLE);
                    txtpassesp.setVisibility(View.VISIBLE);
                    txtpassdig.setVisibility(View.VISIBLE);
                    txtpassmayus.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private boolean validar_mail(String email){
        if(TextUtils.isEmpty(email)){
            edmail.setError("Este campo no puede estar vacio");
            return false;
        }else{
            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                edmail.setError("Correo invalido");
                return false;
            }else{
                String[] tempdom = email.split("@");
                if(tempdom.length<3){
                    String dominio = tempdom[1];
                    if(correo_permitidos.contains(dominio)){
                        return true;
                    }else{
                        edmail.setError("Correo invalido");
                        return false;
                    }

                }else{
                    edmail.setError("Correo invalido");
                    return false;
                }


            }
        }
    }

    private boolean validar_pass(String pass){
        String pass_conf = edpassconf.getText().toString().trim();
        if(TextUtils.isEmpty(pass)|| TextUtils.isEmpty(pass_conf)){
            edpass.setError("Este campo no puede estar vacio");
            edpassconf.setError("Este campo no puede estar vacio");
            return false;
        }else{
            if(!PASSWORD_PATTERN.matcher(pass).matches()){
                edpass.setError("Contraseña invalida");
                return false;
            }else{
                if(!pass.equals(pass_conf)){
                    edpassconf.setError("Contraseña mal escrita");
                    return false;
                }else{
                    return true;
                }
            }
        }

    }


    private boolean validar_vacios(){
        String nombre = ednom.getText().toString().trim();
        String apellido = edapell.getText().toString().trim();
        String email = edmail.getText().toString().trim();
        String pass = edpass.getText().toString().trim();
        String pass_conf = edpassconf.getText().toString().trim();
        String phone = edphone.getText().toString().trim();
        if(TextUtils.isEmpty(nombre) || TextUtils.isEmpty(apellido)||
                TextUtils.isEmpty(email)|| TextUtils.isEmpty(pass)||
                TextUtils.isEmpty(pass_conf) || TextUtils.isEmpty(phone)) {
            if(TextUtils.isEmpty(nombre)){
                ednom.setError("Este campo no puede estar vacio");
            }
            if(TextUtils.isEmpty(apellido)){
                edapell.setError("Este campo no puede estar vacio");
            }
            if(TextUtils.isEmpty(email)){
                edmail.setError("Este campo no puede estar vacio");
            }
            if(TextUtils.isEmpty(pass)){
                edpass.setError("Este campo no puede estar vacio");
            }
            if(TextUtils.isEmpty(pass_conf)){
                edpassconf.setError("Este campo no puede estar vacio");
            }
            if(TextUtils.isEmpty(phone)){
                edphone.setError("Este campo no puede estar vacio");
            }

            return false;
        }else{
            return true;
        }
    }

    public void registro(final View view) {
        final String email = edmail.getText().toString().trim();
        String pass = edpass.getText().toString().trim();
        final String nombre = ednom.getText().toString().trim();
        final String apellido = edapell.getText().toString().trim();
        String phone = edphone.getText().toString().trim();
        if (validar_vacios() && validar_mail(email) && validar_pass(pass)) {

            final Map<String, Object> map_user = new HashMap<>();
            map_user.put("dispvinculados", Collections.emptyList());
            map_user.put("nombre", nombre);
            map_user.put("apellido", apellido);
            map_user.put("celular",phone);

            try {
                ConnectivityManager con = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
                assert con != null;
                NetworkInfo networkInfo = con.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    progressDialog.setMessage("Realizando registro en linea");
                    progressDialog.show();
                    firebaseAuth.createUserWithEmailAndPassword(email,pass)
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        Objects.requireNonNull(firebaseAuth.getCurrentUser()).sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(nombre+" "+apellido).build();
                                                    firebaseAuth.getCurrentUser().updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                db.collection("usuarios").document(email).set(map_user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if(task.isSuccessful()){
                                                                            Toast.makeText(Registro.this,"Se ha registrado correctamente. Por favor verificar correo de verficacion",Toast.LENGTH_LONG).show();
                                                                            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                                                                            Objects.requireNonNull(imm).hideSoftInputFromWindow(view.getWindowToken(), 0);
                                                                            Intent i = new Intent(Registro.this,Login.class);
                                                                            startActivity(i);
                                                                            FirebaseAuth.getInstance().signOut(); //cerrar sesion
                                                                            finish();

                                                                        }else{
                                                                            Toast.makeText(Registro.this,"No se pudo registrar correctamente",Toast.LENGTH_LONG).show();
                                                                        }
                                                                    }
                                                                });

                                                            }else{
                                                                Toast.makeText(Registro.this,"No se pudo registrar correctamente",Toast.LENGTH_LONG).show();
                                                            }
                                                        }
                                                    });


                                                }else{
                                                    Toast.makeText(Registro.this,"No se pudo registrar correctamente",Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                    }else{
                                        if(task.getException() instanceof FirebaseAuthUserCollisionException){
                                            edmail.setError("Correo ya registrado");
                                        }else{
                                            Toast.makeText(Registro.this,"No se pudo registrar correctamente",Toast.LENGTH_LONG).show();
                                        }

                                    }
                                    progressDialog.dismiss();
                                }
                            });
                }else {
                    Toast.makeText(this, "No se pudo conectar, verifique" +
                            " el acceso a Internet e Intente de nuevamente", Toast.LENGTH_LONG).show();
                }
            }catch (NullPointerException n){
                Log.w("Error",n.toString());
            }

        }
    }
}