/*
 * Copyright 2019 OVO Energy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dynosaur
package codec

import cats._, implicits._
import cats.data.Chain

import model.{AttributeName, AttributeValue}
import Schema.structure._

/**
  * A type class that provides a way to produce a value of type `A` from an [[AttributeValue]] instance.
  */
trait Decoder[A] {

  def apply(c: HCursor): Decoder.Res[A]

}

object Decoder {

  final type Res[A] = Either[ReadError, A]

  def instance[A](f: HCursor => Res[A]): Decoder[A] =
    new Decoder[A] {
      def apply(c: HCursor): Res[A] = f(c)
    }

  def fromSchema[A](s: Schema[A]): Decoder[A] = {

    def decodeInt: HCursor => Res[Int] = { cursor =>
      cursor.value.n
        .toRight(
          ReadError(
            s"Expected value of type N but got ${cursor.value}",
            cursor.history
          )
        )
        .flatMap { n =>
          Either
            .catchNonFatal(n.value.toInt)
            .leftMap(
              _ =>
                ReadError(s"Could not parse to Int: ${n.value}", cursor.history)
            )
        }
    }

    def decodeString: HCursor => Res[String] = { cursor =>
      cursor.value.s
        .toRight(
          ReadError(
            s"Expected value of type S but got ${cursor.value}",
            cursor.history
          )
        )
        .map(_.value)
    }

    def decodeObject[R](record: Ap[Field[R, ?], R]): HCursor => Res[R] = {
      cursor =>
        cursor.value.m
          .toRight(
            ReadError(
              s"Expected value of type M but got ${cursor.value}",
              cursor.history
            )
          )
          .flatMap { v =>
            record.foldMap {
              λ[Field[R, ?] ~> Res] { field =>
                v.values
                  .get(AttributeName(field.name))
                  .toRight(
                    ReadError(
                      s"Field not found: ${field.name}",
                      cursor.history
                    )
                  )
                  .flatMap { v =>
                    val newCursor = HCursor
                      .fromAttributeValue(v)
                      .addOp(
                        cursor,
                        CursorOp.DownField(AttributeName(field.name))
                      )
                    fromSchema(field.elemSchema).apply(newCursor)
                  }
              }
            }
          }
    }

    implicit val monoidK: MonoidK[Res] = new MonoidK[Res] {
      override def empty[B]: Res[B] =
        Either.left(ReadError("", Nil))

      override def combineK[B](x: Res[B], y: Res[B]): Res[B] =
        x.orElse(y)
    }

    def decodeSum[B](cases: Chain[Alt[B]]): HCursor => Res[B] = { cursor =>
      cursor.value.m
        .toRight(
          ReadError(
            s"Expected value of type M but got ${cursor.value}",
            cursor.history
          )
        )
        .flatMap { _ =>
          cases
            .foldMapK[Res, B] { alt =>
              fromSchema(alt.caseSchema)
                .apply(cursor)
                .map(alt.prism.inject)
            }
        }
    }

    s match {
      case Num => Decoder.instance(decodeInt)
      case Str => Decoder.instance(decodeString)
      case Rec(rec) => Decoder.instance(decodeObject(rec))
      case Sum(cases) => Decoder.instance(decodeSum(cases))
    }
  }

}
