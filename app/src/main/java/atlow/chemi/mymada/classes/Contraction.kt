package atlow.chemi.mymada.classes

import java.util.Date

// Field names and nullable Dates are the installed native Gson format, not session.v1.
open class Contraction {
    open var start: Date? = null
    open var end: Date? = null
    open var oldEnd: Date? = null
    open var number: Int = 0
    open var previous: String? = null
    open var length: String? = null

    constructor(number: Int, length: String?, previous: String?) {
        this.number = number
        this.previous = previous
        this.length = length
    }

    constructor(number: Int, start: Date?, end: Date?, oldEnd: Date?) {
        this.number = number
        this.start = start
        this.end = end
        this.oldEnd = oldEnd
    }
}
