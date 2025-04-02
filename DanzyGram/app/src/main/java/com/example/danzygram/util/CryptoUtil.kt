package com.example.danzygram.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object CryptoUtil {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val IV_LENGTH = 12
    private const val TAG_LENGTH = 128

    fun generateSecretKey(alias: String) {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )

        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }

    fun getSecretKey(alias: String): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        return keyStore.getKey(alias, null) as SecretKey
    }

    fun encrypt(data: String, alias: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(alias))
        val iv = cipher.iv
        val encrypted = cipher.doFinal(data.toByteArray(Charsets.UTF_8))

        // Combine IV and encrypted data
        val combined = ByteArray(iv.size + encrypted.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encrypted, 0, combined, iv.size, encrypted.size)

        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    fun decrypt(encryptedData: String, alias: String): String {
        val decoded = Base64.decode(encryptedData, Base64.DEFAULT)

        // Extract IV
        val iv = ByteArray(IV_LENGTH)
        System.arraycopy(decoded, 0, iv, 0, IV_LENGTH)

        // Extract encrypted data
        val encrypted = ByteArray(decoded.size - IV_LENGTH)
        System.arraycopy(decoded, IV_LENGTH, encrypted, 0, encrypted.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(alias), spec)

        return String(cipher.doFinal(encrypted), Charsets.UTF_8)
    }

    fun deleteKey(alias: String) {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        keyStore.deleteEntry(alias)
    }

    fun sha256(input: String): String {
        val bytes = input.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    fun md5(input: String): String {
        val bytes = input.toByteArray()
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    fun generateRandomKey(length: Int = 32): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    fun obfuscateEmail(email: String): String {
        val parts = email.split("@")
        if (parts.size != 2) return email
        
        val name = parts[0]
        val domain = parts[1]
        
        val obfuscatedName = when {
            name.length <= 2 -> "*".repeat(name.length)
            name.length <= 4 -> name.take(1) + "*".repeat(name.length - 1)
            else -> name.take(2) + "*".repeat(name.length - 2)
        }
        
        return "$obfuscatedName@$domain"
    }

    fun obfuscatePhone(phone: String): String {
        val cleaned = phone.replace(Regex("[^0-9]"), "")
        return when {
            cleaned.length <= 4 -> "*".repeat(cleaned.length)
            cleaned.length <= 8 -> "*".repeat(cleaned.length - 4) + cleaned.takeLast(4)
            else -> "*".repeat(cleaned.length - 6) + cleaned.takeLast(6)
        }
    }

    fun isKeyStoreContainAlias(alias: String): Boolean {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        return keyStore.containsAlias(alias)
    }

    fun getAllAliases(): List<String> {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        return keyStore.aliases().toList()
    }

    fun clearKeyStore() {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        keyStore.aliases().toList().forEach { alias ->
            keyStore.deleteEntry(alias)
        }
    }

    fun isValidHash(hash: String): Boolean {
        return hash.matches(Regex("^[a-fA-F0-9]+$"))
    }

    fun compareHashes(hash1: String, hash2: String): Boolean {
        return MessageDigest.isEqual(
            hash1.toByteArray(Charsets.UTF_8),
            hash2.toByteArray(Charsets.UTF_8)
        )
    }
}