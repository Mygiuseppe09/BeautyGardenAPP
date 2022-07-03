package com.example.beautygardenbanqueting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
 Quest'activity è quella relativa alla Home di un utente "superuser"

 Quello che fa è, oltre a mostare il nome ed il cognome del superuser, è mostrare un'icona che ne indica
 i "superpoteri": infatti, è possibile vedere quanti utenti iscritti vi sono alla piattaforma e quante
 sale vi sono pubblicate.

 E' presente un pulsante che consente l'aggiunta di una sala.

 E' presente la RecyclerView contenente le sale reperite dalla collection "items" nel database.
 Ad ogni oggetto della recycler view è associato un listener al click che rimanda alla SuperItemAcitviy.
 **************************************************************************************************/

public class SuperHomeActivity extends AppCompatActivity implements ItemAdapter.onItemClickListener {

    // elementi del file xml
    private TextView name;
    private TextView numPeopleValue;
    private TextView numItemsValue;
    private ImageView logoutTab, profileTab;
    private Button add;

    private RecyclerView contents;
    private ItemAdapter itemAdapter;
    private FirebaseRecyclerOptions<Item> options;

    // oggetti che rappresentano i punti di accesso a: database Firebase e autenticazione Firebase
    private FirebaseDatabase db;
    private FirebaseAuth mAuth;

    // oggetto della classe User che useremo per reperire informazioni durante le queries
    private User loggedSuperuser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_home);

        // salvataggio dei riferimenti dei relativi componenti xml
        numPeopleValue = findViewById(R.id.numPeopleValue);
        numItemsValue = findViewById(R.id.numItemsValue);
        name = findViewById(R.id.hiUserTextView);
        add = findViewById(R.id.addButton);

        logoutTab = findViewById(R.id.logoutIcon);
        profileTab = findViewById(R.id.profileIcon);

        // otteniamo le istanze relative a Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

        //Per la RecyclerView:
        // collegamento alla RycicleView all'interno del file xml
        contents = findViewById(R.id.adminItemsRecyclerView);
        contents.setLayoutManager(new LinearLayoutManager(this));
        // ne impostiamo la query
        options = new FirebaseRecyclerOptions.Builder<Item>()
                .setQuery(db.getReference("items"), Item.class)
                .build();

        // riempiamo il nome e il cognome dell'amministratore
        db.getReference("users").child(mAuth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.getValue() != null) {
                            loggedSuperuser = snapshot.getValue(User.class);
                            name.setText(loggedSuperuser.getName() + " " + loggedSuperuser.getSurname());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(SuperHomeActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();

       // riempiamo il contatore utenti registrati
       db.getReference("users")
               .addListenerForSingleValueEvent(new ValueEventListener() {
                   @Override
                   public void onDataChange(@NonNull DataSnapshot snapshot) {
                       if (snapshot.exists() && snapshot.getValue() != null) {
                           numPeopleValue.setText(String.valueOf(snapshot.getChildrenCount()));
                       }
                   }
                   @Override
                   public void onCancelled(@NonNull DatabaseError error) {
                       Toast.makeText(SuperHomeActivity.this,
                               error.getMessage(),
                               Toast.LENGTH_SHORT).show();
                   }
               });

       //// riempiamo il contatore sale pubblicate
        db.getReference("items")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.getValue() != null) {
                            numItemsValue.setText(String.valueOf(snapshot.getChildrenCount()));
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(SuperHomeActivity.this,
                                error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });


        // per la RecyclerView
        itemAdapter = new ItemAdapter(options);
        contents.setAdapter(itemAdapter);
        itemAdapter.startListening();
        // per il click di ogni elemento
        itemAdapter.setOnItemClickListener(this);

        //listeners
        add.setOnClickListener(this::onClick);
        logoutTab.setOnClickListener(this::onClick);
        profileTab.setOnClickListener(this::onClick);
    }



    @SuppressLint("NonConstantResourceId")
    private void onClick(View view) {
        switch (view.getId()) {
            case R.id.logoutIcon:
                mAuth.signOut();
                startActivity(new Intent(SuperHomeActivity.this, LoginActivity.class));
                finish();
                break;
            case R.id.addButton:
                startActivity(new Intent(SuperHomeActivity.this, AddItemActivity.class));
                break;
            case R.id.profileIcon:
                startActivity(new Intent(SuperHomeActivity.this, SuperUserActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
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
        // al click recuperiamo l'oggetto e passiamo
        // all'activity "itemActivity" l'id
        String uniqueId = snapshot.getKey();
        if (uniqueId != null && !uniqueId.isEmpty()) {
            startActivity(new Intent(SuperHomeActivity.this, SuperItemActivity.class)
                    .putExtra("id", uniqueId));
        }
    }

}