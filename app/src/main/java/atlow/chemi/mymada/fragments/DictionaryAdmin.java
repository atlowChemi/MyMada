package atlow.chemi.mymada.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import atlow.chemi.mymada.classes.DictionaryWord;
import atlow.chemi.mymada.adapters.DictionaryWordAdapter;
import atlow.chemi.mymada.R;

import static android.app.Activity.RESULT_OK;

public class DictionaryAdmin extends Fragment {
    private DictionaryWordAdapter adapter;
    private ArrayList<DictionaryWord> dictionary = new ArrayList<>();
    private FirebaseFirestore db;
    private SearchView searchView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);if (getActivity() != null)
            getActivity().setTitle(R.string.button_dict);
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_dictionary_admin, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new DictionaryWordAdapter(dictionary, getActivity());
        RecyclerView wordsList = view.findViewById(R.id.dictionary_word_list);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        wordsList.setLayoutManager(mLayoutManager);
        wordsList.setAdapter(adapter);

        //Data
        db = FirebaseFirestore.getInstance();
        Query words = db.collection("dictionary").whereEqualTo("approved", true);
        words.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                Log.e("words", "error getting words");
                return;
            }
            Set<DictionaryWord> set = new HashSet<>();
            if (queryDocumentSnapshots != null) {
                for (QueryDocumentSnapshot data : queryDocumentSnapshots) {
                    if (data.get("name") != null && data.get("approved") != null) {
                        String name = data.getString("name");
                        String definition = data.getString("definition");
                        String key = data.getId();
                        boolean approved = data.getBoolean("approved");
                        set.add(new DictionaryWord(name, definition, key, approved));
                    }
                }
            }
            dictionary.clear();
            dictionary.addAll(set);
            Collections.sort(dictionary, (dictionaryWord, t1) -> dictionaryWord.getName().compareToIgnoreCase(t1.getName()));
            adapter.notifyDataSetChanged();
            if (searchView == null || searchView.getQuery().toString().isEmpty())
                adapter.filter("");
            else
                adapter.filter(searchView.getQuery().toString());

        });
    }
    @SuppressLint("InflateParams")
    private void wordAdder()   {
        LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View newMan;
        EditText wordName, wordDefinition;
        TextInputLayout wordNameTil, wordDefinitionTil;
        if (inflater != null) {
            newMan = inflater.inflate(R.layout.new_word_dialog, null);
            wordName = newMan.findViewById(R.id.wordName);
            wordDefinition = newMan.findViewById(R.id.wordDefinition);
            wordNameTil = newMan.findViewById(R.id.nameTil);
            wordDefinitionTil = newMan.findViewById(R.id.definitionTil);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogStyle);
            builder.setTitle("הוספת מילה").setCancelable(true).setView(newMan)
                    .setNegativeButton("ביטול", (dialog, id) -> dialog.cancel())
                    .setPositiveButton("הוסף", null);
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
                    ok.setEnabled(false);
                    String name = wordName.getText().toString().trim(), definition = wordDefinition.getText().toString().trim();
                    wordDefinitionTil.setErrorEnabled(false);
                    wordNameTil.setErrorEnabled(false);
                    DocumentReference newWord = db.collection("dictionary").document();
                    DictionaryWord word = new DictionaryWord(name, definition, newWord.getId(), false);
                    newWord.set(word).addOnSuccessListener(documentReference -> {
                        dialog.dismiss();
                        View rootView = getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
                        Snackbar bar = Snackbar.make(rootView, R.string.successAddWord, Snackbar.LENGTH_LONG);
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
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu, menu);
        MenuItem searchItemView = menu.findItem(R.id.app_bar_search);
        searchView = (SearchView) searchItemView.getActionView();
        searchView.setQueryHint(getString(R.string.searchDictionary));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                adapter.filter("");
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty())
                    newText = "";
                adapter.filter(newText);
                return false;
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.app_bar_word_add) {
            wordAdder();
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 6 && resultCode == RESULT_OK) {
            String barcodeR = data.getStringExtra("barcodeR");
            Toast.makeText(getActivity(), barcodeR, Toast.LENGTH_LONG).show();
        }
    }
}
