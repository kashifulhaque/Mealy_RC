package tkzy.mealy_rc;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import tkzy.mealy_rc.models.User;

@SuppressWarnings("FieldCanBeLocal")
public class ManageGuestsDialog extends AppCompatDialogFragment {

    // Widgets
    private Button mMinus, mPlus;
    private TextView mGuests;
    private int guestCount;

    // Listener
    private ManageGuestListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        getGuestCount();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_manage_guests, null);

        initialize(view);

        builder.setView(view)
                .setTitle("Manage Guests")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton("Add Guests", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        listener.addGuests(mGuests.getText().toString());

                    }
                });

        return builder.create();
    }

    private void getGuestCount() {
        Query query = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.dbnode_users))
                .orderByKey()
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    User user = singleSnapshot.getValue(User.class);

                    if (user != null) {
                        mGuests.setText(user.getNumberOfGuests());
                    } else {
                        mGuests.setText("0");
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (ManageGuestListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement ManageGuestListener");
        }
    }

    private void initialize(View view) {
        mMinus = view.findViewById(R.id.btDecreaseGuests);
        mPlus = view.findViewById(R.id.btIncreaseGuests);
        mGuests = view.findViewById(R.id.tvNumberOfGuests);

        guestCount = Integer.parseInt(mGuests.getText().toString());

        mMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (guestCount == 0) {
                    mGuests.setText("0");
                } else {
                    --guestCount;
                    mGuests.setText("" + guestCount);
                }
            }
        });

        mPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ++guestCount;
                mGuests.setText("" + guestCount);
            }
        });
    }

    public interface ManageGuestListener {
        void addGuests(String numberOfGuests);
    }

}
