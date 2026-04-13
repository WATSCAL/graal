package bcGen.Performance

abstract class BinaryOp[T] {
  def apply(lhs: T, rhs: T): T
}

class IntAddition extends BinaryOp[Int] {
  override def apply(lhs: Int, rhs: Int): Int = lhs + rhs
}
