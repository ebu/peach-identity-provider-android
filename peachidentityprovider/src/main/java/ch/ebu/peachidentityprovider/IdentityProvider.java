package ch.ebu.peachidentityprovider;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.app.ActivityCompat;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static ch.ebu.peachidentityprovider.Constant.*;
import static java.net.HttpURLConnection.*;

public class IdentityProvider {

    private static final String TAG = "IdentityProvider";
    public static IdentityProvider instance;

    private PendingIntent callbackIntent;

    private final AccountManager accountManager;

    private Context mContext;

    // developer defined properties
    protected String serviceURL;
    private String webURL;
    private String urlScheme;
    private String accountID;


    private IdentityProvider(Context context) {
        mContext = context;
        checkNoDefaultValues(context);
        this.accountManager = AccountManager.get(context);
        this.serviceURL = context.getString(R.string.identity_webservice_url);
        this.webURL = context.getString(R.string.identity_web_url);
        this.accountID = context.getString(R.string.account_type);
        this.urlScheme = context.getString(R.string.identity_application_scheme_url);

        fetchUserProfile();
    }

    public static IdentityProvider init(Context context) {
        if (instance == null) {
            instance = new IdentityProvider(context);
        }
        return instance;
    }


    public static IdentityProvider getInstance() {
        if (instance == null) {
            throw new IllegalStateException("You have to call init before.");
        }
        return instance;
    }


    private void checkNoDefaultValues(Context context) {
        if (TextUtils.isEmpty(context.getString(R.string.identity_web_url))) {
            throw new IllegalStateException("You have to override default config value: identity_web_url");
        }
        if (TextUtils.isEmpty(context.getString(R.string.identity_webservice_url))) {
            throw new IllegalStateException("You have to override default config value: identity_webservice_url");
        }
        if (TextUtils.isEmpty(context.getString(R.string.account_type))) {
            throw new IllegalStateException("You have to override default config value: account_type");
        }
        if (TextUtils.isEmpty(context.getString(R.string.identity_name))) {
            throw new IllegalStateException("You have to override default config value: identity_name");
        }
        if (TextUtils.isEmpty(context.getString(R.string.identity_application_scheme_url))) {
            throw new IllegalStateException("You have to override default config value: identity_application_scheme_url");
        }
    }



    /**
     * @return identity account if exist, otherwise return null
     */
    @Nullable
    public Account getIdentityAccount() {
        Account[] accounts = getAccounts();
        if (accounts.length >= 1) {
            return accounts[0];
        }
        return null;
    }

    public boolean hasAlreadyAccount() {
        return getIdentityAccount() != null;
    }

    @Nullable
    public String getIdentityAccountEmail() {
        Account account = getIdentityAccount();
        if (account != null) {
            return accountManager.getUserData(account, KEY_EMAIL);
        } else {
            return null;
        }
    }

    @Nullable
    public Profile getIdentityAccountProfile() {
        Account account = getIdentityAccount();
        if (account != null) {
            String profileString = accountManager.getUserData(account, KEY_PROFILE);
            return Profile.init(profileString);
        } else {
            return null;
        }
    }

    @NonNull
    private Account[] getAccounts() {
        if (accountID != null && !accountID.isEmpty()) {
            return accountManager.getAccountsByType(accountID);
        } else {
            return new Account[0];
        }
    }

    public String getAuthToken() {
        Account account = getIdentityAccount();
        if (account != null) {
            return accountManager.getUserData(account, KEY_ACCESS_TOKEN);
        } else {
            return null;
        }
    }


    public void fetchUserProfile() {
        String token = getAuthToken();
        if (token != null && !token.isEmpty()) {
            new ProfileTask().execute(token);
        }
    }


    public class ProfileTask extends AsyncTask<String, String, String> {

        HttpURLConnection urlConnection;

