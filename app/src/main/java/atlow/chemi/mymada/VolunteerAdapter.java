package atlow.chemi.mymada;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

class VolunteerAdapter extends RecyclerView.Adapter<VolunteerAdapter.MyViewHolder> {

    private Context mContext;
    private List<Volunteer> albumList;

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView title, count;
        private ImageView thumbnail, overflow;
        private CardView cardView;

        private MyViewHolder(View view) {
            super(view);
            view.setRotationY(180);
            title = view.findViewById(R.id.title);
            count = view.findViewById(R.id.count);
            thumbnail = view.findViewById(R.id.thumbnail);
            overflow = view.findViewById(R.id.overflow);
            cardView = view.findViewById(R.id.vol_card_view);
        }
    }


    VolunteerAdapter(Context mContext, List<Volunteer> albumList) {
        this.mContext = mContext;
        this.albumList = albumList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.volunteer_layout, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        Volunteer album = albumList.get(holder.getAdapterPosition());
        String jobs[] = {"נהג","חובש","פראמדיק","רופא","מתנדב","משתלם","שירות לאומי"};
        int colors[] = {R.color.hovesh,R.color.hovesh,R.color.para,R.color.para,R.color.noar,R.color.hovesh,R.color.hovesh};
        String job = jobs[album.getJob()];
        holder.title.setText(album.getName());
        holder.count.setText(job);
        Typeface myTypeface = Typeface.createFromAsset(this.mContext.getAssets(), "fonts/opensanshebrew.ttf");
        holder.title.setTypeface(myTypeface);
        holder.count.setTypeface(myTypeface);
        holder.thumbnail.setColorFilter(mContext.getResources().getColor(colors[album.getJob()]));

        holder.overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(holder.overflow, holder.getAdapterPosition());//position);
            }
        });
    }

    /**
     * Showing popup menu when tapping on 3 dots
     */
    private void showPopupMenu(View view, int position) {
        // inflate menu
        PopupMenu popup = new PopupMenu(mContext, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu2, popup.getMenu());
        popup.setGravity(Gravity.START);
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener(position, mContext));
        popup.show();
    }

    /**
     * Click listener for popup menu items
     */
    private class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
        Context mContext;
        int position;
        private MyMenuItemClickListener(int position, Context context) {
            this.position = position;
            this.mContext = context;
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_edit:
                    MainActivity2.editVol(position, this.mContext);
                    return true;
                case R.id.action_delete:
                    MainActivity2.deleteVol(this.position);
                    return true;
                default:
            }
            return false;
        }
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }
}