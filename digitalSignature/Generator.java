package digitalSignature;

import java.math.BigInteger;
import java.util.Random;

public class Generator {	
	static void getPQ(BigIntWrapper p, BigIntWrapper q) {
		q.value = (new BigInteger("2")).pow(254);
		do {
			q.value = q.value.nextProbablePrime();
			p.value = q.value.multiply(q.value.add(BigInteger.ONE)).add(BigInteger.ONE);  //p = q*(q+1)+1;
		} while (!p.value.isProbablePrime(100)); // certainty is 1 - (1/2)**100
	}

	static BigInteger getA(BigInteger p, BigInteger q) {
		BigInteger f, d;
		do {
			d = new BigInteger(p.bitLength() - 1, new Random()).add(new BigInteger("2"));
			f = d.modPow(p.subtract(BigInteger.ONE).divide(q), p);
		} while (f.equals(BigInteger.ONE));
		return f;
	}
}
