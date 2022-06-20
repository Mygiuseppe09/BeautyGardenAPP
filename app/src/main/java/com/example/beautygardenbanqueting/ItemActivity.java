package com.example.beautygardenbanqueting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class ItemActivity extends AppCompatActivity {
    // item passato dalla Home (da reperire dal db, in quanto viene passato solo l'id)
    private Item item;
    // id della sala passato tratime l'intent dalla Home
    private String itemId;

    //componenti del file xml (dinamici)
    private ImageView image, addToWishListButton;
    private TextView capacityValue, priceValue, description;
    private RatingBar ratingBar;

    //Firebase
    private FirebaseDatabase db;
    private FirebaseAuth mAuth;
    private DatabaseReference itemRef;

    // Wishlist
    private boolean isPresent;
    private int position;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        // reperiamo l'id della sala da mostrare nell'activity
        itemId = getIntent().getStringExtra("id");

        // prendiamo i riferimenti degli oggetti xml
        image = (ImageView) findViewById(R.id.ImageViewItem);
        addToWishListButton = (ImageView) findViewById(R.id.wishListIcon);
        capacityValue = (TextView) findViewById(R.id.capacityValue);
        priceValue = (TextView) findViewById(R.id.priceValue);
        description = (TextView) findViewById(R.id.description);
        ratingBar = (RatingBar) findViewById(R.id.ratingBarItem);

        //inizializziamo quanto concerne col db
        db = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        db.getReference("items").child(itemId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()  && snapshot.getValue() != null) {
                                    item = snapshot.getValue(Item.class);
                                    itemRef = snapshot.getRef();
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
                                    // review
                                    HashMap<String, Float> reviews = item.getReviews();
                                    if (reviews != null) {
                                        for (String key: reviews.keySet()) {
                                            if (key.equals(mAuth.getCurrentUser().getUid())) {
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
                                Toast.makeText(ItemActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });


        // vediamo se l'utente ha già questa salla nella wishlist
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
                                Toast.makeText(ItemActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

        //LISTENERS

        // clickListener sul pulsante "wishlist"
        addToWishListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.getReference("users")
                        .child(mAuth.getCurrentUser().getUid())
                        .child("wishlist")
                          .addListenerForSingleValueEvent(new ValueEventListener() {
                              @Override
                              public void onDataChange(@NonNull DataSnapshot snapshot) {
                                  if (snapshot.exists() && snapshot.getValue() != null) {
                                      // se esiste una wishlist
                                      if (!snapshot.hasChild(itemId)) {
                                          // se non c'è ancora l'item con l'id lo aggiungiamo
                                          snapshot.getRef().child(itemId).setValue(item);
                                          addToWishListButton.setImageResource(R.drawable.wishlist_full);
                                      }
                                      else {
                                          // sennò lo rimuoviamo
                                          snapshot.child(itemId).getRef().removeValue();
                                          addToWishListButton.setImageResource(R.drawable.wishlist_empty);
                                      }
                                  }
                                  else {
                                      // wishlist vuota
                                      snapshot.getRef().child(itemId).setValue(item);
                                      addToWishListButton.setImageResource(R.drawable.wishlist_full);
                                  }
                              }
                              @Override
                              public void onCancelled(@NonNull DatabaseError error) {
                                  Toast.makeText(ItemActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                              }
                          });
            }
        });

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                // quando l'utente valuta una sala, dobbiamo salvare la review nell'oggetto item nel db
                // Ricordiamoci che in itemRef abbiamo la referenza alla nostra sala
                itemRef.child("reviews").child(mAuth.getCurrentUser().getUid()).setValue(rating);
            }
        });

    }




    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onRestart() {
        super.onRestart();


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
