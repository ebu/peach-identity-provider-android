package ch.ebu.peachidentityprovider;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.core.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ch.ebu.peachidentityprovider.Constant.*;
import static java.net.HttpURLConnection.*;


/**
 * Copyright (c) SRG SSR. All rights reserved.
 * <p>
 * License information is available from the LICENSE file.
 */
public class IdentityActivity extends AccountAuthenticatorActivity {
    /**
     * Integer argument (one of RESULT_ERROR, RESULT_PROFILE_ERROR or RESULT_SUCCESS) added to
     * callback intent.
     */
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.executorService = Executors.newSingleThreadExecutor();
        processIntent(getIntent());
    }

    /**
     * Handle callback from custom tab or action view and login from Account manager.
     * See {@link IdentityProvider#createLoginWebIntent}
     */
    private void processIntent(Intent intent) {
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            finishWithToken(intent.getData());
        } else {
            startWebLogin();
        }
    }

    private void startWebLogin() {
        Intent intent = IdentityProvider.getInstance().createLoginWebIntent(this);
        ActivityCompat.startActivity(this, intent, null);
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        processIntent(intent);
    }

    private void finishWithToken(@Nullable Uri uri) {
        if (uri != null) {
            String actionParameter = uri.getQueryParameter("action");
            String authToken = uri.getQueryParameter("token");
            if (!TextUtils.isEmpty(authToken)) {
                authToken = "sessionToken " + authToken;
                finishLogin(authToken);
            } else if (!TextUtils.isEmpty(actionParameter)) {
                IdentityProvider identityProvider = IdentityProvider.init(this);
                switch (actionParameter) {
                    case ACTION_LOGGED_IN:
                        // Nothing
                        break;
                    case ACTION_UNAUTHORIZED:
                    case ACTION_ACCOUNT_DELETED:
                    case ACTION_LOG_OUT:
                        identityProvider.logOut();
                        finish();
                        break;
                }
            } else {
                finish();
            }
        } else {
            finish();
        }
    }

    private void finishLogin(final String authToken) {
        final String accountType = getString(R.string.account_type);
        IdentityProvider.init(this); // Can't guarantee that init is called before, for example when launching from account manager.

        executorService.execute(() -> {
            int result = RESULT_ERROR;
            HttpURLConnection urlConnection = null;
            StringBuilder response = new StringBuilder();

            try {
                URL url = new URL(IdentityProvider.getInstance().serviceURL + PROFILE_URL);
                urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.setRequestProperty("Authorization", authToken);
                urlConnection.setRequestMethod("GET");//important
                urlConnection.connect();

                //get response code and check if valid (HTTP OK)
                int responseCode = urlConnection.getResponseCode();

                if (responseCode >= HTTP_OK && responseCode < HTTP_MULT_CHOICE) {

                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    String login = null;
                    try {
                        JSONObject obj = new JSONObject(response.toString());
                        Profile profile = new Profile(obj.getJSONObject("user"));
                        login = profile.login;

                    } catch (Throwable t) {
                        Log.e("Profile Request", "Could not parse malformed JSON: \"" + result + "\"");
                    }

                    if (!TextUtils.isEmpty(login)){

                        Account account = new Account(getString(R.string.identity_name), accountType);

                        AccountManager accountManager = AccountManager.get(this);
                        accountManager.addAccountExplicitly(account, null, null);
                        accountManager.setAuthToken(account, TOKEN_TYPE, authToken);
                        accountManager.setUserData(account, KEY_ACCESS_TOKEN, authToken);
                        accountManager.setUserData(account, KEY_EMAIL, login);
                        accountManager.setUserData(account, KEY_PROFILE, response.toString());

                        Bundle bundle = new Bundle();
                        bundle.putString(AccountManager.KEY_ACCOUNT_NAME, getString(R.string.identity_name));
                        bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
                        bundle.putString(AccountManager.KEY_AUTHTOKEN, authToken);
                        setAccountAuthenticatorResult(bundle);

                        result = RESULT_SUCCESS;
                    } else {
                        Log.e("IdentityActivity", "Profile login field was not found");
                        result = RESULT_PROFILE_ERROR;
                    }

                }
                else {
                    result = RESULT_PROFILE_ERROR;
                }
            } catch (IOException e) {
                result = RESULT_PROFILE_ERROR;
            } finally {
                urlConnection.disconnect();
                final int resultFinal = result;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sendResult(resultFinal);
                    }
                });
            }
        });

    }

    @UiThread
    private void sendResult(int result) {
        try {
            PendingIntent pendingIntent = IdentityProvider.getInstance().getCallbackIntent();
            if (pendingIntent != null) {
                Intent argIntent = new Intent();
                argIntent.putExtra(ARG_CALLBACK_RESULT, result);
                pendingIntent.send(this, 0, argIntent);
            }
        } catch (PendingIntent.CanceledException e) {
            Log.e("Callback intent", e.toString());
        }
        finish();
    }
}
