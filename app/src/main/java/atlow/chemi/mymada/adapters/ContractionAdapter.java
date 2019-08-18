package atlow.chemi.mymada.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

import atlow.chemi.mymada.classes.Contraction;
import atlow.chemi.mymada.R;

public class ContractionAdapter extends RecyclerView.Adapter<ContractionAdapter.MyViewHolder> {
    private List<Contraction> albumList;
    private Context context;

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView title, count, number;

        private MyViewHolder(View view) {
            super(view);
            view.setRotationY(180);
            title = view.findViewById(R.id.title);
            count = view.findViewById(R.id.count);
            number = view.findViewById(R.id.number);
        }
    }


    public ContractionAdapter(List<Contraction> albumList, Context context) {
        this.albumList = albumList;
        this.context = context;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.contraction_layout, parent, false);
            return new MyViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        Contraction album = albumList.get(holder.getAdapterPosition());
        Date oldEnd = album.getOldEnd();
        Date start = album.getStart();
        Date end = album.getEnd();
        String ContractionLength = printDifference(start, end);
        holder.title.setText(this.context.getResources().getString(R.string.ContracLength, ContractionLength));
        String TimeBetweenContractions = "";
        try {
            TimeBetweenContractions = this.context.getResources().getString(R.string.ContracBetween, printDifference(oldEnd, start));
        }catch (Exception e)    {
            e.printStackTrace();
        }
        holder.count.setText(TimeBetweenContractions);
        holder.number.setText("" + album.getNumber());
        Typeface myTypeface = Typeface.createFromAsset(this.context.getAssets(), "fonts/opensanshebrew.ttf");
        holder.count.setTypeface(myTypeface);
        holder.number.setTypeface(myTypeface);
        holder.title.setTypeface(myTypeface);
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }

    private String printDifference(Date startDate, Date endDate){
        //milliseconds
        long different = endDate.getTime() - startDate.getTime();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;
        return elapsedMinutes > 0 ? (this.context.getResources().getString(R.string.ContracMinutes, elapsedMinutes, elapsedSeconds)) : (this.context.getResources().getString(R.string.ContracSecondes, elapsedSeconds));
    }
}