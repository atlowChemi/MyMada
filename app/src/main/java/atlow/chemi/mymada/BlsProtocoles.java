package atlow.chemi.mymada;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

public class BlsProtocoles extends Fragment {
    private ProgressDialog pDialog;
    private ProgressBar spinner;
    private WebView webview;
    private String ShowOrHideWebViewInitialUse = "show";
    public BlsProtocoles()    {

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pDialog = new ProgressDialog(getActivity());
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle)  {
        View v = inflater.inflate(R.layout.activity_protocole_bls, container, false);
        v.setRotationY(180);
        return v;
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pDialog.setTitle(getActivity().getString(R.string.app_name));
        pDialog.setMessage("טוען ספר פרוטוקולים...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        webview = (WebView) view.findViewById(R.id.webview);
        spinner = (ProgressBar)view.findViewById(R.id.progressBar2);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.setWebViewClient(new BlsProtocoles.CustomWebViewClient());

        final String BLS = "https://firebasestorage.googleapis.com/v0/b/project-3918384647339420127.appspot.com/o/bls%5B1%5D.pdf?alt=media&token=7da40564-fe83-4254-a929-f8b2ec67ca41";
        final String ALS = "https://firebasestorage.googleapis.com/v0/b/project-3918384647339420127.appspot.com/o/als%5B1%5D.pdf?alt=media&token=49d9e5f1-03d5-49d5-a951-b58dce094ca6";
        webview.loadUrl("http://drive.google.com/viewerng/viewer?embedded=true&url=https://firebasestorage.googleapis.com/v0/b/project-3918384647339420127.appspot.com/o/bls%5B1%5D.pdf?alt=media&token=7da40564-fe83-4254-a929-f8b2ec67ca41");
        FloatingActionButton fab = (FloatingActionButton)view.findViewById(R.id.typeProtocole);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence protos[] = new CharSequence[] {"פרוטוקלי BLS", "פרוטוקולי ALS"};
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogStyle);
                builder.setTitle("בחר פרוטוקולים לצפייה").setItems(protos, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        webview.loadUrl("http://drive.google.com/viewerng/viewer?embedded=true&url=" + (i == 0 ? BLS : ALS));
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
                webview.setVisibility(webview.INVISIBLE);
            }
        }
        @Override
        public void onPageFinished(WebView view, String url) {
            ShowOrHideWebViewInitialUse = "hide";
            spinner.setVisibility(View.GONE);
            view.setVisibility(webview.VISIBLE);
            super.onPageFinished(view, url);
        }
    }
}
