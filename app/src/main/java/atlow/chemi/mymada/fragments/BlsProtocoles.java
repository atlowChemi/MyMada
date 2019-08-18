package atlow.chemi.mymada.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.Locale;

import atlow.chemi.mymada.R;

public class BlsProtocoles extends Fragment {
    private ProgressBar spinner;
    private WebView webview;
    private String BLS = "https://firebasestorage.googleapis.com/v0/b/project-3918384647339420127.appspot.com/o/bls%5B1%5D.pdf?alt=media&token=7da40564-fe83-4254-a929-f8b2ec67ca41",
    ALS = "https://firebasestorage.googleapis.com/v0/b/project-3918384647339420127.appspot.com/o/als%5B1%5D.pdf?alt=media&token=49d9e5f1-03d5-49d5-a951-b58dce094ca6",
    Dead = "https://firebasestorage.googleapis.com/v0/b/project-3918384647339420127.appspot.com/o/kuntres.pdf?alt=media&token=2b7c5ce5-e5e5-4fae-9b27-fb2ba4d7e826";
    private String[] strings = {BLS, ALS, Dead};
    private boolean isConnected = true;
    private int current = 0;

    public BlsProtocoles(){}
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getActivity() != null) {
            getActivity().setTitle(R.string.button_p);
            ConnectivityManager manager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (manager != null) {
                NetworkInfo info = manager.getActiveNetworkInfo();
                if (info == null || !info.isConnectedOrConnecting())
                    isConnected = false;
            }
        }
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle)  {
        return inflater.inflate(R.layout.activity_protocole_bls, container, false);
    }
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        webview = view.findViewById(R.id.webview);
        spinner = view.findViewById(R.id.progressBar2);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.setWebViewClient(new BlsProtocoles.CustomWebViewClient());
        Log.e("URLTOLOAD:", "current is:" + current + " url: " + strings[current]);
        webview.loadUrl("http://drive.google.com/viewerng/viewer?embedded=true&url=" + strings[current]);
    }
    @SuppressLint("SetJavaScriptEnabled")
    public void setCurrentPage(int type) {
        current = type;
        if (webview != null) {
            webview.setVisibility(View.GONE);
            spinner.setVisibility(View.VISIBLE);
            webview.loadUrl("http://drive.google.com/viewerng/viewer?embedded=true&url=" + strings[current]);
            Log.e("TRACK_URL", "Request New URL");
        }
    }
    private class CustomWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView webview, String url, Bitmap favicon) {
            Log.e("TRACK_URL", "started");
            if (spinner.getVisibility() == View.GONE) {
                webview.setVisibility(View.GONE);
                spinner.setVisibility(View.VISIBLE);
            }
        }
        @Override
        public void onPageFinished(WebView view, String url) {
            Log.e("TRACK_URL", "ended");
            spinner.setVisibility(View.GONE);
            view.setVisibility(View.VISIBLE);
            super.onPageFinished(view, url);
        }
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (isConnected)
                return false;
            else {
                String er = "file:///android_asset/err.html";
                if(Locale.getDefault().getLanguage().equals("en"))
                    er += "?lng=en";
                view.loadUrl(er);
                return true;
            }
        }
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String FailingUrl) {
            Log.e("TRACK_URL", "error");
            String er = "file:///android_asset/err.html";
            if(Locale.getDefault().getLanguage().equals("en"))
                er += "?lng=en";
            spinner.setVisibility(View.GONE);
            view.setVisibility(View.VISIBLE);
            view.loadUrl(er);
        }
    }
}
