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

import dynosaur.model.{AttributeName, AttributeValue}

abstract class HCursor(lastCursor: HCursor, lastOp: CursorOp)
    extends ACursor(lastCursor, lastOp) {

  def value: AttributeValue

  def addOp(cursor: HCursor, op: CursorOp): HCursor

  final def focus: Option[AttributeValue] = Some(value)

  final def top: Option[AttributeValue] = {
    var current: HCursor = this

    while (!current.isInstanceOf[TopCursor]) {
      current = current.up.asInstanceOf[HCursor]
    }

    Some(current.asInstanceOf[TopCursor].value)
  }

  final def values: Option[List[AttributeValue]] = value match {
    case AttributeValue.L(list) => Some(list)
    case _ => None
  }

  final def keys: Option[Iterable[AttributeName]] = value match {
    case AttributeValue.M(map) => Some(map.keys)
    case _ => None
  }

  final def leftN(n: Int): ACursor = value match {
    case AttributeValue.L(_) =>
      if (n < 0)
        rightN(-n)
      else {
        @scala.annotation.tailrec
        def go(i: Int, c: ACursor): ACursor =
          if (i == 0) c else go(i - 1, c.left)

        go(n, this)
      }
    case _ =>
      fail(CursorOp.LeftN(n))
  }

  final def rightN(n: Int): ACursor = value match {
    case AttributeValue.L(_) =>
      if (n < 0)
        leftN(-n)
      else {
        @scala.annotation.tailrec
        def go(i: Int, c: ACursor): ACursor =
          if (i == 0) c else go(i - 1, c.right)

        go(n, this)
      }
    case _ =>
      fail(CursorOp.RightN(n))
  }

  final def downField(k: AttributeName): ACursor = value match {
    case AttributeValue.M(map) =>
      if (!map.contains(k))
        fail(CursorOp.DownField(k))
      else
        new MapCursor(map, k, this)(this, CursorOp.DownField(k))
    case _ =>
      fail(CursorOp.DownField(k))
  }

  protected[this] final def fail(op: CursorOp): ACursor =
    new FailedCursor(this, op)

}

object HCursor {

  def fromAttributeValue(value: AttributeValue): HCursor =
    new TopCursor(value)(null, null)

}
