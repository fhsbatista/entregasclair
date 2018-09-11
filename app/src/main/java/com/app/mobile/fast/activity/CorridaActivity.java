package com.app.mobile.fast.activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.app.mobile.fast.R;
import com.app.mobile.fast.config.ConfigFirebase;
import com.app.mobile.fast.model.Motorista;
import com.app.mobile.fast.model.Requisicao;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class CorridaActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    private Button mAcceptRideButton;
    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private Location mDriverLocation;

    //Request which is open
    private Requisicao mRequisicao;

    //Markers
    private Marker mMarkerMotorista;
    private Marker mMarkerPassageiro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_corrida);
        inicializarComponentes();
        inicializarAtributos();
        iniciarListenerRequisicao();




    }

    private void iniciarListenerRequisicao() {
        DatabaseReference refRequisicao = ConfigFirebase.getDatabaseReference()
                .child("requisicoes").child(mRequisicao.getId());

        refRequisicao.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Requisicao requisicao = dataSnapshot.getValue(Requisicao.class);

                switch (requisicao.getStatus()){

                    case Requisicao.STATUS_WAITING :
                        requisicaoAguardando();
                        break;

                    case Requisicao.STATUS_ON_THE_WAY:
                        requisicaoACAminho();
                        break;


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void requisicaoAguardando() {
        //Change the button's text
        mAcceptRideButton.setText(R.string.activity_corrida_accept_ride);

        //Set the location of the passenger
        LatLng latLngPassenger = new LatLng(mRequisicao.getLatitude(), mRequisicao.getLongitude());


        //Set the passenger's marker
        inserirMarcadorPassageiro(latLngPassenger);


    }

    private void requisicaoACAminho() {
        //Change the button's text
        mAcceptRideButton.setText(R.string.activity_corrida_on_the_way);

        //Set the location of the driver
        LatLng latLngDriver = new LatLng(mDriverLocation.getLatitude(), mDriverLocation.getLongitude());
        LatLng latLngPassenger = new LatLng(mRequisicao.getLatitude(), mRequisicao.getLongitude());


        //Insert the passenger's and driver's maker on the map
        inserirMarcadorMotorista(latLngDriver);
        inserirMarcadorPassageiro(latLngPassenger);



    }

    public void aceitarCorrida(View view) {

        mRequisicao.setDriver(recuperarBundleMotorista());
        mRequisicao.setStatus(Requisicao.STATUS_ON_THE_WAY);
        mRequisicao.atualizar();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        recuperarLocalizacaoUsuario();

    }private void inicializarComponentes() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mAcceptRideButton = findViewById(R.id.bt_aceitar_corrida);


    }

    private void inicializarAtributos() {

        mRequisicao = recuperarBundleRequisicao();

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        recuperarLocalizacaoUsuario();
        recuperarBundleMotorista();
    }

    private void recuperarLocalizacaoUsuario() {
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //Reference the global variable "mLocationDriver" to the refreshed location
                mDriverLocation = location;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        //Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000,
                    10,
                    mLocationListener);
        } else {
            Toast.makeText(this, "Voce tem problemas com permissoes", Toast.LENGTH_SHORT).show();
        }


    }


    private void inserirMarcadorMotorista(LatLng latLng) {

        //Remove previous instances of the marker
        if (mMarkerMotorista != null) {
            mMarkerMotorista.remove();
        }

        mMarkerMotorista = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(getString(R.string.map_marker_where_the_user_is))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.carro)));

        //Zoom the camera
        centralizarMarcadores(mMarkerMotorista, mMarkerPassageiro);
    }


    private void inserirMarcadorPassageiro(LatLng latLng){

        //Remove previous instances of the marker
        if (mMarkerPassageiro != null) {
            mMarkerPassageiro.remove();
        }

        mMarkerPassageiro = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(getString(R.string.map_marker_where_the_passenger_is))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.usuario)));



    }

    private void centralizarMarcadores(Marker markerDriver, Marker markerPassenger){

        //Create the object which will show the markers
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(markerDriver.getPosition());
        builder.include(markerPassenger.getPosition());
        LatLngBounds bounds = builder.build();

        //Get the width and height of the device's screen
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.15);

        //Centralize the markers
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding));

    }


    private Requisicao recuperarBundleRequisicao() {

        if (getIntent().getExtras().containsKey("requisicao")) {
            Bundle extras = getIntent().getExtras();
            Requisicao requisicao = (Requisicao) extras.getSerializable("requisicao");
            return requisicao;
        } else {
            Toast.makeText(this, "Houve um erro ao abrir a requisicao", Toast.LENGTH_SHORT).show();
            finish();
        }
        return null;
    }

    private Motorista recuperarBundleMotorista(){

        if (getIntent().getExtras().containsKey("motorista")) {
            Bundle extras = getIntent().getExtras();
            Motorista motorista= (Motorista) getIntent().getSerializableExtra("motorista");
            return motorista;
        } else {
            Toast.makeText(this, "Houve um erro ao abrir a requisicao", Toast.LENGTH_SHORT).show();
            finish();
        }

        return null;

    }


}
