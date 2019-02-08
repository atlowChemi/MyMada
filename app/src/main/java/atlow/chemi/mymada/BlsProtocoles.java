package atlow.chemi.mymada;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.Locale;

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
        View v = inflater.inflate(R.layout.activity_protocole_bls, container, false);
        if (SettingsActivity.isRTL())
            v.setRotationY(180);
        return v;
    }
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        webview = view.findViewById(R.id.webview);
        spinner = view.findViewById(R.id.progressBar2);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.setWebViewClient(new BlsProtocoles.CustomWebViewClient());
        webview.loadUrl("http://drive.google.com/viewerng/viewer?embedded=true&url=" + BLS);
        FloatingActionButton fab = view.findViewById(R.id.typeProtocole);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence protos[] = new CharSequence[] {getActivity().getResources().getString(R.string.protocolesBls), getActivity().getResources().getString(R.string.protocolesAls), getActivity().getResources().getString(R.string.protocolesDeath)};
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogStyle);
                builder.setTitle(getActivity().getResources().getString(R.string.protocoles)).setItems(protos, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i != current) {
                            ShowOrHideWebViewInitialUse = "show";
                            webview.loadUrl("http://drive.google.com/viewerng/viewer?embedded=true&url=" + (i == 0 ? BLS : i == 1 ? ALS : Dead));
                        }
                    }
                });
                builder.show();
            }
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
