package atlow.chemi.mymada.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.List;

import atlow.chemi.mymada.MainReactActivity;
import atlow.chemi.mymada.classes.HomeB;
import atlow.chemi.mymada.R;

public class homeBAdapter extends RecyclerView.Adapter<homeBAdapter.MyViewHolder> {
    private final Activity mContext;
    private final List<HomeB> albumList;

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private final Button homeB;

        private MyViewHolder(View view) {
            super(view);
            homeB = view.findViewById(R.id.buttonChange);
        }
    }


    public homeBAdapter(Activity mContext, List<HomeB> albumList) {
        this.mContext = mContext;
        this.albumList = albumList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        var itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.button_change, parent, false);
        return new MyViewHolder(itemView);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        final var album = albumList.get(holder.getAdapterPosition());

        var display = mContext.getWindowManager().getDefaultDisplay();
        var width = display.getWidth()/2;
        var heightTol = (int) ((width*2) * 0.409);
        holder.homeB.setLayoutParams(new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, heightTol));
        holder.homeB.setBackgroundResource(album.getBg());
        holder.homeB.setOnClickListener(view -> {
            var intent = new Intent(mContext, MainReactActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        });
    }
    @Override
    public int getItemCount() {
        return albumList.size();
    }
}