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

final class FailedCursor(lastCursor: HCursor, lastOp: CursorOp)
    extends ACursor(lastCursor, lastOp) {

  def focus: Option[AttributeValue] = None
  def top: Option[AttributeValue] = None

  def values: Option[List[AttributeValue]] = None
  def keys: Option[Iterable[AttributeName]] = None

  def up: ACursor = this
  def left: ACursor = this
  def right: ACursor = this
  def leftN(n: Int): ACursor = this
  def rightN(n: Int): ACursor = this
  def downField(k: AttributeName): ACursor = this

}
