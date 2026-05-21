package bcGen.Performance

import scala.annotation.tailrec
import scala.reflect.ClassTag

final class PriorityQueue[A](using ordering: Ordering[A]):
  private val heap = bcGen.Performance.Vector.empty[A]

  def isEmpty: Boolean = heap.isEmpty

  def nonEmpty: Boolean = heap.nonEmpty

  def size: Int = heap.size

  def enqueue(value: A): Unit =
    heap.pushBack(value)
    siftUp(heap.size - 1)

  def peek: A =
    heap(0)

  def dequeue(): A =
    val result = heap(0)
    val last = heap(heap.size - 1)
    heap.popBack()
    if heap.nonEmpty then
      heap(0) = last
      siftDown(0)
    result

  def clear(): Unit =
    heap.clear()

  @tailrec
  private def siftUp(startIndex: Int): Unit =
    if startIndex > 0 then
      val parent = (startIndex - 1) / 2
      if ordering.lt(heap(parent), heap(startIndex)) then
        swap(parent, startIndex)
        siftUp(parent)

  @tailrec
  private def siftDown(parent: Int): Unit =
    val left = parent * 2 + 1
    val right = left + 1

    if left < heap.size then
      val largest =
        if right < heap.size && ordering.gt(heap(right), heap(left)) then right
        else left

      if ordering.lt(heap(parent), heap(largest)) then
        swap(parent, largest)
        siftDown(largest)

  private def swap(i: Int, j: Int): Unit =
    val tmp = heap(i)
    heap(i) = heap(j)
    heap(j) = tmp