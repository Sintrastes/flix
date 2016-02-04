package ca.uwaterloo.flix.language.ast

import scala.collection.mutable

sealed trait ExecutableAst

object ExecutableAst {

  case class Root(constants: Map[Name.Resolved, ExecutableAst.Definition.Constant],
                  lattices: Map[Type, ExecutableAst.Definition.Lattice],
                  collections: Map[Name.Resolved, ExecutableAst.Collection],
                  indexes: Map[Name.Resolved, ExecutableAst.Definition.Index],
                  facts: Array[ExecutableAst.Constraint.Fact],
                  rules: Array[ExecutableAst.Constraint.Rule],
                  time: Time,
                  dependenciesOf: Map[Name.Resolved, mutable.Set[(Constraint.Rule, ExecutableAst.Predicate.Body.Collection)]]) extends ExecutableAst

  sealed trait Definition

  object Definition {

    case class Constant(name: Name.Resolved,
                        exp: ExecutableAst.Expression,
                        tpe: Type,
                        loc: SourceLocation) extends ExecutableAst.Definition

    case class Lattice(tpe: Type,
                       bot: ExecutableAst.Expression,
                       top: ExecutableAst.Expression,
                       leq: ExecutableAst.Expression,
                       lub: ExecutableAst.Expression,
                       glb: ExecutableAst.Expression,
                       loc: SourceLocation) extends ExecutableAst.Definition

    case class Index(name: Name.Resolved,
                     indexes: Seq[Seq[Name.Ident]],
                     loc: SourceLocation) extends ExecutableAst.Definition

  }

  sealed trait Collection

  object Collection {

    case class Relation(name: Name.Resolved,
                        attributes: Array[ExecutableAst.Attribute],
                        loc: SourceLocation) extends ExecutableAst.Collection

    case class Lattice(name: Name.Resolved,
                       keys: Array[ExecutableAst.Attribute],
                       values: Array[ExecutableAst.Attribute],
                       loc: SourceLocation) extends ExecutableAst.Collection

  }

  sealed trait Constraint extends ExecutableAst

  object Constraint {

    case class Fact(head: ExecutableAst.Predicate.Head) extends ExecutableAst.Constraint

    case class Rule(head: ExecutableAst.Predicate.Head,
                    body: List[ExecutableAst.Predicate.Body],
                    collections: List[ExecutableAst.Predicate.Body.Collection],
                    filters: List[ExecutableAst.Predicate.Body.ApplyFilter],
                    filterHooks: List[ExecutableAst.Predicate.Body.ApplyHookFilter],
                    disjoint: List[ExecutableAst.Predicate.Body.NotEqual],
                    loops: List[ExecutableAst.Predicate.Body.Loop]) extends ExecutableAst.Constraint {
      var elapsedTime: Long = 0
      var hitcount: Int = 0
    }

  }

  sealed trait Expression extends ExecutableAst {
    def tpe: Type

    def loc: SourceLocation
  }

  sealed trait LoadExpression extends Expression {
    val e: ExecutableAst.Expression
    val offset: scala.Int
    val mask: scala.Int
    final val loc = SourceLocation.Unknown
  }

  sealed trait StoreExpression extends Expression {
    val e: ExecutableAst.Expression
    val offset: scala.Int
    val v: ExecutableAst.Expression
    val mask: Long
    final val targetMask = ~(mask << offset)
    final val tpe = Type.Int64
    final val loc = SourceLocation.Unknown
  }

  object Expression {

    case object Unit extends ExecutableAst.Expression {
      final val tpe = Type.Unit
      final val loc = SourceLocation.Unknown
      override def toString: String = "#U"
    }

    case object True extends ExecutableAst.Expression {
      final val tpe = Type.Bool
      final val loc = SourceLocation.Unknown
      override def toString: String = "#t"
    }

    case object False extends ExecutableAst.Expression {
      final val tpe = Type.Bool
      final val loc = SourceLocation.Unknown
      override def toString: String = "#f"
    }

    case class Int8(lit: scala.Byte) extends ExecutableAst.Expression {
      final val tpe = Type.Int8
      final val loc = SourceLocation.Unknown
    }

    case class Int16(lit: scala.Short) extends ExecutableAst.Expression {
      final val tpe = Type.Int16
      final val loc = SourceLocation.Unknown
    }

