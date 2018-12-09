package atlow.chemi.mymada;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ContracTimer extends Fragment {
    private static ContractionAdapter adapter;
    private static ArrayList<Contraction> albumList;
    private static int num = 0;
    private static Date start, end;
    private static boolean running = false;
    private Button contractionButton;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView recyclerView;
    SharedPreferences sp;
    public ContracTimer()    {

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = getActivity().getSharedPreferences("Settings", 0);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle)  {
        View v= inflater.inflate(R.layout.activity_contract_calc, container, false);
        if (SettingsActivity.isRTL())
            v.setRotationY(180);
        return v;
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        albumList = new ArrayList<>();
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setRotationY(180);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(1, dpToPx(4), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new ContractionAdapter(albumList, getActivity());
        recyclerView.setAdapter(adapter);
        long no = Calendar.getInstance().getTime().getTime();
        long ol = sp.getLong("contractLong", 0);
        boolean leesT20M = (no - ol) <= (20*60*1000);
        if (!sp.getString("contractList", "").isEmpty() && leesT20M) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.AlertDialogStyle);
            dialog.setMessage("שחזר נתונים קודמים?").setCancelable(false).setPositiveButton("אישור", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
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
                    recyclerView.setAdapter(adapter);
                }
            }).setNegativeButton("לא", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    SharedPreferences.Editor spe;
                    if (sp!=null) {
                        spe = sp.edit();
                        spe.putString("contractList", "");
                        spe.putInt("contarctNum", 0);
                        spe.putLong("contractLong", 0);
                        spe.apply();
                    }
                    num = 0;
                    end = null;
                    adapter = new ContractionAdapter(albumList, getActivity());
                    recyclerView.setAdapter(adapter);
                }
            });
            final AlertDialog alert = dialog.create();
            alert.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    Button canc = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                    Typeface myTypeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/opensanshebrew.ttf");
                    canc.setTypeface(myTypeface);
                    canc.setTextColor(getActivity().getResources().getColor(R.color.ColorPrimary));
                }
            });
            alert.show();
        }else   {
            try {
                SharedPreferences.Editor spe;
                if (sp!= null) {
                    spe = sp.edit();
                    spe.putString("contractList", "");
                    spe.putInt("contarctNum", 0);
                    spe.putLong("contractLong", 0);
                    spe.apply();
                }
                num = 0;
                end = null;
                adapter = new ContractionAdapter(albumList, getActivity());
                recyclerView.setAdapter(adapter);
            }catch (Exception e)    {
                e.printStackTrace();
                SharedPreferences.Editor spe;
                if (sp!= null)  {
                    spe = sp.edit();
                    spe.putString("contractList", "");
                    spe.putInt("contarctNum", 0);
                    spe.putLong("contractLong", 0);
                    spe.apply();
                }
                num = 0;
                end = null;
                adapter = new ContractionAdapter(albumList, getActivity());
                recyclerView.setAdapter(adapter);
            }
        }
        contractionButton = (Button) view.findViewById(R.id.contra_btn);
        Typeface myTypeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/opensanshebrew.ttf");
        contractionButton.setTypeface(myTypeface);
        contractionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                running = !running;
                if (running) {
                    start = Calendar.getInstance().getTime();
                    contractionButton.setText(getActivity().getResources().getString(R.string.endContract));
                    contractionButton.setBackground(getActivity().getResources().getDrawable(R.drawable.round_button_stop));
                }else   {
                    Date oldEnd = null;
                    if (end!=null)
                        oldEnd = end;
                    end = Calendar.getInstance().getTime();
                    contractionButton.setText(getActivity().getResources().getString(R.string.startContract));
                    contractionButton.setBackground(getActivity().getResources().getDrawable(R.drawable.round_button));
                    //Contraction a = new Contraction(num++, printDifference(start, end), hefresh);
                    Contraction b = new Contraction(++num, start, end, oldEnd);
                    albumList.add(b);
                    adapter.notifyDataSetChanged();
                    recyclerView.smoothScrollToPosition(albumList.size());
                }
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
    public String printDifference(Date startDate, Date endDate){
        //milliseconds
        long different = endDate.getTime() - startDate.getTime();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;
        return elapsedMinutes + " דקות ו" + elapsedSeconds + " שניות";
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
