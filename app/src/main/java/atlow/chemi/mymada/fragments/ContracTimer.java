package atlow.chemi.mymada.fragments;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import atlow.chemi.mymada.classes.CircularProgressButton;
import atlow.chemi.mymada.classes.Contraction;
import atlow.chemi.mymada.adapters.ContractionAdapter;
import atlow.chemi.mymada.R;

public class ContracTimer extends Fragment {
    @SuppressLint("StaticFieldLeak")
    private static ContractionAdapter adapter;
    private static ArrayList<Contraction> albumList;
    private static int num = 0;
    private static Date start, end;
    private static boolean running = false;
    private CircularProgressButton contractBtn;
    private RecyclerView recyclerView;
    private SharedPreferences sp;
    public ContracTimer()    {}
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getActivity() != null)
            getActivity().setTitle(R.string.button_f);
        sp = getActivity().getSharedPreferences("Settings", 0);
        if (albumList == null || albumList.isEmpty()) {
            albumList = new ArrayList<>();
            adapter = new ContractionAdapter(albumList, getContext());
            long no = Calendar.getInstance().getTime().getTime();
            long ol = sp.getLong("contractLong", 0);
            boolean leesT20M = (no - ol) <= (20 * 60 * 1000);
            if (!sp.getString("contractList", "").isEmpty() && leesT20M) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.AlertDialogStyle);
                dialog.setMessage(getActivity().getResources().getString(R.string.ContracRecover)).setCancelable(false).setPositiveButton(getActivity().getResources().getString(R.string.ok), (dialogInterface, i) -> {
                    /*****/
                    num = sp.getInt("contarctNum", 0);
                    end = new Date(sp.getLong("contractLong", 0));
                    Gson gson = new Gson();
                    String json = sp.getString("contractList", "");
                    if (!json.isEmpty()) {
                        Type type = new TypeToken<List<Contraction>>() {
                        }.getType();
                        albumList = gson.fromJson(json, type);
                    }
                    /******/
                    adapter = new ContractionAdapter(albumList, getActivity());
                    if (recyclerView != null) {
                        recyclerView.setAdapter(adapter);
                    }
                }).setNegativeButton(getActivity().getResources().getString(R.string.dialog_no), (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    SharedPreferences.Editor spe;
                    if (sp != null) {
                        spe = sp.edit();
                        spe.putString("contractList", "");
                        spe.putInt("contarctNum", 0);
                        spe.putLong("contractLong", 0);
                        spe.apply();
                    }
                    num = 0;
                    end = null;
                    adapter = new ContractionAdapter(albumList, getActivity());
                    if (recyclerView != null) {
                        recyclerView.setAdapter(adapter);
                    }
                });
                final AlertDialog alert = dialog.create();
                alert.setOnShowListener(dialogInterface -> {
                    Button canc = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                    try {
                        Typeface myTypeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/opensanshebrew.ttf");
                        TextView textView = alert.findViewById(android.R.id.message);
                        if (textView != null)
                            textView.setTypeface(myTypeface);
                        canc.setTypeface(myTypeface);
                        canc.setTextColor(getActivity().getResources().getColor(R.color.OldPrimary));
                    } catch (NullPointerException e) {
                        Crashlytics.log("Contractions remake defect");
                        Crashlytics.logException(e);
                    }
                });
                alert.show();
            } else {
                try {
                    SharedPreferences.Editor spe;
                    if (sp != null) {
                        spe = sp.edit();
                        spe.putString("contractList", "");
                        spe.putInt("contarctNum", 0);
                        spe.putLong("contractLong", 0);
                        spe.apply();
                    }
                    num = 0;
                    end = null;
                    adapter = new ContractionAdapter(albumList, getActivity());
                    if (recyclerView != null) {
                        recyclerView.setAdapter(adapter);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    SharedPreferences.Editor spe;
                    if (sp != null) {
                        spe = sp.edit();
                        spe.putString("contractList", "");
                        spe.putInt("contarctNum", 0);
                        spe.putLong("contractLong", 0);
                        spe.apply();
                    }
                    num = 0;
                    end = null;
                    adapter = new ContractionAdapter(albumList, getActivity());
                    if (recyclerView != null) {
                        recyclerView.setAdapter(adapter);
                    }
                }
            }
        }
        else if (!albumList.isEmpty() && adapter == null){
            adapter = new ContractionAdapter(albumList, getActivity());
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle)  {
        return inflater.inflate(R.layout.activity_contract_calc, container, false);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setRotationY(180);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(1, dpToPx(4), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        contractBtn = view.findViewById(R.id.contra_btn);
        Typeface myTypeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/opensanshebrew.ttf");
        contractBtn.setTypeface(myTypeface);
        if (running) {
            if (start == null)
                start = Calendar.getInstance().getTime();
            contractBtn.setText(getActivity().getResources().getString(R.string.endContract));
            contractBtn.startAnimation();
            contractBtn.setBackgroundColor(Color.argb(255,218,218,218));
        }
        contractBtn.setOnClickListener(view1 -> {
            running = !running;
            if (running) {
                start = Calendar.getInstance().getTime();
                contractBtn.setText(getActivity().getResources().getString(R.string.endContract));
                contractBtn.startAnimation();
                contractBtn.setBackgroundColor(Color.argb(255,218,218,218));
            }
            else   {
                Date oldEnd = null;
                if (end!=null)
                    oldEnd = end;
                end = Calendar.getInstance().getTime();
                contractBtn.setText(getActivity().getResources().getString(R.string.startContract));
                try {
                    contractBtn.stopAnimation();
                } catch (NullPointerException e) {
                    Crashlytics.log("error stopping circular btn");
                    Crashlytics.logException(e);
                }
                contractBtn.revertAnimation();
                contractBtn.setBackgroundColor(Color.rgb(255,255,255));
                Contraction b = new Contraction(++num, start, end, oldEnd);
                albumList.add(b);
                adapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(albumList.size());
            }
        });
    }
    /**
     * RecyclerView item decoration - give equal margin around grid item
     */
    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing/9; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing/9; // item top
                }
            }
        }
    }
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }
    @Override
    public void onDestroy() {
        if (albumList.size() > 0) {
            Gson gson = new Gson();
            String json = gson.toJson(albumList);
            try {
                SharedPreferences.Editor se = sp.edit();
                se.putString("contractList", json);
                se.putInt("contarctNum", num);
                se.putLong("contractLong", end.getTime());
                se.apply();
            }catch (NullPointerException e) {
                Crashlytics.log(e.getMessage());
            }
        }
        super.onDestroy();
    }
}