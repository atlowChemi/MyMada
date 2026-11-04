package atlow.chemi.mymada.adapters

import android.graphics.Typeface
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import atlow.chemi.mymada.R
import atlow.chemi.mymada.classes.Volunteer
import atlow.chemi.mymada.integrations.TeamContract

class VolunteerAdapter(private val edit: (Int) -> Unit, private val delete: (Int) -> Unit) : RecyclerView.Adapter<VolunteerAdapter.Holder>() {
    var members: List<Volunteer> = emptyList()
    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val role: TextView = view.findViewById(R.id.count)
        val image: ImageView = view.findViewById(R.id.thumbnail)
        val menu: ImageView = view.findViewById(R.id.overflow)
        init { view.rotationY = 180f }
    }
    override fun getItemCount() = members.size
    override fun onCreateViewHolder(parent: ViewGroup, type: Int) = Holder(LayoutInflater.from(parent.context).inflate(R.layout.volunteer_layout, parent, false))
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val member = members[position]
        val context = holder.itemView.context
        val font = Typeface.createFromAsset(context.assets, "fonts/opensanshebrew.ttf")
        holder.title.text = member.name
        holder.role.text = TeamContract.roles[member.job]
        holder.title.typeface = font
        holder.role.typeface = font
        holder.image.setColorFilter(context.getColor(when (member.job) { 2, 3 -> R.color.para; 4 -> R.color.noar; else -> R.color.hovesh }))
        holder.menu.contentDescription = "${member.name}: ${context.getString(R.string.Nteam)}"
        holder.menu.setOnClickListener { anchor ->
            PopupMenu(context, anchor, Gravity.START).apply {
                menuInflater.inflate(R.menu.menu2, menu)
                setOnMenuItemClickListener {
                    val index = holder.bindingAdapterPosition
                    if (index !in members.indices) false else when (it.itemId) {
                        R.id.action_edit -> { edit(index); true }
                        R.id.action_delete -> { delete(index); true }
                        else -> false
                    }
                }
            }.show()
        }
    }
}
