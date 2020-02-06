package ch.ebu.peachidpdemo;

import android.app.Application;
import ch.ebu.peachidentityprovider.IdentityProvider;

/**
 * Copyright (c) SRG SSR. All rights reserved.
 * <p>
 * License information is available from the LICENSE file.
 */
public class DemoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        IdentityProvider.init(this);
    }
}
