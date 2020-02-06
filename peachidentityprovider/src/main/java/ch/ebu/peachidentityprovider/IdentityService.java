package ch.ebu.peachidentityprovider;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class IdentityService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        IdentityAuthenticator authenticator = new IdentityAuthenticator(this);
        return authenticator.getIBinder();
    }
}
