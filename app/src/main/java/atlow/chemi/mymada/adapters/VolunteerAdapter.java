package atlow.chemi.mymada.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import atlow.chemi.mymada.R;
import atlow.chemi.mymada.classes.Volunteer;
import atlow.chemi.mymada.helpers.Event;

public class VolunteerAdapter extends RecyclerView.Adapter<VolunteerAdapter.MyViewHolder> {

    private Context mContext;
    private List<Volunteer> albumList;
    @FunctionalInterface
    public interface ChangeVolunteer
    {
        /**
         * @see  Event
         * @param volunteerPosition the position of the volunteer in the adapters list.
         */
        void invoke(int volunteerPosition);
    }
    public Event<ChangeVolunteer> DeleteVolunteerEvent = new Event<>();
    public Event<ChangeVolunteer> EditVolunteerEvent = new Event<>();

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView title, count;
        private ImageView thumbnail, overflow;

        private MyViewHolder(View view) {
            super(view);
            view.setRotationY(180);
            title = view.findViewById(R.id.title);
            count = view.findViewById(R.id.count);
            thumbnail = view.findViewById(R.id.thumbnail);
            overflow = view.findViewById(R.id.overflow);
        }
    }


    public VolunteerAdapter(Context mContext, List<Volunteer> albumList) {
        this.mContext = mContext;
        this.albumList = albumList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.volunteer_layout, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        Volunteer album = albumList.get(holder.getAdapterPosition());
        String[] jobs = {"נהג", "חובש", "פראמדיק", "רופא", "מתנדב", "משתלם", "שירות לאומי"};
        int[] colors = {R.color.hovesh,R.color.hovesh,R.color.para,R.color.para,R.color.noar,R.color.hovesh,R.color.hovesh};
        String job = jobs[album.getJob()];
        holder.title.setText(album.getName());
        holder.count.setText(job);
        Typeface myTypeface = Typeface.createFromAsset(this.mContext.getAssets(), "fonts/opensanshebrew.ttf");
        holder.title.setTypeface(myTypeface);
        holder.count.setTypeface(myTypeface);
        holder.thumbnail.setColorFilter(mContext.getResources().getColor(colors[album.getJob()]));

        holder.overflow.setOnClickListener(view -> {
            showPopupMenu(holder.overflow, holder.getAdapterPosition());//position);
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
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener(position));
        popup.show();
    }

    /**
     * Click listener for popup menu items
     */
    private class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
        int position;
        private MyMenuItemClickListener(int position) {
            this.position = position;
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_edit:
                    for (ChangeVolunteer listener : EditVolunteerEvent.listeners())
                        listener.invoke(position);
                    return true;
                case R.id.action_delete:
                    for (ChangeVolunteer listener : DeleteVolunteerEvent.listeners())
                        listener.invoke(position);
                    return true;
                default:
                    return false;
            }
        }
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }
}