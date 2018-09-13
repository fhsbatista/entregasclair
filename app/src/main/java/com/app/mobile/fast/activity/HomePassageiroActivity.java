package com.app.mobile.fast.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.app.mobile.fast.R;
import com.app.mobile.fast.config.ConfigFirebase;
import com.app.mobile.fast.helper.UserProfile;
import com.app.mobile.fast.model.Destino;
import com.app.mobile.fast.model.Passageiro;
import com.app.mobile.fast.model.Requisicao;
import com.app.mobile.fast.model.Usuario;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class HomePassageiroActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    private EditText mLocalDestino;
    private Button mChamarCarro;
    private LinearLayout mLayoutEnderecos;

    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private LatLng mLatLng;
    private boolean isRideRequested = false;
    private Requisicao mRequisicao;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_passageiro);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mLayoutEnderecos = findViewById(R.id.layout_enderecos);
        mLocalDestino = findViewById(R.id.et_local_destino);
        mChamarCarro = findViewById(R.id.bt_chamar_uber);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        verificaRequisicaoPendente();


    }



    private void verificaRequisicaoPendente() {

        //verify whether there is some pendent request of the user, if there is, the screen will be updated to show the data of this request
        DatabaseReference refUserRequest = ConfigFirebase.getDatabaseReference()
                .child("requisicoes_usuarios")
                .child(UserProfile.getFirebaseUserLogado().getUid());

        refUserRequest.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    String idRequesicao = dataSnapshot.child("idRequisicao").getValue(String.class);
                    DatabaseReference refRequesicoes = ConfigFirebase.getDatabaseReference()
                            .child("requisicoes")
                            .child(idRequesicao);

                    refRequesicoes.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            mRequisicao = dataSnapshot.getValue(Requisicao.class);
                            mChamarCarro.setText(R.string.activity_home_passageiro_cancel_ride);
                            mLayoutEnderecos.setVisibility(View.GONE);
                            isRideRequested = true;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        recuperarLocalizacaoUsuario();

    }

    public void chamarCarro(View view) {


        if (isRideRequested == false) {


            //Get the address which was typed by the user
            String endereco = mLocalDestino.getText().toString();

            if (!endereco.equals("")) {
                Address address = recuperaEndereco(endereco);

                if (address != null) {

                    final Destino destino = new Destino();
                    destino.setCidade(address.getSubAdminArea());
                    destino.setBairro(address.getSubLocality());
                    destino.setCep(address.getPostalCode());
                    destino.setRua(address.getThoroughfare());
                    destino.setNumero(address.getFeatureName());
                    destino.setLatitude(String.valueOf(address.getLatitude()));
                    destino.setLongitute(String.valueOf(address.getLongitude()));

                    StringBuilder stringDestino = new StringBuilder();
                    stringDestino.append("\nCidade: " + destino.getCidade());
                    stringDestino.append("\nRua: " + destino.getRua());
                    stringDestino.append("\nNumero: " + destino.getNumero());
                    stringDestino.append("\nBairro: " + destino.getBairro());
                    stringDestino.append("\nCEP: " + destino.getCep());

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Confirme seu  destino");
                    builder.setMessage("Confirme se o endereco abaixo e o desejado");
                    builder.setMessage(stringDestino);
                    builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            isRideRequested = true;
                            mChamarCarro.setText(R.string.activity_home_passageiro_cancel_ride);
                            mLayoutEnderecos.setVisibility(View.GONE);
                            mRequisicao = criaRequisicao(destino);
                            mRequisicao.salvarRequisicao();

                        }
                    }).setNegativeButton("Recusar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();

                } else {
                    Toast.makeText(this, "Houve um problema ao recuperar o endereco de destino", Toast.LENGTH_SHORT).show();
                }


            }

        } else {
            mLayoutEnderecos.setVisibility(View.VISIBLE);
            isRideRequested = false;
            mChamarCarro.setText(R.string.activity_home_passageiro_request_car);
            mRequisicao.cancelarRequisicao();

            //The request is setted as null because a new request has to be create when the user click at the button again
            mRequisicao = null;

        }


    }

    private Requisicao criaRequisicao(Destino destino) {

        Requisicao requisicao = new Requisicao();

        //Set the destination to the requisicao
        requisicao.setDestination(destino);

        //Set the status to "Waiting" because at this point the user is still waiting for some driver
        requisicao.setStatus(Requisicao.STATUS_WAITING);

        //Get and set the current user data
        Passageiro passageiro = UserProfile.getPassageiroLogado();
        requisicao.setPassenger(passageiro);

        //Set the coordinates
        requisicao.setLatitude(mLatLng.latitude);
        requisicao.setLongitude(mLatLng.longitude);

        return requisicao;


    }


    private Address recuperaEndereco(String endereco) {

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> listaEnderecos = geocoder.getFromLocationName(endereco, 1);

            if (listaEnderecos != null && listaEnderecos.size() > 0) {

                Address address = listaEnderecos.get(0);
                return address;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;


    }

    private void recuperarLocalizacaoUsuario() {
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                UserProfile.atualizaGeoFireLocalizacaoUsuario(location.getLatitude(), location.getLongitude() );

                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                //Clear markers which are already displayed on the map
                mMap.clear();

                // Add a marker in the current position of the user
                mLatLng = new LatLng(latitude, longitude);
                mMap.addMarker(new MarkerOptions()
                        .position(mLatLng)
                        .title("Voce esta aqui")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.usuario)));
                //Move the camera and zoom the screen to the user's location
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 17));


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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_passageiro_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_sair:
                ConfigFirebase.getFirebaseAuth().signOut();
                finish();
                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
