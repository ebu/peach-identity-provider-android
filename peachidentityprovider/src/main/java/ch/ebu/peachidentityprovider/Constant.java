package ch.ebu.peachidentityprovider;

public class Constant {

    public static final String KEY_ACCESS_TOKEN = "ch.ebu.peach.identity.provider.token.KEY_ACCESS_TOKEN";
    public static final String KEY_LAST_KNOWN_USER_ID = "ch.ebu.peach.identity.provider.KEY_LAST_KNOWN_USER_ID";
    public static final String TOKEN_TYPE = "ch.ebu.peach.identity.provider.token.ALL_ACCESS";
    public static final String KEY_EMAIL = "ch.ebu.peach.identity.provider.EMAIL";
    public static final String KEY_PROFILE = "ch.ebu.peach.identity.provider.PROFILE";

    public static final String[] BROWSER_WHITE_LIST = {"com.android.chrome", "com.sec.android.app.sbrowser", "org.mozilla.firefox", "com.opera.browser"};
    public static final String CHROME_PACKAGE_NAME = "com.android.chrome";
    public static final int CHROME_MINIMUM_VERSION = 45;

    public static final String WEBSERVICE_VERSION = "v2";
    public static final String PROFILE_URL = WEBSERVICE_VERSION + "/session/user/profile";
    public static final String SIGNUP_URL = WEBSERVICE_VERSION + "/session/signup";
    public static final String LOGIN_URL = WEBSERVICE_VERSION + "/session/login";
    public static final String LOGOUT_URL = WEBSERVICE_VERSION + "/session/logout";

    public static final String ARG_CALLBACK_RESULT = "ch.ebu.peach.identity.provider.result";

    public static final String PEACH_PROFILE_UPDATED = "ch.ebu.peach.identity.provider.profile.updated";

    public static final String PEACH_ERROR = "ch.ebu.peach.identity.provider.error";
    public static final String PEACH_ERROR_DESCRIPTION = "ch.ebu.peach.identity.provider.error.description";
    public static final String PEACH_ERROR_UNKNOWN = "UNKNOWN";
    public static final String PEACH_ERROR_BAD_DATA = "BAD_DATA";
    public static final String PEACH_ERROR_INCORRECT_LOGIN_OR_PASSWORD = "INCORRECT_LOGIN_OR_PASSWORD";

    /**
     * Log in attempt failed
     */
    public static final int RESULT_ERROR = 1;
    /**
     * Log in attempt was successful and user profile fetched
     */
    public static final int RESULT_SUCCESS = 2;
    /**
     * Log in attempt successful but user profile could not be fetched
     */
    public static final int RESULT_PROFILE_ERROR = 3;


    public static final String ACTION_LOGGED_IN = "logged_in";
    public static final String ACTION_UNAUTHORIZED = "unauthorized";
    public static final String ACTION_ACCOUNT_DELETED = "account_deleted";
    public static final String ACTION_LOG_OUT = "log_out";


    public static final class Gender {
        public static final int OTHER = 0;
        public static final int MALE = 1;
        public static final int FEMALE = 2;
        public static final int UNDEFINED = 3;
    }
}