    case class Int32(lit: scala.Int) extends ExecutableAst.Expression {
      final val tpe = Type.Int32
      final val loc = SourceLocation.Unknown
    }

    case class Int64(lit: scala.Long) extends ExecutableAst.Expression {
      final val tpe = Type.Int64
      final val loc = SourceLocation.Unknown
    }

    case class Str(lit: java.lang.String) extends ExecutableAst.Expression {
      final val tpe = Type.Str
      final val loc = SourceLocation.Unknown
    }

    /**
      * An AST node representing a value (of type Bool) loaded from an Int64.
      *
      * @param e      the expression, returning an Int64, that the value is loaded from.
      * @param offset the offset (in bits) from the least significant bit that the value is loaded from.
      */
    case class LoadBool(e: ExecutableAst.Expression, offset: scala.Int) extends ExecutableAst.LoadExpression {
      val mask = 1
      val tpe = Type.Bool
    }

    /**
      * An AST node representing a value (of type Int8) loaded from an Int64.
      *
      * @param e      the expression, returning an Int64, that the value is loaded from.
      * @param offset the offset (in bits) from the least significant bit that the value is loaded from.
      */
    case class LoadInt8(e: ExecutableAst.Expression, offset: scala.Int) extends ExecutableAst.LoadExpression {
      val mask = 0xFF
      val tpe = Type.Int8
    }

    /**
      * An AST node representing a value (of type Int16) loaded from an Int64.
      *
      * @param e      the expression, returning an Int64, that the value is loaded from.
      * @param offset the offset (in bits) from the least significant bit that the value is loaded from.
      */
    case class LoadInt16(e: ExecutableAst.Expression, offset: scala.Int) extends ExecutableAst.LoadExpression {
      val mask = 0xFFFF
      val tpe = Type.Int16
    }

    /**
      * An AST node representing a value (of type Int32) loaded from an Int64.
      *
      * @param e      the expression, returning an Int64, that the value is loaded from.
      * @param offset the offset (in bits) from the least significant bit that the value is loaded from.
      */
    case class LoadInt32(e: ExecutableAst.Expression, offset: scala.Int) extends ExecutableAst.LoadExpression {
      // If we had unsigned ints, would be 0xFFFFFFFF
      val mask = -1
      val tpe = Type.Int32
    }

    /**
      * An AST node representing a value (of type Bool) to be stored into an Int64.
      *
      * @param e      the expression, returning an Int64, that the value is stored into.
      * @param offset the offset (in bits) from the least significant bit that the value is stored into.
      * @param v      the value to be stored.
      */
    case class StoreBool(e: ExecutableAst.Expression,
                         offset: scala.Int,
                         v: ExecutableAst.Expression) extends ExecutableAst.StoreExpression {
      val mask = 0x1L
    }

    /**
      * An AST node representing a value (of type Int8) to be stored into an Int64.
      *
      * @param e      the expression, returning an Int64, that the value is stored into.
      * @param offset the offset (in bits) from the least significant bit that the value is stored into.
      * @param v      the value to be stored.
      */
    case class StoreInt8(e: ExecutableAst.Expression,
                         offset: scala.Int,
                         v: ExecutableAst.Expression) extends ExecutableAst.StoreExpression {
      val mask = 0xFFL
    }

    /**
      * An AST node representing a value (of type Int16) to be stored into an Int64.
      *
      * @param e      the expression, returning an Int64, that the value is stored into.
      * @param offset the offset (in bits) from the least significant bit that the value is stored into.
      * @param v      the value to be stored.
      */
    case class StoreInt16(e: ExecutableAst.Expression,
                          offset: scala.Int,
                          v: ExecutableAst.Expression) extends ExecutableAst.StoreExpression {
      val mask = 0xFFFFL
    }

    /**
      * An AST node representing a value (of type Int32) to be stored into an Int64.
      *
      * @param e      the expression, returning an Int64, that the value is stored into.
      * @param offset the offset (in bits) from the least significant bit that the value is stored into.
      * @param v      the value to be stored.
      */
    case class StoreInt32(e: ExecutableAst.Expression,
                          offset: scala.Int,
                          v: ExecutableAst.Expression) extends ExecutableAst.StoreExpression {
      val mask = 0xFFFFFFFFL
    }

