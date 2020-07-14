package com.example.g_bag.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.g_bag.Mochila;
import com.example.g_bag.R;

import java.util.ArrayList;

public class HomeFragment extends Fragment {


    private RecyclerView recyclerView;
    ArrayList<String> listaDatos;
    AdapterDatos adapter;

    public HomeFragment() {
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = (RecyclerView) root.findViewById(R.id.recyclerMochilas);
        adapter = new AdapterDatos(listaDatos,getContext());
        //recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext(),LinearLayoutManager.VERTICAL,false));
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));



        return root;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listaDatos = new ArrayList<>();
        for (int i =0; i<5; i++){
            Mochila mochila = new Mochila(String.valueOf(i));
            listaDatos.add("Mochila"+mochila.getIdDispositivo());

        }
    }
}