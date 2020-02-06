
[![](https://jitpack.io/v/bkhezry/MapDrawingTools.svg)](https://jitpack.io/#bkhezry/MapDrawingTools)

# About

The **Peach Identity Provider** framework for Android provides simple functionalities to facilitate the single sign on process of a user and visualization of their profile.


# Compatibility

The library is suitable for applications running on Android API 16 and above. The framework is built using the build tools version 29.0.2


# Setup
## 1. Provide the gradle dependency
Add it in your root build.gradle at the end of repositories:
```gradle
allprojects {
  repositories {
    ...
    maven { url "https://jitpack.io" }
  }
}
```
Add the dependency:
```gradle
dependencies {
  implementation 'com.github.ebu:peach-identity-provider-android:1.0.0'
}
```

# Usage

## 1. Configuration

In your app `build.gradle` file, add the following resources **and adapt the values to your needs**:

```gradle
defaultConfig {  
  ...

  resValue "string", "identity_web_url", "https://peach-staging.ebu.io/idp/"  
  resValue "string", "identity_webservice_url", "https://peach-staging.ebu.io/idp/api/"  
  resValue "string", "account_type", "ch.ebu.peachidpdemo"  
  resValue "string", "identity_name", "Peach Identity Demo"  
  resValue "string", "identity_application_scheme_url", "peachidp-demo"  
}
```
`identity_name` will be the name of the account that will appear in the phone's account manager.
`identity_application_scheme_url` should be configured as an authorized URL Scheme on the Identity Provider you will be linking to. This URL Scheme is used for the callback after a login action to trigger the result management.


## 2. Initialize the collector
In your main activity, provide the application to the IdentityProvider init function. After initialization, you can retrieve the shared instance with `getInstance()` method.
```java
provider = IdentityProvider.init(this);
```

## 3. Listen to any profile update
Create a receiver to manage any update of the profile (login, logout, fields update...)
```java
IntentFilter filter = new IntentFilter();  
filter.addAction(PEACH_PROFILE_UPDATED);  
registerReceiver(receiver, filter);
```

## 4. Open the login web view
```java
Intent intent = new Intent(this, MainActivity.class);  
intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);  
PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);  
IdentityProvider.getInstance().startLogin(this, pendingIntent);  
```
## 5. Open the profile view
```java
Intent intent = new Intent(this, UserProfileActivity.class);  
startActivity(intent);  
```

## 6. Token

Once a user has successfully logged in, a corresponding session token is available in the account manager.
```java
IdentityProvider.getInstance().getAuthToken()
```

## 7. Profile

Once a user has successfully logged in, a corresponding `Profile` object will be filled.
```java
Profile profile = IdentityProvider.getInstance().getIdentityAccountProfile()
```

## 8. Logout

To logout the current user:
```java
IdentityProvider.getInstance().logOut()
```
