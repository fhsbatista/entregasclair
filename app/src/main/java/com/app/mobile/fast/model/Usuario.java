package com.app.mobile.fast.model;

import android.graphics.Bitmap;

import com.app.mobile.fast.config.ConfigFirebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import java.io.Serializable;

public class Usuario implements Serializable{

    private String id;
    private String nome;
    private String email;
    private String senha;
    private String tipo;

    public Usuario() {
    }

    public Usuario(String id, String nome, String email) {
        this.id = id;
        this.nome = nome;
        this.email = email;
    }

    public void salvarNoFirebase(){

        //Recuperando o id do usuario
        FirebaseAuth auth = ConfigFirebase.getFirebaseAuth();
        String idUsuario = auth.getCurrentUser().getUid();

        //Definindo o id do usuario como sendo o id recuperado acima
        this.id = idUsuario;

        DatabaseReference firebaseRef = ConfigFirebase.getDatabaseReference();
        firebaseRef.child("usuarios").child(this.id).setValue(this);
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Exclude
    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
