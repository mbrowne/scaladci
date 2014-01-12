package scaladci
package util

import scala.reflect.macros.{Context => MacroContext}

trait MacroHelper[C <: MacroContext] {
  val c0: C

  import c0.universe._

  def expr(tree: Tree) = {
    val typeCheckedTree = c0.typeCheck(tree)
    c0.Expr(typeCheckedTree)(c0.WeakTypeTag(typeCheckedTree.tpe))
  }

  def abort(t: Any) {
    c0.abort(c0.enclosingPosition, t.toString)
  }


  def comp(t1: Any, t2: Any) {
    abort(s"\n$t1\n-------------------\n$t2")
  }

  def comp(t1: List[Tree], t2: List[Tree]) {
    val source = code(t1) + "\n\n================\n" + code(t2)
    val ast = raw(t1) + "\n\n================\n" + raw(t2)
    abort(s"\n$source\n\n#################################################\n\n$ast")
  }

  def comp(t1: Tree, t2: Tree) {
    val source = t1 + "\n\n================\n" + t2
    val ast = showRaw(t1) + "\n\n================\n" + showRaw(t2)
    abort(s"\n$source\n\n#################################################\n\n$ast")
  }

  def l(ts: List[Tree]) {
    abort(code(ts))
  }

  def r(t: Any) {
    c0.abort(c0.enclosingPosition, showRaw(t))
  }

  def r(ts: List[Tree]) {
    abort(raw(ts))
  }

  def sep(ts: List[Tree]) = {
    code(ts) + "\n\n================\n" + raw(ts)
  }

  def lr(ts: List[Tree]) {
    abort(sep(ts))
  }

  def code(ts: List[Tree]) = ts.zipWithIndex.map {
    case (t, i) => s"\n-- $i -- " + t
  }

  def raw(ts: List[Tree]) = ts.zipWithIndex.map {
    case (t, i) => s"\n-- $i -- " + showRaw(t)
  }

  //      def raw(ts: List[Tree]) = ts.map("\n----- " + showRaw(_))

  def err(t: Tree, msg: String) = {
    abort(msg + t)
    t
  }
  case class debug(clazz: String, threshold: Int, max: Int = 9999) {
    def apply(id: Int, params: Any*): Unit = {
      if (id >= threshold && id <= max) {
        val sp = "\n...                 "
        val x = s"$id, $clazz \n" + params.toList.zipWithIndex.map {
          case (l: List[_], i) => s"${i + 1}  ##   List($sp" + l.mkString(s"$sp") + "    )"
          //          case (l@h :: t, i) => s"${i + 1}  ##   List($sp" + l.mkString(s"$sp") + "    )"
          case (value, i) => s"${i + 1}  ##   $value"
        }.mkString("\n") + "\n---------------------------------"
        c0.abort(NoPosition, x)
      }
    }
  }


        object Name {
          def apply(s: String) = newTermName(s)
          def unapply(name: Name): Option[String] = Some(name.decoded)
        }

    object TermName {
      def apply(s: String) = newTermName(s)
      def unapply(name: TermName): Option[String] = Some(name.toString)
    }
}