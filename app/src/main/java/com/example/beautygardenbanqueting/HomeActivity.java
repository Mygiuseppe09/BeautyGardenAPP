package com.example.beautygardenbanqueting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/***************************************************************************************************
 Quest'activity è quella relativa alla Home di un utente "normale" e non superuser/admin.

 Quello che fa è, oltre a mostare il nome di chi è loggato con un saluto annesso, mostrare la lista
 delle sale reperite dalla collection "items" nel database all'interno di un RecyclerView.
 Ad ogni oggetto della recycler view è associato un listener al click che rimanda all'ItemAcitviy.
 **************************************************************************************************/

public class HomeActivity extends AppCompatActivity implements ItemAdapter.onItemClickListener {

    // oggetti che rappresentano i punti di accesso a: database Firebase e autenticazione Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase db;

    // elementi del file xml
    private TextView hiUser;
    private ImageView profileTab;
    private ImageView homeTab;
    private ImageView logoutTab;

    private RecyclerView contents;
    private ItemAdapter itemAdapter; // Estrae i dati delle sale dal database
    private FirebaseRecyclerOptions<Item> options; // setta la query

    // oggetto della classe User che useremo per reperire informazioni durante le queries
    private User loggedUser;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // otteniamo le istanze relative a Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

        // salvataggio dei riferimenti dei relativi componenti xml
        profileTab = (ImageView) findViewById(R.id.profileIcon);
        homeTab = (ImageView) findViewById(R.id.homeIcon);
        logoutTab = (ImageView) findViewById(R.id.logoutIcon);
        hiUser = (TextView) findViewById(R.id.hiUserTextView);

        //Per la RecyclerView
        contents = (RecyclerView) findViewById(R.id.contentsRecyclerView);
        contents.setLayoutManager(new LinearLayoutManager(this));

        options = new FirebaseRecyclerOptions.Builder<Item>()
                        .setQuery(db.getReference().child("items"), Item.class)
                        .build();

        // reperiamo il nome dell'utente loggato e lo mettiamo in alto
        if (mAuth.getCurrentUser() != null)
        db.getReference("users")
                .child(mAuth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()  && snapshot.getValue() != null) {
                            loggedUser = snapshot.getValue(User.class);
                            if (loggedUser != null) {
                                hiUser.setText("Ciao, " + loggedUser.getName() + "!");
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(HomeActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();


        // per la RecyclerView
        itemAdapter = new ItemAdapter(options);
        contents.setAdapter(itemAdapter);

        itemAdapter.startListening();

        // per il click di ogni elemento
        itemAdapter.setOnItemClickListener(this);

        // listeners ai click sulla navbar
        logoutTab.setOnClickListener(this::onClick);
        profileTab.setOnClickListener(this::onClick);

    }

    @Override
    protected void onStop() {
        super.onStop();

        itemAdapter.stopListening();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        itemAdapter.startListening();
    }


    @SuppressLint("NonConstantResourceId")
    private void onClick(View view) {
        switch (view.getId()) {
            case R.id.logoutIcon:
                mAuth.signOut();
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                finish();
                break;
            case R.id.profileIcon:
                // quando clicchiamo sul "tab" utente, andiamo nell'activity dove mostriamo le informazioni
                // dell'utente ma rimuoviamo le animazioni.
                startActivity(new Intent(HomeActivity.this, UserActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                break;

        }
    }

    // metodo che si attiva al click dei un elemento della RecyclerView
    @Override
    public void onItemClick(DataSnapshot snapshot, int position) {
        // al click recuperiamo l'id dell'oggetto e lo passiamo all'activity "itemActivity"
        String uniqueId = snapshot.getKey();
        if (uniqueId != null && !uniqueId.isEmpty()) {
            startActivity(new Intent(HomeActivity.this, ItemActivity.class)
                    .putExtra("id", uniqueId));
        }
    }

}