package atlow.chemi.mymada.integrations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import atlow.chemi.mymada.R
import atlow.chemi.mymada.classes.Volunteer

@Composable
internal fun TeamScreen(members: List<Volunteer>, onEdit: (Int) -> Unit, onDelete: (Int) -> Unit, onPreview: (String) -> Unit) {
    var ambulance by rememberSaveable { mutableStateOf("") }
    var invalid by rememberSaveable { mutableStateOf(false) }
    val accent = colorResource(R.color.ColorNice)
    val font = FontFamily(Font(R.font.opensans_hebrew))
    val hint = stringResource(R.string.ambN)
    val error = stringResource(R.string.err_amb)
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(hint, color = if (invalid) Color.Red else accent, fontFamily = font, fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
        BasicTextField(ambulance, onValueChange = { if (it.length <= 4) { ambulance = it; invalid = false } },
            modifier = Modifier.fillMaxWidth().heightIn(min = 28.dp).testTag("team-ambulance")
                .semantics { contentDescription = hint; if (invalid) error(error) },
            singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            textStyle = TextStyle(fontFamily = font, fontSize = 18.sp), cursorBrush = SolidColor(accent))
        Spacer(Modifier.fillMaxWidth().height(2.dp).background(if (invalid) Color.Red else accent))
        if (invalid) Text(error, color = Color.Red, fontSize = 12.sp)
        if (members.isEmpty()) Text(stringResource(R.string.explain), fontFamily = font, fontSize = 16.sp,
            color = colorResource(R.color.DarkGrey), modifier = Modifier.testTag("team-empty"))
        // Published grid begins on the right in both app locales; each card's Hebrew role stays right-aligned.
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)) {
                members.indices.chunked(2).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    row.forEach { index ->
                    val member = members[index]
                    var menu by remember(member) { mutableStateOf(false) }
                    Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(6.dp), color = Color.White) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(painterResource(R.drawable.ic_person), null,
                                tint = colorResource(when (member.job) { 2, 3 -> R.color.para; 4 -> R.color.noar; else -> R.color.hovesh }),
                                modifier = Modifier.size(100.dp))
                            Box(Modifier.fillMaxWidth()) {
                                Column(Modifier.fillMaxWidth().padding(start = 10.dp, end = 28.dp, top = 8.dp, bottom = 6.dp)) {
                                    Text(member.name, fontFamily = font, fontSize = 14.sp, color = colorResource(R.color.album_title))
                                    Text(TeamContract.roles[member.job], fontFamily = font, fontSize = 12.sp, color = Color.Black)
                                }
                                Box(Modifier.align(Alignment.CenterEnd)) {
                                    IconButton(onClick = { menu = true }, modifier = Modifier.size(48.dp).testTag("team-menu-$index")) {
                                        Icon(painterResource(R.drawable.ic_more), contentDescription = "${member.name}: ${stringResource(R.string.Nteam)}", tint = Color.Black)
                                    }
                                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                                        DropdownMenuItem(text = { Text(stringResource(R.string.edit)) }, onClick = { menu = false; onEdit(index) }, modifier = Modifier.testTag("team-edit-$index"))
                                        DropdownMenuItem(text = { Text(stringResource(R.string.delete)) }, onClick = { menu = false; onDelete(index) }, modifier = Modifier.testTag("team-delete-$index"))
                                    }
                                }
                            }
                        }
                    }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 20.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            if (members.size < TeamContract.LIMIT) Surface(onClick = { onEdit(-1) }, color = Color.White,
                shape = RoundedCornerShape(15.dp), shadowElevation = 4.dp,
                modifier = Modifier.weight(.6f).heightIn(min = 60.dp).testTag("team-add")) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(8.dp)) {
                    Text(stringResource(R.string.Nteam), color = accent, fontFamily = font, fontSize = 16.sp, textAlign = TextAlign.Center)
                }
            } else Spacer(Modifier.weight(.6f))
            Surface(onClick = { invalid = ambulance.isBlank(); if (!invalid) onPreview(ambulance) }, color = accent,
                shape = RoundedCornerShape(15.dp), shadowElevation = 4.dp,
                modifier = Modifier.weight(.4f).heightIn(min = 60.dp).testTag("team-send")) {
                Row(Modifier.padding(horizontal = 12.dp, vertical = 14.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.succ), color = Color.White, fontFamily = font, fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Icon(painterResource(R.drawable.ic_send), null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}
