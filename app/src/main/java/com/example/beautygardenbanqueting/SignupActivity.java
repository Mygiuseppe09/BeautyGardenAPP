package com.example.beautygardenbanqueting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

/***************************************************************************************************
 Quest'activity è quella relativa alla registrazione di un nuovo utente.

 Quello che fa è ricevere in input: nome, cognome, email e password di un nuovo utente, verificare che
 i campi non siano vuoti, che abbiano contenuto valido (email e password con lunghezza minima) e solo
 alla fine avviare il processo di registrazione con email e password grazie alla funzione
 apposita di FireBase, dopodichè andiamo a recuperare l'id univoco dell'utente corrente (appena creato)
 e si va a inserire un nuovo utente nella "collection" users => se tutto va bene, si rimanda l'utente
 all'Activity dove è richiesta la verifica dell' email.
 **************************************************************************************************/

public class SignupActivity extends AppCompatActivity {

    // oggetti che rappresentano i punti di accesso a: database Firebase e autenticazione Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase db;

    // elementi del file xml
    private Button signupButton;
    private EditText name, surname, email, password;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_signup);

        // otteniamo le istanze relative a Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

        // salvataggio dei riferimenti dei relativi componenti xml
        name = (EditText) findViewById(R.id.editTextName);
        surname = (EditText) findViewById(R.id.editTextSurname);
        email = (EditText) findViewById(R.id.editTextEmailAddress);
        password = (EditText) findViewById(R.id.editTextPassword);
        signupButton = (Button) findViewById(R.id.signupButton);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    @Override
    protected void onStart() {
        super.onStart();

        signupButton.setOnClickListener(this::registerUser);
    }

    private void registerUser(View v) {
        String name = this.name.getText().toString().trim();
        String surname = this.surname.getText().toString().trim();
        String email = this.email.getText().toString().trim();
        String password = this.password.getText().toString().trim();

        // client validations

        // controlliamo che il campo "nome" non sia vuoto
        if (name.isEmpty()) {
            this.name.setError("Inserisci il nome!");
            this.name.requestFocus();
            return;
        }

        // controlliamo che il campo "cognome" non sia vuoto
        if (surname.isEmpty()) {
            this.surname.setError("Inserisci il cognome!");
            this.surname.requestFocus();
            return;
        }

        // controlliamo che il campo "email" non sia vuoto
        if (email.isEmpty()) {
            this.email.setError("Inserisci l'indirizzo email!");
            this.email.requestFocus();
            return;
        }

        // controlliamo che l'email sia valida
        if (! Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            this.email.setError("Inserisci un indirizzo email valido!");
            this.email.requestFocus();
            return;
        }

        // controlliamo che il campo "password" non sia vuoto
        if (password.isEmpty()) {
            this.password.setError("Inserisci una password");
            this.password.requestFocus();
            return;
        }

        // controlliamo che la password sia lunga almeno 8 caratteri
        if (password.length() < 8 ) {
            this.password.setError("La password deve essere lunga almeno 8 caratteri");
            this.password.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // nel caso in cui l'utente non l'avesse fatto, rendiamo maiuscole
        // le iniziali di nome e cognome
        String firstChar;
        String remaining;

        //nome
        firstChar = name.substring(0,1);
        remaining = name.substring(1);
        name = firstChar.toUpperCase() + remaining;
        String finalName = name;

        //cognome
        firstChar = surname.substring(0,1);
        remaining = surname.substring(1);
        surname = firstChar.toUpperCase() + remaining;
        String finalSurname = surname;


        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // se la registrazione è andata a buon fine, creiamo una nuova istanza della
                            // classe User e la inseriamo nella collection "users" creando un "child" avente
                            // Uid generato alla registrazione da Firebase
                            User newUser = new User(finalName,finalSurname,email, false);

                            if (mAuth.getCurrentUser() != null)
                            db.getReference("users")
                                    .child(mAuth.getCurrentUser().getUid())
                                    .setValue(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    progressBar.setVisibility(View.INVISIBLE);

                                    if (task.isSuccessful()) {
                                        // se l'utente è stato anche salvato, comunichiamo che è tutto ok
                                        Toast.makeText(SignupActivity.this,
                                                "Registrazione andata a buon fine :)",
                                                Toast.LENGTH_LONG).show();
                                        // mandiamo la mail di verifica dell'email
                                        mAuth.getCurrentUser().sendEmailVerification();
                                        // avviamo l'activity apposita
                                        startActivity(new Intent(SignupActivity.this, VerifyMailActivity.class));
                                        finish();
                                    }
                                    else
                                        Toast.makeText(SignupActivity.this,
                                                "Account creato, ma non è stato possibile aggiornare il database",
                                                Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                        else {
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(SignupActivity.this,
                                    "Account già esistente", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
