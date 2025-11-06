package cw.rsa

import java.math.BigInteger
import java.security.SecureRandom
import java.util.*

object Rsa {

    private val primes = sieveOfEratosthenes(500)
    private val random = SecureRandom.getInstanceStrong()

    fun generateKeys(): Keys {
        val random1 = random.nextInt(primes.size)
        val random2 = if (random.nextBoolean())
            random1 - 1 - random.nextInt(random1)
        else
            random1 + 1 + random.nextInt(primes.size - random1 - 1)

        val q = primes[random1]
        val p = primes[random2]

        val n = q * p
        val phi = (p - 1) * (q - 1)

        var e = 2
        for (i in 2 until n) {
            e = i
            if (gcd(e, phi) == 1)
                break
        }

        val d = modInverse(e.toLong(), phi.toLong())

        return Keys(
            RsaKey(BigInteger.valueOf(n.toLong()), d.toInt()),
            RsaKey(BigInteger.valueOf(n.toLong()), e)
        )
    }

    fun encrypt(ìn: String, public: RsaKey): String = encrypt(ìn.encodeToByteArray(), public)

    fun encrypt(`in`: ByteArray, public: RsaKey): String {
        val out = mutableListOf<BigInteger>()
        for (byte in `in`)
            out += BigInteger.valueOf(byte.toLong()).pow(public.exp).mod(public.mod)
        return Base64.getEncoder().encodeToString(out.joinToString(" ").encodeToByteArray())
    }

    fun decrypt(`in`: String, private: RsaKey): String {
        val `in` = String(Base64.getDecoder().decode(`in`)).split(" ").map { BigInteger.valueOf(it.toLong()) }
        val out = ByteArray(`in`.size)
        for ((index, bigInteger) in `in`.withIndex())
            out[index] = bigInteger.pow(private.exp).mod(private.mod).toByte()
        return String(out)
    }
}
