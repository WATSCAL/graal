package bcGen.Performance

final class MaxQueue[A](initialCapacity: Int)(using ordering: TotalOrdering[A]) extends Deque[A](initialCapacity):

  def enqueue(value: A): Unit =
    while nonEmpty && ordering.lt(back, value) do
      popBack()
    pushBack(value)
  
  def dequeue(value: A): Unit =
    if nonEmpty && ordering.equiv(front, value) then
      popFront()

  def getMax: A = front
