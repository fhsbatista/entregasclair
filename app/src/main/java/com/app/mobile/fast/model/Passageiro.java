package com.app.mobile.fast.model;

public class Passageiro extends Usuario {

    public Passageiro() {
    }

    public Passageiro(Usuario usuario) {
        super(usuario.getId(), usuario.getNome(), usuario.getEmail());
    }
}
