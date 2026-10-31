package atlow.chemi.mymada.integrations

object DispatchContract {
    val phones = listOf("+972523993348", "+972523993345", "+972523993339", "+972523993338",
        "+972523993342", "+972523993337", "+972523993343", "+972523993340", "+972523993346",
        "+972523993356", "+972523993341", "+972586309592")
    val regions = listOf("ירושלים", "איילון", "אשר", "גלבוע", "דן", "ירדן", "ירקון", "כרמל", "לכיש", "נגב", "שרון", "מוקד 4")
    @JvmStatic fun region(index: Int) = index.takeIf { it in phones.indices } ?: 0
}
