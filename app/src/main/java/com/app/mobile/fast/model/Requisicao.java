package com.app.mobile.fast.model;

import com.app.mobile.fast.config.ConfigFirebase;
import com.google.firebase.database.DatabaseReference;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Requisicao implements Serializable {

    public static final String STATUS_WAITING = "Aguardando";
    public static final String STATUS_ON_THE_WAY = "A Caminho";
    public static final String STATUS_TRAVELING = "Em Andamento";
    public static final String STATUS_COMPLETED = "Finalizada";
    public static final String STATUS_CANCELED = "Cancelada";

    private String id;
    private String status;
    private Passageiro passenger;
    private Motorista driver;
    private Destino destination;
    private Double latitude;
    private Double longitude;

    public Requisicao(){

    }

    public void atualizar(){
        DatabaseReference refRequisicao = ConfigFirebase.getDatabaseReference()
                .child("requisicoes").child(this.getId());

        Map requisicao = new HashMap();
        requisicao.put("driver", this.getDriver());
        requisicao.put("status", this.getStatus());

        refRequisicao.updateChildren(requisicao);
    }

    public void setPassenger(Passageiro passenger) {
        this.passenger = passenger;
    }

    public void setDriver(Motorista driver) {
        this.driver = driver;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Passageiro getPassenger() {
        return passenger;
    }

    public Motorista getDriver() {
        return driver;
    }

    public Destino getDestination() {
        return destination;
    }

    public void setDestination(Destino destination) {
        this.destination = destination;
    }

    public void salvarRequisicao() {
        //Save the request of ride
        DatabaseReference refRequest = ConfigFirebase.getDatabaseReference().child("requisicoes");
        this.setId(refRequest.push().getKey());
        refRequest.child(this.getId()).setValue(this);

        salvarReferenciaUsuarioRequisicao();




    }

    private void salvarReferenciaUsuarioRequisicao(){

        DatabaseReference refUserRequestReference = ConfigFirebase.getDatabaseReference()
                .child("requisicoes_usuarios")
                .child(this.getPassenger().getId())
                .child("idRequisicao");

        refUserRequestReference.setValue(this.id);
    }

    public void cancelarRequisicao(){
        //Set the request as canceled
        this.setStatus(STATUS_CANCELED);

        //Create a firebase database reference which will be used to set the request which has been made by the user
        //before and set it as "Canceled"
        DatabaseReference refRequest = ConfigFirebase.getDatabaseReference().child("requisicoes")
                .child(this.getId())
                .child("status");
        refRequest.setValue(STATUS_CANCELED);

        cancelarReferenciaUsuarioRequisicao();

    }

    private void cancelarReferenciaUsuarioRequisicao() {
        //Cancel a reference between the ride and the user which will be used to control whether the user
        //already has a ride to complete

        DatabaseReference refUserRequestReference = ConfigFirebase.getDatabaseReference()
                .child("requisicoes_usuarios")
                .child(this.getPassenger().getId());

        refUserRequestReference.removeValue();


    }
}
