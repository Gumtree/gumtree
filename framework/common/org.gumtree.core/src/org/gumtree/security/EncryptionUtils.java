/*******************************************************************************
 * Copyright (c) 2012 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bragg Institute - initial API and implementation
 ******************************************************************************/

package org.gumtree.security;

import java.io.FileInputStream;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// See: http://stackoverflow.com/questions/1205135/how-to-encrypt-string-in-java
public final class EncryptionUtils {
	
	private static final String EXTRA_PROPERTIES_PATH = "gumtree.core.properties";
	private static final String CRYPTO_KEY = "gumtree.security.cryptoKey";
	private static final String CRYPTO_IV = "gumtree.security.cryptoIV";
	
	private static Logger logger = LoggerFactory.getLogger(EncryptionUtils.class);
	
	private static Properties properties = new Properties();
	
	static {
		try {
			String path = System.getProperty(EXTRA_PROPERTIES_PATH);
			properties.load(new FileInputStream(path));
//			System.setProperties(properties);
		} catch (Exception e) {
			logger.error("failed to load extra properties", e);
		}
	}

	public static String encryptBase64(String input) throws Exception {
//		return encryptBase64(input, CoreProperties.CRYPTO_KEY.getValue(),
//				CoreProperties.CRYPTO_IV.getValue());
		return encryptBase64(input, properties.getProperty(CRYPTO_KEY),
				properties.getProperty(CRYPTO_IV));
	}

	public static String encryptBase64(String input, String key, String iv)
			throws Exception {
		byte[] inputBytes = input.getBytes();
		SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "DES");
		IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());
		Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
		byte[] encrypted = new byte[cipher.getOutputSize(inputBytes.length)];
		int enc_len = cipher.update(inputBytes, 0, inputBytes.length,
				encrypted, 0);
		enc_len += cipher.doFinal(encrypted, enc_len);
		return new String(Base64.encodeBase64(encrypted));
	}

	public static String decryptBase64(String encrytedBase64String)
			throws Exception {
		return decryptBase64(encrytedBase64String,
				properties.getProperty(CRYPTO_KEY),
				properties.getProperty(CRYPTO_IV));
	}
	
	public static String decryptProperty(String propertyName) throws Exception {
		String value = properties.getProperty(propertyName);
		if (value != null) {
			return decryptBase64(value);
		} else {
			throw new Exception("property not found");
		}
	}

	public static String decryptBase64(String encrytedBase64String, String key,
			String iv) throws Exception {
		byte[] data = Base64.decodeBase64(encrytedBase64String.getBytes());
		SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "DES");
		IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());
		Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
		byte[] decrypted = new byte[cipher.getOutputSize(data.length)];
		int dec_len = cipher.update(data, 0, data.length, decrypted, 0);
		dec_len += cipher.doFinal(decrypted, dec_len);
		return new String(decrypted).trim();
	}

	private EncryptionUtils() {
		super();
	}

}
