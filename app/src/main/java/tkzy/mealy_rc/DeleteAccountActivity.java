package tkzy.mealy_rc;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import tkzy.mealy_rc.R;

@SuppressWarnings("FieldCanBeLocal")
public class DeleteAccountActivity extends AppCompatActivity {

    // Widgets
    private Button mDelete;
    private TextView mTicket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_account);

        mDelete = findViewById(R.id.btDeleteAccount);
        mTicket = findViewById(R.id.tvDeleteTicket);

        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAccount();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseDatabase.getInstance().getReference().child("account_deletion_tickets")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                            if (FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber().equals(singleSnapshot.getValue(String.class))) {
                                mTicket.setText(getString(R.string.account_deletion_soon));
                                mDelete.setVisibility(View.GONE);
                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        if (mTicket.getText().equals(getString(R.string.account_deletion_soon))) {
            mDelete.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mTicket.getText().equals(getString(R.string.account_deletion_soon))) {
            mDelete.setVisibility(View.GONE);
        }
    }

    private void deleteAccount() {

        String key = FirebaseDatabase.getInstance().getReference()
                .child("account_deletion_tickets")
                .push().getKey();

        FirebaseDatabase.getInstance().getReference()
                .child("account_deletion_tickets")
                .child(key)
                .setValue(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        mTicket.setText(getString(R.string.account_deletion_soon));
                        mDelete.setVisibility(View.GONE);
                        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                        homeIntent.addCategory(Intent.CATEGORY_HOME);
                        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(homeIntent);

                    }
                });

    }

}
