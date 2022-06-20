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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private Button signupButton;
    private EditText name, surname, email, password;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Login", "__ON_CREATE");
        /****************************************************************************************
         In questo metodo metteremo tutte quelle operazioni che vengono eseguite una sola volta,
         ovvero l’impostazione del layout e il salvataggio dei riferimenti dei relativi componenti.
         ****************************************************************************************/

        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

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
        Log.d("Signup", "__ON_START");
        /****************************************************************************************
         In questo metodo vengono implementate tutte quelle funzionalità che sono legate alla
         visualizzazione, ma non all’interazione con gli utenti. (es: listener, check sul login)
         ****************************************************************************************/

        setAllOnClickListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Signup", "__ON_RESUME");
        /****************************************************************************************
         In questo metodo vengono implementate tutte quelle funzioni che sono legate all’effettivo
         uso da parte dell’utente, come per esempio l’accesso alla fotocamera, suoni, animazioni.
         Inoltre, viene invocato al posto dell'onRestart() se l'activity non era stata stoppata,
         ma solo messa in pausa (capita quando si lancia una seconda activity non in full screen)
         ****************************************************************************************/

        checkIfUserIsSignedIn();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("Signup", "__ON_PAUSE");
        /****************************************************************************************
         Metodo  simmetrico rispetto a onResume() --> elimina le risorse in esso allocate.
         ****************************************************************************************/

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("Signup", "__ON_STOP");
        /****************************************************************************************
         Metodo simmetrico rispetto a onStart() --> elimina le risorse in esso allocate (es. listener)
         ****************************************************************************************/

        removeAllOnClickListener();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("Signup", "__ON_RESTART");
        /****************************************************************************************
         Il metodo onRestart() viene invocato quando un'activity era già stata creata e
         successivamente messa in pausa e quindi stoppata (passaggio ad un'altra activity).
         Permette di ripristinare quanto disabilitato nell’onStop() (es. listener)
         ****************************************************************************************/

        setAllOnClickListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Signup", "__ON_DESTROY");
        /****************************************************************************************
         Metodo simmetrico rispetto a onCreate() --> elimina le risorse in esso allocate.
         ****************************************************************************************/
    }

    /*********************************************************************************************/

    private void setAllOnClickListener() {
        signupButton.setOnClickListener(this::onClick);
    }

    private void removeAllOnClickListener() {
        signupButton.setOnClickListener(null);
    }

    private void onClick(View v) {
      registerUser();
    }

    private void checkIfUserIsSignedIn() {
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null)
            // l'utente è già loggato
            startActivity(new Intent(SignupActivity.this, HomeActivity.class));
    }

    private void registerUser() {
        String name = this.name.getText().toString().trim();
        String surname = this.surname.getText().toString().trim();
        String email = this.email.getText().toString().trim();
        String password = this.password.getText().toString().trim();

        //client validations

        if (name.isEmpty()) {
            this.name.setError("Inserisci il nome!");
            this.name.requestFocus();
            return;
        }

        if (surname.isEmpty()) {
            this.surname.setError("Inserisci il cognome!");
            this.surname.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            this.email.setError("Inserisci l'indirizzo email!");
            this.email.requestFocus();
            return;
        }

        if (! Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            this.email.setError("Inserisci un indirizzo email valido!");
            this.email.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            this.password.setError("Inserisci una password");
            this.password.requestFocus();
            return;
        }

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
                            User newUser = new User(finalName,finalSurname,email, false);

                            FirebaseDatabase.getInstance().getReference("users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    progressBar.setVisibility(View.INVISIBLE);

                                    if (task.isSuccessful()) {
                                        Toast.makeText(SignupActivity.this, "Registrazione andata a buon fine :)", Toast.LENGTH_LONG).show();
                                        mAuth.getCurrentUser().sendEmailVerification();
                                        startActivity(new Intent(SignupActivity.this, VerifyMailActivity.class));
                                        finish();
                                    }
                                    else {
                                        Toast.makeText(SignupActivity.this, "Qualcosa è andato storto durante l'aggiornamento del database :(", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                        else {
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(SignupActivity.this, "Qualcosa è andato storto durante la comunicazione col database :(", Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }
}