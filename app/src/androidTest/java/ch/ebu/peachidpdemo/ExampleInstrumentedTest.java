package ch.ebu.peachidpdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import ch.ebu.peachidentityprovider.IdentityProvider;
import ch.ebu.peachidentityprovider.Profile;

import static ch.ebu.peachidentityprovider.Constant.PEACH_PROFILE_UPDATED;
import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    private Context mContext;
    public ProfileReceiver mReceiver = new ProfileReceiver();

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        assertEquals("ch.ebu.peachidpdemo", appContext.getPackageName());
    }

    @Test
    public void testConfiguration(){
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("https://peach-staging.ebu.io/idp/", appContext.getString(R.string.identity_web_url));
        assertEquals("https://peach-staging.ebu.io/idp/api/", appContext.getString(R.string.identity_webservice_url));
        assertEquals("ch.ebu.peachidpdemo", appContext.getString(R.string.account_type));
        assertEquals("Peach Identity Demo", appContext.getString(R.string.identity_name));
        assertEquals("peachidp-demo", appContext.getString(R.string.identity_application_scheme_url));
    }

    @Test
    public void testFakeLoginResponse(){
        mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        IntentFilter filter = new IntentFilter();
        filter.addAction(PEACH_PROFILE_UPDATED);
        mContext.registerReceiver(mReceiver, filter);

        IdentityProvider.getInstance().removeAccount();
        IdentityProvider.getInstance().storeUserId(null);
        IdentityProvider.getInstance().storeProfile(null);

        assertNull(IdentityProvider.getInstance().getLastKnownUserId());
        assertNull(IdentityProvider.getInstance().getAuthToken());
        assertNull(IdentityProvider.getInstance().getIdentityAccount());
        assertNull(IdentityProvider.getInstance().getIdentityAccountEmail());
        assertNull(IdentityProvider.getInstance().getIdentityAccountProfile());


        String url ="peachidp-demo://identity?token=s%3Agf7lR10Tp0sXa-s0uL_lj6_cEe1Ef_pu.eG97AVf0gpeoZ5r9cKGcHf%2BrqQwjJY3XRwaPdAuNSiE";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
        InstrumentationRegistry.getInstrumentation().getTargetContext().startActivity(intent);


        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Profile profile = IdentityProvider.getInstance().getIdentityAccountProfile();

        assertNotNull(IdentityProvider.getInstance().getLastKnownUserId());
        assertNotNull(IdentityProvider.getInstance().getAuthToken());
        assertNotNull(IdentityProvider.getInstance().getIdentityAccount());
        assertNotNull(IdentityProvider.getInstance().getIdentityAccountEmail());
        assertNotNull(profile);

        assertEquals("arnaout@ebu.ch", profile.emailAddress);

        boolean b = mReceiver.testSuccess;
        assertTrue("Notification was sent",b);
    }


    public class ProfileReceiver extends BroadcastReceiver {

        boolean testSuccess;

        public ProfileReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            testSuccess = true;

        }
    }

}
