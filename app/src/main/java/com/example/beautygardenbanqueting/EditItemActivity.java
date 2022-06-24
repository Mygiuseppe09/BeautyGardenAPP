package com.example.beautygardenbanqueting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/***************************************************************************************************
 Quest'activity è quella relativa alla modifica di un oggetto dalla collection "items"

 Quello che fa è mostrare le informazioni correnti della sala all'interno di tante "EditTex",
 così da creare un'stanza della classe Item con le nuove informazioni per poi inserirla nel db
 sovrascrivendo quella già esistente in quanto il nuovo nodo avrà lo stesso id di quello precedente
 **************************************************************************************************/

public class EditItemActivity extends AppCompatActivity {

    // componenti del file xml
    private EditText name, price, capacity, slogan, image, description;
    private Button editButton;
    private ProgressBar progressBar;

    // punto di accesso al database Firebase
    private FirebaseDatabase db;

    // id della sala passato tramime l'intent dalla SuperHome
    private String itemId;
    // item (sala), da reperire dal db grazie all'id (stringa) passato dalla precedente activity
    private Item item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);

        // salvataggio dei riferimenti dei relativi componenti xml
        name = findViewById(R.id.nameEditText);
        price = findViewById(R.id.priceEditTex);
        capacity = findViewById(R.id.capacityEditTex);
        slogan = findViewById(R.id.sloganEditText);
        image = findViewById(R.id.imageEditText);
        description = findViewById(R.id.description);
        editButton = findViewById(R.id.editButtonDB);
        progressBar = findViewById(R.id.progressBar);

        itemId = getIntent().getStringExtra("id");

        // otteniamo l'istanza del db
        db = FirebaseDatabase.getInstance();

        // accediamo alle informazioni della sala mostrata e le inseriamo nei campi editabili
        db.getReference("items").child(itemId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.getValue() != null) {
                            item = snapshot.getValue(Item.class);

                            name.setText(item.getName());
                            price.setText(item.getPrice().toString());
                            capacity.setText(item.getCapacity().toString());
                            slogan.setText(item.getSlogan());
                            image.setText(item.getImage());
                            description.setText(item.getDescription());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(EditItemActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();

        editButton.setOnClickListener(this::onClick);
    }

    private void onClick(View view) {
        switch(view.getId()) {
            case R.id.editButtonDB:
                editItemInDatabase();
                break;
        }
    }

    private void editItemInDatabase() {
        String name = this.name.getText().toString().trim();
        String description = this.description.getText().toString().trim();
        String slogan = this.slogan.getText().toString().trim();
        String imageURL = this.image.getText().toString().trim();
        Integer capacity = Integer.valueOf(this.capacity.getText().toString().trim());
        Integer price = Integer.valueOf(this.price.getText().toString().trim());

        if (name.isEmpty()) {
            this.name.setError("Inserisci un nome");
            this.name.requestFocus();
            return;
        }

        if (description.isEmpty()) {
            this.description.setError("Inserisci una descrizione");
            this.description.requestFocus();
            return;
        }

        if (imageURL.isEmpty()) {
            this.image.setError("Inserisci il link ad una immagine");
            this.image.requestFocus();
            return;
        }

        if (slogan.isEmpty()) {
            this.slogan.setError("Inserisci uno slogan");
            this.slogan.requestFocus();
            return;
        }

        if (capacity.toString().isEmpty()) {
            this.capacity.setError("Inserisci una capacità massima");
            this.capacity.requestFocus();
            return;
        }

        if (price.toString().isEmpty()) {
            this.price.setError("Inserisci un prezzo");
            this.price.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        Item newItem = new Item(name,slogan,imageURL,description,capacity,price);

        db.getReference("items").child(itemId).setValue(newItem)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(EditItemActivity.this, "Sala aggiornata correttamente", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(EditItemActivity.this, SuperHomeActivity.class));
                            finish();
                        }
                    }
                });
    }
}