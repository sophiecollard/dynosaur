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

/**
  * A zipper that represents a position in an AttributeValue instance and supports navigation and modification.
  *
  * The `focus` represents the current position of the cursor; it may be updated with `withFocus` or changed using
  * navigational methods like `left` and `right`.
  */
abstract class ACursor(
    private val lastCursor: HCursor,
    private val lastOp: CursorOp
) {

  /**
    * The current location in an AttributeValue instance.
    *
    * @group Access
    */
  def focus: Option[AttributeValue]

  /**
    * Return to the top of the AttributeValue instance.
    *
    * @group Access
    */
  def top: Option[AttributeValue]

  /**
    * If the focus is a list (AttributeValue.L), return its elements.
    *
    * @group ListAccess
    */
  def values: Option[List[AttributeValue]]

  /**
    * If the focus is a map (AttributeValue.M), return its field names in their original order.
    *
    * @group MapAccess
    */
  def keys: Option[Iterable[AttributeName]]

  /**
    * The operations that have been performed so far.
    *
    * Note that these will be listed in reverse chronological order (last performed operation comes first).
    *
    * @group Decoding
    */
  final def history: List[CursorOp] = {
    var next = this
    val builder = List.newBuilder[CursorOp]

    while (next.ne(null)) {
      if (next.lastOp.ne(null)) {
        builder += next.lastOp
      }
      next = next.lastCursor
    }

    builder.result()
  }

  /**
    * Move the focus to the parent.
    *
    * @group Navigation
    */
  def up: ACursor

  /**
    * If the focus element is a list (AttributeValue.L), move one element to the left.
    *
    * @group ListNavigation
    */
  def left: ACursor

  /**
    * If the focus element is a list (AttributeValue.L), move one element to the right.
    *
    * @group ListNavigation
    */
  def right: ACursor

  /**
    * If the focus element is a list (AttributeValue.L), move to the left the given number of times.
    *
    * @group ListNavigation
    */
  def leftN(n: Int): ACursor

  /**
    * If the focus element is a list (AttributeValue.L), move to the right the given number of times.
    *
    * @group ListNavigation
    */
  def rightN(n: Int): ACursor

  /**
    * If the focus element is a map (AttributeValue.M), move to the value of the given key.
    *
    * @group MapNavigation
    */
  def downField(k: AttributeName): ACursor

}
