package com.example.beautygardenbanqueting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StartActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseDatabase db;
    User loggedUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("ActivityName", "_ON_START");
        /****************************************************************************************
         In questo metodo vengono implementate tutte quelle funzionalità che sono legate alla
         visualizzazione, ma non all’interazione con gli utenti. (es: listener, check sul login utente)
         ****************************************************************************************/

        if (mAuth.getCurrentUser() != null) {
            db.getReference("users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
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
                        public void onCancelled(@NonNull DatabaseError error) { }
                    });
        }
        else {
            // l'utente deve loggarsi
            startActivity(new Intent(StartActivity.this, LoginActivity.class));
            finish();
        }

    }
}