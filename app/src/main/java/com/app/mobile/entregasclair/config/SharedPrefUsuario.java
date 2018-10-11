package com.app.mobile.entregasclair.config;


import android.content.Context;
import android.content.SharedPreferences;

import com.app.mobile.entregasclair.model.Usuario;

public class SharedPrefUsuario {

    private static final String NOME_SHARED_PREFERENCES = "preferences";

    //Chaves para salvar os dados do usuario logado
    private static String USUARIO_LOGADO_EMAIL = "USUARIO_LOGADO_EMAIL";
    private static String USUARIO_LOGADO_NOME = "USUARIO_LOGADO_NOME";
    private static String USUARIO_LOGADO_TIPO = "USUARIO_LOGADO_TIPO";
    private static String USUARIO_LOGADO_ID = "USUARIO_LOGADO_ID";

    private static SharedPreferences getSP(Context context) {

        return context.getSharedPreferences(NOME_SHARED_PREFERENCES, Context.MODE_PRIVATE);
    }


    public static void salvarDadosUsuarioLogado(Usuario usuario, Context context) {

        SharedPreferences sp = getSP(context);

        //Salva dados do usuario
        sp.edit().putString(USUARIO_LOGADO_ID, usuario.getId()).apply();
        sp.edit().putString(USUARIO_LOGADO_EMAIL, usuario.getEmail()).apply();
        sp.edit().putString(USUARIO_LOGADO_NOME, usuario.getNome()).apply();
        sp.edit().putString(USUARIO_LOGADO_TIPO, usuario.getTipo()).apply();
    }

    public static Usuario recuperaUsuarioLogado(Context context) {

        SharedPreferences sp = getSP(context);

        if (sp.contains(USUARIO_LOGADO_ID)) {
            String id = sp.getString(USUARIO_LOGADO_ID, "");
            String email = sp.getString(USUARIO_LOGADO_EMAIL, "");
            String nome = sp.getString(USUARIO_LOGADO_NOME, "");
            String tipo = sp.getString(USUARIO_LOGADO_TIPO, "");

            return new Usuario(id, nome, email, tipo);
        } else {
            return null;
        }

    }


}

