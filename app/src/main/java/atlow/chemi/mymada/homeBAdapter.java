package atlow.chemi.mymada;

import android.annotation.SuppressLint;
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
import androidx.appcompat.app.AlertDialog;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.List;

class homeBAdapter extends RecyclerView.Adapter<homeBAdapter.MyViewHolder> {

    private Activity mContext;
    private Typeface myTypeface;
    private boolean isMADA, realM, isMove1 = false;
    private List<HomeB> albumList;
    private ArrayList<Button> btns;
    private float oldX1, oldY1;
    private InterstitialAd openUp;

    class MyViewHolder extends RecyclerView.ViewHolder {
        private Button homeB;

        private MyViewHolder(View view) {
            super(view);
            homeB = view.findViewById(R.id.buttonChange);
        }
    }


    homeBAdapter(Activity mContext, List<HomeB> albumList) {
        this.mContext = mContext;
        this.albumList = albumList;
        btns = new ArrayList<>();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.button_change, parent, false);
        return new MyViewHolder(itemView);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        final HomeB album = albumList.get(holder.getAdapterPosition());
        final SharedPreferences sharedPreferences = mContext.getSharedPreferences("Settings", 0);
        myTypeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/opensanshebrew.ttf");
        try {
            btns.add(holder.homeB);
        }catch (NullPointerException e) {
            Log.e("not Added btn", e.getMessage());
        }
        MobileAds.initialize(mContext, "ca-app-pub-7600269766434003~4255496174");
        openUp = new InterstitialAd(mContext);
        openUp.setAdUnitId("ca-app-pub-7600269766434003/4483563620");
        openUp.loadAd(new AdRequest.Builder().build());

        Display display = mContext.getWindowManager().getDefaultDisplay();
        int width = display.getWidth()/2;
        int heightAll = (int) (width * 0.8);
        int heightTol = (int) ((width*2) * 0.409);
        holder.homeB.setLayoutParams(new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, heightAll));
        if (album.getIntent().equals("tol"))
            holder.homeB.setLayoutParams(new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, heightTol));
        holder.homeB.setBackgroundResource(album.getBg());
        holder.homeB.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    boolean x = oldX1 - event.getX() > 50 || event.getX() - oldX1 > 50;
                    boolean y = oldY1 - event.getY() > 50 || event.getY() - oldY1 > 50;
                    if (x || y) {
                        isMove1 = true;
                        holder.homeB.setAlpha(1f);
                    }
                    return true;
                case MotionEvent.ACTION_DOWN:
                    oldY1 = event.getY();
                    oldX1 = event.getX();
                    isMove1 = false;
                    holder.homeB.setAlpha(0.4f);
                    return true;
                case MotionEvent.ACTION_UP:
                    holder.homeB.setAlpha(1f);
                    if (!isMove1)
                        holder.homeB.performClick();
                    return true;
            }
            return false;
        });
        holder.homeB.setOnClickListener(view -> {
            String in = album.getIntent();
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
                    intent = new Intent(mContext, UtillTools.class);
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
                                            testPI = PendingIntent.getActivity(mContext, (int) System.currentTimeMillis(), launchIntent, 0);
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
                                            testPI = PendingIntent.getActivity(mContext, (int) System.currentTimeMillis(), launchIntent, 0);
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
                    if (openUp != null && openUp.isLoaded()) {
                        openUp.setAdListener(new AdListener() {
                            @Override
                            public void onAdLeftApplication() {
                                //TODO: close ad
                            }
                            @Override
                            public void onAdClosed() {
                                openUp.loadAd(new AdRequest.Builder().build());
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
                        openUp.show();
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
                    if (openUp != null && openUp.isLoaded()) {
                        openUp.setAdListener(new AdListener() {
                            @Override
                            public void onAdClosed() {
                                openUp.loadAd(new AdRequest.Builder().build());
                                Intent intent = new Intent(mContext, InfoActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                mContext.startActivity(intent);
                            }
                        });
                        openUp.show();
                    }
                    else {
                        intent = new Intent(mContext, InfoActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                    }
                    break;
            }
        });
    }
    @Override
    public int getItemCount() {
        return albumList.size();
    }
    void setBtnsAlpha() {
        for(Button b : btns)
            b.setAlpha(1f);
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