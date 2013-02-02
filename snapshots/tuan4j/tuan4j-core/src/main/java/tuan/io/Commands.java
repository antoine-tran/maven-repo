package tuan.io;

import java.util.HashMap;
import java.util.Map;

/** Contains a set of convenience methods for handling program commands 
 * command parameters */
public class Commands {

	/**
	 * This class employs the idea from the method 
	 * {@link edu.stanford.nlp.util.StringUtils.parseCommandLineArguments},
	 * with the capacity to print error messages.
	 * 
	 * Parses command line arguments into a Map. Arguments of the form
	 * "-flag1 arg1 -flag2 -flag3 arg3" will be parsed so that the flag is
	 * a key in the Map (including the hyphen) and the optional argument will
	 * be its value (if present).
	 *
	 * @return A Properties object to store parameters
	 */
	public static Map<String, String> parseCommandLineArguments(String... args) {
		Map<String, String> result = new HashMap<String, String>();
		for (int i = 0; i < args.length; i++) {
			String key = args[i];
			if (key.charAt(0) == '-') {
				if (i + 1 < args.length) {
					String value = args[i + 1];
					if (value.charAt(0) != '-') {
						result.put(key, value);
						i++;
					} else {
						result.put(key, null);
					}
				} else result.put(key, null);
			}
		}
		return result;
	}
}
