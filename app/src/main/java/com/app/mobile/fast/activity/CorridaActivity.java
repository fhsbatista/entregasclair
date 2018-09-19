package com.app.mobile.fast.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
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
import com.app.mobile.fast.helper.UserProfile;
import com.app.mobile.fast.model.Destino;
import com.app.mobile.fast.model.Motorista;
import com.app.mobile.fast.model.Requisicao;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
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


    private static final String TAG = CorridaActivity.class.getSimpleName();

    //UI
    private Button mButtonAceitar;
    private FloatingActionButton mFabRotas;

    //Entidades
    private Requisicao mRequisicao;

    //Firebase
    private DatabaseReference mRefRequisicao;

    //Localizacao
    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;

    //Marcadores
    private Marker mMarkerPassageiro;
    private Marker mMarkerMotorista;
    private Marker mMarkerDestino;

    //Controles
    //Esta variavel sera usada para decidir se a rota a ser traçada e em direçao ao passageiro ou entao ao destino
    private boolean isDestinoAtivo = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_corrida);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mButtonAceitar = findViewById(R.id.bt_aceitar_corrida);
        mFabRotas = findViewById(R.id.fb_rota);



        /*Passos
        1 - o metodo onMapReady quando finalizado, ira chamar o metodo para adicionar o marcador do passageiro
        2 - O metodo "adicionarMarcadorPassageiro" ira chamar o metodo que recupera a requisicao que chegou na activity pela intent
        3 - a partir disto, ele ira inserir o marcador no mapa, e entao ira chamar o metodo "recuperarlocalizacaoMotorista", neste passo tambem sera inserido o circulo no marcador do passageiro.
        **Obs: No passo acima, conforme a localizacao do motorista mudar, o GeoFire sera atualizado e consequemente sera atualizada o no "localizacoes_usuarios" no firebase
        4 - O metodo recuperarLocalizacaoMotorista ira configurar a localizacao do usuario, e entao quando receber a localizacao o proprio metodo ira chamar o metodo "adicionarmarcadorMotorista.
        5 - O metodo "centralizarMarcadores()" sera chamado, e este ira centralizar o marcador do motorista e do passageiro
        6 - Uma vez que os marcadores foram centralizados, entao o botao de "Aceitar corrida" sera acionado
        7 - Tambem sera adicionado o listener que ficara verificando as mudanças da requisicao no firebase
        8 - Com a verificao das requisicoes ativadas, quando o status da requisicao recuperada for a ON_THE_WAY, sera ativado o listener do geofire com o metodo firebaseAtivarListenerLocalizacaoGeo()
        9 - No metodo firebaseAtivarListenerLocalizacaoGeo(), quando o motorista entrar no raio de 50m do passageiros, o status da requisicao no firebase sera atualizado para TRAVELING.
        10 - A partir deste momento, ainda dentro do metodo firebaseAtivarListenerLocalizacaoGeo(), sera chamado o metodo adicionarMarcadorDestino(), e consequentemente o marcador do destino sera inserido e a camera sera centralizada pelo metodo centralizarMarcadores(). Tambem sera inserido o CIRCULO no marcador do destino


         */


    }

    private void adicionarMarcadorPassageiro() {

        Log.d(TAG, "Passo 2: Metodo 'adicionarMarcadorPassageiro() chamado com sucesso");

        mRequisicao = recuperarRequisicao();

        if (mRequisicao != null) {
            Log.d(TAG, "Passo 2: a requisicao foi recuperada com sucesso");
        } else {
            Log.d(TAG, "Passo 2: houve um erro ao recuperar a requisicao");

        }
        //Verifica se ja existe algum marcador, caso haja, ele sera removido
        if (mMarkerPassageiro != null) {
            mMarkerPassageiro.remove();
        }

        //Recupera as coordenadas referente a onde o passageiro ira embarcar
        double latitudePassageiro = mRequisicao.getLatitude();
        double longitudePassageiro = mRequisicao.getLongitude();
        LatLng location = new LatLng(latitudePassageiro, longitudePassageiro);

        //Insere o Marcador
        mMarkerPassageiro = mMap.addMarker(new MarkerOptions()
                .position(location)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.usuario))
                .title("O passageiro esta aqui"));


        //Insere um circulo ao redor do marcador do passageiro, para representar a area de 50m ao redor dele
        Circle circle = mMap.addCircle(
                new CircleOptions()
                        .center(location)
                        .fillColor(Color.argb(90, 255, 153, 0))
                        .strokeColor(Color.argb(190, 255, 152, 0))
                        .radius(50) //metros
        );


        recuperarLocalizacaoMotorista();


    }

    //Os parametros da assinatura deste metodo possuem nomes genericos porque nem sempre serao chamados os mesmos marcadores (hora sera marcador motorista e passageiro, hora sera motorista e destino)

    private void centralizarMarcadores(Marker marker1, Marker marker2) {

        Log.d(TAG, "Passo 5: O metodo 'centralizar Marcadores' foi chamado");


        LatLngBounds.Builder builder = LatLngBounds.builder();
        builder.include(marker1.getPosition());
        builder.include(marker2.getPosition());


        LatLngBounds bounds = builder.build();

        //Get the width and height of the device's screen
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = (int) ((getResources().getDisplayMetrics().heightPixels) * 0.85);
        int padding = (int) (width * 0.15);

        //Centralize the markers
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding));

        //Ativa o botao de "Aceitar corrida"
        Log.d(TAG, "Passo 6: Botao 'Ativar' foi liberado para o usuario");
        layoutAtivarBotaoAceitar();

        //Ativa o listener do firebase que ira acompanhar o status da requisicao, caso ainda nao tenha sido ativado
        if (mRefRequisicao == null) {
            firebaseAtivarListenerRequisicao();
        }


    }

    private void firebaseAtivarListenerRequisicao() {

        Log.d(TAG, "Passo 7 : Listener do firebase ativado, este ira acompanhar o status da requisicao");

        mRefRequisicao = ConfigFirebase.getDatabaseReference()
                .child("requisicoes").child(mRequisicao.getId());

        mRefRequisicao.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Requisicao requisicao = dataSnapshot.getValue(Requisicao.class);

                if (requisicao.getStatus() != null) {

                    switch (requisicao.getStatus()) {

                        case Requisicao.STATUS_WAITING:
                            break;

                        case Requisicao.STATUS_ON_THE_WAY:
                            mButtonAceitar.setText(R.string.map_a_caminho_do_passageiro);
                            /**
                             * Inicia o listener do geofire que ira verifica a posicao do motorista em relacao
                             * ao passageiro, e entao executar açoes especificas quando o motorista chegar em
                             * um raio de 50m do passageiro
                             */
                            layoutAtivarBotaoRotas();
                            firebaseAtivarListenerLocalizacaoGeoFire();
                            break;

                        case Requisicao.STATUS_TRAVELING:
                            mButtonAceitar.setText(R.string.map_a_caminho_do_destino);
                            break;

                        default:
                            break;


                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void layoutAtivarBotaoRotas() {

        mFabRotas.setVisibility(View.VISIBLE);

    }

    private void firebaseAtivarListenerLocalizacaoGeoFire() {

        /**
         * Coordenadas do passageiro, estas coordenas serao as que o GeoQuery ira utilizar para comparar com a
         Localizaçao do motorista
         **/
        double latitude = mRequisicao.getLatitude();
        double longitude = mRequisicao.getLongitude();

        //Referencia da localizacao do motorista, esta referencia tera sempre a posicao do motorista em tempo real
        DatabaseReference refLocalizacoesUsuarios = ConfigFirebase.getDatabaseReference()
                .child("localizacoes_usuarios");
        GeoFire geoFire = new GeoFire(refLocalizacoesUsuarios);
        GeoLocation geoLocation = new GeoLocation(latitude, longitude);
        final GeoQuery geoQuery = geoFire.queryAtLocation(geoLocation, 0.05); //Radius em kilometros
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(key.equals(UserProfile.getMotoristaLogado().getId())){
                    //Avisa o motorista para que procure o passageiro
                    Toast.makeText(CorridaActivity.this, "Procure o passageiro", Toast.LENGTH_LONG).show();

                    //Remove o listener do geoquery
                    geoQuery.removeAllListeners();

                    //Atualiza o status da requisicao
                    mRequisicao.setStatus(Requisicao.STATUS_TRAVELING);
                    mRequisicao.atualizarStatus();

                    //Insere o marcador do destino
                    adicionarMarcadorDestino();
                }
            }

            @Override
            public void onKeyExited(String key) {
                Log.d(TAG, "onKeyExited");
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                Log.d(TAG, "onKeyMoved");

            }

            @Override
            public void onGeoQueryReady() {
                Log.d(TAG, "onGeoQueryReady");

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.d(TAG, "onGeoQueryError");
            }
        });


    }

    private void adicionarMarcadorDestino() {

        isDestinoAtivo = true;

        Destino destino = mRequisicao.getDestination();

        LatLng latLng = new LatLng(Double.parseDouble(destino.getLatitude()),Double.parseDouble(destino.getLongitute()));

        if(mMarkerDestino != null){
            mMarkerDestino.remove();
        }

        mMarkerDestino = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.destino))
                .title(getString(R.string.map_marker_destination)));

        Log.d(TAG, "Passo 10: Marcador do destino foi inserido");

        centralizarMarcadores(mMarkerMotorista, mMarkerDestino);

        //Insere um circulo ao redor do marcador do destino, para representar a area de 50m ao redor dele
        Circle circle = mMap.addCircle(
                new CircleOptions()
                        .center(latLng)
                        .fillColor(Color.argb(90, 255, 153, 0))
                        .strokeColor(Color.argb(190, 255, 152, 0))
                        .radius(50) //metros
        );




    }

    private void layoutAtivarBotaoAceitar() {

        mButtonAceitar.setVisibility(View.VISIBLE);

    }

    public void onClickButtonAceitar(View view) {

        //Recupera os dados do motorista
        Motorista motorista = UserProfile.getMotoristaLogado();

        //Atualiza a requisicao do passageiro
        mRequisicao.setDriver(motorista);
        mRequisicao.setStatus(Requisicao.STATUS_ON_THE_WAY);
        mRequisicao.atualizar();


    }

    public void onClickFabRota(View view){

        String latitude = "";
        String longitude = "";

        if(isDestinoAtivo){
            Destino destino = mRequisicao.getDestination();
            latitude = destino.getLatitude();
            longitude = destino.getLongitute();
        } else{
            latitude = String.valueOf(mRequisicao.getLatitude());
            longitude = String.valueOf(mRequisicao.getLongitude());

        }
        String coordenadas = latitude + ", " + longitude;

        Uri uri = Uri.parse("google.navigation:q=" + coordenadas + "&mode=d");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);

    }

    private Requisicao recuperarRequisicao() {

        Bundle extras = getIntent().getExtras();

        if (extras.containsKey("requisicao")) {
            Requisicao requisicao = (Requisicao) extras.getSerializable("requisicao");

            return requisicao;
        }

        return null;
    }

    private void adicionarMarcadorMotorista(Location location) {

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        if (mMarkerMotorista != null) {
            mMarkerMotorista.remove();
        }

        mMarkerMotorista = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.carro))
                .title("Voce esta aqui"));

        Log.d(TAG, "Passo 4 : Marcador do motorista inserido");

        centralizarMarcadores(mMarkerMotorista, mMarkerPassageiro);


    }

    private void recuperarLocalizacaoMotorista() {

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG, "Passo 3: Localizacao recebida");
                adicionarMarcadorMotorista(location);
                UserProfile.atualizaGeoFireLocalizacaoUsuario(location.getLatitude(), location.getLongitude());


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
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d(TAG, "Passo 1 : metodo onMapReady chamado.");
        adicionarMarcadorPassageiro();
    }
}






