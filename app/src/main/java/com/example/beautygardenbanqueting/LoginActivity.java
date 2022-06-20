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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase db;

    private ProgressBar progressBar;
    private EditText email, password;
    private TextView signupTextView;
    private Button loginButton;

    private User loggedUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Login", "__ON_CREATE");
        /****************************************************************************************
         In questo metodo metteremo tutte quelle operazioni che vengono eseguite una sola volta,
         ovvero l’impostazione del layout e il salvataggio dei riferimenti dei relativi componenti.
         ****************************************************************************************/

        setContentView(R.layout.activity_login);

        // createSuperuserAccount();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        email = (EditText) findViewById(R.id.loginEmailAddress);
        password = (EditText) findViewById(R.id.loginPassword);
        signupTextView = (TextView) findViewById(R.id.signupTextView);
        loginButton = (Button) findViewById(R.id.loginButton);

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("Login", "__ON_START");
        /****************************************************************************************
         In questo metodo vengono implementate tutte quelle funzionalità che sono legate alla
         visualizzazione, ma non all’interazione con gli utenti. (es: listener, check sul login)
         ****************************************************************************************/

        // listeners
        signupTextView.setOnClickListener(this::onClick);
        loginButton.setOnClickListener(this::onClick);
    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.d("Login", "__ON_STOP");

        signupTextView.setOnClickListener(null);
        loginButton.setOnClickListener(null);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("Login", "__ON_RESTART");

        checkIfUserIsSignedIn();

        signupTextView.setOnClickListener(this::onClick);
        loginButton.setOnClickListener(this::onClick);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Login", "__ON_DESTROY");
        /****************************************************************************************
         Metodo simmetrico rispetto a onCreate() --> elimina le risorse in esso allocate.
         ****************************************************************************************/
    }


    /**********************************************************************************************/

    @SuppressLint("NonConstantResourceId")
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signupTextView:
                startActivity(new Intent(this, SignupActivity.class));
                break;
            case R.id.loginButton:
                loginUser();
                break;


        }
    }

    private void loginUser() {
        String email = this.email.getText().toString().trim();
        String password = this.password.getText().toString().trim();

        // client validations
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

        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressBar.setVisibility(View.INVISIBLE);
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
        FirebaseDatabase.getInstance().getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()  && snapshot.getValue() != null) {
                            loggedUser = snapshot.getValue(User.class);
                            Log.d("__user",loggedUser.toString());
                            if (loggedUser.isSuperuser())
                                startActivity(new Intent(LoginActivity.this, SuperHomeActivity.class));
                            else
                                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            finish();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    private void checkIfUserIsSignedIn() {
        if (mAuth.getCurrentUser() != null)
            // l'utente è già loggato
            redirectUserOrSuperuserToHome();
    }


}