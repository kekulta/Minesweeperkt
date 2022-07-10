import kotlin.random.Random
import java.util.Scanner

val scanner = Scanner(System.`in`)
const val ROWS = 9
const val COLUMNS = 9
const val MINE = "X"
const val SPACE = "."
const val FOG = "*"


class Field() {
    enum class GameState() {
        NEW, GOING, WIN, LOOSE
    }

    private var mineField: MutableList<MutableList<Cell>> = MutableList(ROWS) { MutableList<Cell>(COLUMNS) { Cell() } }
    private var freeCells = ROWS * COLUMNS
    private var markedMines = 0
    private var marks = 0
    private var mines = 0
    private var state: GameState = GameState.NEW

    fun start(_row: Int, _column: Int, _mines: Int) {
        if (state == GameState.GOING) return
        var minesCounter = _mines
        this.freeCells -= _mines
        this.mines = _mines
        this.state = GameState.GOING
        while (minesCounter > 0) {
            val row = Random.nextInt(0, ROWS)
            val column = Random.nextInt(0, COLUMNS)
            if (!this.mineField[row][column].isMine() && !(row == _row && column == _column)) {
                this.mineField[row][column].setMine()
                minesCounter--
            } else {
                continue
            }
        }
        for (i in 0 until ROWS) {
            for (j in 0 until COLUMNS) {
                if (!this.mineField[i][j].isMine()) this.mineField[i][j].setCount(this.mineCounter(i, j))
            }
        }
    }

    fun isStarted(): Boolean {
        if (this.state == GameState.NEW) return false
        return true
    }

    fun mineCounter(row: Int, column: Int): Int {
        var counter = 0
        if (row < ROWS - 1 && this.mineField[row + 1][column].isMine()) counter++
        if (row > 0 && this.mineField[row - 1][column].isMine()) counter++
        if (column < COLUMNS - 1 && this.mineField[row][column + 1].isMine()) counter++
        if (column > 0 && this.mineField[row][column - 1].isMine()) counter++

        if (row < ROWS - 1 && column < COLUMNS - 1 && this.mineField[row + 1][column + 1].isMine()) counter++
        if (row > 0 && column > 0 && this.mineField[row - 1][column - 1].isMine()) counter++
        if (row > 0 && column < COLUMNS - 1 && this.mineField[row - 1][column + 1].isMine()) counter++
        if (row < ROWS - 1 && column > 0 && this.mineField[row + 1][column - 1].isMine()) counter++
        return counter
    }

    fun print() {
        var fieldPrint = " |"
        repeat(COLUMNS) {
            fieldPrint += (it + 1).toString()
        }
        fieldPrint += "|\n"
        var deliter = ""
        repeat(COLUMNS) {
            deliter += "-"
        }
        fieldPrint += "-|$deliter|\n"
        for (i in 0 until ROWS) {
            fieldPrint += "${i + 1}|"
            for (j in 0 until COLUMNS) {
                fieldPrint += this.mineField[i][j].getPerspective()
            }
            fieldPrint += "|\n"
        }
        fieldPrint += "-|$deliter|\n"
        println(fieldPrint)
    }

    fun exploreCell(row: Int, column: Int): Boolean {
        if (this.state != GameState.GOING) return true
        fun explosion(): Boolean {
            for (i in 0 until ROWS) {
                for (j in 0 until COLUMNS) {
                    if (this.mineField[i][j].isMine()) this.mineField[i][j].setVisible()
                }
            }
            this.state = GameState.LOOSE
            return false
        }

        if (this.mineField[row][column].isMine()) return explosion()
        if (this.mineField[row][column].isVisible()) return true
        if (this.mineField[row][column].isMarked()) this.mineField[row][column].unmarkIt()
        this.freeCells--
        this.mineField[row][column].setVisible()
        if (this.mineField[row][column].isFree()) {
            if (row < ROWS - 1) this.exploreCell(row + 1, column)
            if (row > 0) this.exploreCell(row - 1, column)
            if (column < COLUMNS - 1) this.exploreCell(row, column + 1)
            if (column > 0) this.exploreCell(row, column - 1)

            if (row < ROWS - 1 && column < COLUMNS - 1) this.exploreCell(row + 1, column + 1)
            if (row > 0 && column > 0) this.exploreCell(row - 1, column - 1)
            if (row > 0 && column < COLUMNS - 1) this.exploreCell(row - 1, column + 1)
            if (row < ROWS - 1 && column > 0) this.exploreCell(row + 1, column - 1)
        }
        return true
    }

    fun markCell(row: Int, column: Int) {
        if (!this.mineField[row][column].isVisible() && this.state == GameState.GOING) {
            if (this.mineField[row][column].isMarked()) {
                if (this.mineField[row][column].unmarkIt()) this.markedMines--
                this.marks--
            } else {
                if (this.mineField[row][column].markIt()) this.markedMines++
                this.marks++
            }
        }

    }

    fun winCondition(): Boolean {
        if (this.marks == this.markedMines && this.markedMines == this.mines && this.state == GameState.GOING) {
            this.state = GameState.WIN
            return true
        }
        if (this.freeCells == 0) {
            this.state = GameState.WIN
            return true
        }
        return false
    }
}


class Cell(var mine: Boolean = false, var visibility: Boolean = false, var marked: Boolean = false) {
    var view = SPACE
    var number = false

    fun getPerspective(): String = if (this.marked) "*" else if (!this.visibility) "." else this.view
    fun isMine(): Boolean = this.mine
    fun setMine() {
        this.view = "X"
        this.mine = true
    }

    fun isVisible(): Boolean = this.visibility
    fun setVisible() {
        this.visibility = true
    }

    fun setCount(_count: Int) {
        this.view = if (_count == 0) "/" else {
            this.number = true
            _count.toString()
        }
    }

    fun isMarked() = this.marked
    fun markIt(): Boolean {
        this.marked = true
        return this.mine
    }

    fun unmarkIt(): Boolean {
        this.marked = false
        return this.mine
    }

    fun isFree(): Boolean {
        return this.view == "/"
    }
}

fun main() {
    println("How many mines do you want on the field?")
    val mines = readln().toInt()
    val field = Field()
    while (true) {
        field.print()
        print("Set/unset mine marks or claim a cell as free:")
        val column = scanner.nextInt() - 1
        val row = scanner.nextInt() - 1
        val op = scanner.next()
        if (!field.isStarted()) field.start(row, column, mines)
        when (op) {
            "mine" -> {
                field.markCell(row, column)
            }
            "free" -> {
                if (!field.exploreCell(row, column)) {
                    field.print()
                    println("You stepped on a mine and failed!")
                    break
                }
            }
        }
        if (field.winCondition()) {
            field.print()
            println("Congratulations! You found all the mines!")
            break
        }
    }

}
