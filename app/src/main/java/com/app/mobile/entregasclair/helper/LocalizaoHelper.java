package com.app.mobile.entregasclair.helper;

import android.location.Location;

import java.text.DecimalFormat;

public class LocalizaoHelper {

    public static double calcularDistancia(Location locationInicial, Location localFinal){

        double distancia = locationInicial.distanceTo(localFinal);

        return distancia;

    }

    public static String criarStringDistanciaEstilizada(double distancia){

        DecimalFormat df = new DecimalFormat("0.00");



        if(distancia < 1000) {
            return df.format(distancia) + " metros";
        } else{
            return df.format(distancia / 1000) + " Kilometros";
        }

    }


}
