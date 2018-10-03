package com.app.mobile.entregasclair.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.app.mobile.entregasclair.R;
import com.app.mobile.entregasclair.config.ConfigFirebase;
import com.app.mobile.entregasclair.helper.Permissoes;
import com.app.mobile.entregasclair.helper.UserProfile;

public class MainActivity extends AppCompatActivity {

    private String[] permissoes = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        //Validando as permissoes
        Permissoes.validarPermissoes(permissoes, this, 1);




    }

    public void abrirLogin(View view){
        startActivity(new Intent(this, LoginActivity.class));
    }

    public void abrirCadastro(View view){
        startActivity(new Intent(this, CadastroActivity.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        UserProfile.redirecionarUsuario(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for(int permissaoResultado : grantResults){
            if(permissaoResultado == PackageManager.PERMISSION_DENIED){
                alertaPermissaoNecessaria();
            }
        }
    }

    private void alertaPermissaoNecessaria() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissoes necessarias");
        builder.setMessage("Para que o app possa ser iniciado, e necessario que todas as permissoes sejam dadas");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();




    }
}

