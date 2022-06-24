package com.example.beautygardenbanqueting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/***************************************************************************************************
 Quest'activity è quella d'avvio (settata nel file AndroidManifest.xml).

 Quello che fa è controllare se è l'utente è loggato o meno:
 * se è loggato, recuperiamo le sue informazioni e vediamo se è un supeuser:
        * se lo è => andiamo nella home relativa agli admins
        * se non lo è => andiamo nella home "normale"
 * se NON è loggato => andiamo nella login.
 **************************************************************************************************/

public class StartActivity extends AppCompatActivity {

    // dichiariamo un oggetto della classe astratta 'FirebaseAuth',
    // la quale rappresenta il punto di accesso all'SDK di autenticazione Firebase
    private FirebaseAuth mAuth;

    // dichiariamo un oggetto della classe astratta 'FirebaseDatabase',
    // la quale rappresenta il punto di accesso al database connesso all'app
    private FirebaseDatabase db;

    private User loggedUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // otteniamo le istanze sia del database che dell'SDK relativo all'autenticazione
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // se l'utente è loggato ed ha verificato l'email
        if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().isEmailVerified()) {
            db.getReference("users") //accediamo alla "collection" degli utenti
                    .child(mAuth.getCurrentUser().getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()  && snapshot.getValue() != null) {
                                loggedUser = snapshot.getValue(User.class);

                                if (loggedUser != null && loggedUser.isSuperuser())
                                    startActivity(new Intent(StartActivity.this, SuperHomeActivity.class));
                                else
                                    startActivity(new Intent(StartActivity.this, HomeActivity.class));
                                finish();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(StartActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
        else if (mAuth.getCurrentUser() != null) {
            // se l'utente è loggato ma non Ha verificato l'email
            startActivity(new Intent(StartActivity.this, VerifyMailActivity.class));
            finish();
        }
        else {
            // l'utente deve loggarsi
            startActivity(new Intent(StartActivity.this, LoginActivity.class));
            finish();
        }

    }
}