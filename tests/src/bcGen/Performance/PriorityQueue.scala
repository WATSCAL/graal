package bcGen.Performance

import scala.annotation.tailrec
import scala.reflect.ClassTag

final class PriorityQueue[A](using ordering: TotalOrdering[A]) extends bcGen.Performance.Vector[A](1):
  def enqueue(value: A): Unit =
    pushBack(value)
    siftUp(currentSize - 1)

  def peek: A =
    elements(0)

  def dequeue(): A =
    val result = elements(0)
    val last = elements(currentSize - 1)
    popBack()
    if currentSize != 0 then
      elements(0) = last
      siftDown(0)
    result

  @tailrec
  private def siftUp(startIndex: Int): Unit =
    if startIndex > 0 then
      val parent = (startIndex - 1) / 2
      if ordering.lt(elements(parent), elements(startIndex)) then
        val tmp = elements(parent)
        elements(parent) = elements(startIndex)
        elements(startIndex) = tmp
        siftUp(parent)

  @tailrec
  private def siftDown(parent: Int): Unit =
    val left = parent * 2 + 1
    val right = left + 1

    if left < currentSize then
      val largest =
        if right < currentSize && ordering.lt(elements(left), elements(right)) then right
        else left

      if ordering.lt(elements(parent), elements(largest)) then
        val tmp = elements(parent)
        elements(parent) = elements(largest)
        elements(largest) = tmp
        siftDown(largest)