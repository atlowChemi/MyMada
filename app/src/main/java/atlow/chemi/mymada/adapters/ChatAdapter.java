package atlow.chemi.mymada.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import atlow.chemi.mymada.classes.Chat;
import atlow.chemi.mymada.R;
import atlow.chemi.mymada.contactChatActivity;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {
    private List<Chat> albumList;
    private Context context;

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView msgSender, msgTxt, msgTime;
        private AppCompatImageView waBtn;
        private CardView msgHold;
        private MyViewHolder(View view) {
            super(view);
            msgSender = view.findViewById(R.id.msgSender);
            msgTxt = view.findViewById(R.id.msgTxt);
            msgTime = view.findViewById(R.id.msgTime);
            msgHold = view.findViewById(R.id.chat_card_view);
            waBtn = view.findViewById(R.id.waBtn);
        }
    }

    public ChatAdapter(List<Chat> albumList, Context context) {
        this.albumList = albumList;
        this.context = context;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_layout, parent, false);
            return new MyViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        Chat msg = albumList.get(position);
        String sender = msg.getSender();
        String msgTxt = msg.getMsg();
        String msgTime = msg.getTime();
        if (sender.equals("NAME_CHANGE") && msgTxt.equals("NAME_CHANGE") && msgTime.equals("NAME_CHANGE")) {
            holder.msgSender.setText(context.getString(R.string.changeName));
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.msgSender.getLayoutParams();
            int properDp = dpToPx(10);
            params.setMargins(properDp,properDp,properDp,properDp);
            holder.msgSender.requestLayout();
            holder.msgTxt.setVisibility(View.GONE);
            holder.msgTime.setVisibility(View.GONE);
            holder.msgHold.setCardBackgroundColor(ContextCompat.getColor(context, R.color.White));
            holder.msgHold.setOnClickListener(v -> contactChatActivity.askForName(context, true));
        }
        else {
            if (msg.isWhatsApp()) {
                msgTime = formatDate(msg.getTime());
                holder.msgTime.setText(msgTime);
                holder.msgSender.setVisibility(View.GONE);
                holder.waBtn.setVisibility(View.VISIBLE);
                holder.msgTxt.setText("לחץ על כפתור הוואטסאפ בכדי לתקשר איתנו בוואטסאפ.");
                holder.msgHold.setCardBackgroundColor(ContextCompat.getColor(context, R.color.chatAdmin));
                holder.msgHold.setOnClickListener((view) -> {
                    try {
                        String toNumber = "+972557249771";
                        String name = context.getSharedPreferences("Settings", 0).getString("UserName", context.getString(R.string.user));
                        String message = "שלום, אני " + name + " ואני פונה לגבי השיחה שלנו באפליקציה";
                        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                        sendIntent.setData(Uri.parse("http://api.whatsapp.com/send?phone="+toNumber +"&text="+message));
                        sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(sendIntent);
                    } catch (Exception e) {
                        Log.e("WHATSAPP ERR", e.getMessage());
                        Toast.makeText(context, "וואטסאפ לא מותקן כנראה!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else {
                sender = sender.equals("admin") ? this.context.getString(R.string.admin) : context.getSharedPreferences("Settings", 0).getString("UserName", context.getString(R.string.user));
                msgTime = formatDate(msg.getTime());
                holder.msgSender.setText(sender);
                holder.msgTxt.setText(msgTxt);
                holder.msgTime.setText(msgTime);
                holder.msgTxt.setVisibility(View.VISIBLE);
                holder.msgTime.setVisibility(View.VISIBLE);
                holder.msgSender.setVisibility(View.VISIBLE);
                holder.waBtn.setVisibility(View.GONE);
                holder.msgTxt.setMovementMethod(new ScrollingMovementMethod());
                Typeface myTypeface = Typeface.createFromAsset(this.context.getAssets(), "fonts/opensanshebrew.ttf");
                holder.msgTxt.setTypeface(myTypeface);
                holder.msgSender.setTypeface(myTypeface);
                holder.msgTime.setTypeface(myTypeface);
                holder.msgHold.setOnClickListener(null);
                if (msg.getSender().equals("admin"))
                    holder.msgHold.setCardBackgroundColor(ContextCompat.getColor(context, R.color.chatAdmin));
                else
                    holder.msgHold.setCardBackgroundColor(ContextCompat.getColor(context, R.color.White));
            }
        }
    }
    private int dpToPx(int dp) {
        Resources r = context.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }
    @Override
    public int getItemCount() {
        return albumList.size();
    }
    private String formatDate(String milli) {
        if (milli == null || milli.length() < 1)
            return "";
        long milliseconds = Long.parseLong(milli.replaceAll("[\\D]", ""));
        DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy' 'HH:mm:ss", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        TimeZone tz = TimeZone.getDefault();
        sdf.setTimeZone(tz);
        return sdf.format(calendar.getTime());
    }
}