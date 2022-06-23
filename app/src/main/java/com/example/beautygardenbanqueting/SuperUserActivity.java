package com.example.beautygardenbanqueting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SuperUserActivity extends AppCompatActivity {

    private TextView nameView;
    private TextView surnameView;
    private TextView emailView;
    private ImageView homeTab;
    private ImageView logoutTab;

    private FirebaseDatabase db;
    private FirebaseAuth mAuth;

    private User loggedSuperuser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_user);

        nameView = (TextView) findViewById(R.id.nameView);
        surnameView = (TextView) findViewById(R.id.surnameView);
        emailView = (TextView) findViewById(R.id.emailView);
        homeTab = (ImageView) findViewById(R.id.homeIcon);
        logoutTab = (ImageView) findViewById(R.id.logoutIcon);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

        // reperiamo le informazioni del superuser loggato e le mettiamo in alto
        db.getReference("users")
                .child(mAuth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()  && snapshot.getValue() != null) {
                            loggedSuperuser = snapshot.getValue(User.class);
                            if (loggedSuperuser != null) {
                                nameView.setText(loggedSuperuser.getName());
                                surnameView.setText(loggedSuperuser.getSurname());
                                emailView.setText(loggedSuperuser.getEmail());
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(SuperUserActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();

        homeTab.setOnClickListener(this::onClick);
        logoutTab.setOnClickListener(this::onClick);
    }

    @SuppressLint("NonConstantResourceId")
    private void onClick(View view) {
        switch (view.getId()) {
            case R.id.homeIcon:
                startActivity(new Intent(SuperUserActivity.this, SuperHomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                break;
            case R.id.logoutIcon:
                mAuth.signOut();
                startActivity(new Intent(SuperUserActivity.this, LoginActivity.class));
                finish();
                break;
        }
    }
}