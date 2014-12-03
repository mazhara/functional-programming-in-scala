package com.dominikgruber.fpinscala.chapter15

sealed trait Process[I,O] {
  def apply(s: Stream[I]): Stream[O] = this match {
    case Halt() => Stream()
    case Await(recv) => s match {
      case h #:: t => recv(Some(h))(t)
      case xs => recv(None)(xs)
    }
    case Emit(h,t) => h #:: t(s)
  }

  def repeat: Process[I,O] = {
    def go(p: Process[I,O]): Process[I,O] = p match {
      case Halt() => go(this)
      case Await(recv) => Await {
        case None => recv(None)
        case i => go(recv(i))
      }
      case Emit(h, t) => Emit(h, go(t))
    }
    go(this)
  }

  /**
   * Exercise 05
   * Hard: Implement |> as a method on Process. Let the types guide your
   * implementation.
   */
  def |>[O2](p2: Process[O,O2]): Process[I,O2] = p2 match {
    case Halt() => Halt()
    case Emit(h2, t2) => Emit(h2, this |> t2)
    case Await(recv2) => this match {
      case Halt() => Halt() |> recv2(None)
      case Emit(h1, t1) => t1 |> recv2(Some(h1))
      case Await(recv1) => Await(i => recv1(i) |> p2)
    }
  }
}

case class Emit[I,O](head: O, tail: Process[I,O] = Halt[I,O]()) extends Process[I,O]
case class Await[I,O](recv: Option[I] => Process[I,O]) extends Process[I,O]
case class Halt[I,O]() extends Process[I,O]

object Process {

  def liftOne[I,O](f: I => O): Process[I,O] = Await {
    case Some(i) => Emit(f(i))
    case None => Halt()
  }

  def lift[I,O](f: I => O): Process[I,O] = liftOne(f).repeat

  def filter[I](p: I => Boolean): Process[I,I] = Await[I,I] {
    case Some(i) if p(i) => Emit(i)
    case _ => Halt()
  }.repeat

  def sum: Process[Double,Double] = {
    def go(acc: Double): Process[Double,Double] = Await {
      case Some(d) => Emit(d + acc, go(d + acc))
      case None => Halt()
    }
    go(0.0)
  }

  /**
   * Exercise 01
   * Implement take, which halts the Process after it encounters the given
   * number of elements, and drop, which ignores the given number of arguments
   * and then emits the rest. Also implement takeWhile and dropWhile, that take
   * and drop elements as long as the given predicate remains true.
   */
  def take[I](n: Int): Process[I,I] =
    if (n <= 0) Halt()
    else Await[I,I] {
      case Some(i) => Emit(i, take(n - 1))
      case _ => Halt()
    }

  def drop[I](n: Int): Process[I,I] = Await[I,I] {
    case Some(i) if n == 0 => Emit(i)
    case Some(i) if n > 0 => drop(n - 1)
    case _ => Halt()
  }.repeat

  def takeWhile[I](f: I => Boolean): Process[I,I] = Await[I,I] {
    case Some(i) if f(i) => Emit(i, takeWhile(f))
    case _ => Halt()
  }

  def dropWhile[I](f: I => Boolean): Process[I,I] = Await[I,I] {
    case Some(i) if !f(i) => Emit(i, id)
    case Some(i) if f(i) => dropWhile(f)
    case _ => Halt()
  }

  def id[I]: Process[I,I] = Await[I,I] {
    case Some(i) => Emit(i)
    case None => Halt()
  }.repeat

  /**
   * Exercise 02
   * Implement count. It should emit the number of elements seen so far. For
   * instance, count(Stream("a", "b", "c")) should yield Stream(1, 2, 3)
   * (or Stream(0, 1, 2, 3), your choice).
   */
  def count[I]: Process[I,Int] = {
    def go(acc: Int): Process[I,Int] = Await {
      case Some(d) => Emit(acc, go(acc + 1))
      case None => Halt()
    }
    go(1)
  }

  /**
   * Exercise 03
   * Implement mean. It should emit a running average of the values seen so far.
   */
  def mean: Process[Double,Double] = {
    def go(sum: Double, cnt: Int): Process[Double,Double] = Await {
      case Some(d) => Emit((sum + d) / (cnt + 1), go(sum + d, cnt + 1))
      case None => Halt()
    }
    go(0, 0)
  }

  def loop[S,I,O](z: S)(f: (I,S) => (O,S)): Process[I,O] =
    Await {
      case Some(i) => f(i, z) match {
        case (o, s2) => Emit(o, loop(s2)(f))
      }
      case None => Halt()
    }

  /**
   * Exercise 04
   * Write sum and count in terms of loop.
   */
  def sumViaLoop: Process[Double,Double] =
    loop(0.0)((i, z) => (z + i, z + i))

  def countViaLoop[I]: Process[I,Int] =
    loop(0)((_, z) => (z + 1, z + 1))
}