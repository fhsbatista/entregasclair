package com.app.mobile.fast.activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.app.mobile.fast.R;
import com.app.mobile.fast.model.Motorista;
import com.app.mobile.fast.model.Requisicao;
import com.app.mobile.fast.model.Usuario;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class CorridaActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    private Button mAcceptRide;
    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private Motorista mMotorista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_corrida);
        inicializarComponentes();
        inicializarAtributos();


    }

    public void aceitarCorrida(View view) {

        Requisicao requisicao = new Requisicao();
        requisicao.setId(recuperarBundleIdRequisicao());
        requisicao.setDriver(recuperarBundleMotorista());
        requisicao.setStatus(Requisicao.STATUS_ON_THE_WAY);
        requisicao.atualizar();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        recuperarLocalizacaoUsuario();

    }


    private void inicializarComponentes() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    private void inicializarAtributos() {
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        recuperarLocalizacaoUsuario();
        recuperarBundleMotorista();
    }

    private void recuperarLocalizacaoUsuario() {
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                //Clean the previous markers which can be being shown in the map
                mMap.clear();

                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                //Add the marker which shows the driver's position
                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(getString(R.string.map_marker_where_the_user_is))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.carro)));

                //Zoom the camera
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));


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

    private String recuperarBundleIdRequisicao() {

        if (getIntent().getExtras().containsKey("idRequisicao")) {
            Bundle extras = getIntent().getExtras();
            String idRequisicao = extras.getString("idRequisicao");
            return idRequisicao;
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
