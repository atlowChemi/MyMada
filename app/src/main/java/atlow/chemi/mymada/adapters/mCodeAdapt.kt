package atlow.chemi.mymada.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.TextView
import atlow.chemi.mymada.R
import atlow.chemi.mymada.classes.mCodeKid

class mCodeAdapt(context: Context, codes: List<mCodeKid>) : BaseAdapter() {
    private val all = codes.toList()
    private var shown = all
    private val inflater = LayoutInflater.from(context)
    private val font = Typeface.createFromAsset(context.assets, "fonts/opensanshebrew.ttf")
    override fun getCount() = shown.size
    override fun getItem(position: Int) = shown[position]
    override fun getItemId(position: Int) = position.toLong()
    override fun isEnabled(position: Int) = getItem(position).id != "sep"
    override fun areAllItemsEnabled() = false
    fun getId(position: Int) = getItem(position).id
    fun getChecked() = all.filter { it.isChecked && it.id != "sep" }.joinToString(", ") { it.id }

    fun filter(query: String) {
        shown = if (query.isEmpty()) all else all.filter {
            it.name.contains(query, ignoreCase = true) || it.id.contains(query, ignoreCase = true) ||
                (query.contains("לבבי") && it.category == "vasc")
        }
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val row = convertView ?: inflater.inflate(R.layout.m_code_row, parent, false)
        val item = getItem(position)
        val checkbox = row.findViewById<CheckBox>(R.id.mcName)
        val header = row.findViewById<TextView>(R.id.mcSep)
        val color = Color.parseColor(when (item.category) {
            "area", "pain" -> "#4bacc6"
            "trauma" -> "#8064a2"
            "vasc" -> "#c0504d"
            "resp" -> "#4f81bd"
            "neuro" -> "#a1a30e"
            "gastro" -> "#d8863a"
            "infect" -> "#829ca3"
            "pshyc" -> "#00b050"
            "ando" -> "#92d050"
            "pish" -> "#ba8280"
            else -> "#6b7177"
        })
        row.findViewById<TextView>(R.id.mcId).text = item.id
        header.text = item.name
        header.setBackgroundColor(color)
        header.visibility = if (item.id == "sep") View.VISIBLE else View.GONE
        checkbox.visibility = if (item.id == "sep") View.GONE else View.VISIBLE
        checkbox.typeface = font
        checkbox.text = item.name
        checkbox.buttonTintList = ColorStateList.valueOf(color)
        checkbox.setOnCheckedChangeListener(null)
        checkbox.isChecked = item.isChecked
        checkbox.setOnCheckedChangeListener { _, checked -> item.isChecked = checked }
        return row
    }
}
