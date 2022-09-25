package discord.util

import java.security.{MessageDigest, SecureRandom}
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import scala.util.Random

object Encryption {


  def encrypt(key: String, value: String): String = {
    val cipher: Cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
    cipher.init(Cipher.ENCRYPT_MODE, keyToSpec(key))
  }

  def keyToSpec(key: String): SecretKeySpec = {
    val keyBytes: Array[Byte] = (generateRandomRawSalt + key).getBytes("UTF-8")
    val sha: MessageDigest = MessageDigest.getInstance("SHA-1")
    val hashedKeyBytes: Array[Byte] = sha.digest(keyBytes)
    new SecretKeySpec(hashedKeyBytes, "AES")
  }

  def generateRandomRawSalt: String = Random.nextBytes(16).mkString

}
