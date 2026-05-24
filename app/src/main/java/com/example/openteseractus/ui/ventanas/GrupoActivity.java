package com.example.openteseractus.ui.ventanas;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.openteseractus.R;
import com.example.openteseractus.adapters.GrupoPagerAdapter;
import com.example.openteseractus.repositorios.GrupoRepository;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class GrupoActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private GrupoPagerAdapter adapter;

    private String idGrupo;
    private GrupoRepository grupoRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_grupo);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Obtener ID del grupo
        idGrupo = getIntent().getStringExtra("GRUPO_ID");
        grupoRepository = new GrupoRepository();

        // Vistas
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        // Adapter
        adapter = new GrupoPagerAdapter(this, idGrupo);
        viewPager.setAdapter(adapter);

        // Tabs
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText(R.string.group_billboard);
                    } else {
                        tab.setText(R.string.group_members);
                    }
                }).attach();
    }

}