    /**
      * A typed AST node representing a local variable expression (i.e. a parameter or let-bound variable).
      *
      * @param ident  the name of the variable.
      * @param offset the (0-based) index of the variable.
      * @param tpe    the type of the variable.
      * @param loc    the source location of the variable.
      */
    case class Var(ident: Name.Ident,
                   offset: scala.Int,
                   tpe: Type,
                   loc: SourceLocation) extends ExecutableAst.Expression

    case class Ref(name: Name.Resolved, tpe: Type, loc: SourceLocation) extends ExecutableAst.Expression {
      override def toString: String = "Ref(" + name.fqn + ")"
    }

    // TODO: Lambda lift?
    case class Lambda(annotations: Ast.Annotations,
                      args: Array[ExecutableAst.FormalArg],
                      body: ExecutableAst.Expression,
                      tpe: Type.Lambda,
                      loc: SourceLocation) extends ExecutableAst.Expression {
      override def toString: String = "λ(" + args.map(_.tpe).mkString(", ") + ") " + body
    }

    case class Hook(hook: Ast.Hook, tpe: Type, loc: SourceLocation) extends ExecutableAst.Expression

    // TODO: Eliminate once we have lambda lifting
    case class Closure(args: Array[ExecutableAst.FormalArg],
                       body: ExecutableAst.Expression,
                       env: Map[String, ExecutableAst.Expression],
                       tpe: Type,
                       loc: SourceLocation) extends ExecutableAst.Expression {
      override def toString: String = "Closure(<>)"
    }

    /**
      * A typed AST node representing a function call.
      *
      * @param name the name of the function being called.
      * @param args the function arguments.
      * @param tpe  the return type of the function.
      * @param loc  the source location of the expression.
      */
    case class Apply(name: Name.Resolved,
                     args: Array[ExecutableAst.Expression],
                     tpe: Type,
                     loc: SourceLocation) extends ExecutableAst.Expression

    case class Apply3(exp: ExecutableAst.Expression,
                      args: Array[ExecutableAst.Expression],
                      tpe: Type,
                      loc: SourceLocation) extends ExecutableAst.Expression

    /**
      * A typed AST node representing a unary expression.
      *
      * @param op  the unary operator.
      * @param exp the expression.
      * @param tpe the type of the expression.
      * @param loc the source location of the expression.
      */
    case class Unary(op: UnaryOperator,
                     exp: ExecutableAst.Expression,
                     tpe: Type,
                     loc: SourceLocation) extends ExecutableAst.Expression {
      override def toString: String = "Unary(" + op + ", " + exp + ")"
    }

    /**
      * A typed AST node representing a binary expression.
      *
      * @param op   the binary operator.
      * @param exp1 the left expression.
      * @param exp2 the right expression.
      * @param tpe  the type of the expression.
      * @param loc  the source location of the expression.
      */
    case class Binary(op: BinaryOperator,
                      exp1: ExecutableAst.Expression,
                      exp2: ExecutableAst.Expression,
                      tpe: Type,
                      loc: SourceLocation) extends ExecutableAst.Expression  {
      override def toString: String = "Binary(" + op + ", " + exp1 + ", " + exp2 + ")"
    }

    /**
      * A typed AST node representing an if-then-else expression.
      *
      * @param exp1 the conditional expression.
      * @param exp2 the consequent expression.
      * @param exp3 the alternative expression.
      * @param tpe  the type of the consequent and alternative expression.
      * @param loc  the source location of the expression.
      */
    case class IfThenElse(exp1: ExecutableAst.Expression,
                          exp2: ExecutableAst.Expression,
                          exp3: ExecutableAst.Expression,
                          tpe: Type,
                          loc: SourceLocation) extends ExecutableAst.Expression {
      override def toString: String = "IfThenElse(" + exp1 + ", " + exp2 + ", " + exp3 + ")"
    }

    /**
      * A typed AST node representing a let expression.
      *
      * @param ident  the name of the bound variable.
      * @param offset the (0-based) index of the bound variable.
      * @param exp1   the value of the bound variable.
      * @param exp2   the body expression in which the bound variable is visible.
      * @param tpe    the type of the expression (which is equivalent to the type of the body expression).
      * @param loc    the source location of the expression.
      */
    case class Let(ident: Name.Ident,
                   offset: scala.Int,
                   exp1: ExecutableAst.Expression,
                   exp2: ExecutableAst.Expression,
                   tpe: Type,
                   loc: SourceLocation) extends ExecutableAst.Expression {
      override def toString: String = "Let(" + ident.name + " = " + exp1 + " in " + exp2 + ")"
    }

