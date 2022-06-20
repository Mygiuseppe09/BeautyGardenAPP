package com.example.beautygardenbanqueting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class VerifyMailActivity extends AppCompatActivity {

    private Button resendMail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("VerifyMail", "__ON_CREATE");
        /****************************************************************************************
         In questo metodo metteremo tutte quelle operazioni che vengono eseguite una sola volta,
         ovvero l’impostazione del layout e il salvataggio dei riferimenti dei relativi componenti.
         ****************************************************************************************/

        setContentView(R.layout.activity_verify_mail);
        resendMail = (Button) findViewById(R.id.resendMail);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("ActivityName", "_ON_START");
        /****************************************************************************************
         In questo metodo vengono implementate tutte quelle funzionalità che sono legate alla
         visualizzazione, ma non all’interazione con gli utenti. (es: listener, check sul login utente)
         ****************************************************************************************/

        resendMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification()
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
        Log.d("VerifyMail", "__ON_RESTART");
        /****************************************************************************************
         Il metodo onRestart() viene invocato quando un'activity era già stata creata e
         successivamente messa in pausa e quindi stoppata (passaggio ad un'altra activity).
         Serve a ripristinare quanto disabilitato nel metodo onStop() (es. listener),
         oppure ad aggiornare una variabile / oggetto inizializzato nella onCreate().
         ****************************************************************************************/

        FirebaseAuth.getInstance().getCurrentUser().reload()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            if (FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
                                startActivity(new Intent(VerifyMailActivity.this, HomeActivity.class));
                                finish();
                            }
                        }
                    });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("ActivityName", "_ON_RESUME");
        /****************************************************************************************
         In questo metodo vengono implementate tutte quelle funzioni che sono legate all’effettivo
         uso da parte dell’utente, come per esempio l’accesso alla fotocamera, suoni, animazioni.
         Inoltre, viene invocato al posto dell'onRestart() se l'activity non era stata stoppata,
         ma solo messa in pausa (capita quando si lancia una seconda activity non in full screen)
         ****************************************************************************************/

    }
}