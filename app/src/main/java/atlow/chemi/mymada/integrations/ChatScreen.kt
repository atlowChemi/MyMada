package atlow.chemi.mymada.integrations

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import atlow.chemi.mymada.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(state: ChatModel.State, name: String, changeName: () -> Unit, send: (String) -> Unit) {
    var draft by rememberSaveable { mutableStateOf("") }
    var sent by rememberSaveable { mutableLongStateOf(state.sent) }
    val list = rememberLazyListState()
    var count by rememberSaveable { mutableIntStateOf(0) }
    val context = LocalContext.current
    val font = FontFamily(Font(R.font.opensans_hebrew))
    LaunchedEffect(state.sent) { if (sent != state.sent) { draft = ""; sent = state.sent } }
    LaunchedEffect(state.messages.size) {
        if (state.messages.size > count) list.animateScrollToItem(state.messages.size)
        count = state.messages.size
    }
    Column(Modifier.fillMaxSize().padding(8.dp)) {
        LazyColumn(state = list, modifier = Modifier.weight(1f).testTag("chat-messages"), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            item(key = "change-name") {
                Surface(Modifier.fillMaxWidth().clickable(onClick = changeName), color = colorResource(R.color.White)) {
                    Text(stringResource(R.string.changeName), fontFamily = font, modifier = Modifier.padding(10.dp))
                }
            }
            items(state.messages, key = { it.key }) { message ->
                val action = if (message.whatsApp) Modifier.clickable {
                    val uri = Uri.parse("http://api.whatsapp.com/send").buildUpon()
                        .appendQueryParameter("phone", "+972557249771")
                        .appendQueryParameter("text", "שלום, אני $name ואני פונה לגבי השיחה שלנו באפליקציה").build()
                    try { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                    catch (_: ActivityNotFoundException) { Toast.makeText(context, "וואטסאפ לא מותקן כנראה!", Toast.LENGTH_SHORT).show() }
                } else Modifier
                Surface(Modifier.fillMaxWidth().then(action), color = colorResource(if (message.sender == "admin" || message.whatsApp) R.color.chatAdmin else R.color.White)) {
                    Column(Modifier.padding(10.dp)) {
                        if (!message.whatsApp) Text(if (message.sender == "admin") stringResource(R.string.admin) else name.ifBlank { context.getString(R.string.user) }, fontFamily = font)
                        Text(if (message.whatsApp) "לחץ על כפתור הוואטסאפ בכדי לתקשר איתנו בוואטסאפ." else message.text, fontFamily = font)
                        if (message.whatsApp) Icon(painterResource(R.drawable.ic_whatsapp), contentDescription = "WhatsApp", modifier = Modifier.size(48.dp), tint = androidx.compose.ui.graphics.Color.Unspecified)
                        Text(message.time?.let { SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(it)) }.orEmpty(), fontFamily = font, fontSize = 12.sp)
                    }
                }
            }
        }
        Text(state.seen?.let { stringResource(if (it) R.string.msgSeen else R.string.msgNotSeen) }.orEmpty(),
            fontFamily = font, color = colorResource(R.color.DarkGrey), modifier = Modifier.testTag("chat-seen"))
        Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            TextField(draft, { draft = it }, enabled = !state.busy, singleLine = true,
                label = { Text(stringResource(R.string.mailBT), fontFamily = font) },
                modifier = Modifier.weight(1f).testTag("chat-draft"))
            IconButton(onClick = { send(draft) }, enabled = state.ready && !state.busy, modifier = Modifier.size(60.dp).testTag("chat-send")) {
                Icon(painterResource(R.drawable.ic_send), contentDescription = stringResource(R.string.send), tint = colorResource(R.color.ColorNice))
            }
        }
    }
}
