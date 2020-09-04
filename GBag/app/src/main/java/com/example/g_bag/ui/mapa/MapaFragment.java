package com.example.g_bag.ui.mapa;
import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;


import com.example.g_bag.Preferences;
import com.example.g_bag.R;
import com.example.g_bag.Usuario;
import com.example.g_bag.ui.mochila.Mochila;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class MapaFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    GoogleMap mapaGoogle;

    DatabaseReference db_reference;
    private ArrayList<Marker> temprealTimeMarker = new ArrayList<>();
    private ArrayList<Marker> realTimeMarker = new ArrayList<>();
    LocationManager manejadorLocalizacion;
    public LatLng UBICACIONU = new LatLng(-0.947334, -80.732324);
    double milongitudeGPS, milatitudeGPS;
    private GoogleApiClient clienteGoogleApi;
    private Location ultimaLocalizacion;
    public static final int REQUEST_LOCATION = 1;
    public static final int REQUEST_CHECK_SETTINGS = 2;
    public static final long INTERVALO_ACTUALIZACION = 2000;
    public static final long INTERVALO_ACTUALIZACION_RAPIDA = INTERVALO_ACTUALIZACION / 2;
    private LocationRequest requisitoLocalizacion;
    private LocationSettingsRequest configresquisitoLocalizacion;
    FloatingActionButton floatingSearch;
    String distanciaMaxima;
    Usuario usuario;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_mapa, container, false);
        manejadorLocalizacion = (LocationManager) getActivity().getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        db_reference = FirebaseDatabase.getInstance().getReference();
        floatingSearch = root.findViewById(R.id.floatingSearch);
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);
        usuario = Preferences.getUsuario(getActivity().getApplicationContext(),"obusuario");


        //inicializacion variables
        clienteGoogleApi = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .enableAutoManage(getActivity(), this)
                .build();
        requisitoLocalizacion = new LocationRequest()
                .setInterval(INTERVALO_ACTUALIZACION)
                .setFastestInterval(INTERVALO_ACTUALIZACION_RAPIDA)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(requisitoLocalizacion)
                .setAlwaysShow(true);
        configresquisitoLocalizacion = builder.build();

        final PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(
                clienteGoogleApi, builder.build()
        );
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                Status status = result.getStatus();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        System.out.println("Los ajustes de ubicación satisfacen la configuración.");
                        processLastLocation();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            System.out.println("Los ajustes de ubicación no satisfacen la configuración. " +
                                    "Se mostrará un diálogo de ayuda.");
                            status.startResolutionForResult(
                                    getActivity(),
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            System.out.println("El Intent del diálogo no funcionó.");
                            // Sin operaciones
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        System.out.println("Los ajustes de ubicación no son apropiados.");
                        break;
                }
            }
        });

        floatingSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialogo = new AlertDialog.Builder(getActivity());
                dialogo.setTitle("Establezca la distancia maxima en Km");
                final EditText edtxDistancia = new EditText(getActivity());
                edtxDistancia.setInputType(InputType.TYPE_CLASS_NUMBER);
                dialogo.setView(edtxDistancia);

                dialogo.setPositiveButton("BUSCAR", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(TextUtils.isEmpty(edtxDistancia.getText().toString().trim())){
                            edtxDistancia.setError("Este campo no puede estar vacio");
                        }else{
                            mapaGoogle.clear();
                            LatLng milatLng = new LatLng(milatitudeGPS, milongitudeGPS);
                            distanciaMaxima = edtxDistancia.getText().toString().trim();
                            for(Mochila mochila: usuario.getMochilas()){
                                if(mochila.getEncd_apagado().equalsIgnoreCase("on")){
                                    LatLng LatLng_dispositivo = new LatLng(mochila.getLatitud(),mochila.getLongitud());
                                    //realTimeMarker.get(indice).remove();
                                    MarkerOptions markerOptions = new MarkerOptions();
                                    double distancia_ub_dispo = CalculationByDistance(milatLng,LatLng_dispositivo);
                                    if(Double.parseDouble(distanciaMaxima)>=distancia_ub_dispo){
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.mochila_green)).anchor(0.0f,0.5f);
                                        mochila.setRango("aceptable");
                                    }else{
                                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.mochila_red)).anchor(0.0f,0.5f);
                                        mochila.setRango("fuera");
                                    }
                                    Preferences.save(getActivity().getApplicationContext(),usuario,"obusuario");
                                    markerOptions.position(LatLng_dispositivo);
                                    mapaGoogle.addMarker(markerOptions);
                                }

                            }
                        }
                    }
                });

                dialogo.setNegativeButton("CANCELAR",null);
                dialogo.show();
            }
        });


        return root;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapaGoogle = googleMap;
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mapaGoogle.setMyLocationEnabled(true);
        //UiSettings settings = mapaGoogle.getUiSettings();
        //settings.setZoomControlsEnabled(true);
        //setMarkerDragListener(mapaGoogle);

    }

    public void obtenerMochila(DatabaseReference databaseReference, final Mochila mochila){
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    String latitud = String.valueOf(dataSnapshot.child("ubicacion").child("latitud").getValue());
                    String longitud = String.valueOf(dataSnapshot.child("ubicacion").child("longitud").getValue());
                    if(!latitud.isEmpty() && !longitud.isEmpty()){
                        if(Double.parseDouble(latitud)!=0.0&&Double.parseDouble(longitud)!=0.0){
                            mochila.setLatitud(Double.parseDouble(latitud));
                            mochila.setLongitud(Double.parseDouble(longitud));
                            MarkerOptions markerOptions = new MarkerOptions();
                            if(mochila.getRango().equalsIgnoreCase("neutral")){
                                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.mochila_black)).anchor(0.0f,1.0f);
                            }else if(mochila.getRango().equalsIgnoreCase("fuera")){
                                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.mochila_red)).anchor(0.0f,1.0f);
                            }else{
                                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.mochila_green)).anchor(0.0f,1.0f);
                            }
                            Preferences.save(getActivity().getApplicationContext(),usuario,"obusuario");
                            markerOptions.position(new LatLng(Double.parseDouble(latitud),Double.parseDouble(longitud)));
                            temprealTimeMarker.add(mapaGoogle.addMarker(markerOptions));
                        }else{
                            Toast.makeText(getActivity(),"Dispositivo "+mochila.getId_dispositivo()+" fuera de alcance para enviar datos",Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(getActivity(),"Active el dispositivo en un rango permitido",Toast.LENGTH_SHORT).show();
                    }


                }catch (NullPointerException n){
                    Toast.makeText(getActivity(),"Error al obtener la ubicacion del dispositivo",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println(databaseError.toException());

            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        clienteGoogleApi.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        clienteGoogleApi.disconnect();
        if(usuario!=null){
            for(Mochila mochila: usuario.getMochilas()){
                mochila.setRango("neutral");
            }
            Preferences.save(getActivity().getApplicationContext(),usuario,"obusuario");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (clienteGoogleApi.isConnected()) {
            stopLocationUpdates();
        }
        clienteGoogleApi.stopAutoManage(getActivity());
        clienteGoogleApi.disconnect();
        if(usuario!=null){
            for(Mochila mochila: usuario.getMochilas()){
                mochila.setRango("neutral");
            }
            Preferences.save(getActivity().getApplicationContext(),usuario,"obusuario");
        }


    }

    @Override
    public void onResume() {
        super.onResume();
        if (clienteGoogleApi.isConnected()) {
            startLocationUpdates();
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(verConexioInternet()){
            // Obtenemos la última ubicación al ser la primera vez
            processLastLocation();
            // Iniciamos las actualizaciones de ubicación
            startLocationUpdates();
        }


    }

    private void startLocationUpdates() {

        if (isLocationPermissionGranted()) {
            if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    clienteGoogleApi, requisitoLocalizacion, this);
        } else {
            manageDeniedPermission();
        }
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi
                .removeLocationUpdates(clienteGoogleApi, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        System.out.println("Conexión suspendida");
        clienteGoogleApi.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getActivity(),"Error de conexión con el código:" + connectionResult.getErrorCode(), Toast.LENGTH_LONG).show();
    }

    //Procesando la ultima ubicacion
    private void processLastLocation() {
        getLastLocation();
        if (ultimaLocalizacion != null) {
            updateLocationUI();
        }
    }

    private void getLastLocation() {
        if (isLocationPermissionGranted()) {
            if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            }
            System.out.println("ACEPTADO");
            ultimaLocalizacion = LocationServices.FusedLocationApi.getLastLocation(clienteGoogleApi);
        } else {
            manageDeniedPermission();
        }
    }

    private void updateLocationUI() {
        mi_marcador(ultimaLocalizacion.getLatitude(), ultimaLocalizacion.getLongitude());

    }

    private void mi_marcador(double latitude, double longitude) {
        mapaGoogle.clear(); //limpio el mapa
        if(this.milatitudeGPS==0.0 && this.milongitudeGPS==0.0){
            milatitudeGPS = latitude;
            milongitudeGPS = longitude;
            LatLng latLng = new LatLng(milatitudeGPS, milongitudeGPS);
            mapaGoogle.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mapaGoogle.addMarker(new MarkerOptions().position(latLng).title("Mi ubicacion"));
        }
        LatLng latLng = new LatLng(milatitudeGPS, milongitudeGPS);
        mapaGoogle.addMarker(new MarkerOptions().position(latLng).title("Mi ubicacion"));

        //mapaGoogle.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_logotipo)).anchor(0.0f,1.0f).position(latLng).title("Mi ubicacion")); //añade un marcador con un icono


        for(Marker marker: realTimeMarker){
            marker.remove();
        }
        if(usuario!=null){
            if(usuario.getMochilas().size()!=0){
                if(usuario.obtenerMochilasEnc().size()!=0){
                    for(Mochila mochila: usuario.getMochilas()){
                        if(mochila.getEncd_apagado().equalsIgnoreCase("on")){
                            DatabaseReference db_dispositivos = db_reference.child("dispositivos").child(mochila.getId_dispositivo());
                            obtenerMochila(db_dispositivos,mochila);
                        }
                    }
                }else{
                    Toast.makeText(getActivity(),"Active al menos un dispositivo",Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(getActivity(),"Dirijase a la seccion mochila para agregar un dispositivo",Toast.LENGTH_SHORT).show();
            }


        }
        realTimeMarker.clear();
        realTimeMarker.addAll(temprealTimeMarker);
    }




    //Calculando distancia radial de un punto a otro punto
    public double CalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);

        return Radius * c;
    }

    public void setMarkerDragListener(GoogleMap map) {
        map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {

            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                LatLng p = marker.getPosition();
                marker.setTitle("Mochila " + String.valueOf(CalculationByDistance(p, UBICACIONU) + " Km"));
            }
        });
    }


    //Permisos de CONEXION INTERNET - LOCALIZACIÓN
    public boolean verConexioInternet() {
        try {
            ConnectivityManager con = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            assert con != null;
            NetworkInfo networkInfo = con.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                return true;
            } else {
                Toast.makeText(getActivity(), "Verifique su conexion de Internet", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NullPointerException n) {
            return false;
        }
    }

    //Verifica y pide permisos de localizacion
    private boolean isLocationPermissionGranted() {
        int permission = ActivityCompat.checkSelfPermission(
                getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permission == PackageManager.PERMISSION_GRANTED;
    }

    private void manageDeniedPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Aquí muestras confirmación explicativa al usuario
            // por si rechazó los permisos anteriormente
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                ultimaLocalizacion = LocationServices.FusedLocationApi.getLastLocation(clienteGoogleApi);
                if (ultimaLocalizacion != null) {
                    mi_marcador(ultimaLocalizacion.getLatitude(),ultimaLocalizacion.getLongitude());

                } else {
                    Toast.makeText(getActivity(), "Ubicación no encontrada", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getActivity(), "Permisos no otorgados", Toast.LENGTH_LONG).show();
            }
        }
    }

    //Escucha los cambios de la localizacion, es decir actualiza la localizacion ultima actual
    @Override
    public void onLocationChanged(Location location) {
        ultimaLocalizacion = location;
        updateLocationUI();
    }
}