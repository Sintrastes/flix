/*
 * Copyright 2015-2016 Magnus Madsen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ca.uwaterloo.flix.language.phase

import ca.uwaterloo.flix.TestUtils
import ca.uwaterloo.flix.language.errors.NameError
import ca.uwaterloo.flix.util.Options
import org.scalatest.FunSuite

class TestNamer extends FunSuite with TestUtils {

  // TODO NS-REFACTOR move to Redundancy
  ignore("AmbiguousVarOrUse.01") {
    val input =
      s"""
         |def foo(): Bool =
         |    use Foo.f;
         |    let f = _ -> true;
         |    f(123)
         |
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.AmbiguousVarOrUse](result)
  }

  // TODO NS-REFACTOR move to Redundancy
  ignore("AmbiguousVarOrUse.02") {
    val input =
      s"""
         |def foo(): Bool =
         |    use Foo.f;
         |    let f = _ -> true;
         |    use Foo.g;
         |    let g = _ -> true;
         |    f(g(123))
         |
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.AmbiguousVarOrUse](result)
  }

  test("DuplicateLowerName.01") {
    val input =
      s"""
         |def f(): Int = 42
         |def f(): Int = 21
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateLowerName](result)
  }

  test("DuplicateLowerName.02") {
    val input =
      s"""
         |def f(): Int = 42
         |def f(): Int = 21
         |def f(): Int = 11
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateLowerName](result)
  }

  test("DuplicateLowerName.03") {
    val input =
      s"""
         |def f(x: Int): Int = 42
         |def f(x: Int): Int = 21
         |def f(x: Int): Int = 11
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateLowerName](result)
  }

  test("DuplicateLowerName.04") {
    val input =
      s"""
         |def f(): Int = 42
         |def f(x: Int): Int = 21
         |def f(x: Bool, y: Int, z: String): Int = 11
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateLowerName](result)
  }

  test("DuplicateLowerName.05") {
    val input =
      s"""
         |namespace A {
         |  def f(): Int = 42
         |}
         |
         |namespace A {
         |  def f(): Int = 21
         |}
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateLowerName](result)
  }

  test("DuplicateLowerName.06") {
    val input =
      s"""
         |namespace A/B/C {
         |  def f(): Int = 42
         |}
         |
         |namespace A {
         |  namespace B {
         |    namespace C {
         |      def f(): Int = 21
         |    }
         |  }
         |}
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateLowerName](result)
  }

  test("DuplicateLowerName.07") {
    val input =
      """
        |class C[a] {
        |    pub def f(x: a): Int
        |    pub def f(x: a): Bool
        |}
        |""".stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateLowerName](result)
  }

  test("DuplicateLowerName.08") {
    val input =
      """
        |class C[a] {
        |    pub def f(x: a): Int
        |    pub def f(x: a): Bool
        |    pub def f(x: Int): a
        |}
        |""".stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateLowerName](result)
  }

  test("DuplicateLowerName.09") {
    val input =
      s"""
         |class A[a] {
         |  pub def f(x: a): Int
         |}
         |
         |namespace A {
         |  pub def f(): Int = 21
         |}
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateLowerName](result)
  }

  test("DuplicateLowerName.10") {
    val input =
      s"""
         |namespace A/B/C {
         |  def f(): Int = 42
         |}
         |
         |namespace A {
         |  namespace B {
         |    class C[a] {
         |      pub def f(x: a): Int
         |    }
         |  }
         |}
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateLowerName](result)
  }

  test("DuplicateLowerName.11") {
    val input =
      s"""
         |namespace A/C {
         |  def f(): Int = 42
         |}
         |
         |namespace A {
         |  class C[a] {
         |    pub def f(x: a): Int
         |  }
         |}
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateLowerName](result)
  }

  test("DuplicateLowerName.12") {
    val input =
      """
        |namespace N {
        |    def f(): Int32 = 123
        |}
        |
        |eff N {
        |    pub def f(): Unit
        |}
        |""".stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateLowerName](result)
  }

  test("DuplicateLowerName.13") {
    val input =
      """
        |eff N {
        |    pub def f(): Unit
        |    pub def f(): Unit
        |}
        |""".stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateLowerName](result)
  }

  // TODO NS-REFACTOR move to redundancy
  ignore("DuplicateUseLower.01") {
    val input =
      s"""
         |def foo(): Bool =
         |    use A.f;
         |    use B.f;
         |    f() == f()
         |
         |namespace A {
         |    def f(): Int = 1
         |}
         |
         |namespace B {
         |    def f(): Int = 1
         |}
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUseLower](result)
  }

  // TODO NS-REFACTOR move to redundancy
  ignore("DuplicateUseLower.02") {
    val input =
      s"""
         |use A.f
         |use B.f
         |
         |def foo(): Bool =
         |    f() == f()
         |
         |namespace A {
         |    pub def f(): Int = 1
         |}
         |
         |namespace B {
         |    pub def f(): Int = 1
         |}
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUseLower](result)
  }

  // TODO NS-REFACTOR move to redundancy
  ignore("DuplicateUseLower.03") {
    val input =
      s"""
         |use A.f
         |
         |def foo(): Bool =
         |    use B.f;
         |    f() == f()
         |
         |namespace A {
         |    pub def f(): Int = 1
         |}
         |
         |namespace B {
         |    pub def f(): Int = 1
         |}
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUseLower](result)
  }

  // TODO NS-REFACTOR move to redundancy
  ignore("DuplicateUseLower.04") {
    val input =
      s"""
         |def foo(): Bool =
         |    use A.{f => g, f => g};
         |    g() == g()
         |
         |namespace A {
         |    pub def f(): Int = 1
         |}
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUseLower](result)
  }


  ignore("DuplicateUseLower.05") {
    val input =
      s"""
         |namespace T {
         |    def foo(): Bool =
         |        use A.f;
         |        use B.f;
         |        f() == f()
         |}
         |
         |namespace A {
         |    pub def f(): Int = 1
         |}
         |
         |namespace B {
         |    pub def f(): Int = 1
         |}
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUseLower](result)
  }

  // TODO NS-REFACTOR move to redundancy
  ignore("DuplicateUseLower.06") {
    val input =
      s"""
         |namespace T {
         |    use A.f
         |    use B.f
         |    def foo(): Bool =
         |        f() == f()
         |}
         |
         |namespace A {
         |    pub def f(): Int = 1
         |}
         |
         |namespace B {
         |    pub def f(): Int = 1
         |}
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUseLower](result)
  }

  // TODO NS-REFACTOR move to redundancy
  ignore("DuplicateUseLower.07") {
    val input =
      s"""
         |namespace T {
         |    use A.{f => g, f => g}
         |    def foo(): Bool =
         |        g() == g()
         |}
         |
         |namespace A {
         |    pub def f(): Int = 1
         |}
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUseLower](result)
  }

  // TODO NS-REFACTOR move to redundancy
  ignore("DuplicateUseLower.08") {
    val input =
      s"""
         |namespace T {
         |    use A.f
         |    def foo(): Bool =
         |        use B.f;
         |        f() == f()
         |}
         |
         |namespace A {
         |    pub def f(): Int = 1
         |}
         |
         |namespace B {
         |    pub def f(): Int = 1
         |}
         |""".stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUseLower](result)
  }

  // TODO NS-REFACTOR move to redundancy
  ignore("DuplicateUseUpper.01") {
    val input =
      s"""
         |def foo(): Bool =
         |    use A.Color;
         |    use B.Color;
         |    true
         |
         |namespace A {
         |    enum Color {
         |        case Red, Blue
         |    }
         |}
         |
         |namespace B {
         |    enum Color {
         |        case Red, Blue
         |    }
         |}
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUpperName](result)
  }

  // TODO NS-REFACTOR move to redundancy
  ignore("DuplicateUseUpper.02") {
    val input =
      s"""
         |use A.Color
         |use B.Color
         |
         |def foo(): Bool = true
         |
         |namespace A {
         |    enum Color {
         |        case Red, Blue
         |    }
         |}
         |
         |namespace B {
         |    enum Color {
         |        case Red, Blue
         |    }
         |}
         |
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUpperName](result)
  }

  // TODO NS-REFACTOR move to redundancy
  ignore("DuplicateUseUpper.03") {
    val input =
      s"""
         |namespace T {
         |    use A.Color
         |    use B.Color
         |    def foo(): Bool =
         |        true
         |}
         |
         |namespace A {
         |    enum Color {
         |        case Red, Blue
         |    }
         |}
         |
         |namespace B {
         |    enum Color {
         |        case Red, Blue
         |    }
         |}
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUpperName](result)
  }

  // TODO NS-REFACTOR move to redundancy
  ignore("DuplicateUseTag.01") {
    val input =
      s"""
         |def foo(): Bool =
         |    use A.Color.Red;
         |    use B.Color.Red;
         |    Red == Red
         |
         |namespace A {
         |    enum Color {
         |        case Red, Blu
         |    }
         |}
         |
         |namespace B {
         |    enum Color {
         |        case Red, Blu
         |    }
         |}
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUseTag](result)
  }

  // TODO NS-REFACTOR move to redundancy
  ignore("DuplicateUseTag.02") {
    val input =
      s"""
         |use A.Color.Red
         |use B.Color.Red
         |def foo(): Bool =
         |    Red == Red
         |
         |namespace A {
         |    enum Color {
         |        case Red, Blu
         |    }
         |}
         |
         |namespace B {
         |    enum Color {
         |        case Red, Blu
         |    }
         |}
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUseTag](result)
  }

  // TODO NS-REFACTOR move to redundancy
  ignore("DuplicateUseTag.03") {
    val input =
      s"""
         |
         |use A.Color.Red
         |def foo(): Bool =
         |    use B.Color.Red;
         |    Red == Red
         |
         |namespace A {
         |    enum Color {
         |        case Red, Blu
         |    }
         |}
         |
         |namespace B {
         |    enum Color {
         |        case Red, Blu
         |    }
         |}
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUseTag](result)
  }

  // TODO NS-REFACTOR move to redundancy
  ignore("DuplicateUseTag.04") {
    val input =
      s"""
         |def foo(): Bool =
         |    use B.Color.{Red => R};
         |    use B.Color.{Blu => R};
         |    R == R
         |
         |namespace A {
         |    enum Color {
         |        case Red, Blu
         |    }
         |}
         |
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUseTag](result)
  }

  // TODO NS-REFACTOR move to redundancy
  ignore("DuplicateUseTag.05") {
    val input =
      s"""
         |namespace T {
         |    use A.Color.Red
         |    use B.Color.Red
         |    def foo(): Bool =
         |        Red == Red
         |}
         |
         |def foo(): Bool =
         |    use A.Color.Red;
         |    use B.Color.Red;
         |    Red == Red
         |
         |namespace A {
         |    enum Color {
         |        case Red, Blu
         |    }
         |}
         |
         |namespace B {
         |    enum Color {
         |        case Red, Blu
         |    }
         |}
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUseTag](result)
  }

  // TODO NS-REFACTOR move to redundancy
  ignore("DuplicateUseTag.06") {
    val input =
      s"""
         |namespace T {
         |    use A.Color.Red
         |    def foo(): Bool =
         |        use B.Color.Red;
         |        Red == Red
         |}
         |
         |namespace A {
         |    enum Color {
         |        case Red, Blu
         |    }
         |}
         |
         |namespace B {
         |    enum Color {
         |        case Red, Blu
         |    }
         |}
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUseTag](result)
  }

  // TODO NS-REFACTOR move to redundancy
  ignore("DuplicateUseTag.07") {
    val input =
      s"""
         |namespace T {
         |    use B.Color.{Red => R}
         |    use B.Color.{Blu => R}
         |    def foo(): Bool =
         |        R == R
         |}
         |namespace A {
         |    enum Color {
         |        case Red, Blu
         |    }
         |}
         |
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUseTag](result)
  }

  test("DuplicateUpperName.01") {
    val input =
      s"""
         |type alias USD = Int
         |type alias USD = Int
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUpperName](result)
  }

  test("DuplicateUpperName.02") {
    val input =
      s"""
         |type alias USD = Int
         |type alias USD = Int
         |type alias USD = Int
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUpperName](result)
  }

  test("DuplicateUpperName.03") {
    val input =
      s"""
         |namespace A {
         |  type alias USD = Int
         |}
         |
         |namespace A {
         |  type alias USD = Int
         |}
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUpperName](result)
  }

  test("DuplicateUpperName.04") {
    val input =
      s"""
         |type alias USD = Int
         |enum USD {
         |  case A
         |}
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUpperName](result)
  }

  test("DuplicateUpperName.05") {
    val input =
      s"""
         |type alias USD = Int
         |type alias USD = Int
         |enum USD {
         |  case A
         |}
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUpperName](result)
  }

  test("DuplicateUpperName.06") {
    val input =
      s"""
         |namespace A {
         |  type alias USD = Int
         |}
         |
         |namespace A {
         |  enum USD {
         |    case B
         |  }
         |}
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUpperName](result)
  }

  test("DuplicateUpperName.07") {
    val input =
      s"""
         |enum USD {
         |  case A
         |}
         |enum USD {
         |  case B
         |}
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUpperName](result)
  }

  test("DuplicateUpperName.08") {
    val input =
      s"""
         |enum USD {
         |  case A
         |}
         |enum  USD {
         |  case B
         |}
         |enum USD {
         |  case C
         |}
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUpperName](result)
  }

  test("DuplicateUpperName.09") {
    val input =
      s"""
         |namespace A {
         |  enum USD {
         |    case A
         |  }
         |}
         |
         |namespace A {
         |  enum USD {
         |    case B
         |  }
         |}
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUpperName](result)
  }

  test("DuplicateUpperName.10") {
    val input =
      s"""
         |type alias USD = Int
         |class USD[a]
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUpperName](result)
  }

  test("DuplicateUpperName.11") {
    val input =
      s"""
         |type alias USD = Int
         |type alias USD = Int
         |class USD[a]
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUpperName](result)
  }

  test("DuplicateUpperName.12") {
    val input =
      s"""
         |namespace A {
         |  type alias USD = Int
         |}
         |
         |namespace A {
         |  class USD[a]
         |}
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUpperName](result)
  }

  test("DuplicateUpperName.13") {
    val input =
      s"""
         |enum USD {
         |  case A
         |}
         |class USD[a]
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUpperName](result)
  }

  test("DuplicateUpperName.14") {
    val input =
      s"""
         |enum USD {
         |  case A
         |}
         |enum USD {
         |  case B
         |}
         |class USD[a]
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUpperName](result)
  }

  test("DuplicateUpperName.15") {
    val input =
      s"""
         |namespace A {
         |  enum USD {
         |    case A
         |  }
         |}
         |
         |namespace A {
         |  class USD[a]
         |}
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUpperName](result)
  }

  test("DuplicateUpperName.16") {
    val input =
      s"""
         |class USD[a]
         |class USD[a]
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUpperName](result)
  }

  test("DuplicateUpperName.17") {
    val input =
      s"""
         |class USD[a]
         |class USD[a]
         |class USD[a]
         """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUpperName](result)
  }

  test("DuplicateUpperName.18") {
    val input =
      s"""
         |namespace A {
         |  class USD[a]
         |}
         |
         |namespace A {
         |  class USD[a]
         |}
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUpperName](result)
  }

  test("DuplicateUpperName.19") {
    val input =
      """
        |enum E
        |eff E
        |""".stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUpperName](result)
  }

  test("DuplicateUpperName.20") {
    val input =
      """
        |class C[a]
        |eff C
        |""".stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUpperName](result)
  }

  // TODO NS-REFACTOR move to Redundancy
  ignore("DuplicateUpperName.21") {
    val input =
      """
        |import java.sql.Statement
        |enum Statement
        |""".stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUpperName](result)
  }

  test("DuplicateUpperName.22") {
    val input =
      """
        |enum Statement
        |type alias Statement = Int
        |""".stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUpperName](result)
  }

  // TODO NS-REFACTOR move to Redundancy
  ignore("DuplicateUpperName.23") {
    val input =
      """
        |use A.Statement
        |enum Statement
        |""".stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUpperName](result)
  }

  // TODO NS-REFACTOR move to Redundancy
  ignore("DuplicateUpperName.24") {
    val input =
      """
        |namespace A {
        |    import java.sql.Statement
        |    enum Statement
        |}
        |""".stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUpperName](result)
  }

  // TODO NS-REFACTOR move to Redundancy
  ignore("DuplicateUpperName.25") {
    val input =
      """
        |namespace A {
        |    use B.Statement
        |    import java.sql.Statement
        |}
        |""".stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUpperName](result)
  }

  // TODO NS-REFACTOR move to Redundancy
  ignore("DuplicateUpperName.26") {
    val input =
      """
        |enum Statement
        |namespace A {
        |    use B.Statement
        |}
        |""".stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUpperName](result)
  }

  // TODO NS-REFACTOR move to Redundancy
  ignore("DuplicateUpperName.27") {
    val input =
      """
        |enum Statement
        |namespace A {
        |    import B.Statement
        |}
        |""".stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUpperName](result)
  }

  test("SuspiciousTypeVarName.01") {
    val input =
      s"""
         |def f(_x: List[unit]): Unit = ()
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.SuspiciousTypeVarName](result)
  }

  test("SuspiciousTypeVarName.02") {
    val input =
      s"""
         |def f(_x: List[Result[Unit, bool]]): Unit = ()
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.SuspiciousTypeVarName](result)
  }

  test("SuspiciousTypeVarName.03") {
    val input =
      s"""
         |def f(): List[char] = ()
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.SuspiciousTypeVarName](result)
  }

  test("SuspiciousTypeVarName.04") {
    val input =
      s"""
         |enum A {
         |    case X(string)
         |}
       """.stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.SuspiciousTypeVarName](result)
  }

  // TODO NS-REFACTOR move to Redundancy
  ignore("DuplicateImport.01") {
    val input =
      """
        |import java.lang.StringBuffer
        |import java.lang.StringBuffer
        |""".stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUpperName](result)
  }

  // TODO NS-REFACTOR move to Redundancy
  ignore("DuplicateImport.02") {
    val input =
      """
        |import java.lang.{StringBuffer => StringThingy}
        |import java.lang.{StringBuffer => StringThingy}
        |""".stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUpperName](result)
  }

  // TODO NS-REFACTOR move to Redundancy
  ignore("DuplicateImport.03") {
    val input =
      """
        |namespace A {
        |    import java.lang.StringBuffer
        |    import java.lang.StringBuffer
        |}
        |""".stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUpperName](result)
  }

  // TODO NS-REFACTOR move to Redundancy
  ignore("DuplicateImport.04") {
    val input =
      """
        |namespace A {
        |    import java.lang.{StringBuffer => StringThingy}
        |    import java.lang.{StringBuilder => StringThingy}
        |}
        |""".stripMargin
    val result = compile(input, Options.TestWithLibNix)
    expectError[NameError.DuplicateUpperName](result)
  }
}
