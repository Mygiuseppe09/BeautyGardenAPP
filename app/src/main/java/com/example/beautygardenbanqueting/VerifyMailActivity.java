package com.example.beautygardenbanqueting;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

/***************************************************************************************************
 Quest'activity è quella relativa alla verifica della mail inserita in fase di registrazione.

 Quello che fa è mostrare del testo (in realta è un immagine che si prende tutto lo schermo) che dice
 all'utente di aprire il proprio client di posta elettronica e lo invita a tornare una volta fatto.
 Se la verifica va a buon fine, una volta che torna nell'app, nel metodo onRestart() (che si attiva
 proprio dopo l'invocazione del metodo onStop() e cioè quando l'acitivity non è più visualizzata),
 è presente un check sulla mail:
 * se è verificata => allora l'utente va alla Home
 * se NON è verificata => allora viene mostrato un messaggio in cui lo si fa presente all'utente.

 p.s. se l'utente non la verifica e chiude l'app (ne forza l'interruzione) comunque nella StartActivity
 vi è un check sulla verifica dell'email, quindi non bypassa quest'aspetto di sicurezza.
 **************************************************************************************************/

public class VerifyMailActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Button resendMail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_mail);

        // otteniamo l'istanza relative a Firebase (autenticazione)
        mAuth = FirebaseAuth.getInstance();

        resendMail = (Button) findViewById(R.id.resendMail);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // mettiamo il listener al click sul pulsante "NON HO RICEVUTO L'EMAIL"
        resendMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuth.getCurrentUser() != null)
                mAuth.getCurrentUser()
                        .sendEmailVerification()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(VerifyMailActivity.this, "Fatto! controlla adesso", Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // questo metodo si invoca dopo che l'activity non viene più visualizzata e viene successivamente riaperta..
        // in poche parole, è qui che bisogna fare il reload sull'utente corrente delle sue informazioni

        if (mAuth.getCurrentUser() != null)
        mAuth.getCurrentUser()
                .reload()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        if (mAuth.getCurrentUser().isEmailVerified()) {
                            startActivity(new Intent(VerifyMailActivity.this, HomeActivity.class));
                            finish();
                        }
                        else
                            Toast.makeText(VerifyMailActivity.this,
                                    "sei tornato qui, ma non hai verificato ancora la tua email",
                                    Toast.LENGTH_SHORT).show();
                    }
                });
    }

}