object QuillUtils {
  val aliasExtract                   = "SELECT (\\w+).*FROM".r
  val fieldsReplace                  = "SELECT.+FROM".r
  def aliasStarSelect(alias: String) = s"SELECT ${alias}.* FROM"

  implicit class SqlTransformer(val sql: String) extends AnyVal {
    def fieldsToStar() = {
      val alias = Option(aliasExtract.findAllIn(sql).matchData.next().group(1))
      fieldsReplace.replaceFirstIn(sql, aliasStarSelect(alias.getOrElse("")))
    }

    def likeToFullSearch() = {
      sql
        .replaceAll("like LOWER \\(\\?\\)", "like CONCAT('%', LOWER(?), '%')")
        .replaceAll("like \\?", "like CONCAT('%', ?, '%')")
    }

    final def removeEmptyOrFilters(args: List[Any]): String = {
      val argSplit = sql.indexOf('?') + 1
      args match {
        case Nil => sql
        case Some(a) :: Some(b) :: tail if a == b => {
          val withRemovedNullCheck = sql.replaceFirst("\\? IS NULL OR ", "")
          withRemovedNullCheck.substring(0, argSplit) + withRemovedNullCheck
            .substring(argSplit)
            .removeEmptyOrFilters(tail)
        }
        case _ :: tail => sql.substring(0, argSplit) + sql.substring(argSplit).removeEmptyOrFilters(tail)
      }
    }
  }

  final def removeDuplicateVals(valArgs: List[Any]): List[Any] =
    valArgs.foldLeft(List().asInstanceOf[List[Any]])(
      (args, arg) =>
        if (args.isEmpty) args :+ arg
        else if (arg.isInstanceOf[Some[_]] && args.last == arg) args
        else args :+ arg)
}
