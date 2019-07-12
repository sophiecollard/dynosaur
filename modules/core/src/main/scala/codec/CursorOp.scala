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

import dynosaur.model.AttributeName

sealed abstract class CursorOp extends Product with Serializable {
  def requiresList: Boolean
  def requiresMap: Boolean
}

object CursorOp {

  abstract sealed class ListOp extends CursorOp {
    final def requiresList: Boolean = true
    final def requiresMap: Boolean = false
  }

  abstract sealed class MapOp extends CursorOp {
    final def requiresList: Boolean = false
    final def requiresMap: Boolean = true
  }

  abstract sealed class UnconstrainedOp extends CursorOp {
    final def requiresList: Boolean = false
    final def requiresMap: Boolean = false
  }

  final case object Up extends UnconstrainedOp

  final case object Left extends ListOp
  final case object Right extends ListOp
  final case class LeftN(n: Int) extends ListOp
  final case class RightN(n: Int) extends ListOp

  final case class DownField(k: AttributeName) extends MapOp

}
