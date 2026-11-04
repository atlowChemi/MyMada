package atlow.chemi.mymada.integrations

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import atlow.chemi.mymada.classes.Volunteer

class TeamModel(state: SavedStateHandle) : ViewModel() {
    val entries = state.getLiveData<ArrayList<Bundle>>("members", arrayListOf())
    val members get() = entries.value.orEmpty().map { Volunteer(it.getString("name").orEmpty(), it.getInt("job")) }
    fun save(index: Int, name: String, job: Int): Boolean {
        if (name.isBlank() || job !in TeamContract.roles.indices) return false
        val next = ArrayList(entries.value.orEmpty())
        val entry = Bundle().apply { putString("name", name); putInt("job", job) }
        if (index == -1) {
            if (next.size >= TeamContract.LIMIT) return false
            next.add(entry)
        } else {
            if (index !in next.indices) return false
            next[index] = entry
        }
        entries.value = next
        return true
    }
    fun delete(index: Int) {
        val next = ArrayList(entries.value.orEmpty())
        if (index !in next.indices) return
        next.removeAt(index)
        entries.value = next
    }
}
