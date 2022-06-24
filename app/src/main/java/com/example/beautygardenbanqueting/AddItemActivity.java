package com.example.beautygardenbanqueting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

/***************************************************************************************************
 Quest'activity è quella relativa all'aggiunta di un nuovo oggetto nella collection "items"

 Quello che fa è prendere in input tutti i valori che vanno associati alle chiavi del modello Item,
 così da creare un'istanza, appunto, della classe Item e salvarla nel database.
 **************************************************************************************************/

public class AddItemActivity extends AppCompatActivity {

    // elementi del file xml
    private EditText name, price, capacity, slogan, image, description;
    private Button addButton;
    private ProgressBar progressBar;

    // punto di accesso al database Firebase
    private FirebaseDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        // salvataggio dei riferimenti dei relativi componenti xml
        name = findViewById(R.id.nameEditText);
        price = findViewById(R.id.priceEditTex);
        capacity = findViewById(R.id.capacityEditTex);
        slogan = findViewById(R.id.sloganEditText);
        image = findViewById(R.id.imageEditText);
        description = findViewById(R.id.description);
        addButton = findViewById(R.id.addToDb);
        progressBar = findViewById(R.id.progressBar);

        // otteniamo l'istanza del database
        db = FirebaseDatabase.getInstance();

    }

    @Override
    protected void onStart() {
        super.onStart();

        addButton.setOnClickListener(this::onClick);
    }

    @SuppressLint("NonConstantResourceId")
    private void onClick(View view) {
        switch (view.getId()) {
            case R.id.addToDb:
                addNewItemToDatabase();
                break;

        }
    }

    private void addNewItemToDatabase() {
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

        db.getReference("items")
                .push()
                .setValue(newItem)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(AddItemActivity.this,
                                    "Sala aggiunta correttamente", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(AddItemActivity.this, SuperHomeActivity.class));
                            finish();
                        }
                    }
                });
    }
}
