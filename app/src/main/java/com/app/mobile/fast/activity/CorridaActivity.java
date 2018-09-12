package com.app.mobile.fast.activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.app.mobile.fast.R;
import com.app.mobile.fast.config.ConfigFirebase;
import com.app.mobile.fast.model.Motorista;
import com.app.mobile.fast.model.Requisicao;
import com.google.android.gms.maps.CameraUpdate;
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


    private Button mButton;
    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private Marker mMarkerDriver;
    private Marker mMarkerPassenger;
    private Requisicao mRequisicao;

    private DatabaseReference mRefRequisicao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_corrida);
        inicializarComponentes();


    }
    private void inicializarComponentes() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        mButton = findViewById(R.id.bt_aceitar_corrida);

    }

    private void configuracoesMapa(){
        configuracaoLocalizacaoPassageiro();
        configuracaoLocalizacaoMotorista();
        centralizarMarcadoresNaCamera();
    }

    private void configuracaoLocalizacaoPassageiro() {

        if (getIntent().getExtras().containsKey("requisicao")) {
            Bundle extras = getIntent().getExtras();
            mRequisicao = (Requisicao) extras.getSerializable("requisicao");
            LatLng location = new LatLng(mRequisicao.getLatitude(), mRequisicao.getLongitude());
            adicionarMarcadorPassageiro(location);
        } else {
            Toast.makeText(this, "Houve um erro ao abrir a requisicao", Toast.LENGTH_SHORT).show();
            finish();
        }


    }

    private void configuracaoLocalizacaoMotorista() {

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //O que sera feito quando a localizacao mudar
                adicionarMarcadorMotorista(location);
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

    private void adicionarMarcadorPassageiro(LatLng location) {
        //Remove previous instances of the marker
        if (mMarkerPassenger != null) {
            mMarkerPassenger.remove();
        }

        mMarkerPassenger = mMap.addMarker(new MarkerOptions()
                .position(location)
                .title(getString(R.string.map_marker_where_the_user_is))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.usuario)));

        centralizarMarcadoresNaCamera();
    }

    private void adicionarMarcadorMotorista(Location location) {

        //Configura a latitude e a longitude para o marcador
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        //Remove previous instances of the marker
        if (mMarkerDriver != null) {
            mMarkerDriver.remove();
        }

        mMarkerDriver = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(getString(R.string.map_marker_where_the_user_is))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.carro)));

        centralizarMarcadoresNaCamera();


    }

    private void centralizarMarcadoresNaCamera() {

        if(mMarkerDriver == null && mMarkerPassenger == null)
            return;
        else if (mMarkerDriver != null && mMarkerPassenger == null){
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mMarkerDriver.getPosition(), 15));
            return;
        } else{
            //Create the object which will show the markers
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            if(mMarkerDriver != null)
                builder.include(mMarkerDriver.getPosition());
            if(mMarkerPassenger != null)
                builder.include(mMarkerPassenger.getPosition());

            LatLngBounds bounds = builder.build();

            //Get the width and height of the device's screen
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;
            int padding = (int) (width * 0.15);

            //Centralize the markers
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding));

            //Ativa o listener que ira controlar a UI a partir deste ponto
            if (mRefRequisicao == null) {
                listenerRequisicao();
            }
        }





    }

    private void listenerRequisicao() {

        mRefRequisicao = ConfigFirebase.getDatabaseReference()
                .child("requisicoes").child(mRequisicao.getId());

        mRefRequisicao.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                centralizarMarcadoresNaCamera();
                Requisicao requisicao = dataSnapshot.getValue(Requisicao.class);

                switch (requisicao.getStatus()){

                    case Requisicao.STATUS_ON_THE_WAY :
                        mButton.setText(R.string.activity_corrida_on_the_way);
                        break;

                    case Requisicao.STATUS_WAITING :
                        mButton.setText(R.string.activity_corrida_accept_ride);
                        break;


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




    }

    public void aceitarCorrida(View view){
        mRequisicao.setStatus(Requisicao.STATUS_ON_THE_WAY);
        mRequisicao.setDriver(recuperarMotorista());
        mRequisicao.atualizar();
    }

    private Motorista recuperarMotorista(){
        if (getIntent().getExtras().containsKey("motorista")) {
            Bundle extras = getIntent().getExtras();
            Motorista motorista = (Motorista) extras.getSerializable("motorista");
            return motorista;
        } else {
            Toast.makeText(this, "Houve um erro ao abrir a requisicao", Toast.LENGTH_SHORT).show();
            finish();
        }

        return null;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        configuracoesMapa();
    }


}






