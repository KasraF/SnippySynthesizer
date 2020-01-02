package pcShell

object Tabulator {
  def format(table: Seq[Seq[Any]]) = table match {
    case Seq() => ""
    case _ =>
      val sizes = for (row <- table) yield (for (cell <- row) yield if (cell == null) 0 else cell.toString.length)
      val colSizes = for (col <- sizes.transpose) yield col.max
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
    val cells = (for ((item, size) <- row.zip(colSizes)) yield if (size == 0) "" else /*("%" + size + "s").format(item)*/item.toString.padTo(size,' '))
    cells.mkString("|", "|", "|")
  }

  def headerRowSeparator(colSizes: Seq[Int]) = colSizes map { "=" * _ } mkString("+", "+", "+")
  def rowSeparator(colSizes: Seq[Int]) = colSizes map { "-" * _ } mkString("+", "+", "+")
}