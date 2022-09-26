package discord.util

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class EncryptionDecryptionTest extends AnyFunSuite with Matchers {

  test("return the same value when encrypting and decrypting the same value") {

    val key = "encryption-key"
    val value = "test-encryption-and-decryption"

    val encrypted = EncryptionDecryption.encrypt(key, value)
    val decrypted = EncryptionDecryption.decrypt(key, encrypted)

    decrypted shouldBe value
  }
}
