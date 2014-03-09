package com.dominikgruber.fpinscala.chapter03

sealed trait List[+A]
case object Nil extends List[Nothing]
case class Cons[+A](head: A, tail: List[A]) extends List[A]

object List {
  def sum(ints: List[Int]): Int = ints match {
    case Nil => 0
    case Cons(x,xs) => x + sum(xs)
  }

  def product(ds: List[Double]): Double = ds match {
    case Nil => 1.0
    case Cons(0.0, _) => 0.0
    case Cons(x,xs) => x * product(xs)
  }

  def apply[A](as: A*): List[A] =
    if (as.isEmpty) Nil
    else Cons(as.head, apply(as.tail: _*))

  /**
   * Exercise 02
   * Implement the function tail for “removing” the first element of a List.
   * Note that the function takes constant time. What are different choices
   * you could make in your implementation if the List is Nil?
   * We’ll return to this question in the next chapter.
   */
  def tail[A](l: List[A]) = l match {
    case Nil => Nil
    case Cons(_, t) => t
  }

  /**
   * Exercise 03
   * Using the same idea, implement the function setHead for replacing the first
   * element of a List with a different value.
   */
  def setHead[A](head: A, l: List[A]) = l match {
    case Nil => Cons(head, Nil)
    case Cons(_, t) => Cons(head, t)
  }

  /**
   * Exercise 04
   * Generalize tail to the function drop, which removes the first n elements
   * from a list. Note that this function takes time proportional only to the
   * number of elements being dropped—we don’t need to make a copy of the
   * entire List.
   */
  def drop[A](n: Int, l: List[A]): List[A] =
    if (n <= 0) l
    else drop(n - 1, tail(l))

  /**
   * Exercise 05
   * Implement dropWhile, which removes elements from the List prefix as long as
   * they match a predicate.
   */
  def dropWhile[A](l: List[A], f: A => Boolean): List[A] = l match {
    case Nil => Nil
    case Cons(h, t) =>
      if (f(h)) dropWhile(t, f)
      else l
  }

  def append[A](a1: List[A], a2: List[A]): List[A] =
    a1 match {
      case Nil => a2
      case Cons(h,t) => Cons(h, append(t, a2))
    }

  /**
   * Exercise 06
   * Not everything works out so nicely. Implement a function, init, which
   * returns a List consisting of all but the last element of a List. So, given
   * List(1,2,3,4), init will return List(1,2,3). Why can’t this function be
   * implemented in constant time like tail?
   */
  def init[A](l: List[A]): List[A] = {
    def acc(l1: List[A], l2: List[A]): List[A] = l1 match {
      case Nil => l2
      case Cons(h, Nil) => l2
      case Cons(h, t) => acc(t, append(l2, List(h)))
    }
    acc(l, Nil)
  }

  def foldRight[A,B](as: List[A], z: B)(f: (A, B) => B): B =
    as match {
      case Nil => z
      case Cons(x, xs) => f(x, foldRight(xs, z)(f))
    }

  def sum2(ns: List[Int]) =
    foldRight(ns, 0)((x,y) => x + y)

  def product2(ns: List[Double]) =
    foldRight(ns, 1.0)(_ * _)

  /**
   * Exercise 07
   * Can product, implemented using foldRight, immediately halt the recursion
   * and return 0.0 if it encounters a 0.0? Why or why not? Consider how any
   * short-circuiting might work if you call foldRight with a large list. This
   * is a deeper question that we’ll return to in chapter 5.
   *
   * Answer: It can not because the function f has as of right now no way of
   * stopping the foldRight calls
   */

  /**
   * Exercise 09
   * Compute the length of a list using foldRight.
   */
  def length[A](l: List[A]): Int =
    foldRight(l, 0)((_, y) => y + 1)

  /**
   * Exercise 10
   * Our implementation of foldRight is not tail-recursive and will
   * StackOverflow for large lists (we say it’s not stack-safe). Convince
   * yourself that this is the case, and then write another general
   * list-recursion function, foldLeft that is tail-recursive, using the
   * techniques we discussed in the previous chapter.
   */
  def foldLeft[A, B](l: List[A], z: B)(f: (B, A) => B): B = {
    @annotation.tailrec
    def loop(xs: List[A], z2: B): B = xs match {
      case Nil => z2
      case Cons(y, ys) => loop(ys, f(z2, y))
    }
    loop(l, z)
  }

  /**
   * Exercise 11
   * Write sum, product, and a function to compute the length of a list using
   * foldLeft.
   */
  def sum3(ns: List[Int]) =
    foldLeft(ns, 0)(_ + _)

  def product3(ns: List[Double]) =
    foldLeft(ns, 1.0)(_ * _)

  def length2[A](l: List[A]): Int =
    foldLeft(l, 0)((x, _) => x + 1)

  /**
   * Exercise 12
   * Write a function that returns the reverse of a list (so given List(1,2,3)
   * it returns List(3,2,1)). See if you can write it using a fold.
   */
  def reverse[A](l: List[A]): List[A] =
    foldLeft(l, List[A]())((x, y) => Cons(y, x))

  /**
   * Exercise 13 (hard, optional)
   * Can you write foldLeft in terms of foldRight? How about the other way
   * around? Implementing foldRight via foldLeft is useful because it lets us
   * implement foldRight tail-recursively, which means it works even for large
   * lists without overflowing the stack.
   */
  def foldLeft2[A, B](l: List[A], z: B)(f: (B, A) => B): B =
    foldRight(l, z)((x, y) => f(y, x))

  def foldRight2[A,B](l: List[A], z: B)(f: (A, B) => B): B =
    foldLeft(l, z)((x, y) => f(y, x))

  /**
   * Exercise 14
   * Implement append in terms of either foldLeft or foldRight.
   */
  def append2[A](a1: List[A], a2: List[A]): List[A] =
    foldRight(a1, a2)((x, y) => Cons(x, y))

  /**
   * Exercise 15 (hard)
   * Write a function that concatenates a list of lists into a single list. Its
   * runtime should be linear in the total length of all lists. Try to use
   * functions we have already defined.
   */
  def concatListOfLists[A](l: List[List[A]]) =
    foldLeft(l, List[A]())((x, y) => append(x, y))

  /**
   * Exercise 16
   * Write a function that transforms a list of integers by adding 1 to each
   * element. (Reminder: this should be a pure function that returns a new List!)
   */
  def add1(l: List[Int]): List[Int] = l match {
    case Nil => Nil
    case Cons(x, xs) => Cons(x + 1, add1(xs))
  }

  /**
   * Exercise 17
   * Write a function that turns each value in a List[Double] into a String.
   * You can use the expression d.toString to convert some d: Double to a String.
   */
  def doubleListToString(l: List[Double]): List[String] = l match {
    case Nil => Nil
    case Cons(x, xs) => Cons(x.toString, doubleListToString(xs))
  }

  /**
   * Exercise 18
   * Write a function map that generalizes modifying each element in a list
   * while maintaining the structure of the list.
   */
  def map[A,B](l: List[A])(f: A => B): List[B] = l match {
    case Nil => Nil
    case Cons(x, xs) => Cons(f(x), map(xs)(f))
  }

  /**
   * Exercise 19
   * Write a function filter that removes elements from a list unless they
   * satisfy a given predicate. Use it to remove all odd numbers from a
   * List[Int].
   */
  def filter[A](l: List[A])(f: A => Boolean): List[A] = l match {
    case Nil => Nil
    case Cons(x, xs) =>
      if (f(x)) Cons(x, filter(xs)(f))
      else filter(xs)(f)
  }
}