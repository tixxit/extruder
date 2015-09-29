package net.tixxit.extruder

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

trait PropExtruder[S <: String, A, B] {
  def apply(a: A): B
}

object PropExtruder {
  implicit def materialize[S <: String, A, B]: PropExtruder[S, A, B] =
    macro materializeImpl[S, A, B]

  def materializeImpl[S <: String: c.WeakTypeTag, A: c.WeakTypeTag, B: c.WeakTypeTag](c: Context): c.Expr[PropExtruder[S, A, B]] = {
    import c.universe._

    val sTpe = weakTypeOf[S]
    val fieldName: String = sTpe match {
      case ConstantType(Constant(field)) => field.asInstanceOf[String]
      case _ => c.abort(c.enclosingPosition, "...")
    }

    val aTpe = weakTypeOf[A]
    val field = aTpe.decl(TermName(fieldName)).asMethod
    val fieldType = field.typeSignature
    if (fieldType.paramLists.nonEmpty) {
      c.abort(c.enclosingPosition, "expected nullary method")
    }
    val returnType = fieldType.resultType
    val bTpe = weakTypeOf[B]
    if (returnType <:< bTpe) {
      c.Expr[PropExtruder[S, A, B]](q"""
      new PropExtruder[$sTpe, $aTpe, $bTpe] {
        def apply(a: $aTpe): $bTpe = a.${field.name.toTermName}
      }
      """)
    } else {
      c.abort(c.enclosingPosition, "type bad")
    }
  }
}
