package ch.ebu.peachidentityprovider;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Browser;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


/**
 * Copyright (c) SRG SSR. All rights reserved.
 * <p>
 * License information is available from the LICENSE file.
 */
public class IdentityProfileWebFragment extends Fragment {
    private static final String TAG = "IdentityProfileFragment";

    public static final String ACTION_LOGGED_IN = "logged_in";
    public static final String ACTION_UNAUTHORIZED = "unauthorized";
    public static final String ACTION_ACCOUNT_DELETED = "account_deleted";
    public static final String ACTION_LOG_OUT = "log_out";

    @Nullable
    private WebView webView;
    private ProgressBar progressBar;
    private String profileUrl;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String webBaseUrl = getString(R.string.identity_web_url);
        String schemeUrl = getString(R.string.identity_application_scheme_url);
        StringBuilder str = new StringBuilder(webBaseUrl);
        if (!TextUtils.isEmpty(schemeUrl)) {
            str.append("?redirect=").append(schemeUrl).append("://profile");
        }
        profileUrl = str.toString();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            view = inflater.inflate(R.layout.fragment_web_profile_api18, container, false);
        } else {
            view = inflater.inflate(R.layout.fragment_web_profile, container, false);
            webView = view.findViewById(R.id.webview);
            progressBar = view.findViewById(R.id.progressBar);
            if (requireContext().getResources().getBoolean(R.bool.identity_web_content_debugging)) {
                WebView.setWebContentsDebuggingEnabled(true);
            }
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button clickToProfile = view.findViewById(R.id.button_click_to_profile);
        if (clickToProfile != null) {
            initClickToProfileMode(clickToProfile);
        }
        if (webView != null) {
            initWebViewMode(webView);
        }
    }

    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        if (webView == null) {
            return false;
        } else if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        } else {
            return false;
        }
    }

    private void initClickToProfileMode(Button clickToProfile) {
        clickToProfile.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_VIEW);
            Bundle bundle = new Bundle();
            bundle.putString("Authorization", IdentityProvider.getInstance().getAuthToken());
            i.putExtra(Browser.EXTRA_HEADERS, bundle);
            i.setData(Uri.parse(profileUrl + "identity"));
            startActivity(i);
            requireActivity().finish();
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebViewMode(@NonNull WebView webView) {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setUserAgentString(WebUtils.getUserAgent());
        if (!TextUtils.isEmpty(profileUrl)) {
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                    super.onReceivedError(view, request, error);
                    progressBar.setVisibility(View.GONE);
                    //Toast.makeText(requireContext(), getString(R.string.toast_profile_error), Toast.LENGTH_SHORT).show();
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (url != null) {
                        Uri uri = Uri.parse(url);
                        if (TextUtils.equals(getString(R.string.identity_application_scheme_url), uri.getScheme())) {
                            return parseUri(uri);
                        }
                        view.loadUrl(url);
                        return true;
                    }
                    return false;
                }
            });
            webView.loadUrl(profileUrl, WebUtils.getAuthorizationHeaders());
        }
    }

    protected boolean parseUri(@NonNull Uri uri) {
        String actionParameter = uri.getQueryParameter("action");
        if (actionParameter != null) {
            switch (actionParameter) {
                case ACTION_LOGGED_IN:
                    onLoggedInAction();
                    break;
                case ACTION_UNAUTHORIZED:
                    onUnauthorizedAction();
                    break;
                case ACTION_ACCOUNT_DELETED:
                    onAccountDeletedAction();
                    break;
                case ACTION_LOG_OUT:
                    onLogOutAction();
                    break;
            }
            return true;
        }
        return true;
    }

    protected void onLogOutAction() {
        IdentityProvider.getInstance().logOut();
        getActivity().finish();
    }

    protected void onAccountDeletedAction() {
        IdentityProvider.getInstance().logOut();
        getActivity().finish();
    }

    protected void onUnauthorizedAction() {
        IdentityProvider.getInstance().logOut();
        getActivity().finish();
    }

    protected void onLoggedInAction() {
        getActivity().finish();
    }
}

