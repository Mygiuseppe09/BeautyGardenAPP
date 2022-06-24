package com.example.beautygardenbanqueting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/***************************************************************************************************
 Quest'activity è quella relativa alla visualizzazione delle informazioni di una singola sala con cui
 è possibile interagire.

 Le informazioni mostrate sono: prezzo, capacità massima, descrizione ed una foto.
 Le interazioni sono: modifica ed elimina.

 Quest'activity viene lanciata al tap dalla Home del Superuser
 **************************************************************************************************/

public class SuperItemActivity extends AppCompatActivity {

    // id della sala passato tramime l'intent dalla SuperHome
    private String itemId;
    // item (sala), da reperire dal db grazie all'id (stringa) passato dalla precedente activity
    private Item item;

    // componenti del file xml (dinamici)
    private ImageView image;
    private TextView capacityValue, priceValue, description;

    // componenti del file xml (statici)
    private Button edit, delete;

    // Firebase
    private FirebaseDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_item);

        // prendiamo i riferimenti degli oggetti xml
        image = (ImageView) findViewById(R.id.ImageViewItem);
        capacityValue = (TextView) findViewById(R.id.capacityValue);
        priceValue = (TextView) findViewById(R.id.priceValue);
        description = (TextView) findViewById(R.id.description);
        edit = (Button) findViewById(R.id.editButton);
        delete = (Button) findViewById(R.id.deleteButton);

        // reperiamo l'id della sala da mostrare nell'activity
        itemId = getIntent().getStringExtra("id");

        //inizializziamo quanto concerne col db
        db = FirebaseDatabase.getInstance();

        db.getReference("items").child(itemId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()  && snapshot.getValue() != null) {
                            item = snapshot.getValue(Item.class);

                            // ora possiamo inizializzare il tutto..

                            // immagine
                            Glide.with(SuperItemActivity.this)
                                    .load(item.getImage())
                                    .into(image);
                            // prezzo
                            String priceWithSymbol = item.getPrice().toString() + "€";
                            priceValue.setText(priceWithSymbol);
                            // capienza massima
                            capacityValue.setText(item.getCapacity().toString());
                            // descrizione
                            description.setText(item.getDescription());
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(SuperItemActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();

        //listeners
        edit.setOnClickListener(this::onClick);
        delete.setOnClickListener(this::onClick);
    }

    @SuppressLint("NonConstantResourceId")
    private void onClick(View view) {
        switch(view.getId()) {
            case R.id.editButton:
                startActivity(new Intent(SuperItemActivity.this, EditItemActivity.class).putExtra("id", itemId));
                break;
            case R.id.deleteButton:
                db.getReference("items")
                        .child(itemId)
                        .removeValue()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SuperItemActivity.this, "Sala rimossa correttamente", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SuperItemActivity.this, SuperHomeActivity.class));
                        }
                    }
                });
        }
    }
}

