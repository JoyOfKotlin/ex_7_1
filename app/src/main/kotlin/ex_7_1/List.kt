package ex_7_1

sealed class List<out A> {
    abstract fun isEmpty(): Boolean
    fun cons(a: @UnsafeVariance A): List<A> = Cons(a, this)
    fun setHead(a: @UnsafeVariance A): List<A> = when (this) {
        Nil -> throw IllegalStateException("setHead called on an empty list")
        is Cons -> tail.cons(a)
    }


    fun drop(n: Int): List<A> {
        tailrec fun drop(n: Int, list: List<A>): List<A> =
            if (n <= 0) list else when (list) {
                is Cons -> drop(n - 1, list.tail)
                is Nil -> list
            }
        return drop(n, this)
    }

    fun dropWhile(p: (A) -> Boolean): List<A> =
        dropWhile(this, p)

    fun concat(list: List<@UnsafeVariance A>): List<A> = concat(this, list)

    fun reverse(): List<A> =
        reverse(List.invoke(), this)

    fun reverseLeft(): List<A> = foldLeft(Nil as List<A>, { acc -> { acc.cons(it) } })

    fun init(): List<A> = reverse().drop(1).reverse()

    fun <B> foldRight(identity: B, f: (A) -> (B) -> B): B = foldRight(this, identity, f)

    fun <B> foldLeft(identity: B, f: (B) -> (A) -> B): B = foldLeft(identity, this, f)

    fun <B> map(f: (A) -> B): List<B> = foldLeft(Nil) { acc: List<B> -> { h: A -> Cons(f(h), acc) } }.reverse()

    fun <B> flatMap(f:(A)-> List<B>): List<B> = flatten(map(f))

    fun filter(p: (A) -> Boolean): List<A> = flatMap { a -> if (p(a)) List(a) else Nil }

    fun lengthRight(): Int = foldRight(0) { { it + 1 } }
    fun lengthLeft(): Int = foldLeft(0, { i -> { i + 1 } })


    internal object Nil : List<Nothing>() {
        override fun isEmpty() = true
        override fun toString(): String = "[NIL]"
    }

    internal class Cons<A>(internal val head: A, internal val tail: List<A>) : List<A>() {
        override fun isEmpty() = false
        override fun toString(): String = "[${toString("", this)}NIL]"
        private tailrec fun toString(acc: String, list: List<A>): String =
            when (list) {
                is Nil -> acc
                is Cons -> toString("$acc${list.head},", list.tail)
            }
    }

    companion object {
        operator fun <A> invoke(vararg az: A): List<A> =
            az.foldRight(Nil as List<A>) { a: A, list: List<A> -> Cons(a, list) }


        private tailrec fun <A> dropWhile(list: List<A>, p: (A) -> Boolean): List<A> =
            when (list) {
                Nil -> list
                is Cons -> if (p(list.head)) dropWhile(list.tail, p) else list
            }

        private fun <A> concat(list1: List<A>, list2: List<A>): List<A> =
            when (list1) {
                Nil -> list2
                is Cons -> concat(list1.tail, list2).cons(list1.head)
            }

        private fun <A> reverse(acc: List<A>, list: List<A>): List<A> =
            when (list) {
                Nil -> acc
                is Cons -> reverse(acc.cons(list.head), list.tail)
            }

        fun <A, B> foldRight(list: List<A>, identity: B, f: (A) -> (B) -> B): B =
            when (list) {
                Nil -> identity
                is Cons -> f(list.head)(foldRight(list.tail, identity, f))
            }


        tailrec fun <A, B> foldLeft(acc: B, list: List<A>, f: (B) -> (A) -> B): B =
            when (list) {
                List.Nil -> acc
                is List.Cons -> foldLeft(f(acc)(list.head), list.tail, f)
            }


        fun <A> concatViaFoldRight(list1: List<A>, list2: List<A>): List<A> =
            foldRight(list1, list2) { x -> { y -> Cons(x, y) } }

        fun <A> concatViaFoldLeft(list1: List<A>, list2: List<A>): List<A> =
            list1.reverse().foldLeft(list2) { x -> x::cons }

        fun <A> flatten(list: List<List<A>>): List<A> =
            list.foldRight(Nil) { x -> x::concat }


    }
}