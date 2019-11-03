package atlow.chemi.mymada.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.crashlytics.android.Crashlytics;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import atlow.chemi.mymada.classes.DictionaryWord;
import atlow.chemi.mymada.R;

public class DictionaryWordAdapter extends RecyclerView.Adapter<DictionaryWordAdapter.MyViewHolder> {
    private List<DictionaryWord> DictionaryWordList;
    private ArrayList<DictionaryWord> filteredList;
    private Context context;
    private Typeface myTypeface;

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView wordName, wordDefinition, wordKey;
        private ConstraintLayout listElement;
        private MyViewHolder(View view) {
            super(view);
            wordKey = view.findViewById(R.id.wordID);
            wordName = view.findViewById(R.id.wordName);
            wordDefinition = view.findViewById(R.id.wordDefinition);
            listElement = view.findViewById(R.id.word_list_element);
        }
    }

    public DictionaryWordAdapter(List<DictionaryWord> DictionaryWordList, Context context) {
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
                    .setPositiveButton(R.string.editDictionary, (dialogInterface, i) -> wordAdder(key, name, definition))
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
    public void filter(String searchQ) {
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
    private void wordAdder(String key, String name, String definition)   {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View newMan;
        EditText wordName, wordDefinition;
        TextInputLayout wordNameTil, wordDefinitionTil;
        if (inflater != null) {
            newMan = inflater.inflate(R.layout.new_word_dialog, null);
            wordName = newMan.findViewById(R.id.wordName);
            wordDefinition = newMan.findViewById(R.id.wordDefinition);
            wordNameTil = newMan.findViewById(R.id.nameTil);
            wordDefinitionTil = newMan.findViewById(R.id.definitionTil);
            wordName.setText(name);
            wordDefinition.setText(definition);
            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogStyle);
            builder.setTitle("עריכת מילה").setCancelable(true).setView(newMan)
                    .setNegativeButton("ביטול", (dialog, id) -> dialog.cancel())
                    .setPositiveButton("שמור", null);
            final AlertDialog alert = builder.create();
            alert.setOnShowListener((dialog -> {
                Button ok = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                ok.setOnClickListener((v -> {
                    if (wordName.getText().toString().trim().isEmpty() || wordDefinition.getText().toString().trim().isEmpty()) {
                        if (wordName.getText().toString().trim().isEmpty())
                            wordNameTil.setError("לא כתבת מונח");
                        else
                            wordNameTil.setErrorEnabled(false);
                        if (wordDefinition.getText().toString().trim().isEmpty())
                            wordDefinitionTil.setError("לא כתבת הגדרה למונח!");
                        else
                            wordDefinitionTil.setErrorEnabled(false);
                        return;
                    }
                    if (wordName.getText().toString().trim().equals(name) && wordDefinition.getText().toString().trim().equals(definition)) {
                        wordNameTil.setError("לא שינית את שם המונח");
                        wordDefinitionTil.setError("לא שינית את ההגדרה למונח!");
                        return;
                    }
                    ok.setEnabled(false);
                    String newName = wordName.getText().toString().trim(), newDefinition = wordDefinition.getText().toString().trim();
                    wordDefinitionTil.setErrorEnabled(false);
                    wordNameTil.setErrorEnabled(false);
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    DocumentReference newWord = db.collection("dictionary").document(key);
                    Map<String, Object> editedWord = new HashMap<>();
                    if (!name.equals(newName))
                        editedWord.put("newName", newName);
                    if (!definition.equals(newDefinition))
                        editedWord.put("newDefinition", newDefinition);
                    newWord.update(editedWord).addOnSuccessListener(documentReference -> {
                        dialog.dismiss();
                        View rootView = ((Activity)context).getWindow().getDecorView().findViewById(android.R.id.content);
                        Snackbar bar = Snackbar.make(rootView, R.string.successEditWord, Snackbar.LENGTH_LONG);
                        View viewSnk = bar.getView();
                        TextView tv = viewSnk.findViewById(com.google.android.material.R.id.snackbar_text);
                        tv.setTextColor(Color.WHITE);
                        bar.show();
                    }).addOnFailureListener(e -> {
                        Crashlytics.logException(e);
                        ok.setEnabled(true);
                    });
                }));
            }));
            alert.show();
        }
    }
}