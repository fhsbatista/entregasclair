package com.app.mobile.entregasclair.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.app.mobile.entregasclair.R;
import com.app.mobile.entregasclair.config.ConfigFirebase;
import com.app.mobile.entregasclair.helper.UserProfile;
import com.app.mobile.entregasclair.model.Passageiro;
import com.app.mobile.entregasclair.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class CadastroActivity extends AppCompatActivity {

    private EditText mNome, mEmail, mSenha;
    private Switch mTipo;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        mNome = findViewById(R.id.et_nome);
        mEmail = findViewById(R.id.et_email);
        mSenha = findViewById(R.id.et_senha);
        mTipo = findViewById(R.id.swt_tipo);
    }

    public void cadastrarUsuario(View view){

        //Primeiro serao validados os campos digitados, caso eles tenham sido digitados corretamente, o cadastro sera iniciado
        if(validarCamposDigitados()){
            final Passageiro passageiro = new Passageiro();
            passageiro.setNome(mNome.getText().toString());
            passageiro.setEmail(mEmail.getText().toString());
            passageiro.setSenha(mSenha.getText().toString());
            passageiro.setTipo(verificarTipoUsuario());

            mAuth = ConfigFirebase.getFirebaseAuth();
            mAuth.createUserWithEmailAndPassword(passageiro.getEmail(), passageiro.getSenha())
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        passageiro.salvarNoFirebase();
                        UserProfile.atualizarNomeUsuario(passageiro.getNome());
                        if(passageiro.getTipo().equals("Driver"))
                            startActivity(new Intent(CadastroActivity.this, HomeMotoristaActivity.class));
                        else
                            startActivity(new Intent(CadastroActivity.this, HomePassageiroActivity.class));
                        finish();

                    }else{
                        String errorMessage = "";
                        try{
                            throw task.getException();
                        }catch(FirebaseAuthWeakPasswordException e){
                            errorMessage = "Digite uma senha mais forte";
                        }catch (FirebaseAuthInvalidCredentialsException e){
                            errorMessage = "Digite um e-mail valido";
                        }catch(FirebaseAuthUserCollisionException e){
                            errorMessage = "Ja existe um cadastro com e-mail informado";
                        }catch (Exception e){
                            errorMessage = e.getMessage();
                            e.printStackTrace();
                        }
                        Toast.makeText(CadastroActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }


    private boolean validarCamposDigitados(){


        String nome = mNome.getText().toString();
        String senha = mSenha.getText().toString();
        String email = mEmail.getText().toString();


        //Validando se os campos foram preenchidos corretamente
        if(nome.isEmpty()){
            Toast.makeText(this, "Digite seu nome", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(senha.isEmpty()) {
            Toast.makeText(this, "Digite uma senha", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(email.isEmpty()){
            Toast.makeText(this, "Digite um e-mail", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;

    }

    private String verificarTipoUsuario(){
        return mTipo.isChecked() ? "Driver" : "Passenger";
    }
}