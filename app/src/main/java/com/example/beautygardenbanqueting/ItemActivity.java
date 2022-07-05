package com.example.beautygardenbanqueting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

/***************************************************************************************************
 Quest'activity è quella relativa alla visualizzazione delle informazioni di una singola sala con cui
 è possibile interagire.

 Le informazioni mostrate sono: prezzo, capacità massima, descrizione ed una foto.
 Le interazioni sono: aggiungi alla  lista dei desideri e inserisci una recensione (1 a 5 stelle)

Quest'activity viene lanciata sia al tap dalla Home che dal tap dalla lista dei desideri dell'utente.
 **************************************************************************************************/

public class ItemActivity extends AppCompatActivity {

    // id della sala passato tramime l'intent dalla Home
    private String itemId;
    // item (sala), da reperire dal db grazie all'id (stringa) passato dalla precedente activity
    private Item item;

    //componenti del file xml (dinamici)
    private ImageView image, addToWishListButton;
    private TextView capacityValue, priceValue, description;
    private RatingBar ratingBar;

    //Firebase
    private FirebaseDatabase db;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        // reperiamo l'id della sala da mostrare nell'activity
        itemId = getIntent().getStringExtra("id");

        // prendiamo i riferimenti degli oggetti xml
        image = findViewById(R.id.ImageViewItem);
        addToWishListButton = findViewById(R.id.wishListIcon);
        capacityValue = findViewById(R.id.capacityValue);
        priceValue = findViewById(R.id.priceValue);
        description = findViewById(R.id.description);
        ratingBar = findViewById(R.id.ratingBarItem);

        // otteniamo le istanze relative a Firebase
        db = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // ci agganciamo all'oggetto nella collection "items" grazie all'Uid
        db.getReference("items")
                .child(itemId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()  && snapshot.getValue() != null) {
                            item = snapshot.getValue(Item.class);
                            // ora possiamo inizializzare il tutto..

                            // immagine
                            Glide.with(ItemActivity.this)
                                    .load(item.getImage())
                                    .into(image);
                            // prezzo
                            String priceWithSymbol = item.getPrice().toString() + "€";
                            priceValue.setText(priceWithSymbol);
                            // capienza massima
                            capacityValue.setText(item.getCapacity().toString());
                            // descrizione
                            description.setText(item.getDescription());
                            // review (se l'ha già messa)
                            HashMap<String, Float> reviews = item.getReviews();
                            if (reviews != null) { // se la sala ha almeno una review
                                for (String key: reviews.keySet()) {
                                    // cicliamo le chiavi (che sono gli id degli utenti)
                                    if (key.equals(mAuth.getCurrentUser().getUid())) {
                                        // se troviamo tra le chiavi quella dell'utente loggato,
                                        // allora l'utente ha già messo una review
                                        float rating = reviews.get(key);
                                        ratingBar.setRating(rating);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ItemActivity.this, error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });


        // vediamo se l'utente ha già questa salla nella wishlist così da stabilire se caricare l'icona colorata o meno
        if (mAuth.getCurrentUser() != null)
        db.getReference("users")
                .child(mAuth.getCurrentUser().getUid())
                .child("wishlist")
                .child(itemId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists())
                                    // allora c'è già
                                    addToWishListButton.setImageResource(R.drawable.wishlist_full);
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(ItemActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });

        //LISTENERS

        // clickListener sul pulsante "wishlist"
        addToWishListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // quando clicco sul pulsante "wishlist" a prescindere che sia colorato o meno
                // quello che facciamo è accedere al campo "wishlist" dell'utente loggato in Firebase
                db.getReference("users")
                        .child(mAuth.getCurrentUser().getUid())
                        .child("wishlist")
                          .addListenerForSingleValueEvent(new ValueEventListener() {
                              @Override
                              public void onDataChange(@NonNull DataSnapshot snapshot) {
                                  if (snapshot.exists() && snapshot.getValue() != null) {
                                      // se l'utente loggato ha una wishlist con qualcosa dentro, allora controlliamo se ha questa
                                      if (!snapshot.hasChild(itemId)) {
                                          // se non c'è ancora l'item con l'id lo aggiungiamo
                                          snapshot.getRef().child(itemId).setValue(item);
                                          // settiamo l'immagine colorata
                                          addToWishListButton.setImageResource(R.drawable.wishlist_full);
                                      }
                                      else {
                                          // sennò, signfica che ha già la sala nella lista dei desideri e la vuole rimuovere
                                          snapshot.child(itemId).getRef().removeValue();
                                          // settiamo l'immagine NON colorata
                                          addToWishListButton.setImageResource(R.drawable.wishlist_empty);
                                      }
                                  }
                                  else {
                                      // se la wishlist è vuota, allora non c'è sicuramente => la aggiungiamo
                                      snapshot.getRef().child(itemId).setValue(item);
                                      // settiamo l'immagine colorata
                                      addToWishListButton.setImageResource(R.drawable.wishlist_full);
                                  }
                              }
                              @Override
                              public void onCancelled(@NonNull DatabaseError error) {
                                  Toast.makeText(ItemActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                              }
                          });
            }
        });

        // impostaimo un listener al click sulla ratingBar
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                // quando l'utente valuta una sala, dobbiamo salvare la review nell'oggetto item nel db
                if (rating > 0)
                db.getReference("items").child(itemId)
                        .child("reviews")
                        .child(mAuth.getCurrentUser().getUid()) // creiamo un nuovo "figlio" con l'id dell'utente
                        .setValue(rating);
            }
        });

    }
}
