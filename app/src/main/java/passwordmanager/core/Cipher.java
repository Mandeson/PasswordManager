package passwordmanager.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.AEADBadTagException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Cipher {
	private static int PASSWORD_HASH_ITERATIONS = 700000;
	private static int SALT_BYTES = 16;
	private static int IV_BYTES = 12;
	
	public static class WrongPasswordException extends RuntimeException {
		private static final long serialVersionUID = 5480521040891146223L;
	}
	
	public static class Encryptor {
		byte[] encrypted;
		
		public Encryptor(byte[] data, String password) {
			encrypt(data, password);
		}
		
		public byte[] getEncrypted() {
			return encrypted;
		}
		
		private void encrypt(byte[] data, String password) {
			byte[] salt = new byte[SALT_BYTES];
			SecureRandom secureRandom = new SecureRandom();
			secureRandom.nextBytes(salt);
			try {
				SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
				
				KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, PASSWORD_HASH_ITERATIONS, 256);
				byte[] key = factory.generateSecret(keySpec).getEncoded();
				SecretKey secretKey = new SecretKeySpec(key, "AES");
				
				byte[] iv = new byte[IV_BYTES];
		        secureRandom.nextBytes(iv);
				GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, iv);
				
				javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding");
				cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec);
				byte[] ciphertext = cipher.doFinal(data);
				
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				output.write(salt);
				output.write(iv);
				output.write(ciphertext);
				encrypted = output.toByteArray();
			} catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException
					| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException
					| IOException e) {
				throw new RuntimeException("Encryption error", e);
			}
		}
	}
	
	public static class Decryptor {
		private byte[] decrypted = null;
		
		public Decryptor(byte[] encrypted, String password) {
			decrypt(encrypted, password);
		}
		
		public byte[] getDecrypted() {
			return decrypted;
		}
		
		private void decrypt(byte[] encrypted, String password) {
			byte[] salt = new byte[SALT_BYTES];
			byte[] iv = new byte[IV_BYTES];
			ByteArrayInputStream input = new ByteArrayInputStream(encrypted);
			try {
				input.read(salt);
				input.read(iv);
				byte[] ciphertext = input.readAllBytes();
				
				SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
				
				KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, PASSWORD_HASH_ITERATIONS, 256);
				byte[] key = factory.generateSecret(keySpec).getEncoded();
				SecretKey secretKey = new SecretKeySpec(key, "AES");

				GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, iv);
				
				javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding");
				cipher.init(javax.crypto.Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);
				decrypted = cipher.doFinal(ciphertext);
			} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException
					| InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException
					| BadPaddingException e) {
				if (e instanceof AEADBadTagException) { // If the error was caused by wrong password
					throw new WrongPasswordException();
				} else {
					throw new RuntimeException("Decryption error", e);
				}
			}
		}
	}
}
