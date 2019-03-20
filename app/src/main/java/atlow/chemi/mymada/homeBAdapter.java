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
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
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

import java.util.ArrayList;
import java.util.List;

class homeBAdapter extends RecyclerView.Adapter<homeBAdapter.MyViewHolder> {

    private Activity mContext;
    private Typeface myTypeface;
    private boolean isMADA, realM, isMove1 = false;
    private List<HomeB> albumList;
    private ArrayList<Button> btns = new ArrayList<>();
    private float oldX1, oldY1;
    private FragmentActivity fragmentActivity;

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

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.button_change, parent, false);
        return new MyViewHolder(itemView);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final HomeB album = albumList.get(holder.getAdapterPosition());
        final SharedPreferences sharedPreferences = mContext.getSharedPreferences("Settings", 0);
        myTypeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/opensanshebrew.ttf");
        try {
            btns.add(holder.homeB);
        }catch (NullPointerException e) {
            Log.e("not Added btn", e.getMessage());
        }
        Display display = mContext.getWindowManager().getDefaultDisplay();
        int width = display.getWidth()/2;
        int heightAll = (int) (width * 0.8);
        int heightTol = (int) ((width*2) * 0.409);
        holder.homeB.setLayoutParams(new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, heightAll));
        if (album.getIntent().equals("tol"))
            holder.homeB.setLayoutParams(new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, heightTol));
        holder.homeB.setBackgroundResource(album.getBg());
        holder.homeB.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
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
            }
        });
        holder.homeB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String in = album.getIntent();
                Intent intent;
                switch (in) {
                    case "mok":
                        realM = sharedPreferences.getBoolean("realM", false);
                        isMADA = false;
                        if (mContext.getApplication().getPackageName().contains("debug")) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogStyle);
                            builder.setMessage("זוהי גרסה לבדיקה. עמוד חסימה או עמוד דיווחים?").setCancelable(true)
                                    .setPositiveButton("עמוד חסימה", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            Intent intent = new Intent(mContext, lockPage.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            SharedPreferences.Editor sp1 = sharedPreferences.edit();
                                            sp1.putBoolean("realM", false);
                                            sp1.apply();
                                            mContext.startActivity(intent);
                                        }
                                    })
                                    .setNegativeButton("עמוד דיווחים", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            Intent intent = new Intent(mContext, MokedPage.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            SharedPreferences.Editor sp1 = sharedPreferences.edit();
                                            sp1.putBoolean("realM", true);
                                            sp1.apply();
                                            mContext.startActivity(intent);
                                        }
                                    });
                            final AlertDialog alert = builder.create();
                            TextView textView = alert.findViewById(android.R.id.message);
                            if (textView != null)
                                textView.setTypeface(myTypeface);
                            textView = alert.findViewById(android.R.id.title);
                            if (textView != null)
                                textView.setTypeface(myTypeface);
                            alert.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialogInterface) {
                                    Button canc = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                                    Button ok = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                                    ok.setTypeface(myTypeface);
                                    canc.setTypeface(myTypeface);
                                }
                            });
                            alert.show();
                        } else {
                            if (!realM) {
                                isMADA = isPI(mContext, "com.groboot.mdaemergency");
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
                                .setPositiveButton(mContext.getResources().getString(R.string.TestCallSMS), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
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
                                                Intent intent = new Intent("atlow.chemi.MyMada.MADA_APP");
                                                intent.setAction("atlow.chemi.MyMada.MADA_APP");
                                                intent.putExtra("callT", 3);
                                                intent.putExtra("text", mContext.getResources().getString(R.string.TEAMTest));
                                                GrobootRec.madasApp(testPI);
                                                mContext.sendBroadcast(intent);
                                            }
                                        } else {
                                            Toast.makeText(mContext, mContext.getResources().getString(R.string.APP_reco_off), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                })
                                .setNegativeButton(mContext.getResources().getString(R.string.TestCallApp), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
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
                                                Intent intent = new Intent("atlow.chemi.MyMada.MADA_APP");
                                                intent.setAction("atlow.chemi.MyMada.MADA_APP");
                                                intent.putExtra("callT", 1);
                                                intent.putExtra("text", mContext.getResources().getString(R.string.CONANTest));
                                                GrobootRec.madasApp(testPI);
                                                mContext.sendBroadcast(intent);
                                            }
                                        } else {
                                            Toast.makeText(mContext, mContext.getResources().getString(R.string.APP_reco_off), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                        final AlertDialog alert = builder.create();
                        TextView textView = (TextView) alert.findViewById(android.R.id.message);
                        if (textView != null)
                            textView.setTypeface(myTypeface);
                        textView = (TextView) alert.findViewById(android.R.id.title);
                        if (textView != null)
                            textView.setTypeface(myTypeface);
                        alert.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface dialogInterface) {
                                Button canc = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                                Button ok = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                                ok.setTypeface(myTypeface);
                                canc.setTypeface(myTypeface);
                                canc.setTextColor(mContext.getResources().getColor(R.color.OldPrimary));
                            }
                        });
                        alert.show();
                        break;
                    case "con":
                        realM = sharedPreferences.getBoolean("realM", false);
                        isMADA = false;
                        if (!realM) {
                            isMADA = isPI(mContext, "com.groboot.mdaemergency");
                        }
                        if (realM || isMADA) {
                            intent = new Intent(mContext, contactActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(intent);
                        } else   {
                            AlertDialog.Builder builder2 = new AlertDialog.Builder(mContext, R.style.AlertDialogStyle);
                            builder2.setTitle(mContext.getResources().getString(R.string.plsNote)).setMessage(mContext.getResources().getString(R.string.plsNoteCon)).setCancelable(true)
                                    .setNegativeButton(mContext.getResources().getString(R.string.plsNoteConCode), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Intent intent = new Intent(mContext, codeRequest.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            mContext.startActivity(intent);
                                        }
                                    }).setPositiveButton(mContext.getResources().getString(R.string.title_activity_contact), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(mContext, contactActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    mContext.startActivity(intent);
                                }
                            });
                            builder2.show();
                        }
                        break;
                    case "inf":
                        intent = new Intent(mContext, InfoActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                        break;
                }
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

    private static boolean isPI(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(packageName);
        if (intent == null) {
            return false;
        }
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }
}