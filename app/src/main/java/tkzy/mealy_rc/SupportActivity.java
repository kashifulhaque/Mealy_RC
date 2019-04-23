package tkzy.mealy_rc;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import tkzy.mealy_rc.R;

@SuppressWarnings("FieldCanBeLocal")
public class SupportActivity extends AppCompatActivity {

    // Widgets
    private Button mWatchVideo;
    private LinearLayout mPrivacyPolicy;

    // Google AdMob
    private InterstitialAd mInterstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        mWatchVideo = findViewById(R.id.btWatchVideo);
        mPrivacyPolicy = findViewById(R.id.llPrivacyPolicy);

        // Load the Interstitial and set a listener that listens for the Interstitial to get finished
        mInterstitial = new InterstitialAd(this);
        mInterstitial.setAdUnitId(getString(R.string.interstitial_ad_id));

        mInterstitial.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                mInterstitial.loadAd(new AdRequest.Builder().build());
            }

        });

        mWatchVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mInterstitial.isLoaded()) {
                    mInterstitial.show();
                }
            }
        });

        mPrivacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toURL("https://tkzydevs.github.io/");
            }
        });

    }

    private void toURL(String link) {
        Uri uri = Uri.parse(link);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(launchBrowser);
    }
}
