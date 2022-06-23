package com.example.beautygardenbanqueting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class HomeActivity extends AppCompatActivity implements ItemAdapter.onItemClickListener {


    private RecyclerView contents;
    private ItemAdapter itemAdapter;
    private FirebaseRecyclerOptions<Item> options;

    private FirebaseAuth mAuth;
    private FirebaseDatabase db;

    private TextView hiUser;
    private ImageView profileTab;
    private ImageView homeTab;
    private ImageView logoutTab;

    private User loggedUser;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Home", "__ON_CREATE");
        /****************************************************************************************
         In questo metodo metteremo tutte quelle operazioni che vengono eseguite una sola volta,
         ovvero l’impostazione del layout e il salvataggio dei riferimenti dei relativi componenti.
         ****************************************************************************************/
        // impostazione del Layout
        setContentView(R.layout.activity_home);


       // db
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
        db.getReference("users")
                .child(mAuth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()  && snapshot.getValue() != null) {
                            hiUser.setText("Ciao, " + snapshot.getValue(User.class).getName() + "!");
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
        Log.d("Home", "__ON_START");
        /****************************************************************************************
         In questo metodo vengono implementate tutte quelle funzionalità che sono legate alla
         visualizzazione, ma non all’interazione con gli utenti. (es: listener, check sul login)
         ****************************************************************************************/

        setAllOnClickListener();

        // per la RecyclerView
        itemAdapter = new ItemAdapter(options);
        contents.setAdapter(itemAdapter);
        itemAdapter.startListening();
        // per il click di ogni elemento
        itemAdapter.setOnItemClickListener(this);

        checkIfUserIsSignedIn();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Home", "__ON_RESUME");


    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.d("Home", "__ON_PAUSE");


    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("Home", "__ON_STOP");


        removeAllOnClickListener();
        itemAdapter.stopListening();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("Home", "__ON_RESTART");
        /****************************************************************************************
         Il metodo onRestart() viene invocato quando un'activity era già stata creata e
         successivamente messa in pausa e quindi stoppata (passaggio ad un'altra activity).
         Permette di ripristinare quanto disabilitato nell’onStop() (es. listener)
         ****************************************************************************************/

        setAllOnClickListener();
        itemAdapter.startListening();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Home", "__ON_DESTROY");
        /****************************************************************************************
         Metodo simmetrico rispetto a onCreate() --> elimina le risorse in esso allocate.
         ****************************************************************************************/
    }


    /**********************************************************************************************/

    private void setAllOnClickListener() {
        logoutTab.setOnClickListener(this::onClick);
        profileTab.setOnClickListener(this::onClick);
    }

    private void removeAllOnClickListener() {
        logoutTab.setOnClickListener(null);
        profileTab.setOnClickListener(null);
    }

    private void checkIfUserIsSignedIn() {
        if (mAuth.getCurrentUser() == null) {
            // l'utente deve loggarsi
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        }
        else if (!mAuth.getCurrentUser().isEmailVerified()) {
            // l'utente è loggato ma non ha verificato l'email
            startActivity(new Intent(HomeActivity.this, VerifyMailActivity.class));
            finish();
        }
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