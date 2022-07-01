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

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/***************************************************************************************************
 Quest'activity è quella relativa alla visualizzazione delle informazioni e della wishlist dell'utente.

 Per quanto riguarda la lista dei desideri, questa è semplicemente la stessa RecyclerView usata nella
 home ma filtrata in funzione delle sale che l'utente ha aggiunto.
 **************************************************************************************************/

public class UserActivity extends AppCompatActivity implements ItemAdapter.onItemClickListener  {

    // oggetti che rappresentano i punti di accesso a: database Firebase e autenticazione Firebase
    private FirebaseDatabase db;
    private FirebaseAuth mAuth;

    // oggetto della classe User che useremo per reperire informazioni durante le queries
    private User loggedUser;

    // elementi del file xml
    private TextView nameView;
    private TextView surnameView;
    private TextView emailView;
    private ImageView homeTab;
    private ImageView logoutTab;

    private RecyclerView contents;
    private ItemAdapter itemAdapter;
    private FirebaseRecyclerOptions<Item> options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        nameView = (TextView) findViewById(R.id.nameView);
        surnameView = (TextView) findViewById(R.id.surnameView);
        emailView = (TextView) findViewById(R.id.emailView);
        homeTab = (ImageView) findViewById(R.id.homeIcon);
        logoutTab = (ImageView) findViewById(R.id.logoutIcon);


        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

        //Per la RecyclerView:
        // collegamento alla RycicleView all'interno della activity_user.xml
        contents = (RecyclerView) findViewById(R.id.wishlistRecyclerView);
        contents.setLayoutManager(new LinearLayoutManager(this));
        // ne impostiamo la query
        options = new FirebaseRecyclerOptions.Builder<Item>()
                .setQuery(db.getReference("users").child(mAuth.getCurrentUser().getUid()).child("wishlist"), Item.class)
                .build();

        // reperiamo le informazioni dell'utente loggato e le mettiamo in alto
        db.getReference("users")
                .child(mAuth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()  && snapshot.getValue() != null) {
                            loggedUser = snapshot.getValue(User.class);
                            if (loggedUser != null) {
                                nameView.setText(loggedUser.getName());
                                surnameView.setText(loggedUser.getSurname());
                                emailView.setText(loggedUser.getEmail());
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
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


        // background per la wishlist vuota
        db.getReference("users")
                .child(mAuth.getCurrentUser().getUid())
                .child("wishlist")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                                contents.setBackgroundResource(R.drawable.notingh_in_wishlist);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });

        // listeners ai click sulla navbar
        homeTab.setOnClickListener(this::onClick);
        logoutTab.setOnClickListener(this::onClick);
    }


    @SuppressLint("NonConstantResourceId")
    private void onClick(View view) {
        switch (view.getId()) {
            case R.id.homeIcon:
                startActivity(new Intent(UserActivity.this, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                break;
            case R.id.logoutIcon:
                mAuth.signOut();
                startActivity(new Intent(UserActivity.this, LoginActivity.class));
                finishAffinity();
                break;
        }
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

    // metodo che si attiva al click di una sala
    @Override
    public void onItemClick(DataSnapshot snapshot, int position) {
        // al click recuperiamo l'id dell'oggetto e lo passiamo all'activity "itemActivity"
        String uniqueId = snapshot.getKey();
        if (uniqueId != null && !uniqueId.isEmpty()) {
            startActivity(new Intent(UserActivity.this, ItemActivity.class)
                    .putExtra("id", uniqueId));
        }
    }
}