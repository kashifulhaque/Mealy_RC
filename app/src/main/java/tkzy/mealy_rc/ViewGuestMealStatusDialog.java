package tkzy.mealy_rc;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import tkzy.mealy_rc.models.GuestMeal;

@SuppressWarnings("FieldCanBeLocal")
public class ViewGuestMealStatusDialog extends AppCompatDialogFragment {

    // Widgets
    private TextView mDay, mNight;

    // Variables
    private String phoneNumber;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_guest_meal_status, null);

        Bundle getPhoneNumber = getArguments();
        phoneNumber = getPhoneNumber.getString("phoneNumber");

        initialize(view);

        builder.setView(view)
                .setTitle("")
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return builder.create();
    }

    private void initialize(View view) {
        mDay = view.findViewById(R.id.tvDayMealGuestsUserProfile);
        mNight = view.findViewById(R.id.tvNightMealGuestsUserProfile);

        mDay.setText(getString(R.string.off));
        mNight.setText(getString(R.string.off));

        loadGuestMealStatus();
    }

    private void loadGuestMealStatus() {
        Query guestMealQuery = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.dbnode_guest_meal))
                .orderByKey()
                .equalTo(phoneNumber);

        guestMealQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    GuestMeal guestMealDetails = singleSnapshot.getValue(GuestMeal.class);

                    if (guestMealDetails != null) {
                        if (guestMealDetails.isDayMeal()) {
                            mDay.setText(getString(R.string.on));
                        } else {
                            mDay.setText(getString(R.string.off));
                        }

                        if (guestMealDetails.isNightMeal()) {
                            mNight.setText(getString(R.string.on));
                        } else {
                            mNight.setText(getString(R.string.off));
                        }
                    } else {
                        mDay.setText(getString(R.string.off));
                        mNight.setText(getString(R.string.off));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
