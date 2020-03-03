package ch.ebu.peachidpdemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import ch.ebu.peachidentityprovider.IdentityProvider;
import ch.ebu.peachidentityprovider.Profile;

import static ch.ebu.peachidentityprovider.Constant.PEACH_ERROR;
import static ch.ebu.peachidentityprovider.Constant.PEACH_ERROR_BAD_DATA;
import static ch.ebu.peachidentityprovider.Constant.PEACH_ERROR_DESCRIPTION;
import static ch.ebu.peachidentityprovider.Constant.PEACH_ERROR_INCORRECT_LOGIN_OR_PASSWORD;
import static ch.ebu.peachidentityprovider.Constant.PEACH_PROFILE_UPDATED;

public class MainActivity extends AppCompatActivity {
    IdentityProvider provider;
    ProfileUpdateReceiver receiver = new ProfileUpdateReceiver();

    Button button;
    Button profileButton;
    TextView textView;

    public class ProfileUpdateReceiver extends BroadcastReceiver {
        public ProfileUpdateReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            updateProfileDisplay();

            // for login or sign up, check for errors
            if(intent.hasExtra(PEACH_ERROR)) {
                if (intent.getStringExtra(PEACH_ERROR).equalsIgnoreCase(PEACH_ERROR_BAD_DATA)) {
                    Log.e("ERROR", intent.getStringExtra(PEACH_ERROR_DESCRIPTION));
                }
                else if (intent.getStringExtra(PEACH_ERROR).equalsIgnoreCase(PEACH_ERROR_INCORRECT_LOGIN_OR_PASSWORD)) {
                    Log.e("ERROR", "Incorrect login or password");
                }
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        provider = IdentityProvider.init(this);

        setContentView(R.layout.activity_main);


        IntentFilter filter = new IntentFilter();
        filter.addAction(PEACH_PROFILE_UPDATED);
        registerReceiver(receiver, filter);


        textView = findViewById(R.id.textview);
        button = findViewById(R.id.button);
        profileButton = findViewById(R.id.profileButton);

        profileButton.setOnClickListener(v -> displayUserProfile());

        int identityResult = IdentityProvider.getInstance().getCallbackResult(this);
        if (identityResult != -1) {
            Toast.makeText(this, "Identity result: " + identityResult, Toast.LENGTH_LONG).show();
        }

        updateProfileDisplay();
    }

    void updateProfileDisplay(){

        if (IdentityProvider.getInstance().hasAlreadyAccount()) {
            Profile profile = IdentityProvider.getInstance().getIdentityAccountProfile();
            if (profile != null) {
                String firstName = (profile.firstName!=null) ? profile.firstName : "<undefined>";
                String lastName = (profile.lastName!=null) ? profile.lastName : "<undefined>";

                textView.setText("Logged in : " + profile.login + "\nFirstName: " + firstName + "\nLastName: " + lastName);
                button.setText("LOGOUT");
                profileButton.setVisibility(View.VISIBLE);
                button.setOnClickListener(v -> IdentityProvider.getInstance().logOut());
            }
            else{
                textView.setText("Logged out");
                button.setText("LOGIN");
                profileButton.setVisibility(View.GONE);
                button.setOnClickListener(v -> login());
            }

        }
        else {
            textView.setText("Logged out");
            button.setText("LOGIN");
            profileButton.setVisibility(View.GONE);
            button.setOnClickListener(v -> login());
        }
    }

    void login(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        provider.startLogin(this, pendingIntent);
    }

    public void displayUserProfile() {
        Intent intent = new Intent(this, UserProfileActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isChangingConfigurations()) {
            IdentityProvider.getInstance().fetchUserProfile();
        }
    }
}
