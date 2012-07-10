package org.gumtree.service.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.gumtree.service.cli.support.CommandLineOptions;
import org.junit.Test;

public class CommandLineOptionsTest {

	@Test
	public void testCommandLineOptionsParse() {
		String[] args = new String[] { "-version", "3", "-startNow",
				"-service", "me" };
		ICommandLineOptions options = new CommandLineOptions(args);
		runStandardTests(options);
	}

	@Test
	public void testInvolveIllegalArguments() {
		String[] args = new String[] { "illegalArgument", "-version", "3",
				"-startNow", "-", "-service", "me", "you", "-h", "-arg", "1", "2",
				"-",  "3", "-p" };
		ICommandLineOptions options = new CommandLineOptions(args);
		// runs standard tests
		runStandardTests(options);
		// runs extra tests
		// tests options.hasOption(String)
		assertTrue(options.hasOption("h"));
		assertTrue(options.hasOption("arg"));
		assertTrue(options.hasOption("p"));
		assertFalse(options.hasOption("illegalArgument"));
		assertFalse(options.hasOption("you"));
		assertFalse(options.hasOption("1"));
		assertFalse(options.hasOption("2"));
		// tests options.hasOptionValue(String)
		assertTrue(options.hasOptionValue("arg"));
		assertFalse(options.hasOptionValue("illegalArgument"));
		assertFalse(options.hasOptionValue("h"));
		assertFalse(options.hasOptionValue("p"));
		assertFalse(options.hasOptionValue("1"));
		assertFalse(options.hasOptionValue("2"));
		// tests options.getOptionValue(String)
		String arg = options.getOptionValue("arg");
		assertEquals("1", arg);
		arg = options.getOptionValue("2");
		assertNull(arg);
		arg = options.getOptionValue("h");
		assertNull(arg);
		arg = options.getOptionValue("h");
		assertNull(arg);
		arg = options.getOptionValue("p");
		assertNull(arg);
	}
	
	private void runStandardTests(ICommandLineOptions options) {
		// tests options.hasOption(String)
		assertTrue(options.hasOption("version"));
		assertTrue(options.hasOption("startNow"));
		assertTrue(options.hasOption("service"));
		assertFalse(options.hasOption("help"));
		assertFalse(options.hasOption("3"));
		assertFalse(options.hasOption("me"));
		// tests options.hasOptionValue(String)
		assertTrue(options.hasOptionValue("version"));
		assertTrue(options.hasOptionValue("service"));
		assertFalse(options.hasOptionValue("startNow"));
		assertFalse(options.hasOptionValue("help"));
		// tests options.getOptionValue(String)
		String arg = options.getOptionValue("version");
		assertEquals("3", arg);
		arg = options.getOptionValue("service");
		assertEquals("me", arg);
		arg = options.getOptionValue("startNow");
		assertNull(arg);
		arg = options.getOptionValue("help");
		assertNull(arg);
	}
}
