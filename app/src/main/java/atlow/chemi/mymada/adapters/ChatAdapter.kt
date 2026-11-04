package atlow.chemi.mymada.adapters

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import atlow.chemi.mymada.R
import atlow.chemi.mymada.integrations.ChatContract
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter(private val name: () -> String, private val changeName: () -> Unit) : RecyclerView.Adapter<ChatAdapter.Holder>() {
    var messages: List<ChatContract.Message> = emptyList()
    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val sender: TextView = view.findViewById(R.id.msgSender)
        val text: TextView = view.findViewById(R.id.msgTxt)
        val time: TextView = view.findViewById(R.id.msgTime)
        val whatsApp: View = view.findViewById(R.id.waBtn)
        val card: CardView = view.findViewById(R.id.chat_card_view)
    }
    override fun getItemCount() = messages.size + 1
    // Separate name row prevents its special margins leaking into recycled messages.
    override fun getItemViewType(position: Int) = if (position == 0) 0 else 1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(LayoutInflater.from(parent.context).inflate(R.layout.chat_layout, parent, false))
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val context = holder.itemView.context
        val font = Typeface.createFromAsset(context.assets, "fonts/opensanshebrew.ttf")
        listOf(holder.sender, holder.text, holder.time).forEach { it.typeface = font; it.visibility = View.VISIBLE }
        holder.whatsApp.visibility = View.GONE
        holder.card.setOnClickListener(null)
        if (position == 0) {
            holder.sender.setText(R.string.changeName)
            val margin = (10 * context.resources.displayMetrics.density + .5f).toInt()
            (holder.sender.layoutParams as ViewGroup.MarginLayoutParams).setMargins(margin, margin, margin, margin)
            holder.text.visibility = View.GONE
            holder.time.visibility = View.GONE
            holder.card.setCardBackgroundColor(context.getColor(R.color.White))
            holder.card.setOnClickListener { changeName() }
            return
        }
        val message = messages[position - 1]
        holder.time.text = message.time?.let { SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(it)) }.orEmpty()
        holder.sender.text = if (message.sender == "admin") context.getString(R.string.admin) else name().ifBlank { context.getString(R.string.user) }
        holder.text.text = message.text
        holder.text.movementMethod = ScrollingMovementMethod()
        holder.card.setCardBackgroundColor(context.getColor(if (message.sender == "admin" || message.whatsApp) R.color.chatAdmin else R.color.White))
        if (message.whatsApp) {
            holder.sender.visibility = View.GONE
            holder.whatsApp.visibility = View.VISIBLE
            holder.text.text = "לחץ על כפתור הוואטסאפ בכדי לתקשר איתנו בוואטסאפ."
            holder.card.setOnClickListener {
                val uri = Uri.parse("http://api.whatsapp.com/send").buildUpon()
                    .appendQueryParameter("phone", "+972557249771")
                    .appendQueryParameter("text", "שלום, אני ${name()} ואני פונה לגבי השיחה שלנו באפליקציה").build()
                try { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                catch (_: ActivityNotFoundException) { Toast.makeText(context, "וואטסאפ לא מותקן כנראה!", Toast.LENGTH_SHORT).show() }
            }
        }
    }
}
