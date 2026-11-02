package atlow.chemi.mymada.integrations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import atlow.chemi.mymada.R
import atlow.chemi.mymada.classes.mCodeKid

/** Catalog order is retained; selection and search belong to this restorable dialog. */
class MedicalPickerDialog : DialogFragment() {
    private var selected by mutableStateOf(emptySet<String>())
    private var query by mutableStateOf("")
    private lateinit var codes: List<mCodeKid>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AlertDialogStyle)
        val args = requireArguments()
        val names = requireNotNull(args.getCharSequenceArray("names"))
        val ids = requireNotNull(args.getCharSequenceArray("ids"))
        val categories = requireNotNull(args.getStringArray("categories"))
        require(names.size == ids.size && ids.size == categories.size)
        selected = (savedInstanceState?.getString("selected") ?: args.getString("selected")).orEmpty().split(", ").toSet()
        query = savedInstanceState?.getString("query").orEmpty()
        codes = names.indices.map { index -> mCodeKid(names[index].toString(), ids[index].toString(), categories[index], index) }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            id = R.id.userListView
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                IntegrationTheme {
                    val keyboard = LocalSoftwareKeyboardController.current
                    Surface {
                        Column(Modifier.fillMaxWidth().heightIn(max = (LocalConfiguration.current.screenHeightDp - 48).dp)) {
                            Text("בחר קודים רפואיים", style = TextStyle(fontSize = 20.sp, lineHeight = 22.sp),
                                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 18.dp, bottom = 16.dp))
                            IntegrationField(query, { query = it }, label = stringResource(R.string.search),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { keyboard?.hide() }),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp).testTag("medical-query"))
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                LazyColumn(Modifier.weight(1f).testTag("medical-catalog")) {
                                    items(codes.filter { query.isEmpty() || it.name.contains(query, true) || it.id.contains(query, true) || (query.contains("לבבי") && it.category == "vasc") }, key = { it.order }) { code ->
                                        val color = Color(android.graphics.Color.parseColor(when (code.category) {
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
                                        }))
                                        if (code.id == "sep") Surface(color = color) {
                                            Text(code.name, style = TextStyle(fontSize = 14.sp, lineHeight = 18.sp,
                                                platformStyle = PlatformTextStyle(includeFontPadding = true)), textAlign = TextAlign.Center,
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 17.dp))
                                        } else Row(Modifier.fillMaxWidth().testTag("medical-code-${code.id}").toggleable(code.id in selected, role = Role.Checkbox) {
                                            selected = if (it) selected + code.id else selected - code.id
                                            publish()
                                        }.padding(vertical = 12.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                            Checkbox(code.id in selected, onCheckedChange = null, modifier = Modifier.padding(horizontal = 6.dp).size(18.dp),
                                                colors = CheckboxDefaults.colors(checkedColor = color, uncheckedColor = color))
                                            Text(code.name, style = TextStyle(fontFamily = FontFamily(Font(R.font.opensans_hebrew)), fontSize = 14.sp,
                                                lineHeight = 18.sp, platformStyle = PlatformTextStyle(includeFontPadding = true)))
                                        }
                                        HorizontalDivider(color = Color(0xffd0d0d0))
                                    }
                                }
                            }
                                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.End) {
                                    TextButton(onClick = { publish(); dismiss() }, modifier = Modifier.testTag("medical-done")) {
                                        Text(stringResource(R.string.ok), fontWeight = FontWeight.Normal)
                                    }
                                }
                        }
                    }
                }
            }
        }
    override fun onStart() {
        super.onStart()
        requireDialog().window?.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }
    private fun publish() {
        parentFragmentManager.setFragmentResult("medical-selection", Bundle().apply { putString("selected", checked()) })
    }
    private fun checked() = codes.filter { it.id != "sep" && it.id in selected }.joinToString(", ") { it.id }
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("selected", checked())
        outState.putString("query", query)
        super.onSaveInstanceState(outState)
    }
    companion object {
        @JvmStatic fun show(parent: Fragment, names: Array<CharSequence>, ids: Array<CharSequence>, categories: Array<String>, selected: String) {
            if (parent.childFragmentManager.findFragmentByTag("medical-picker") != null) return
            MedicalPickerDialog().apply {
                arguments = Bundle().apply {
                    putCharSequenceArray("names", names); putCharSequenceArray("ids", ids)
                    putStringArray("categories", categories); putString("selected", selected)
                }
            }.show(parent.childFragmentManager, "medical-picker")
        }
    }
}
