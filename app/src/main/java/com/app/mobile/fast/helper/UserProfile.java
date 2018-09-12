package com.app.mobile.fast.helper;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.app.mobile.fast.activity.CorridaActivity;
import com.app.mobile.fast.activity.HomeMotoristaActivity;
import com.app.mobile.fast.activity.HomePassageiroActivity;
import com.app.mobile.fast.config.ConfigFirebase;
import com.app.mobile.fast.model.Motorista;
import com.app.mobile.fast.model.Passageiro;
import com.app.mobile.fast.model.Requisicao;
import com.app.mobile.fast.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class UserProfile {

    public static final String TAG = UserProfile.class.getSimpleName();

    public static FirebaseUser getFirebaseUserLogado(){
        FirebaseAuth auth = ConfigFirebase.getFirebaseAuth();
        FirebaseUser user = auth.getCurrentUser();

        return user;
    }

    private static Usuario getUsuarioLogado(){
        FirebaseUser firebaseUser = getFirebaseUserLogado();
        Usuario usuario = new Usuario();
        usuario.setId(firebaseUser.getUid());
        usuario.setEmail(firebaseUser.getEmail());
        usuario.setNome(firebaseUser.getDisplayName());

        return usuario;
    }

    public static Motorista getMotoristaLogado(){
        Usuario usuario = getUsuarioLogado();

        return new Motorista(usuario);
    }

    public static Passageiro getPassageiroLogado(){
        Usuario usuario = getUsuarioLogado();

        return new Passageiro(usuario);
    }

    public static boolean atualizarNomeUsuario(String nome){

        try {
            FirebaseUser user = getFirebaseUserLogado();
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                    .setDisplayName(nome)
                    .build();
            user.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Log.d(TAG, "Erro ao atualizar o perfil do usuario");
                }
            });
            return true;

        }catch (Exception e){
            e.printStackTrace();
            return false;
        }



    }


    public static void redirecionarUsuario(final Activity activity) {



        if (getFirebaseUserLogado() != null) {
            String idUsuario = getIdUsuario();

            DatabaseReference userReference = ConfigFirebase.getDatabaseReference();
            userReference.child("usuarios").child(idUsuario)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Passageiro passageiro = dataSnapshot.getValue(Passageiro.class);

                            if (passageiro.getTipo().equals("Driver")){

                                //Verify whether the driver already has some open ride
                                DatabaseReference refRequest = ConfigFirebase.getDatabaseReference().child("requisicoes_abertas_motoristas")
                                        .child(UserProfile.getMotoristaLogado().getId());

                                refRequest.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        //If the datasnapshot has some child, that means there is some open ride, so the corridaactivity will be open instead of homemotoristaactivity
                                        if(dataSnapshot.getValue() != null){

                                            Requisicao requisicao = dataSnapshot.child("requisicao").getValue(Requisicao.class);
                                            Motorista motorista = getMotoristaLogado();

                                            Intent intent = new Intent(activity, CorridaActivity.class);
                                            intent.putExtra("requisicao", requisicao);
                                            intent.putExtra("motorista", motorista);
                                            activity.startActivity(intent);

                                        } else{
                                            activity.startActivity(new Intent(activity, HomeMotoristaActivity.class));
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });




                            }
                            else {
                                activity.startActivity(new Intent(activity, HomePassageiroActivity.class));
                            }
                            activity.finish();

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }








    }


    private static String getIdUsuario() {
        return getFirebaseUserLogado().getUid();
    }
}
