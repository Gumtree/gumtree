package org.gumtree.security;

import junit.framework.Assert;

import org.junit.Test;

public class EncryptionUtilsTest {

	@Test
	public void testEncryption() throws Exception {
		String input = "This is a long string";
		String encryptedString = EncryptionUtils.encryptBase64(input);
		Assert.assertNotSame(input, encryptedString);
		String decryptedString = EncryptionUtils.decryptBase64(encryptedString);
		Assert.assertEquals(input, decryptedString);
	}

}
