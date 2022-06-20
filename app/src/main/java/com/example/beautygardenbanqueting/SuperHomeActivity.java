package com.example.beautygardenbanqueting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
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

public class SuperHomeActivity extends AppCompatActivity implements ItemAdapter.onItemClickListener {

    private TextView name;

    private TextView numPeopleValue;
    private TextView numItemsValue;
    private ImageView logoutTab;
    private Button add;

    private RecyclerView contents;
    private ItemAdapter itemAdapter;
    private FirebaseRecyclerOptions<Item> options;

    private FirebaseDatabase db;
    private FirebaseAuth mAuth;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ActivityName", "__ON_CREATE");
        setContentView(R.layout.activity_super_home);

        numPeopleValue = (TextView) findViewById(R.id.numPeopleValue);
        numItemsValue = (TextView) findViewById(R.id.numItemsValue);
        name = (TextView) findViewById(R.id.hiUserTextView);
        add = (Button) findViewById(R.id.addButton);

        logoutTab = (ImageView) findViewById(R.id.logoutIcon);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

        //Per la RecyclerView:
        // collegamento alla RycicleView all'interno del file xml
        contents = (RecyclerView) findViewById(R.id.adminItemsRecyclerView);
        contents.setLayoutManager(new LinearLayoutManager(this));
        // ne impostiamo la query
        options = new FirebaseRecyclerOptions.Builder<Item>()
                .setQuery(db.getReference("items"), Item.class)
                .build();

        // riempiamo il nome dell'amministratore
        db.getReference("users").child(mAuth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.getValue() != null) {
                            name.setText(snapshot.getValue(User.class).getName() + " " + snapshot.getValue(User.class).getSurname());
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
                       Toast.makeText(SuperHomeActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(SuperHomeActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
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
            startActivity(new Intent(SuperHomeActivity.this, SuperuserItemActivity.class)
                    .putExtra("id", uniqueId));
        }
    }

}