        @Override
        protected String doInBackground(String... args) {

            StringBuilder result = new StringBuilder();

            try {
                URL url = new URL(serviceURL + PROFILE_URL);
                urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.setRequestProperty("Authorization", args[0]);
                urlConnection.setRequestMethod("GET");//important
                urlConnection.connect();

                //get response code and check if valid (HTTP OK)
                int responseCode = urlConnection.getResponseCode();
                if (responseCode >= HTTP_OK && responseCode < HTTP_MULT_CHOICE) {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                }
                else if (responseCode == HTTP_UNAUTHORIZED) {
                    storeUserId(null);
                }
            }catch( Exception e) {
                e.printStackTrace();
            }
            finally {
                urlConnection.disconnect();
            }

            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            if (TextUtils.isEmpty(result)) {
                logOut();
            }
            try {
                JSONObject obj = new JSONObject(result);
                Profile profile = new Profile(obj.getJSONObject("user"));
                storeUserId(profile.uid);
                storeProfile(result);

                Intent intent = new Intent();
                intent.setAction(PEACH_PROFILE_UPDATED);
                mContext.sendBroadcast(intent);
            } catch (Throwable t) {
                Log.e("Profile Request", "Could not parse malformed JSON: \"" + result + "\"");
            }
        }

    }

    @UiThread
    public void logOut() {
        storeUserId(null);
        storeProfile(null);

        final Account account = getIdentityAccount();
        if (account != null) {
            String token = getAuthToken();
            removeAccount();
            if (token != null && !token.isEmpty()) {
                new LogOutTask().execute(token);
            }
            else {
                Intent intent = new Intent();
                intent.setAction(PEACH_PROFILE_UPDATED);
                mContext.sendBroadcast(intent);
            }
        }
    }


    public class LogOutTask extends AsyncTask<String, String, String> {

        HttpURLConnection urlConnection;

        @Override
        protected String doInBackground(String... args) {
            try {
                URL url = new URL(serviceURL + LOGOUT_URL);
                urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.setRequestProperty("Authorization", args[0]);
                urlConnection.setRequestMethod("DELETE");//important
                urlConnection.connect();

                int responseCode = urlConnection.getResponseCode();
                if (responseCode >= HTTP_OK && responseCode < HTTP_MULT_CHOICE) {
                    Log.d(TAG, "Logged out");
                }
            }catch( Exception e) {
                e.printStackTrace();
            }
            finally {
                Intent intent = new Intent();
                intent.setAction(PEACH_PROFILE_UPDATED);
                mContext.sendBroadcast(intent);
                urlConnection.disconnect();
            }

            return null;
        }
    }

