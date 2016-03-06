package digitalSignature;

import java.math.BigInteger;
import java.util.*;
import java.io.*;

public class Hasher {
	final static BigInteger p = new BigInteger("1299709"); // prime number
	
	static BigInteger hash(FileInputStream sc, int bitLen) throws IOException {
		BigInteger currentPow = BigInteger.ONE;
		BigInteger res = BigInteger.ZERO;
		BigInteger modulo = (new BigInteger("2")).pow(bitLen);
		byte[] nxByte = new byte[1];
		
		while (sc.read(nxByte) == 1) {
			res = res.add(currentPow.multiply(new BigInteger(nxByte))).mod(modulo);
			currentPow = currentPow.multiply(p).mod(modulo);
		}
		return res;
	}
}
