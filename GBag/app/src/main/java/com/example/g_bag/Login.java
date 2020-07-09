package com.example.g_bag;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.FirebaseAuthCredentialsProvider;
import android.os.Parcelable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Login extends AppCompatActivity implements View.OnClickListener{
    Button btnRegistro, btnLogin;
    SignInButton signInButton;
    GoogleSignInClient mGoogleSignInClient;
    FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private FirebaseFirestore db;
    EditText edtextmail, edtextpass;
    int RC_SIGN_IN = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //Declaracion de variables
        autologin();
        edtextmail = findViewById(R.id.Edtxt_login);
        edtextpass = findViewById(R.id.Edtxt_pass);
        signInButton = findViewById(R.id.signButton);
        firebaseAuth = FirebaseAuth.getInstance();
        db =  FirebaseFirestore.getInstance();
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        btnRegistro = findViewById(R.id.btnRegistro);
        btnLogin = findViewById(R.id.btnLogin);
        progressDialog = new ProgressDialog(this);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
        edtextpass.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEND){
                    edtextpass.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            logueado();
                        }
                    });
                }
                return false;
            }
        });
        signInButton.setOnClickListener(this);
        btnRegistro.setOnClickListener(this);
        btnLogin.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.signButton:
                if(verConexioInternet()){
                    singIn();
                }
                break;
            case R.id.btnRegistro:
                registro();
                break;
            case R.id.btnLogin:
                if(verConexioInternet()){
                    logueado();
                }
                break;
        }
    }

    private void singIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) FirebaseGoogleAuth(account);
                Toast.makeText(Login.this,"Inicio de sesion saisfactoria",Toast.LENGTH_SHORT).show();
            } catch (ApiException e) {
                Log.w("Login", "signInResult:failed code=" + e.getStatusCode());
            }
        }
    }

    private void FirebaseGoogleAuth(GoogleSignInAccount account){
        AuthCredential authCredential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
        firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    updateUI(user);
                }else{
                    System.out.println("Error");
                    updateUI(null);
                }
            }
        });
    }

    private void updateUI(FirebaseUser user){
        if (user != null) {
            final Map<String, Object> map_user = new HashMap<>();
            map_user.put("dispvinculados", Collections.emptyList());
            map_user.put("nombre", user.getDisplayName());
            map_user.put("apellido", "");
            if(user.getPhoneNumber()==null || user.getPhoneNumber().isEmpty()){
                map_user.put("celular","");
            }else{
                map_user.put("celular",user.getPhoneNumber());
            }
            progressDialog.setMessage("Iniciando sesion"); //Muestra un progressDialog con ese mensaje
            progressDialog.show();
            db.collection("usuarios").document(Objects.requireNonNull(user.getEmail())).set(map_user).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(Login.this,"Inicio de Sesion Satisfactorio",Toast.LENGTH_SHORT).show();
                        irSistema();
                        progressDialog.dismiss(); // termina el progressDialog
                    }else{
                        Toast.makeText(Login.this,"Fallo en el registro",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            System.out.println("Fallo al registrarse");
        }
    }
    public void registro(){
        Intent regis = new Intent(Login.this,Registro.class);
        startActivity(regis);
    }

    public void logueado(){
        final String email = edtextmail.getText().toString().trim();
        final String pass = edtextpass.getText().toString().trim();
        if(TextUtils.isEmpty(email)|| TextUtils.isEmpty(pass)){
            if(TextUtils.isEmpty(email)){
                edtextmail.setError("Este campo no puede estar vacio");
            }
            if(TextUtils.isEmpty(pass)){
                edtextpass.setError("Este campo no puede estar vacio");
            }
        }else{
            progressDialog.setMessage("Iniciando sesion"); //Muestra un progressDialog con ese mensaje
            progressDialog.show();
            firebaseAuth.signInWithEmailAndPassword(email,pass). // inicia sesion con correo y contraseña
                    addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                if(Objects.requireNonNull(firebaseAuth.getCurrentUser()).isEmailVerified()){ // permite ingresar si el correo ha sido verificado
                                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser(); // obtengo los parametros del usuario del Firebase Authentication
                                    final String usdisplay = Objects.requireNonNull(firebaseUser.getDisplayName()); // obtengo el nombre a mostrar que esta en los parametros del usuario
                                    final DocumentReference docRef = db.collection("usuarios").document(email);
                                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if(task.isSuccessful()){
                                                DocumentSnapshot document = task.getResult();
                                                assert document != null;
                                                if (document.exists()) {
                                                    Preferences.SaveCredenciales(Login.this,email,"email"); //guarda un  String con llave user
                                                    Preferences.SaveCredenciales(Login.this,usdisplay,"nom_usuario");//guarda un  String con llave nombre
                                                    Toast.makeText(Login.this,"Se ha ingresado correctamente",Toast.LENGTH_LONG).show();
                                                    irSistema();
                                                }else {
                                                    Log.d("ERROR", "Error a obtener el documento");
                                                }
                                            }else{
                                                Log.d("ERROR", "fallas al obtener con ", task.getException());
                                            }
                                        }
                                    });
                                }else{
                                    Toast.makeText(Login.this,"Por favor verifque su correo electronico",Toast.LENGTH_LONG).show();
                                }
                            }else{
                                Toast.makeText(Login.this,"Correo y/o contraseña incorrecta",Toast.LENGTH_LONG).show();
                            }
                            progressDialog.dismiss(); // termina el progressDialog
                        }
                    });
        }
    }
    public void autologin(){
        String correo = Preferences.ObtenerCredenciales(this,"email", (String) null); //obtiene String de las credenciales guardadas
        String nom_usuario = Preferences.ObtenerCredenciales(this,"nom_usuario", (String) null); //obtiene String de las credenciales guardadas
       if(correo!=null && nom_usuario!= null){
            irSistema(); // ingresa al sistema si existen valores guardados en el SharedPreferences
        }
    }
    public void irSistema(){
        Intent i = new Intent(Login.this,Sistema.class);
        startActivity(i);
        finish(); // destruye la actividad

    }

    public boolean verConexioInternet(){
        try {
            ConnectivityManager con = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
            assert con != null;
            NetworkInfo networkInfo = con.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                return true;
            }else{
                Toast.makeText(Login.this,"Verifique su conexion de Internet",Toast.LENGTH_SHORT).show();
                return false;
            }
        }catch (NullPointerException n){
            return false;
        }
    }
}