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
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

@SuppressWarnings("FieldCanbeLocal")
public class SetManagerDialog extends AppCompatDialogFragment {

    // Widgets
    private EditText mSetManager;

    // Listener
    private SetManagerListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        getCurrentManagerPhoneNumber();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_set_manager, null);

        initialize(view);

        builder.setView(view)
                .setTitle("Set Manager")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton("Set Manager", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        listener.setManager(mSetManager.getText().toString());

                    }
                });

        return builder.create();

    }

    private void getCurrentManagerPhoneNumber() {
        Query query = FirebaseDatabase.getInstance().getReference().child("check_manager")
                .orderByKey()
                .equalTo("managerPhoneNumber");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                    mSetManager.setText(singleSnapshot.getValue(String.class));

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
            listener = (SetManagerListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement SetManagerListener.");
        }
    }

    private void initialize(View view) {
        mSetManager = view.findViewById(R.id.etManagerPhoneNumberByGS);
    }

    public interface SetManagerListener {
        void setManager(String phoneNumber);
    }

}