    /**
      * A typed AST node representing a check-tag expression, i.e. check if the tag expression matches the given tag
      * identifier.
      *
      * @param tag the tag identifier.
      * @param exp the tag expression to check.
      * @param loc the source location of the expression.
      */
    case class CheckTag(tag: Name.Ident,
                        exp: ExecutableAst.Expression,
                        loc: SourceLocation) extends ExecutableAst.Expression {
      final val tpe: Type = Type.Bool
      override def toString: String = "CheckTag(" + tag.name + ", " + exp + ")"
    }

    /**
      * A typed AST node representing a dereference of the inner value of a tag, i.e. destruct a tag.
      *
      * @param exp the tag expression to destruct.
      * @param tpe the type of the inner tag value.
      * @param loc the source location of the expression.
      */
    case class GetTagValue(exp: ExecutableAst.Expression,
                           tpe: Type,
                           loc: SourceLocation) extends ExecutableAst.Expression {
      override def toString: String = "GetTagValue(" + exp + ")"
    }

    /**
      * A typed AST node representing a tagged expression.
      *
      * @param enum the name of the enum.
      * @param tag  the name of the tag.
      * @param exp  the expression.
      * @param tpe  the type of the expression.
      * @param loc  The source location of the tag.
      */
    case class Tag(enum: Name.Resolved,
                   tag: Name.Ident,
                   exp: ExecutableAst.Expression,
                   tpe: Type.Enum,
                   loc: SourceLocation) extends ExecutableAst.Expression {
      override def toString: String = {
        val inner = exp match {
          case Expression.Unit => ""
          case _ => s"($exp)"
        }
        tag.name + inner
      }
    }

    /**
      * A typed AST node representing an index into a tuple, i.e. destruct a tuple.
      *
      * @param base   the tuple expression to index into.
      * @param offset the (0-based) offset of the tuple.
      * @param tpe    the type of the expression.
      * @param loc    the source location of the tuple.
      */
    case class GetTupleIndex(base: ExecutableAst.Expression,
                             offset: scala.Int,
                             tpe: Type,
                             loc: SourceLocation) extends ExecutableAst.Expression {
      override def toString: String = base + "[" + offset + "]"
    }

    /**
      * A typed AST node representing a tuple expression.
      *
      * @param elms the elements of the tuple.
      * @param tpe  the type of the tuple.
      * @param loc  the source location of the tuple.
      */
    case class Tuple(elms: Array[ExecutableAst.Expression],
                     tpe: Type,
                     loc: SourceLocation) extends ExecutableAst.Expression {
      override def toString: String = "(" + elms.mkString(", ") + ")"
    }

    case class CheckNil(exp: ExecutableAst.Expression, loc: SourceLocation) extends ExecutableAst.Expression {
      final val tpe: Type = Type.Bool
    }

    case class CheckCons(exp: ExecutableAst.Expression, loc: SourceLocation) extends ExecutableAst.Expression {
      final val tpe: Type = Type.Bool
    }

    case class Set(elms: Array[ExecutableAst.Expression],
                   tpe: Type.Set,
                   loc: SourceLocation) extends ExecutableAst.Expression

    /**
      * A typed AST node representing an error.
      *
      * @param tpe the type of the error.
      * @param loc the source location of the error.
      */
    case class Error(tpe: Type, loc: SourceLocation) extends ExecutableAst.Expression

    /**
      * A typed AST node representing a match error.
      *
      * @param tpe the type of the error.
      * @param loc the source location of the error.
      */
    case class MatchError(tpe: Type, loc: SourceLocation) extends ExecutableAst.Expression

    /**
      * A typed AST node representing a switch error.
      *
      * @param tpe the type of the error.
      * @param loc the source location of the error.
      */
    case class SwitchError(tpe: Type, loc: SourceLocation) extends ExecutableAst.Expression
  }

  sealed trait Predicate extends ExecutableAst {
    def tpe: Type

    def loc: SourceLocation
  }

  object Predicate {

    sealed trait Head extends ExecutableAst.Predicate

    object Head {

      case class Relation(name: Name.Resolved,
                          terms: Array[ExecutableAst.Term.Head],
                          tpe: Type.Predicate,
                          loc: SourceLocation) extends ExecutableAst.Predicate.Head {
        /**
          * Returns the arity of the predicate.
          */
        val arity: Int = terms.length
      }

    }

