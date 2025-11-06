package cw.rsa

import java.util.*

internal fun gcd(a: Int, b: Int): Int {
    return if (a == 0) b else gcd(b % a, a)
}

internal fun sieveOfEratosthenes(n: Int): List<Int> {
    val prime = BooleanArray(n + 1) { true }
    var p = 2
    while (p * p <= n) {
        if (prime[p]) {
            var i = p * 2
            while (i <= n) {
                prime[i] = false
                i += p
            }
        }
        p++
    }
    val primeNumbers = LinkedList<Int>()
    for (i in 2..n)
        if (prime[i])
            primeNumbers.add(i)
    return primeNumbers
}

fun modInverse(e: Long, phi: Long): Long {
    var t = 0L
    var newT = 1L
    var r = phi
    var newR = e

    while (newR != 0L) {
        val quotient = r / newR
        val tempT = t
        t = newT
        newT = tempT - quotient * newT

        val tempR = r
        r = newR
        newR = tempR - quotient * newR
    }

    if (r > 1) throw IllegalArgumentException("e is not invertible mod phi")
    if (t < 0) t += phi
    return t
}
