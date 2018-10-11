package com.app.mobile.entregasclair.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.TextView;

import com.app.mobile.entregasclair.R;
import com.app.mobile.entregasclair.config.SharedPrefUsuario;
import com.app.mobile.entregasclair.model.Usuario;

public class DadosUsuarioActivity extends AppCompatActivity {

    
    private TextView mTextViewId, mTextViewNome, mTextViewEmail, mTextViewTipo;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dados_usuario);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTextViewId = findViewById(R.id.et_id_user);
        mTextViewEmail = findViewById(R.id.et_email_user);
        mTextViewNome = findViewById(R.id.et_nome_user);
        mTextViewTipo = findViewById(R.id.et_tipo_user);

        Usuario usuario = SharedPrefUsuario.recuperaUsuarioLogado(this);

        mTextViewId.setText(usuario.getId());
        mTextViewNome.setText(usuario.getNome());
        mTextViewEmail.setText(usuario.getEmail());
        mTextViewTipo.setText(usuario.getTipo());
        
        
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
