import java.io.File
import java.nio.charset.Charset
import java.util.regex.Pattern
import java.util.*

class NestedList {
    var description: String? = null
    var nestedLists: MutableList<NestedList> = LinkedList()
    var id: String? = null
}

fun main(args: Array<String>) {
    val decimalPointRegex = "(\\d*\\.).*"

    val file = File("./src/nestedlist.txt")
    val regexList = arrayOf(decimalPointRegex, "([a-z]\\..*$)", "([iv]*\\.).*")
    val nestedList = getNestedListFileAsJson(file, regexList)

    val file2 = File("./src/nestedlist2.txt")
    val regexList2 = arrayOf(decimalPointRegex, "(^[a-z]\\..*$)")
    val nestedList1 = getNestedListFileAsJson(file2, regexList2)

    nestedList.nestedLists.addAll(nestedList1.nestedLists)

    for (firstNestedList: NestedList in nestedList.nestedLists) {
        updateDescription(firstNestedList)
        for (secondNestedList: NestedList in firstNestedList.nestedLists) {
            updateDescription(secondNestedList)
        }
    }

    printObjectAsJson(nestedList)
}

private fun updateDescription(nestedList: NestedList) {
    var description = nestedList.description
    if (description != null) {
        description = description.trim()
        nestedList.description = description.substring(IntRange(0, 0)).toUpperCase() + description.substring(1).toLowerCase()
    }
}

fun getNestedListFileAsJson(file: File, regexList: Array<String>): NestedList {
    val lines = file.readLines(Charset.defaultCharset())
    val nestedList = NestedList()
    fillNestedRoot(lines.listIterator(), regexList, 0, nestedList)
    return nestedList
}

fun printObjectAsJson(nestedList: NestedList) {
    println("{")

    println("\"key\": \"" + nestedList.id + "\",")
    println("\"description\": \"" + nestedList.description + "\",")
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
                subNestedList.description += line
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

    val pattern = Pattern.compile("(.*)\\.(.*)")
    val matcher = pattern.matcher(group)
    val find = matcher.find()
    if (find) {
        val id = matcher.group(1)
        nestedList.id = id

        val description = matcher.group(2)
        nestedList.description = description
    }

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
