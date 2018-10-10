package com.app.mobile.entregasclair.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.app.mobile.entregasclair.R;
import com.app.mobile.entregasclair.adapter.PlaceAutoCompleteAdapter;
import com.app.mobile.entregasclair.config.ConfigFirebase;
import com.app.mobile.entregasclair.helper.UserProfile;
import com.app.mobile.entregasclair.model.Destino;
import com.app.mobile.entregasclair.model.Passageiro;
import com.app.mobile.entregasclair.model.Requisicao;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
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

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class HomePassageiroActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    private static final String TAG = HomePassageiroActivity.class.getSimpleName();

    //widgets
    private AutoCompleteTextView mLocalDestino;
    private AutoCompleteTextView mLocalPartida;
    private Button mChamarCarro;
    private ConstraintLayout mLayoutEnderecos;
    private TextView mTextViewAvisoMotoristaACaminho;
    private ProgressBar mProgressBarLoading;


    //Variaveis
    private PlaceAutoCompleteAdapter mPlaceAutoCompleteAdapterDestino;
    private PlaceAutoCompleteAdapter mPlaceAutoCompleteAdapterPartida;
    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private LatLng mLatLng;
    private boolean isRideRequested = false;
    private Requisicao mRequisicao;
    private Marker mMarkerPassageiro;
    private Marker mMarkerDriver;
    private Marker mMarkerDestino;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_passageiro);


        //Este metodo ira inicializar os elementos do layout
        inicializarElementosUI();


        //Verifica se a localizaçao do usuario esta ativa
        if(verificarSensorLocalizaçao()){
            //Mostra a barra de progressdialog
            mostrarProgressBar();


            //Este metodo configura o mapa e seu callback
            configurarMapa();


            //Verifica se ja exista uma requisicao aberta para o usuario que solicitou
            verificaRequisicaoPendente();


        } else{
            ativarLocationSnackBar();
        }
    }

    private void ativarLocationSnackBar() {

        CoordinatorLayout coordinatorLayout = findViewById(R.id.coordinatorLayout);

        final Snackbar snackbar = Snackbar
                .make(coordinatorLayout, "Voce precisa ativar sua localizaçao", Snackbar.LENGTH_INDEFINITE)
                .setAction("Tentar novamente", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(!verificarSensorLocalizaçao())
                            ativarLocationSnackBar();
                        else
                            HomePassageiroActivity.this.recreate();

                    }
                });

        snackbar.setActionTextColor(Color.CYAN);

        snackbar.show();
    }

    private boolean verificarSensorLocalizaçao() {

        LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);


        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void mostrarProgressBar(){

        mLayoutEnderecos.setVisibility(View.GONE);
        mChamarCarro.setVisibility(View.GONE);
        mProgressBarLoading.setVisibility(View.VISIBLE);
    }

    private void esconderProgressBar(){

        mLayoutEnderecos.setVisibility(View.VISIBLE);
        mChamarCarro.setVisibility(View.VISIBLE);
        mProgressBarLoading.setVisibility(View.GONE);
    }

    private void addMarcadorPassageiro(LatLng latLng){

        if(mMarkerPassageiro != null){
            mMarkerPassageiro.remove();
        }


        mMarkerPassageiro = mMap.addMarker(new MarkerOptions()
            .title("O restaurante esta aqui")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.usuario))
            .position(latLng));

        centralizarMarcadores();



    }

    private void centralizarMarcadores() {

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        if (mMarkerPassageiro != null) {
            builder.include(mMarkerPassageiro.getPosition());
        }

        if (mMarkerDriver != null) {
            builder.include(mMarkerDriver.getPosition());
        }

        if(mMarkerDestino != null){
            builder.include(mMarkerDestino.getPosition());
        }

        LatLngBounds bounds = builder.build();

        //Verifica se os marcadores do motorista e do usuario sao nulos, pois caso sejam, quer dizer que a tela ainda nao foi totalmente carregada, e entao a camera ira focalizar apenas um marcador.
        if(mMarkerDriver != null && mMarkerPassageiro != null){
            //Get the width and height of the device's screen
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = (int) ((getResources().getDisplayMetrics().heightPixels) * 0.85);
            int padding = (int) (width * 0.15);

            //Centralize the markers
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding));
        } else{
            if(mMarkerPassageiro != null){
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mMarkerPassageiro.getPosition(), 17));
            }
            if(mMarkerDriver != null){
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mMarkerDriver.getPosition(), 17));
            }
        }




    }

    private void configurarMapa() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void inicializarElementosUI() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mLayoutEnderecos = findViewById(R.id.layout_enderecos);
        mLocalDestino = findViewById(R.id.et_local_destino);
        mLocalPartida = findViewById(R.id.et_local_partida);
        mChamarCarro = findViewById(R.id.bt_chamar_uber);
        mTextViewAvisoMotoristaACaminho = findViewById(R.id.tv_motorista_a_caminho);
        mProgressBarLoading = findViewById(R.id.pb_loading);
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

                            if (dataSnapshot.getValue() != null) {
                                mRequisicao = dataSnapshot.getValue(Requisicao.class);
                                mChamarCarro.setText(R.string.activity_home_passageiro_cancel_ride);
                                mLayoutEnderecos.setVisibility(View.GONE);
                                isRideRequested = true;
                                ativarListenerRequisicao();

                            }


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
                    builder.setTitle("Confirme seu  rota");
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
                            ativarListenerRequisicao();

                        }
                    }).setNegativeButton("Recusar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();

                } else {
                    Toast.makeText(this, "Houve um problema ao recuperar o endereco de rota", Toast.LENGTH_SHORT).show();
                }


            }

        } else {
            mLayoutEnderecos.setVisibility(View.VISIBLE);
            layoutDesativarAvisoMotoristaACaminho();
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
        String enderecoPartida = mLocalPartida.getText().toString();

        if(enderecoPartida.equals("")) {
            requisicao.setLatitude(mLatLng.latitude);
            requisicao.setLongitude(mLatLng.longitude);
        } else{
            //Recupera os dados do endereço digitado
            Address address = recuperaEndereco(enderecoPartida);
            requisicao.setLatitude(address.getLatitude());
            requisicao.setLongitude(address.getLongitude());

            //Adiciona o marcador do local de partida
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
            addMarcadorPassageiro(latLng);
        }

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


                atualizarMapa(location);


            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d(TAG, "PROVIDER ATIVADO");
            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        //Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    200,
                    10,
                    mLocationListener);


            Location lastLocalizacaoConhecida = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (lastLocalizacaoConhecida != null) {
                atualizarMapa(lastLocalizacaoConhecida);
            } else{
                lastLocalizacaoConhecida = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastLocalizacaoConhecida != null) {
                    atualizarMapa(lastLocalizacaoConhecida);
                }
            }

            configurarAdapterSugestoesDestino(lastLocalizacaoConhecida);
            configurarAdapterSugestoesPartida(lastLocalizacaoConhecida);


        } else {
            Toast.makeText(this, "Voce tem problemas com permissoes", Toast.LENGTH_SHORT).show();
        }

    }

    private void configurarAdapterSugestoesDestino(Location lastLocalizacaoConhecida) {

        //Inicia adapter GOOGLE PLACES (usado para sugestoes de endereço do destino)
        GeoDataClient geoDataClient = Places.getGeoDataClient(this);

        //Coordenadas para criar um quadrado em torno da localizaçao do usuario, neste quase sera um quadro
        //em que as bordas terao 0.02 graus de distancia da localizaçao do usuario, o que da em media 4,5km
        Double lat1 = lastLocalizacaoConhecida.getLatitude() + 0.02;
        Double lon1 = lastLocalizacaoConhecida.getLongitude() + 0.02;
        Double lat2 = lastLocalizacaoConhecida.getLatitude() - 0.02;
        Double lon2 = lastLocalizacaoConhecida.getLongitude() - 0.02;

        //LatLng que serao usadas para criar o quadrado
        LatLng latLng1 = new LatLng(lat1, lon1);
        LatLng latLng2 = new LatLng(lat2, lon2);

        //LatLngBounds final para ser usado no adapter
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(latLng1);
        builder.include(latLng2);
        LatLngBounds latLngBounds = builder.build();

        mPlaceAutoCompleteAdapterDestino = new PlaceAutoCompleteAdapter
                (this, geoDataClient, latLngBounds, null);

        mLocalDestino.setAdapter(mPlaceAutoCompleteAdapterDestino);


    }

    private void configurarAdapterSugestoesPartida(Location lastLocalizacaoConhecida) {

        //Inicia adapter GOOGLE PLACES (usado para sugestoes de endereço do destino)
        GeoDataClient geoDataClient = Places.getGeoDataClient(this);

        //Coordenadas para criar um quadrado em torno da localizaçao do usuario, neste quase sera um quadro
        //em que as bordas terao 0.02 graus de distancia da localizaçao do usuario, o que da em media 4,5km
        Double lat1 = lastLocalizacaoConhecida.getLatitude() + 0.02;
        Double lon1 = lastLocalizacaoConhecida.getLongitude() + 0.02;
        Double lat2 = lastLocalizacaoConhecida.getLatitude() - 0.02;
        Double lon2 = lastLocalizacaoConhecida.getLongitude() - 0.02;

        //LatLng que serao usadas para criar o quadrado
        LatLng latLng1 = new LatLng(lat1, lon1);
        LatLng latLng2 = new LatLng(lat2, lon2);

        //LatLngBounds final para ser usado no adapter
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(latLng1);
        builder.include(latLng2);
        LatLngBounds latLngBounds = builder.build();

        mPlaceAutoCompleteAdapterPartida = new PlaceAutoCompleteAdapter
                (this, geoDataClient, latLngBounds, null);

        mLocalPartida.setAdapter(mPlaceAutoCompleteAdapterPartida);


    }

    private void atualizarMapa(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        // Add a marker in the current position of the user
        mLatLng = new LatLng(latitude, longitude);

        //Adicionar o marcador do usuario
        addMarcadorPassageiro(mLatLng);

        //Esconde a progress bar caso esteja ativa
        if(mProgressBarLoading.getVisibility() == View.VISIBLE)
            esconderProgressBar();
    }

    private void ativarListenerRequisicao(){

        DatabaseReference refRequisicao = ConfigFirebase.getDatabaseReference()
                .child("requisicoes").child(mRequisicao.getId());

        refRequisicao.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                mRequisicao = dataSnapshot.getValue(Requisicao.class);

                switch (mRequisicao.getStatus()){

                    case Requisicao.STATUS_ON_THE_WAY :
                        layoutAtivarAvisoMotoristaACaminho();
                        Toast.makeText(HomePassageiroActivity.this, "O motorista esta a caminho", Toast.LENGTH_SHORT).show();
                        break;

                    case Requisicao.STATUS_TRAVELING :
                        layoutAtivarAvisoMotoristaACaminho();
                        Destino destino = mRequisicao.getDestination();
                        LatLng latLng = new LatLng(Double.parseDouble(destino.getLatitude()), Double.parseDouble(destino.getLongitute()));
                        addMarcadorDestino(latLng);
                        mTextViewAvisoMotoristaACaminho.setText("A caminho do destino");
                        mMarkerPassageiro.remove();
                        break;

                    case Requisicao.STATUS_COMPLETED :

                        layoutDesativarAvisoMotoristaACaminho();
                        Toast.makeText(HomePassageiroActivity.this, "Corrida finalizada", Toast.LENGTH_SHORT).show();
                        mChamarCarro.setText("Corrida finalizada");
                        mMarkerPassageiro.remove();
                        mMarkerPassageiro = null;
                        centralizarMarcadores();
                        break;

                    default:
                        break;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void addMarcadorDestino(LatLng latLng) {

        if (mMarkerDestino != null) {
            mMarkerDestino.remove();
        }


        mMarkerDestino = mMap.addMarker(new MarkerOptions()
            .title("O seu destino esta aqui")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.rota))
            .position(latLng));

        centralizarMarcadores();
    }

    private void layoutAtivarAvisoMotoristaACaminho(){

        mTextViewAvisoMotoristaACaminho.setVisibility(View.VISIBLE);
        mLayoutEnderecos.setVisibility(View.GONE);
        ativarListenerPosicaoMotorista();

    }

    private void ativarListenerPosicaoMotorista() {


        DatabaseReference refLocalizacaoMotorista = ConfigFirebase.getDatabaseReference()
                .child("localizacoes_usuarios");


        final GeoFire geoFire = new GeoFire(refLocalizacaoMotorista);

        refLocalizacaoMotorista.child(mRequisicao.getDriver().getId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        geoFire.getLocation(mRequisicao.getDriver().getId(), new LocationCallback() {
                            @Override
                            public void onLocationResult(String key, GeoLocation location) {

                                if(key.equals(mRequisicao.getDriver().getId())){
                                    LatLng latLng = new LatLng(location.latitude, location.longitude);
                                    addMarcadorDriver(latLng);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });




    }

    private void addMarcadorDriver(LatLng latLng) {

        if(mMarkerDriver != null){
            mMarkerDriver.remove();
        }

        mMarkerDriver = mMap.addMarker(new MarkerOptions()
            .title("O motorista esta aqui")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.carro))
            .position(latLng));


        centralizarMarcadores();


    }

    private void layoutDesativarAvisoMotoristaACaminho(){

        mTextViewAvisoMotoristaACaminho.setVisibility(View.GONE);

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
