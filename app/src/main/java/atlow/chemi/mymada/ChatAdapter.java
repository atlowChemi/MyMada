package atlow.chemi.mymada;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.support.design.widget.FixedTextInputEditText;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {
    private List<Chat> albumList;
    private Context context;

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView msgSender, msgTxt, msgTime;
        private CardView msgHold;
        private MyViewHolder(View view) {
            super(view);
            msgSender = view.findViewById(R.id.msgSender);
            msgTxt = view.findViewById(R.id.msgTxt);
            msgTime = view.findViewById(R.id.msgTime);
            msgHold = view.findViewById(R.id.chat_card_view);
        }
    }

    ChatAdapter(List<Chat> albumList, Context context) {
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
            holder.msgHold.setOnClickListener(v -> {contactChatActivity.askForName(context, true);});
        }
        else {
            sender = sender.equals("admin") ? this.context.getString(R.string.admin) : context.getSharedPreferences("Settings", 0).getString("UserName", context.getString(R.string.user));
            msgTime = formatDate(msg.getTime());
            holder.msgSender.setText(sender);
            holder.msgTxt.setText(msgTxt);
            holder.msgTime.setText(msgTime);
            holder.msgTxt.setVisibility(View.VISIBLE);
            holder.msgTime.setVisibility(View.VISIBLE);
            holder.msgTxt.setMovementMethod(new ScrollingMovementMethod());
            Typeface myTypeface = Typeface.createFromAsset(this.context.getAssets(), "fonts/opensanshebrew.ttf");
            holder.msgTxt.setTypeface(myTypeface);
            holder.msgSender.setTypeface(myTypeface);
            holder.msgTime.setTypeface(myTypeface);
            if (msg.getSender().equals("admin"))
                holder.msgHold.setCardBackgroundColor(ContextCompat.getColor(context, R.color.chatAdmin));
            else
                holder.msgHold.setCardBackgroundColor(ContextCompat.getColor(context, R.color.White));
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