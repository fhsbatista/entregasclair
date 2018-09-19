package com.app.mobile.fast.helper;

import android.location.Location;

public class LocalizaoHelper {

    public static double calcularDistancia(Location locationInicial, Location localFinal){

        double distancia = locationInicial.distanceTo(localFinal);

        return distancia;

    }


}
