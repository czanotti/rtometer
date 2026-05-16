package com.rtometer.data.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.security.KeyStore;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class KeystoreKeyProvider {

    private static final String KEY_ALIAS = "rtometer_db_key";
    private static final String PREFS_NAME = "rtometer_db_prefs";
    private static final String PREFS_ENC_PASSPHRASE = "enc_passphrase";
    private static final String PREFS_IV = "enc_passphrase_iv";
    private static final int PASSPHRASE_BYTES = 32;

    private final Context context;

    public KeystoreKeyProvider(Context context) {
        this.context = context.getApplicationContext();
    }

    public byte[] getOrCreatePassphrase() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String encPassphrase = prefs.getString(PREFS_ENC_PASSPHRASE, null);
        String ivStr = prefs.getString(PREFS_IV, null);
        SecretKey key = getOrCreateKeystoreKey();

        if (encPassphrase == null || ivStr == null) {
            byte[] passphrase = new byte[PASSPHRASE_BYTES];
            new SecureRandom().nextBytes(passphrase);
            try {
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                cipher.init(Cipher.ENCRYPT_MODE, key);
                byte[] iv = cipher.getIV();
                byte[] encrypted = cipher.doFinal(passphrase);
                prefs.edit()
                        .putString(PREFS_ENC_PASSPHRASE, Base64.encodeToString(encrypted, Base64.NO_WRAP))
                        .putString(PREFS_IV, Base64.encodeToString(iv, Base64.NO_WRAP))
                        .apply();
                return passphrase;
            } catch (Exception e) {
                throw new RuntimeException("Failed to encrypt database passphrase", e);
            }
        } else {
            try {
                byte[] iv = Base64.decode(ivStr, Base64.NO_WRAP);
                byte[] encrypted = Base64.decode(encPassphrase, Base64.NO_WRAP);
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
                return cipher.doFinal(encrypted);
            } catch (Exception e) {
                throw new RuntimeException("Failed to decrypt database passphrase", e);
            }
        }
    }

    private SecretKey getOrCreateKeystoreKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            if (keyStore.containsAlias(KEY_ALIAS)) {
                return ((KeyStore.SecretKeyEntry) keyStore.getEntry(KEY_ALIAS, null)).getSecretKey();
            }
            KeyGenerator keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            keyGenerator.init(
                    new KeyGenParameterSpec.Builder(KEY_ALIAS,
                            KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                            .setKeySize(256)
                            .setUserAuthenticationRequired(false)
                            .build());
            return keyGenerator.generateKey();
        } catch (Exception e) {
            throw new RuntimeException("Failed to access Android Keystore", e);
        }
    }
}
