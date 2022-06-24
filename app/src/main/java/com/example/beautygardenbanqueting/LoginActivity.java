package com.example.beautygardenbanqueting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/***************************************************************************************************
 Quest'activity è quella relativa al login dell'utente.

 Quello che fa è ricevere in input due stringhe, email e password, con cui (se valide) l'utente accede
 all'app. Inoltre presenta la possibilità di essere rimandati sia al form di registrazione che alla
 schermata di reset della password.
 **************************************************************************************************/

public class LoginActivity extends AppCompatActivity {

    // oggetti che rappresentano i punti di accesso a: database Firebase e autenticazione Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase db;

    // elementi del file xml
    private ProgressBar progressBar;
    private EditText email, password;
    private TextView signupTextView, resetPassword;
    private Button loginButton;

    // oggetto della classe User che useremo per reperire informazioni durante le queries
    private User loggedUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // salvataggio dei riferimenti dei relativi componenti xml
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        email = (EditText) findViewById(R.id.loginEmailAddress);
        password = (EditText) findViewById(R.id.loginPassword);
        signupTextView = (TextView) findViewById(R.id.signupTextView);
        loginButton = (Button) findViewById(R.id.loginButton);
        resetPassword = (TextView) findViewById(R.id.resetPassword);


        // otteniamo le istanze relative a Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // settiamo i listeners
        signupTextView.setOnClickListener(this::onClick);
        loginButton.setOnClickListener(this::onClick);
        resetPassword.setOnClickListener(this::onClick);
    }


    @SuppressLint("NonConstantResourceId")
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signupTextView:
                // se l'utente clicca su "registati" lo rimandiamo all'Activity di registrazione
                startActivity(new Intent(this, SignupActivity.class));
                break;
            case R.id.loginButton:
                // se l'utente clicca su "login" facciamo una serie di operazioni definite in "loginUser"
                loginUser();
                break;
            case R.id.resetPassword:
                // se l'utente clicca su "reset password", lo rimandiamo all'activity dedicata
                startActivity(new Intent(this, ResetPasswordActivity.class));
                break;
        }
    }

    // funzione invocata quando l'utente clicca "login"
    private void loginUser() {
        // recupero i valori inseriti e ne facciamo delle validazioni (sotto)
        String email = this.email.getText().toString().trim();
        String password = this.password.getText().toString().trim();

        // controlliamo che il campo della mail non sia vuoto
        if (email.isEmpty()) {
            this.email.setError("Inserisci l'indirizzo email!");
            this.email.requestFocus();
            return;
        }

        // controlliamo che la mail sia stata scritta correttamente
        if (! Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            this.email.setError("Inserisci un indirizzo email valido!");
            this.email.requestFocus();
            return;
        }

        // controlliamo che il campo della password non sia vuoto
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

        // rendiamo visibile la progress bar
        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) { // se il login è andato a buon fine
                    redirectUserOrSuperuserToHome();
                }
                else {
                    Toast.makeText(LoginActivity.this, "Qualcosa è andato storto: controlla le credenziali :(", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void redirectUserOrSuperuserToHome() {
        // recupero i dati dell'utente loggato
        db.getReference("users")
                .child(mAuth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()  && snapshot.getValue() != null) {
                            // rendiamo invisibile la progress bar
                            progressBar.setVisibility(View.INVISIBLE);

                            loggedUser = snapshot.getValue(User.class);
                            if (loggedUser != null && loggedUser.isSuperuser())
                                startActivity(new Intent(LoginActivity.this, SuperHomeActivity.class));
                            else
                                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            finish();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(LoginActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}