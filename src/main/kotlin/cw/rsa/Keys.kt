package cw.rsa

import java.math.BigInteger

data class RsaKey(val mod: BigInteger, val exp: Int)

data class Keys(
    val private: RsaKey,
    val public: RsaKey
)
