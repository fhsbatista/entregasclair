package com.app.mobile.entregasclair.model;

public class Passageiro extends Usuario {

    public Passageiro() {
    }

    public Passageiro(Usuario usuario) {
        super(usuario.getId(), usuario.getNome(), usuario.getEmail());
    }
}
