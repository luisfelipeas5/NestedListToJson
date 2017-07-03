import java.io.File
import java.nio.charset.Charset
import java.util.regex.Pattern
import java.util.*

class NestedList {
    var key: String? = null
    var nestedLists: MutableList<NestedList> = LinkedList()
}

fun main(args: Array<String>) {
    val decimalPointRegex = "(\\d*\\.).*"

    val file = File("./src/nestedlist.txt")
    val regexList = arrayOf(decimalPointRegex, "(^(?!i)[a-z]\\..*$)", "(i*\\.).*")
    printNestedListFileAsJson(file, regexList)

    println()
    println()
    println()
    println()
    println()

    val file2 = File("./src/nestedlist2.txt")
    val regexList2 = arrayOf(decimalPointRegex, "(^[a-z]\\..*$)")
    printNestedListFileAsJson(file2, regexList2)
}

fun printNestedListFileAsJson(file: File, regexList: Array<String>) {
    val lines = file.readLines(Charset.defaultCharset())
    val nestedList = NestedList()
    fillNestedRoot(lines.listIterator(), regexList, 0, nestedList)
    printObjectAsJson(nestedList)
}

fun printObjectAsJson(nestedList: NestedList) {
    println("{")

    println("\"key\": \"" + nestedList.key + "\",")
    val subNestedLists = nestedList.nestedLists

    println("\"list\": [")
    for (subNestedList: NestedList in subNestedLists) {
        val indexOf = subNestedLists.indexOf(subNestedList)
        printObjectAsJson(subNestedList)
        if (indexOf < subNestedLists.size - 1) {
            println(",")
        }
    }
    println("]")

    println("}")
}

private fun fillNestedRoot(linesIterator: ListIterator<String>, regexList: Array<String>,
                           regexIndex: Int, nestedList: NestedList): Boolean {
    if (regexIndex >= regexList.size) {
        return false
    }

    val regex = regexList[regexIndex]
    var subNestedList = NestedList()
    for (line: String in linesIterator) {
        val group = printMatch(regex, line)
        if (group != null) {
            subNestedList = putNestedLine(group, regexIndex, nestedList)
        } else {
            var i = regexIndex - 1
            while (i >= 0) {
                val regexPrevious = regexList[i]
                if (printMatch(regexPrevious, line) != null) {
                    linesIterator.previous()
                    return true
                }
                i--
            }

            linesIterator.previous()
            if (!fillNestedRoot(linesIterator, regexList, regexIndex + 1, subNestedList) && linesIterator.hasNext()) {
                subNestedList.key += line
                linesIterator.next()
            }

        }
    }
    return false
}

fun putNestedLine(group: String, regexIndex: Int, rootNestedList: NestedList): NestedList {
    var tabs = ""
    var i = 0
    while (i < regexIndex) {
        tabs += "\t"
        i++
    }
    val nestedList = NestedList()
    nestedList.key = group

    rootNestedList.nestedLists.add(nestedList)
    return nestedList
}

fun printMatch(regex: String?, token: String): String? {
    if (regex != null) {
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(token)
        val matches = matcher.matches()

        if (matches) {
            return token
        }
    }
    return null
}
