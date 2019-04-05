package tkzy.mealy_rc;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import tkzy.mealy_rc.R;

@SuppressWarnings("FieldCanBeLocal")
public class SupportActivity extends AppCompatActivity {

    // Widgets
    private Button mWatchVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        mWatchVideo = findViewById(R.id.btWatchVideo);

        mWatchVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SupportActivity.this, "Thank you for checking it out, Coming Soon!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
