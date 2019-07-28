package atlow.chemi.mymada;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

class DictionaryWordAdapter extends RecyclerView.Adapter<DictionaryWordAdapter.MyViewHolder> {
    private List<DictionaryWord> DictionaryWordList;
    private ArrayList<DictionaryWord> filteredList;
    private Context context;
    private Typeface myTypeface;

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView wordName, wordDefinition, wordKey, wordApproved;
        private ConstraintLayout listElement;
        private MyViewHolder(View view) {
            super(view);
            wordKey = view.findViewById(R.id.wordID);
            wordName = view.findViewById(R.id.wordName);
            wordDefinition = view.findViewById(R.id.wordDefinition);
            wordApproved = view.findViewById(R.id.wordApproved);
            listElement = view.findViewById(R.id.word_list_element);
        }
    }

    DictionaryWordAdapter(List<DictionaryWord> DictionaryWordList, Context context) {
        this.DictionaryWordList = DictionaryWordList;
        this.filteredList = new ArrayList<>();
        this.filteredList.addAll(DictionaryWordList);
        this.context = context;
        this.myTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/opensanshebrew.ttf");
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.word_list_element, parent, false);
            return new MyViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        DictionaryWord word = filteredList.get(position);
        String name = word.getName();
        String definition = word.getDefinition();
        String key = word.getKey();
        holder.wordName.setTextColor(Color.BLACK);
        holder.wordName.setTypeface(Typeface.DEFAULT_BOLD);
        holder.wordName.setText(name);
        holder.wordKey.setText(key);
        holder.wordDefinition.setText(definition);
        holder.listElement.setOnClickListener((view) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogStyle)
                    .setTitle(name).setMessage(definition)
                    .setNeutralButton("סגור", ((dialog, which) -> dialog.dismiss()));
            final AlertDialog alert = builder.create();
            alert.setOnShowListener(dialogInterface -> {
                TextView textView = alert.findViewById(android.R.id.message);
                if (textView != null)
                    textView.setTypeface(myTypeface);
                textView = alert.findViewById(android.R.id.title);
                if (textView != null)
                    textView.setTypeface(myTypeface);
            });
            alert.show();
        });
    }
    @Override
    public int getItemCount() {
        return filteredList.size();
    }
    void filter(String searchQ) {
        searchQ = searchQ.toLowerCase();
        filteredList.clear();
        if(searchQ.isEmpty())
            filteredList.addAll(DictionaryWordList);
        else {
            for (DictionaryWord word : DictionaryWordList) {
                String name = word.getName().toLowerCase();
                String definition = word.getDefinition().toLowerCase();
                if (name.contains(searchQ) || definition.contains(searchQ))
                    filteredList.add(word);
            }
        }
        notifyDataSetChanged();
    }
}