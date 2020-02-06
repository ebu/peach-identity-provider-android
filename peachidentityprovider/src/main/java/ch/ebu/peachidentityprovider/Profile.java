package ch.ebu.peachidentityprovider;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import static ch.ebu.peachidentityprovider.Constant.Gender.*;

public class Profile {

    /**
     *  The unique account identifier.
     */
    public String uid;

    /**
     *  Usually email.
     */
    public String login;

    /**
     *  The unique public account identifier.
     */
    public String publicUid;

    /**
     *  The account display name.
     */
    public String displayName;

    /**
     *  The email address associated with the account.
     */
    public String emailAddress;

    /**
     *  The user first name.
     */
    public String firstName;

    /**
     *  The user last name.
     */
    public String lastName;

    /**
     *  The user gender.
     */
    public int gender;

    /**
     *  The user birthdate.
     */
    public Date birthdate;

    /**
     *  `YES` iff the account has been verified.
     */
    public boolean isVerified;

    /**
     *  The user last name.
     */
    public String language;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Profile that = (Profile) o;

        if (isVerified != that.isVerified) return false;
        if (uid != null ? !uid.equals(that.uid) : that.uid != null) return false;
        if (login != null ? !login.equals(that.login) : that.login != null) return false;
        if (!displayName.equals(that.displayName)) return false;
        if (firstName != null ? !firstName.equals(that.firstName) : that.firstName != null)
            return false;
        if (lastName != null ? !lastName.equals(that.lastName) : that.lastName != null)
            return false;
        if (gender != that.gender) return false;
        if (birthdate != null ? !birthdate.equals(that.birthdate) : that.birthdate != null)
            return false;
        if (publicUid != null ? !publicUid.equals(that.publicUid) : that.publicUid != null)
            return false;
        return language != null ? language.equals(that.language) : that.language == null;
    }

    public int parseGender(String value){
        if (!TextUtils.isEmpty(value)){
            if (value.equalsIgnoreCase("male")) return MALE;
            if (value.equalsIgnoreCase("female")) return FEMALE;
            if (value.equalsIgnoreCase("other")) return OTHER;
        }
        return UNDEFINED;
    }



    public Profile(JSONObject obj){
        try {
            if (obj.has("id")) {
                uid = obj.getString("id");
            }
            if (obj.has("public_uid")) {
                publicUid = obj.getString("public_uid");
            }
            if (obj.has("login")) {
                login = obj.getString("login");
            }
            if (obj.has("email")) {
                emailAddress = obj.getString("email");
            }
            if (obj.has("firstname")) {
                firstName = obj.getString("firstname");
            }
            if (obj.has("lastname")) {
                lastName = obj.getString("lastname");
            }
            if (obj.has("display_name")) {
                displayName = obj.getString("display_name");
            }
            if (obj.has("language")) {
                language = obj.getString("language");
            }
            if (obj.has("email_verified") && !obj.isNull("email_verified")) {
                isVerified = obj.getBoolean("email_verified");
            }
            if (obj.has("gender")) {
                gender = parseGender(obj.getString("gender"));
            }

            if (obj.has("date_of_birth") && !obj.isNull("date_of_birth")) {
                birthdate = new Date(obj.getLong("date_of_birth"));
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    public static Profile init(String value){
        try {
            JSONObject obj = new JSONObject(value);
            return new Profile(obj.getJSONObject("user"));

        } catch (Throwable t) {
            return null;
        }
    }
}
