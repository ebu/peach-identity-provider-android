package ch.ebu.peachidentityprovider;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.Map;


/**
 * Copyright (c) SRG SSR. All rights reserved.
 * <p>
 * License information is available from the LICENSE file.
 */
public class WebUtils {

    private static final String NOT_CONNECTED_HEADER_TOKEN = "sessionToken null";

    public static String getUserAgent() {
        return System.getProperty("http.agent");
    }

    @NonNull
    public static Map<String, String> getAuthorizationHeaders() {
        String authToken = IdentityProvider.getInstance().getAuthToken();
        return Collections.singletonMap("Authorization", TextUtils.isEmpty(authToken) ? NOT_CONNECTED_HEADER_TOKEN : authToken);
    }
}
