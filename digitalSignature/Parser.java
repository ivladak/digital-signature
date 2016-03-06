package digitalSignature;

import java.util.*;

public class Parser {
	public static void setArgs(String[] args, TreeMap<String, String> map) {
		for (String arg: args) {
			int i = 0;
			while (arg.charAt(i) == '-') {
				i++;
			}
			if (i == 2) {
				if (arg.substring(i).indexOf('=') >= 0) {
					String[] params = arg.substring(i).split("=");
					if (params.length >= 2) {
						map.put(params[0], params[1]);
					} else {
						System.out.println("Malformatted arguments. --" + params[0] + "=<null> assumed.");
						map.put(params[0], null);
					}
				} else {
					map.put(arg.substring(i), null);
				}
			}
		}
	}
}
