package bcGen.Performance

abstract class TotalOrdering[T] {
  def lt(lhs: T, rhs: T): Boolean
  def equiv(lhs: T, rhs: T): Boolean
}

class IntOrdering extends TotalOrdering[Int] {
  override def lt(lhs: Int, rhs: Int): Boolean = lhs < rhs
  override def equiv(lhs: Int, rhs: Int): Boolean = lhs == rhs
}