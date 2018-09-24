package com.app.mobile.fast.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.app.mobile.fast.R;
import com.app.mobile.fast.helper.LocalizaoHelper;
import com.app.mobile.fast.model.Passageiro;
import com.app.mobile.fast.model.Requisicao;
import com.app.mobile.fast.model.Usuario;

import java.util.List;

public class RequisicaoAdapter extends RecyclerView.Adapter<RequisicaoAdapter.MyViewHolder> {

    private List<Requisicao> mRequisicaoList;

    public RequisicaoAdapter(List<Requisicao> list){

        this.mRequisicaoList = list;

    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_requisicoes, parent, false);
        return new MyViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Requisicao requisicao = mRequisicaoList.get(position);
        Usuario usuario = requisicao.getPassenger();
        if(requisicao.getDistance() == null){
            holder.distance.setText("Calculando distancia");
        } else{
            holder.distance.setText(LocalizaoHelper.criarStringDistanciaEstilizada(requisicao.getDistance()));
        }
        holder.userName.setText(usuario.getNome());




    }

    @Override
    public int getItemCount() {
        return this.mRequisicaoList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        private TextView userName;
        private TextView distance;

        public MyViewHolder(View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.tv_user_name);
            distance = itemView.findViewById(R.id.tv_distance);
        }
    }
}
