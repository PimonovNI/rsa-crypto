package cw.auth

import ch.obermuhlner.math.big.BigDecimalMath
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

object AuthFunction {
    private val mc = MathContext(20, RoundingMode.HALF_UP)

    fun calculate(x: BigDecimal, a: Int): BigDecimal {
        val exponent = BigDecimal("0.85")
        return BigDecimalMath.pow(x, exponent, mc).multiply(BigDecimal(a.toString()), mc)
    }
}
