package cz.uhk.fim.runhk.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import cz.uhk.fim.runhk.R;
import cz.uhk.fim.runhk.model.Player;

public class LogInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;

    private FirebaseUser currentUser;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        Button btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .build(),
                        RC_SIGN_IN);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in


                currentUser = FirebaseAuth.getInstance().getCurrentUser();


                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                DatabaseReference userNameRef = rootRef.child("user").child(currentUser.getUid());
                ValueEventListener eventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            firebaseDatabase = FirebaseDatabase.getInstance();
                            Player player = new Player("", currentUser.getEmail(), "", 1, 0);
                            databaseReference = firebaseDatabase.getReference("user");
                            databaseReference.child(currentUser.getUid()).setValue(player);

                        } else {
                            System.out.println("uz tu je tak cuus");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                };

                userNameRef.addListenerForSingleValueEvent(eventListener);


                Intent intent = new Intent(this, PlayerProfileActivity.class);
                finish();
                startActivity(intent);

            } else {
                Toast.makeText(this, "Špatné jméno nebo heslo", Toast.LENGTH_SHORT).show();

            }
        }
    }
}
