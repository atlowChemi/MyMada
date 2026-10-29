package atlow.chemi.mymada.home;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;

import androidx.annotation.NonNull;
import androidx.core.app.PendingIntentCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.MobileAds;

import java.util.List;
import java.lang.ref.WeakReference;

import atlow.chemi.mymada.InfoActivityKt;
import atlow.chemi.mymada.recieversAndServices.GrobootRec;
import atlow.chemi.mymada.MokedPage;
import atlow.chemi.mymada.R;
import atlow.chemi.mymada.UtillitiesAndTools;
import atlow.chemi.mymada.codeRequest;
import atlow.chemi.mymada.contactChatActivity;
import atlow.chemi.mymada.lockPage;

/** Temporary native navigation/ad bridge; detection and backend modernization follow in #24/#26. */
public class HomeActions {

    private Activity mContext;
    private Typeface myTypeface;
    private boolean isMADA, realM;
    private InterstitialAd openUp;

    public HomeActions(Activity mContext) {
        this.mContext = mContext;
        myTypeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/opensanshebrew.ttf");
        MobileAds.initialize(mContext.getApplicationContext(), status -> {});
        loadAd();
    }

    public void open(String in) {
        final SharedPreferences sharedPreferences = mContext.getSharedPreferences("Settings", 0);
            Intent intent;
            switch (in) {
                case "mok":
                    realM = sharedPreferences.getBoolean("realM", false);
                    isMADA = false;
                    if (mContext.getApplication().getPackageName().contains("debug")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogStyle);
                        builder.setMessage("זוהי גרסה לבדיקה. עמוד חסימה או עמוד דיווחים?").setCancelable(true)
                                .setPositiveButton("עמוד חסימה", (dialog, id) -> {
                                    Intent intent16 = new Intent(mContext, lockPage.class);
                                    intent16.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    SharedPreferences.Editor sp1 = sharedPreferences.edit();
                                    sp1.putBoolean("realM", false);
                                    sp1.apply();
                                    mContext.startActivity(intent16);
                                })
                                .setNegativeButton("עמוד דיווחים", (dialog, id) -> {
                                    Intent intent15 = new Intent(mContext, MokedPage.class);
                                    intent15.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    SharedPreferences.Editor sp1 = sharedPreferences.edit();
                                    sp1.putBoolean("realM", true);
                                    sp1.apply();
                                    mContext.startActivity(intent15);
                                });
                        final AlertDialog alert = builder.create();
                        alert.setOnShowListener(dialogInterface -> {
                            TextView textView = alert.findViewById(android.R.id.message);
                            if (textView != null)
                                textView.setTypeface(myTypeface);
                            textView = alert.findViewById(android.R.id.title);
                            if (textView != null)
                                textView.setTypeface(myTypeface);
                            Button canc = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                            Button ok = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                            ok.setTypeface(myTypeface);
                            canc.setTypeface(myTypeface);
                        });
                        alert.show();
                    } else {
                        if (!realM) {
                            isMADA = isPI(mContext);
                        }
                        if (realM || isMADA) {
                            intent = new Intent(mContext, MokedPage.class);
                            SharedPreferences.Editor sp1 = sharedPreferences.edit();
                            sp1.putBoolean("realM", true);
                            sp1.apply();
                        } else
                            intent = new Intent(mContext, lockPage.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                    }
                    break;
                case "tol":
                    intent = new Intent(mContext, UtillitiesAndTools.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                    break;
                case "tes":
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogStyle);
                    builder.setMessage(mContext.getResources().getString(R.string.TestCallMsg)).setCancelable(true)
                            .setPositiveButton(mContext.getResources().getString(R.string.TestCallSMS), (dialog, id) -> {
                                if ((sharedPreferences.getBoolean("not", true) || sharedPreferences.getBoolean("win", true)) && sharedPreferences.getBoolean("Mapp", false)) {
                                    if (sharedPreferences.getBoolean("cut", false) && !sharedPreferences.getBoolean("callT3", true)) {
                                        Toast.makeText(mContext, mContext.getResources().getString(R.string.MISHM_reco_off), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Intent launchIntent = mContext.getPackageManager().getLaunchIntentForPackage("com.groboot.mdaemergency");
                                        PendingIntent testPI;
                                        if (launchIntent != null) {
                                            launchIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                            testPI = PendingIntentCompat.getActivity(mContext, (int) System.currentTimeMillis(), launchIntent, 0, false);
                                        } else {
                                            testPI = null;
                                        }
                                        Intent intent1 = new Intent("atlow.chemi.MyMada.MADA_APP");
                                        intent1.setAction("atlow.chemi.MyMada.MADA_APP");
                                        intent1.putExtra("callT", 3);
                                        intent1.putExtra("text", mContext.getResources().getString(R.string.TEAMTest));
                                        GrobootRec.madasApp(testPI);
                                        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(mContext);
                                        manager.sendBroadcast(intent1);
                                    }
                                } else {
                                    Toast.makeText(mContext, mContext.getResources().getString(R.string.APP_reco_off), Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton(mContext.getResources().getString(R.string.TestCallApp), (dialog, id) -> {
                                if ((sharedPreferences.getBoolean("not", true) || sharedPreferences.getBoolean("win", true)) && sharedPreferences.getBoolean("Mapp", false)) {
                                    if (sharedPreferences.getBoolean("cut", false) && !sharedPreferences.getBoolean("callT1", true)) {
                                        Toast.makeText(mContext, mContext.getResources().getString(R.string.CONAN_reco_off), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Intent launchIntent = mContext.getPackageManager().getLaunchIntentForPackage("com.groboot.mdaemergency");
                                        PendingIntent testPI;
                                        if (launchIntent != null) {
                                            launchIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                            testPI = PendingIntentCompat.getActivity(mContext, (int) System.currentTimeMillis(), launchIntent, 0, false);
                                        } else {
                                            testPI = null;
                                        }
                                        Intent intent12 = new Intent("atlow.chemi.MyMada.MADA_APP");
                                        intent12.setAction("atlow.chemi.MyMada.MADA_APP");
                                        intent12.putExtra("callT", 1);
                                        intent12.putExtra("text", mContext.getResources().getString(R.string.CONANTest));
                                        GrobootRec.madasApp(testPI);
                                        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(mContext);
                                        manager.sendBroadcast(intent12);
                                    }
                                } else {
                                    Toast.makeText(mContext, mContext.getResources().getString(R.string.APP_reco_off), Toast.LENGTH_SHORT).show();
                                }
                            });
                    final AlertDialog alert = builder.create();
                    alert.setOnShowListener(dialogInterface -> {
                        TextView textView = alert.findViewById(android.R.id.message);
                        if (textView != null)
                            textView.setTypeface(myTypeface);
                        textView = alert.findViewById(android.R.id.title);
                        if (textView != null)
                            textView.setTypeface(myTypeface);
                        Button canc = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                        Button ok = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                        ok.setTypeface(myTypeface);
                        canc.setTypeface(myTypeface);
                        canc.setTextColor(mContext.getResources().getColor(R.color.OldPrimary));
                    });
                    alert.show();
                    break;
                case "con":
                    realM = sharedPreferences.getBoolean("realM", false);
                    isMADA = false;
                    if (!realM) {
                        isMADA = isPI(mContext);
                    }
                    if (openUp != null) {
                        InterstitialAd ad = openUp;
                        openUp = null;
                        ad.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull AdError error) {
                                onAdDismissedFullScreenContent();
                            }
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                loadAd();
                                if (realM || isMADA) {
                                    Intent intent = new Intent(mContext, contactChatActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    mContext.startActivity(intent);
                                } else {
                                    AlertDialog.Builder builder2 = new AlertDialog.Builder(mContext, R.style.AlertDialogStyle);
                                    builder2.setTitle(mContext.getResources().getString(R.string.plsNote)).setMessage(mContext.getResources().getString(R.string.plsNoteCon)).setCancelable(true)
                                            .setNegativeButton(mContext.getResources().getString(R.string.plsNoteConCode), (dialogInterface, i) -> {
                                                Intent intent13 = new Intent(mContext, codeRequest.class);
                                                intent13.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                mContext.startActivity(intent13);
                                            }).setPositiveButton(mContext.getResources().getString(R.string.title_activity_contact), (dialogInterface, i) -> {
                                        Intent intent14 = new Intent(mContext, contactChatActivity.class);
                                        intent14.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        mContext.startActivity(intent14);
                                    });

                                    final AlertDialog alert = builder2.create();
                                    alert.setOnShowListener(dialogInterface -> {
                                        TextView textView = alert.findViewById(android.R.id.message);
                                        if (textView != null)
                                            textView.setTypeface(myTypeface);
                                        textView = alert.findViewById(android.R.id.title);
                                        if (textView != null)
                                            textView.setTypeface(myTypeface);
                                        Button canc = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                                        Button ok = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                                        ok.setTypeface(myTypeface);
                                        canc.setTypeface(myTypeface);
                                        canc.setTextColor(mContext.getResources().getColor(R.color.OldPrimary));
                                    });
                                    alert.show();
                                }
                            }
                        });
                        ad.show(mContext);
                    } else {
                        if (realM || isMADA) {
                            intent = new Intent(mContext, contactChatActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(intent);
                        } else {
                            AlertDialog.Builder builder2 = new AlertDialog.Builder(mContext, R.style.AlertDialogStyle);
                            builder2.setTitle(mContext.getResources().getString(R.string.plsNote)).setMessage(mContext.getResources().getString(R.string.plsNoteCon)).setCancelable(true)
                                    .setNegativeButton(mContext.getResources().getString(R.string.plsNoteConCode), (dialogInterface, i) -> {
                                        Intent intent13 = new Intent(mContext, codeRequest.class);
                                        intent13.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        mContext.startActivity(intent13);
                                    }).setPositiveButton(mContext.getResources().getString(R.string.title_activity_contact), (dialogInterface, i) -> {
                                Intent intent14 = new Intent(mContext, contactChatActivity.class);
                                intent14.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                mContext.startActivity(intent14);
                            });

                            final AlertDialog alert2 = builder2.create();
                            alert2.setOnShowListener(dialogInterface -> {
                                TextView textView = alert2.findViewById(android.R.id.message);
                                if (textView != null)
                                    textView.setTypeface(myTypeface);
                                textView = alert2.findViewById(android.R.id.title);
                                if (textView != null)
                                    textView.setTypeface(myTypeface);
                                Button canc = alert2.getButton(DialogInterface.BUTTON_NEGATIVE);
                                Button ok = alert2.getButton(DialogInterface.BUTTON_POSITIVE);
                                ok.setTypeface(myTypeface);
                                canc.setTypeface(myTypeface);
                                canc.setTextColor(mContext.getResources().getColor(R.color.OldPrimary));
                            });
                            alert2.show();
                        }
                    }
                    break;
                case "inf":
                    if (openUp != null) {
                        InterstitialAd ad = openUp;
                        openUp = null;
                        ad.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull AdError error) {
                                onAdDismissedFullScreenContent();
                            }
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                loadAd();
                                Intent intent = new Intent(mContext, InfoActivityKt.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                mContext.startActivity(intent);
                            }
                        });
                        ad.show(mContext);
                    }
                    else {
                        intent = new Intent(mContext, InfoActivityKt.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                    }
                    break;
            }
    }
    private void loadAd() {
        loadAd(this);
    }
    // Static scope prevents the pending callback from implicitly capturing this bridge/Activity.
    private static void loadAd(HomeActions owner) {
        WeakReference<HomeActions> reference = new WeakReference<>(owner);
        InterstitialAd.load(owner.mContext.getApplicationContext(), "ca-app-pub-7600269766434003/4483563620",
                new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd ad) {
                        HomeActions actions = reference.get();
                        if (actions != null && !actions.mContext.isDestroyed()) actions.openUp = ad;
                    }
                });
    }
    private static boolean isPI(Context context) {
        final PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage("com.groboot.mdaemergency");
        if (intent == null) {
            return false;
        }
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }
}
