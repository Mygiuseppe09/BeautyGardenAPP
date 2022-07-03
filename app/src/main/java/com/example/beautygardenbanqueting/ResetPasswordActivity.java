package com.example.beautygardenbanqueting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/***************************************************************************************************
 Quest'activity è quella relativa alla reimpostazione della password di un utente.

 Quello che fa è ricevere in input la email, verificare che questa mail sia valida e vi sia un account
 associato, e, se così fosse, invia un link all'indirizzo inserito che consente la reimpostazione
 **************************************************************************************************/

public class ResetPasswordActivity extends AppCompatActivity {

    // elementi del file xml
    private EditText email;
    private Button resetButton;

    // oggetto della classe User che useremo per reperire informazioni durante le queries
    private User user;
    // ci servirà per cercare l'email fra gli utenti iscritti
    private Boolean isRegistered;

    // oggetti che rappresentano i punti di accesso a: database Firebase e autenticazione Firebase
    private FirebaseDatabase db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        email = findViewById(R.id.emailEditText);
        resetButton = findViewById(R.id.resetButton);

        // otteniamo le istanze relative a Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        resetButton.setOnClickListener(this::onClickResetPassword);
    }

    private void onClickResetPassword(View view) {
        // recuperiamo la stringa inserita dall'utente
        String email = this.email.getText().toString().trim();

        // controlliamo che abbia inserito qualcosa
        if (email.isEmpty()) {
            this.email.setError("Inserisci l'indirizzo email!");
            this.email.requestFocus();
            return;
        }

        // controlliamo che abbia inserito qualcosa di valido
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            this.email.setError("Inserisci un indirizzo email valido!");
            this.email.requestFocus();
            return;
        }

        // a questo punto vediamo se a quest'email è associato un account vero
        db.getReference("users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.getValue() != null) {
                            for (DataSnapshot userSnaphot: snapshot.getChildren()) {
                                user = userSnaphot.getValue(User.class);
                                isRegistered = false;
                                if (user.getEmail().equals(email)) {
                                    isRegistered = true;
                                    break;
                                }
                            }
                            // se non è stato trovato nessuno con questa email, lanciamo un avviso
                            if (!isRegistered)
                                Toast.makeText(ResetPasswordActivity.this,
                                        "Non esiste nessun account con questa email",
                                        Toast.LENGTH_LONG).show();
                            else {
                                // sennò mandiamo la mail di reset
                                mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(ResetPasswordActivity.this,
                                                "Fatto! Controlla la tua casella di posta elettronica",
                                                Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(ResetPasswordActivity.this, LoginActivity.class));
                                    }
                                });
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ResetPasswordActivity.this,
                                error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

}