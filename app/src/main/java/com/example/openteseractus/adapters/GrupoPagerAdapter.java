package com.example.openteseractus.adapters;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.openteseractus.ui.fragments.CarteleraFragment;
import com.example.openteseractus.ui.fragments.MiembrosFragment;

/**
 * Adapter de ViewPager2 para la pantalla de grupo.
 * Gestiona dos pestañas: posición 0 → {@link com.example.openteseractus.ui.fragments.CarteleraFragment},
 * posición 1 → {@link com.example.openteseractus.ui.fragments.MiembrosFragment}.
 * Ambos fragmentos reciben el ID del grupo a través de un {@link android.os.Bundle}.
 */
public class GrupoPagerAdapter extends FragmentStateAdapter {

    private final String idGrupo;

    public GrupoPagerAdapter(@NonNull AppCompatActivity activity, String idGrupo) {
        super(activity);
        this.idGrupo = idGrupo;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        Fragment fragment;

        if (position == 0) {
            fragment = new CarteleraFragment();
        } else {
            fragment = new MiembrosFragment();
        }

        fragment.setArguments(getBundle());

        return fragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    private Bundle getBundle() {
        Bundle bundle = new Bundle();
        bundle.putString("GRUPO_ID", idGrupo);
        return bundle;
    }
}