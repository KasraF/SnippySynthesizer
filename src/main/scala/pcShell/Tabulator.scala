package pcShell

object Tabulator {
  case class GreenString(plainText: String)
  def format(table: Seq[Seq[Any]]) = table match {
    case Seq() => ""
    case _ =>
      val sizes = for (row <- table) yield (for (cell <- row) yield
        if (cell == null) 0
        else (if (cell.isInstanceOf[GreenString])
          cell.asInstanceOf[GreenString].plainText
        else cell.toString).split('\n').map(_.length).max)
      val colSizes = for (col <- sizes.transpose) yield col.max + 2
      val rows = for (row <- table) yield formatRow(row, colSizes)
      formatRows(rowSeparator(colSizes), headerRowSeparator(colSizes), rows)
  }

  def formatRows(rowSeparator: String, headerRowSeparator: String, rows: Seq[String]): String = (
    rowSeparator ::
      rows.head ::
      headerRowSeparator ::
      rows.tail.flatMap(row => List(row,rowSeparator)).toList :::
      List()).mkString("\n")

  def formatRow(row: Seq[Any], colSizes: Seq[Int]) = {
    val lines = row.map(item => if (item.isInstanceOf[GreenString]) item.asInstanceOf[GreenString].plainText.split('\n').map(l => GreenString(l)) else item.toString.split('\n'))
    val innerRows = for(rowNum <- 0 until lines.map(_.length).max) yield {
      val innerRow = lines.map(l => if (rowNum < l.length) l(rowNum) else "")
      val cells = (for ((item, size) <- innerRow.zip(colSizes)) yield
                          if (size == 0) "" else /*("%" + size + "s").format(item)*/
                            " " + (if (item.isInstanceOf[GreenString])
                                    Console.GREEN + item.asInstanceOf[GreenString].plainText.padTo(size - 1, ' ') + Console.RESET
                                   else item.toString.padTo(size - 1, ' ')))
      cells.mkString("|", "|", "|")
    }
    innerRows.mkString("\n")
  }

  def headerRowSeparator(colSizes: Seq[Int]) = colSizes map { "=" * _ } mkString("+", "+", "+")
  def rowSeparator(colSizes: Seq[Int]) = colSizes map { "-" * _ } mkString("+", "+", "+")
}