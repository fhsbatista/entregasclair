package com.app.mobile.fast.model;

import java.io.Serializable;

public class Motorista extends Usuario {

    private String latitude;
    private String longitude;

    public Motorista(){

    }

    public Motorista(Usuario usuario){
        super(usuario.getId(), usuario.getNome(), usuario.getEmail());
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
