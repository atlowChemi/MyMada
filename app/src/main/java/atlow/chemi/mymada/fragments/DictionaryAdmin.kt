package atlow.chemi.mymada.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import atlow.chemi.mymada.R
import atlow.chemi.mymada.dictionary.DictionaryModel
import atlow.chemi.mymada.dictionary.DictionaryScreen

/** Preserve the Tools destination and published toolbar; Compose owns the content and dialog state. */
class DictionaryAdmin : Fragment() {
    private val model get() = ViewModelProvider(this)[DictionaryModel::class.java]
    private var query by mutableStateOf("")
    private var addRequest by mutableStateOf(0)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        query = savedInstanceState?.getString("dictionaryQuery").orEmpty()
        setHasOptionsMenu(true)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View =
        ComposeView(requireContext()).apply {
            id = R.id.dictionary_word_list
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { DictionaryScreen(model, query, addRequest) }
        }
    override fun onStart() {
        super.onStart()
        requireActivity().setTitle(R.string.button_dict)
        model.start()
    }
    override fun onStop() { model.stop(); super.onStop() }
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("dictionaryQuery", query)
        super.onSaveInstanceState(outState)
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        val item = menu.findItem(R.id.app_bar_search)
        val search = item.actionView as SearchView
        search.queryHint = getString(R.string.searchDictionary)
        if (query.isNotEmpty()) { item.expandActionView(); search.setQuery(query, false); search.clearFocus() }
        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(text: String): Boolean { search.clearFocus(); return true }
            override fun onQueryTextChange(text: String): Boolean { query = text; return true }
        })
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.app_bar_word_add) { addRequest++; return true }
        return super.onOptionsItemSelected(item)
    }
}
