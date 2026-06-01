package com.example.openteseractus.ui.ventanas;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.openteseractus.R;
import com.example.openteseractus.adapters.GrupoPagerAdapter;
import com.example.openteseractus.callbacks.FirestoreCallback;
import com.example.openteseractus.repositorios.GrupoRepository;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class GrupoActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private GrupoPagerAdapter adapter;

    private String idGrupo;
    private GrupoRepository grupoRepository;
    private ListenerRegistration membershipListener;

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

        idGrupo = getIntent().getStringExtra("GRUPO_ID");
        grupoRepository = new GrupoRepository();

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        adapter = new GrupoPagerAdapter(this, idGrupo);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText(R.string.group_billboard);
                    } else {
                        tab.setText(R.string.group_members);
                    }
                }).attach();

        iniciarListenerMembership();
    }

    private void iniciarListenerMembership() {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid == null) { finish(); return; }

        membershipListener = FirebaseFirestore.getInstance()
                .collection("grupos")
                .document(idGrupo)
                .collection("miembros")
                .document(uid)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null) return;
                    if (!snapshot.exists()) {
                        // Borrar la ref del grupo en el propio usuario y volver al Home
                        grupoRepository.eliminarRefGrupoDeUsuario(uid, idGrupo,
                                new FirestoreCallback<Void>() {
                                    @Override public void onSuccess(Void v) { salirPorExpulsion(); }
                                    @Override public void onFailure(String e) { salirPorExpulsion(); }
                                });
                    }
                });
    }

    private void salirPorExpulsion() {
        Toast.makeText(this, getString(R.string.expelled_from_group), Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (membershipListener != null) membershipListener.remove();
    }
}