package atlow.chemi.mymada;

import android.content.Context;
import android.graphics.Typeface;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

class ContractionAdapter extends RecyclerView.Adapter<ContractionAdapter.MyViewHolder> {
    private List<Contraction> albumList;
    private Context context;

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView title, count, number;

        private MyViewHolder(View view) {
            super(view);
            view.setRotationY(180);
            title = (TextView) view.findViewById(R.id.title);
            count = (TextView) view.findViewById(R.id.count);
            number = (TextView) view.findViewById(R.id.number);
        }
    }


    ContractionAdapter(List<Contraction> albumList, Context context) {
        this.albumList = albumList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.contraction_layout, parent, false);
            return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
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