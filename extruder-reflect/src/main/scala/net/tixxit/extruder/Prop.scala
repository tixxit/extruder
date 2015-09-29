package net.tixxit.extruder

import scala.language.dynamics
import scala.language.experimental.macros
import scala.reflect.macros.whitebox.Context

object Prop extends Dynamic {
  def selectDynamic(field: String): Any = macro propImpl

  def propImpl(c: Context)(field: c.Tree): c.Tree = {
    import c.universe._
    import compat._
    val q"${fieldName: String}" = field
    val sTpe = internal.constantType(Constant(fieldName))
    val tpe = c.typecheck(
      tq"{ type Extruder[A, B] = _root_.net.tixxit.extruder.PropExtruder[$sTpe, A, B] }",
      mode = c.TYPEmode
    ).tpe
    Literal(Constant(())).setType(tpe)
  }
}
