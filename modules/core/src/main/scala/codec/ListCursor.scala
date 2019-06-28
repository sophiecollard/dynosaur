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

import dynosaur.model.AttributeValue

final class ListCursor(
    list: List[AttributeValue],
    index: Int,
    parent: HCursor
)(
    lastCursor: HCursor,
    lastOp: CursorOp
) extends HCursor(lastCursor, lastOp) {

  def value: AttributeValue = list(index)

  def addOp(cursor: HCursor, op: CursorOp): HCursor =
    new ListCursor(list, index, parent)(cursor, op)

  def up: ACursor = parent.addOp(this, CursorOp.Up)

  def left: ACursor =
    if (index == 0)
      fail(CursorOp.Left)
    else
      new ListCursor(list, index - 1, parent)(this, CursorOp.Left)

  def right: ACursor =
    if (index == values.size - 1)
      fail(CursorOp.Right)
    else
      new ListCursor(list, index + 1, parent)(this, CursorOp.Right)

}