    public void removeAccount() {
        Account account = getIdentityAccount();
        if (account != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                accountManager.removeAccountExplicitly(account);
            } else {
                accountManager.removeAccount(account, future -> {
                    try {
                        Boolean isSuccess = future.getResult();
                        if (isSuccess == null || !isSuccess) {
                            Log.e(TAG, "unsuccessful removeAccount");
                        }
                    } catch (OperationCanceledException | IOException | AuthenticatorException e) {
                        Log.e(TAG, "removeAccount error : " + e);
                    }
                }, new Handler());
            }
        }
    }


    public void storeUserId(String userId) {
        Account account = getIdentityAccount();
        if (account != null) {
            accountManager.setUserData(account, KEY_LAST_KNOWN_USER_ID, userId);
        }
    }

    public void storeProfile(String profile) {
        Account account = getIdentityAccount();
        if (account != null) {
            accountManager.setUserData(account, KEY_PROFILE, profile);
        }
    }

    @Nullable
    public String getLastKnownUserId() {
        Account account = getIdentityAccount();
        if (account != null) {
            return accountManager.getUserData(account, KEY_LAST_KNOWN_USER_ID);
        }
        return null;
    }


    /**
     * This method start the IdentityActivity with LOGIN_REQUEST_CODE.
     * To update automatically the selected user profile override the onActivityResult of your Activity
     * <code>
     * protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     * super.onActivityResult(requestCode, resultCode, data);
     * IdentityManager.getInstance().loginResult(requestCode, resultCode, data);
     * }
     * </code>
     */
    public void startLogin(@NonNull Context context) {
        startLogin(context, null);
    }

    public void startLogin(@NonNull Context context, PendingIntent pendingIntent) {
        startLogin(context, pendingIntent, null);
    }

    /**
     * This method start the IdentityActivity with LOGIN_REQUEST_CODE.
     * To update automatically the selected user profile override the onActivityResult of your Activity
     * <pre>
     * protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     *   super.onActivityResult(requestCode, resultCode, data);
     *   IdentityManager.getInstance().loginResult(requestCode, resultCode, data);
     * }
     * </pre>
     *
     * @param context       context to use
     * @param pendingIntent intent sent on identity callback. The intent will be decorated with
     *                      {@link Constant#ARG_CALLBACK_RESULT} with login result.
     */
    public void startLogin(@NonNull Context context, PendingIntent pendingIntent, Bundle options) {
        callbackIntent = pendingIntent;
        Intent loginIntent = createLoginWebIntent(context);
        ActivityCompat.startActivity(context, loginIntent, options);
    }

    public PendingIntent getCallbackIntent() {
        return callbackIntent;
    }

    /**
     * @param activity that receive the callback PendingIntent
     * @return -1 if nothing is received
     * @see Constant#RESULT_ERROR
     * @see Constant#RESULT_SUCCESS
     * @see Constant#RESULT_PROFILE_ERROR
     */
    public int getCallbackResult(Activity activity) {
        return activity.getIntent().getIntExtra(ARG_CALLBACK_RESULT, -1);
    }

    protected Intent createLoginWebIntent(Context context) {
        Uri uri = Uri.parse(getLoginUrl(context));
        if (!canUseCustomTabs(context)) {
            Intent webActionViewIntent = new Intent(Intent.ACTION_VIEW);
            webActionViewIntent.setData(uri);
            webActionViewIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            return webActionViewIntent;
        } else {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            builder.enableUrlBarHiding();
            builder.setInstantAppsEnabled(false);
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.intent.setData(uri);
            customTabsIntent.intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY); // To close custom tabs when finished
            return customTabsIntent.intent;
        }
    }

    private static boolean canUseCustomTabs(Context context) {
        boolean customTabs = false;
        int chromeVersion = 0;
        String browserPackage = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                browserPackage = getDefaultBrowserPackage(context);
                if (browserPackage != null && browserPackage.contains(CHROME_PACKAGE_NAME)) {
                    String chromeVersionString = context.getApplicationContext().getPackageManager().getPackageInfo(CHROME_PACKAGE_NAME, 0).versionName;
                    if (chromeVersionString.contains(".")) {
                        chromeVersionString = chromeVersionString.substring(0, chromeVersionString.indexOf('.'));
                    }
                    chromeVersion = Integer.parseInt(chromeVersionString);
                    return (chromeVersion >= CHROME_MINIMUM_VERSION);
                }
                customTabs = isBrowserInWhiteList(browserPackage);
            } catch (Exception ignored) {
            }
        }
        //Log.d("TABS CHECK", browserPackage + " " + chromeVersion + " -> " + customTabs);
        return customTabs;
    }

    private String getLoginUrl(Context context) {
        String initialEmail = "";
        StringBuilder str = new StringBuilder(webURL);
        str.append("login?withcode=true&redirect=").append(urlScheme).append("://").append("identity");
        if (!TextUtils.isEmpty(initialEmail)) {
            str.append("&email=").append(initialEmail);
        }

        return str.toString();
    }

    /**
     * Workaround because google/facebook login opens a popup.
     * CustomTabs with firefox as default browser will open the popup in Firefox and not in CustomTabs
     * Making impossible to redirect to the parent url, so we are stuck in the login page in some browsers.
     * We are working on a solution on the web directly to allows login without popup is possible.
     * <p>
     * White list @see {@link Constant#BROWSER_WHITE_LIST}
     *
     * @param packageName browser package
     */
    private static boolean isBrowserInWhiteList(String packageName) {
        for (String whiteListItem : BROWSER_WHITE_LIST) {
            if (packageName.contains(whiteListItem)) {
                return true;
            }
        }
        return false;
    }

    private static String getDefaultBrowserPackage(Context context) {
        Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse("http://"));
        ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(browserIntent, PackageManager.MATCH_DEFAULT_ONLY);
        return resolveInfo.activityInfo.packageName;
    }




}
