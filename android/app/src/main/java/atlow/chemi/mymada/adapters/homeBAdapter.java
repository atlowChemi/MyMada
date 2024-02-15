package atlow.chemi.mymada.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;

import androidx.annotation.NonNull;
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

// import com.google.android.gms.ads.AdRequest;
// import com.google.android.gms.ads.InterstitialAd;
// import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.List;

import atlow.chemi.mymada.MainReactActivity;
import atlow.chemi.mymada.classes.HomeB;
import atlow.chemi.mymada.R;

public class homeBAdapter extends RecyclerView.Adapter<homeBAdapter.MyViewHolder> {

    private Activity mContext;
    private Typeface myTypeface;
    private boolean isMADA, realM, isMove1 = false;
    private List<HomeB> albumList;
    private ArrayList<Button> btns;
    private float oldX1, oldY1;
    // private InterstitialAd openUp;

    class MyViewHolder extends RecyclerView.ViewHolder {
        private Button homeB;

        private MyViewHolder(View view) {
            super(view);
            homeB = view.findViewById(R.id.buttonChange);
        }
    }


    public homeBAdapter(Activity mContext, List<HomeB> albumList) {
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
        // MobileAds.initialize(mContext, "ca-app-pub-7600269766434003~4255496174");
        // openUp = new InterstitialAd(mContext);
        // openUp.setAdUnitId("ca-app-pub-7600269766434003/4483563620");
        // openUp.loadAd(new AdRequest.Builder().build());

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
            switch (in) {
                case "mok":
                case "tes":
                case "con":
                case "inf":
                case "tol": {
                    var intent = new Intent(mContext, MainReactActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }
            }
        });
    }
    @Override
    public int getItemCount() {
        return albumList.size();
    }
    public void setBtnsAlpha() {
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