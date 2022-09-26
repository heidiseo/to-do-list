package discord.util

import org.apache.commons.codec.binary.Base64

import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.{IvParameterSpec, SecretKeySpec}
import scala.util.Random

object EncryptionDecryption {

  val ivParameterSpec = new IvParameterSpec(Random.nextBytes(16))

  val salt: String = "random-salt-for-encryption-and-decryption"

  def encrypt(key: String, value: String): String = {
    val cipher: Cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    cipher.init(Cipher.ENCRYPT_MODE, keyToSpec(key), ivParameterSpec)
    Base64.encodeBase64String(cipher.doFinal(value.getBytes("UTF-8")))
  }

  def decrypt(key: String, encryptedValue: String): String = {
    val cipher: Cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    cipher.init(Cipher.DECRYPT_MODE, keyToSpec(key), ivParameterSpec)
    new String(cipher.doFinal(Base64.decodeBase64(encryptedValue)))
  }

  def keyToSpec(key: String): SecretKeySpec = {
    val keyBytes: Array[Byte] = (salt + key).getBytes("UTF-8")
    val sha: MessageDigest = MessageDigest.getInstance("SHA-1")
    val hashedKeyBytes: Array[Byte] = sha.digest(keyBytes)
    val sizedHashedKeyBytes = java.util.Arrays.copyOf(hashedKeyBytes, 16)
    new SecretKeySpec(sizedHashedKeyBytes, "AES")
  }
}
