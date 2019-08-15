package atlow.chemi.mymada.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import java.util.Locale;

import atlow.chemi.mymada.R;
import atlow.chemi.mymada.SettingsDesigned;

public class BlsProtocoles extends Fragment {
    private ProgressBar spinner;
    private WebView webview;
    private String ShowOrHideWebViewInitialUse = "show", ALS, BLS, Dead;
    private boolean isConnected = true;
    private int current = 0;
    public BlsProtocoles()    {

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        BLS = "https://firebasestorage.googleapis.com/v0/b/project-3918384647339420127.appspot.com/o/bls%5B1%5D.pdf?alt=media&token=7da40564-fe83-4254-a929-f8b2ec67ca41";
        ALS = "https://firebasestorage.googleapis.com/v0/b/project-3918384647339420127.appspot.com/o/als%5B1%5D.pdf?alt=media&token=49d9e5f1-03d5-49d5-a951-b58dce094ca6";
        Dead = "https://firebasestorage.googleapis.com/v0/b/project-3918384647339420127.appspot.com/o/kuntres.pdf?alt=media&token=2b7c5ce5-e5e5-4fae-9b27-fb2ba4d7e826";
        ConnectivityManager manager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager != null) {
            NetworkInfo info = manager.getActiveNetworkInfo();
            if (info == null || !info.isConnectedOrConnecting())
                isConnected = false;
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle)  {
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
        webview.loadUrl("http://drive.google.com/viewerng/viewer?embedded=true&url=" + BLS);
        FloatingActionButton fab = view.findViewById(R.id.typeProtocole);
        fab.setOnClickListener((vi) -> {
            CharSequence[] protos = new CharSequence[]{getActivity().getResources().getString(R.string.protocolesBls), getActivity().getResources().getString(R.string.protocolesAls), getActivity().getResources().getString(R.string.protocolesDeath)};
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogStyle);
            builder.setTitle(getActivity().getResources().getString(R.string.protocoles)).setItems(protos, (dialogInterface, i) -> {
                if (i != current) {
                    ShowOrHideWebViewInitialUse = "show";
                    webview.stopLoading();
                    webview.loadUrl("http://drive.google.com/viewerng/viewer?embedded=true&url=" + (i == 0 ? BLS : i == 1 ? ALS : Dead));
                }
            });
            builder.show();
        });
    }
    private class CustomWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView webview, String url, Bitmap favicon) {
            if (ShowOrHideWebViewInitialUse.equals("show")) {
                webview.setVisibility(View.GONE);
                spinner.setVisibility(View.VISIBLE);
            }
        }
        @Override
        public void onPageFinished(WebView view, String url) {
            ShowOrHideWebViewInitialUse = "hide";
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
            String er = "file:///android_asset/err.html";
            if(Locale.getDefault().getLanguage().equals("en"))
                er += "?lng=en";
            view.loadUrl(er);
        }
    }
}
