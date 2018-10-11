package com.app.mobile.entregasclair.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.mobile.entregasclair.R;
import com.app.mobile.entregasclair.adapter.RequisicaoAdapter;
import com.app.mobile.entregasclair.config.ConfigFirebase;
import com.app.mobile.entregasclair.helper.LocalizaoHelper;
import com.app.mobile.entregasclair.helper.RecyclerItemClickListener;
import com.app.mobile.entregasclair.helper.UserProfile;
import com.app.mobile.entregasclair.model.Motorista;
import com.app.mobile.entregasclair.model.Requisicao;
import com.app.mobile.entregasclair.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HomeMotoristaActivity extends AppCompatActivity {

    private static final String TAG = HomeMotoristaActivity.class.getSimpleName();

    private TextView mTextViewNoPassagensWarning;
    private RecyclerView mRecyclerView;
    private RequisicaoAdapter mAdapter;
    private List<Requisicao> mListRequests;


    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private Motorista mMotorista;




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_motorista_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.menu_sair :
                ConfigFirebase.getFirebaseAuth().signOut();
                finish();
                break;


            case R.id.menu_dados:
                startActivity(new Intent(HomeMotoristaActivity.this, DadosUsuarioActivity.class));
                break;

            case android.R.id.home:
                finish();
                return true;

            default:
                break;


        }

        return super.onOptionsItemSelected(item);



    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_motorista);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initialize the layout components
        inicializaComponentesDeLayout();
        inicializaAtributos();

        //Configure recyclerview
        configuraRecyclerView();

        //Gets the driver's location
        recuperarLocalizacaoUsuario();

        //Display either a recyclerview with the passengers or a warning message
        recuperaRequisicoes();
    }

    private void inicializaComponentesDeLayout(){
        mTextViewNoPassagensWarning = findViewById(R.id.tv_warner);
        mRecyclerView = findViewById(R.id.rv);

        mRecyclerView
                .addOnItemTouchListener(
                        new RecyclerItemClickListener(
                                this,
                                mRecyclerView,
                                new RecyclerItemClickListener.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(View view, int position) {
                                        Requisicao requisicao = mListRequests.get(position);
                                        Motorista motorista = UserProfile.getMotoristaLogado();
                                        Intent intent = new Intent(HomeMotoristaActivity.this, CorridaActivity.class);
                                        intent.putExtra("idRequisicao", requisicao.getId());
                                        intent.putExtra("requisicao", requisicao);
                                        intent.putExtra("motorista", motorista);
                                        startActivity(intent);
                                    }

                                    @Override
                                    public void onLongItemClick(View view, int position) {

                                    }

                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                    }
                                }
                        ));
    }

    private void inicializaAtributos(){
        mListRequests = new ArrayList<>();
        mMotorista = UserProfile.getMotoristaLogado();
        mAdapter = new RequisicaoAdapter(mListRequests);
    }

    private void configuraRecyclerView(){
        //Set the adapter
        mRecyclerView.setAdapter(mAdapter);

        //Set the layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        //Set the item decoretor
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    private void recuperarLocalizacaoUsuario() {
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                String latitude = String.valueOf(location.getLatitude());
                    String longitude = String.valueOf(location.getLongitude());

                mMotorista.setLatitude(latitude);
                mMotorista.setLongitude(longitude);

                if(mListRequests.size() > 0){
                    for(Requisicao requisicao : mListRequests){
                        Location locationRequisicao = new Location("Localizacao usuario");
                        locationRequisicao.setLatitude(requisicao.getLatitude());
                        locationRequisicao.setLongitude(requisicao.getLongitude());

                        //Este metodo calcula a distancia entre a distancia atual do motorista e a localizacao do usuario
                        double distanciaMotoristaPassageiro = LocalizaoHelper.calcularDistancia(location, locationRequisicao);
                        requisicao.setDistance(distanciaMotoristaPassageiro);

                        mAdapter.notifyDataSetChanged();
                    }
                }
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
                    LocationManager.NETWORK_PROVIDER,
                    10000,
                    10,
                    mLocationListener);
        } else {
            Toast.makeText(this, "Voce tem problemas com permissoes", Toast.LENGTH_SHORT).show();
        }

    }


    private void recuperaRequisicoes(){
        DatabaseReference refRequisicoes = ConfigFirebase.getDatabaseReference()
                .child("requisicoes");

        Query queryRequisicoesPorStatus = refRequisicoes.orderByChild("status").equalTo(Requisicao.STATUS_WAITING);

        queryRequisicoesPorStatus.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //Checks whether there are passengers waiting for a ride, if there are, the recycler view will be
                //shown, if not, a message for the user will be shown in order to warn that there aren't passengers
                if(dataSnapshot.getChildrenCount() > 0){
                    mTextViewNoPassagensWarning.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mListRequests.clear();
                    for (DataSnapshot data : dataSnapshot.getChildren()){
                        Requisicao requisicao = data.getValue(Requisicao.class);
                        mListRequests.add(requisicao);
                        mAdapter.notifyDataSetChanged();
                        Log.d(TAG, requisicao.getId());
                    }

                } else{
                    mTextViewNoPassagensWarning.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.GONE);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