    sealed trait Body extends ExecutableAst.Predicate {
      /**
        * Returns the set of free variables in the term.
        */
      // TODO: Move
      def freeVars: Set[String] = this match {
        case ExecutableAst.Predicate.Body.Collection(_, terms, _, _, _) => terms.foldLeft(Set.empty[String]) {
          case (xs, t: ExecutableAst.Term.Body.Wildcard) => xs
          case (xs, t: ExecutableAst.Term.Body.Var) => xs + t.ident.name
          case (xs, t: ExecutableAst.Term.Body.Exp) => xs
        }
        case ExecutableAst.Predicate.Body.ApplyFilter(_, terms, _, _) => terms.foldLeft(Set.empty[String]) {
          case (xs, t: ExecutableAst.Term.Body.Wildcard) => xs
          case (xs, t: ExecutableAst.Term.Body.Var) => xs + t.ident.name
          case (xs, t: ExecutableAst.Term.Body.Exp) => xs
        }
        case ExecutableAst.Predicate.Body.ApplyHookFilter(_, terms, _, _) => terms.foldLeft(Set.empty[String]) {
          case (xs, t: ExecutableAst.Term.Body.Wildcard) => xs
          case (xs, t: ExecutableAst.Term.Body.Var) => xs + t.ident.name
          case (xs, t: ExecutableAst.Term.Body.Exp) => xs
        }
        case ExecutableAst.Predicate.Body.NotEqual(x, y, _, _) => Set(x.name, y.name)
        case ExecutableAst.Predicate.Body.Loop(_, _, _, _) => ???
      }
    }

    object Body {

      case class Collection(name: Name.Resolved,
                            terms: Array[ExecutableAst.Term.Body],
                            index2var: Array[String],
                            tpe: Type.Predicate,
                            loc: SourceLocation) extends ExecutableAst.Predicate.Body {
        /**
          * Returns the arity of this collection predicate.
          */
        val arity: Int = terms.length
      }

      case class ApplyFilter(name: Name.Resolved,
                             terms: Array[ExecutableAst.Term.Body],
                             tpe: Type.Lambda,
                             loc: SourceLocation) extends ExecutableAst.Predicate.Body

      case class ApplyHookFilter(hook: Ast.Hook,
                                 terms: Array[ExecutableAst.Term.Body],
                                 tpe: Type.Lambda,
                                 loc: SourceLocation) extends ExecutableAst.Predicate.Body

      case class NotEqual(ident1: Name.Ident,
                          ident2: Name.Ident,
                          tpe: Type,
                          loc: SourceLocation) extends ExecutableAst.Predicate.Body

      case class Loop(ident: Name.Ident,
                      term: ExecutableAst.Term.Head,
                      tpe: Type,
                      loc: SourceLocation) extends ExecutableAst.Predicate.Body

    }

  }

  object Term {

    sealed trait Head extends ExecutableAst {
      def tpe: Type

      def loc: SourceLocation
    }

    object Head {

      case class Var(ident: Name.Ident, tpe: Type, loc: SourceLocation) extends ExecutableAst.Term.Head

      case class Exp(e: ExecutableAst.Expression, tpe: Type, loc: SourceLocation) extends ExecutableAst.Term.Head

      case class Apply(name: Name.Resolved,
                       args: Array[ExecutableAst.Term.Head],
                       tpe: Type,
                       loc: SourceLocation) extends ExecutableAst.Term.Head

      case class ApplyHook(hook: Ast.Hook,
                           args: Array[ExecutableAst.Term.Head],
                           tpe: Type,
                           loc: SourceLocation) extends ExecutableAst.Term.Head

    }

    sealed trait Body extends ExecutableAst {
      def tpe: Type

      def loc: SourceLocation
    }

    object Body {

      case class Wildcard(tpe: Type, loc: SourceLocation) extends ExecutableAst.Term.Body

      case class Var(ident: Name.Ident, v: scala.Int, tpe: Type, loc: SourceLocation) extends ExecutableAst.Term.Body

      case class Exp(e: ExecutableAst.Expression, tpe: Type, loc: SourceLocation) extends ExecutableAst.Term.Body
    }

  }

  case class Attribute(ident: Name.Ident, tpe: Type) extends ExecutableAst

  case class FormalArg(ident: Name.Ident, tpe: Type) extends ExecutableAst

}
