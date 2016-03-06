package digitalSignature;

import java.io.*;
import java.math.BigInteger;
import java.util.*;

public class Main {
	final static int argFile = 0;
	static TreeMap<String, String> argSet;
	final static String TERM = "Terminated.";
	final static int bits_256 = 256 / 8;
	
	public static void main(String[] args) {
		argSet = new TreeMap<String, String>();
		Parser.setArgs(args, argSet);
		
		if (argSet.containsKey("help") || args.length == 0) {
			printHelp();
			return;
		}
		
		if (!argSet.containsKey("check")) {
			System.out.println("Signature is now being generated.");
			runSign(args[argFile]);
			System.out.println("Done.");
		} else {
			System.out.println("Checking whether the signature is correct");
			if (runCheck(args[argFile])) {
				System.out.println("Signature validated successfully!");
			} else {
				System.out.println("Signature invalid");
			}
		}
	}

	static boolean runCheck(String inputFilename) {
		FileInputStream in = null;
		try {
			in = new FileInputStream(new File(getSgFilename(inputFilename)));
		} catch (FileNotFoundException e) {
			System.out.println("file " + (new File(getSgFilename(inputFilename))).getAbsolutePath() + " not found");
			System.out.println(TERM);
			return false;			
		}
		byte[] b = new byte[bits_256];
		BigInteger r_ = null, s = null;
		try {
			in.read(b);
			r_ = new BigInteger(b);
			in.read(b);
			s = new BigInteger(b);
		} catch (IOException E) {
			System.out.println("IOException occured.");
			System.out.println(TERM);
			return false;
		} 
		BigIntWrapper pw = new BigIntWrapper(), qw = new BigIntWrapper(), aw = new BigIntWrapper(), yw = new BigIntWrapper();
		try {
			loadPQAY(pw, qw, aw, yw);
		} catch (BadInfoException e) {
			System.out.println(TERM);
			return false;
		}
		BigInteger p = pw.value, q = qw.value, a = aw.value, y = yw.value;
		if (s.compareTo(q) >= 0 || s.compareTo(BigInteger.ZERO) <= 0 || r_.compareTo(q) >= 0 || r_.compareTo(BigInteger.ZERO) <= 0) {			
			return false;
		}
		BigInteger h = readInputGetHash(inputFilename, q);
		if (h == null) {
			System.out.println(TERM);
			return false;			
		}
		BigInteger v = h.modPow(q.subtract(new BigInteger("2")), q);
		BigInteger z1 = (s.multiply(v)).mod(q);
		BigInteger z2 = ((q.subtract(r_)).multiply(v)).mod(q);
		BigInteger u = (((a.modPow(z1, p)).multiply(y.modPow(z2, p))).mod(p)).mod(q);

		return r_.equals(u);
	}
	
	static void loadPQAY(BigIntWrapper p, BigIntWrapper q, BigIntWrapper a, BigIntWrapper y) throws BadInfoException {
		String filename = argSet.get("check");
		if (filename == null) {
			filename = ".info";
		}
		File file = new File(filename);
		if (!file.exists()) {
			System.out.println("file " + file.getAbsolutePath() + " not found");
			throw new BadInfoException();
		}
		Scanner sc = null;
		try {
			sc = new Scanner(file);
			p.value = new BigInteger(sc.next());
			q.value = new BigInteger(sc.next());
			a.value = new BigInteger(sc.next());
			y.value = new BigInteger(sc.next());			
			
		} catch (Exception e) {			
			throw new BadInfoException();
		}
	}
	
	static void storePQAY(BigInteger p, BigInteger q, BigInteger a, BigInteger y) {
		try {
			String filename = argSet.get("check");
			if (filename == null) {
				filename = ".info";
			}
			File file = new File(filename);
			file.createNewFile();
			Writer writer = new BufferedWriter(new FileWriter(file));
			writer.write(p + "\n" + q + "\n" + a + "\n" + y + "\n");
			writer.close();
		} catch (Exception e) {
			System.out.println("Parameter store failed.");
		}
	}
	
	static BigInteger readInputGetHash(String inputFilename, BigInteger q) {
		FileInputStream sc = null; // make compiler happy
		try {
			sc = new FileInputStream(new File(inputFilename));
		} catch (FileNotFoundException e) {
			System.out.println("file " + (new File(inputFilename)).getAbsolutePath() + " not found");
			System.out.println(TERM);
			return null;
		}
		BigInteger res;
		try {
			res = Hasher.hash(sc, 256);
		} catch (IOException e) {
			System.out.println("IO exception occured. Please retry.");
			return null;
		}		
		if (res.mod(q).equals(BigInteger.ZERO)) {
			res = BigInteger.ONE;
		}
		return res;
	}
	
	static void runSign(String inputFilename) {		
		BigIntWrapper pw = new BigIntWrapper(), qw = new BigIntWrapper();
		Generator.getPQ(pw, qw);
		BigInteger p = pw.value, q = qw.value; 
		BigInteger a = Generator.getA(p, q);
		BigInteger x;
		if (!argSet.containsKey("private_key")) {
			x = new BigInteger(q.bitLength() - 1, new Random()).add(BigInteger.ONE); // private key. x < q
		} else {
			try {
				x = new BigInteger(argSet.get("private_key"));
			} catch (Exception  e) {
				System.out.println("Private key must be a decimal number.");
				System.out.println(TERM);
				return;
			}
			if (x.compareTo(q) >= 0 || x.compareTo(BigInteger.ZERO) <= 0) {
				System.out.println("Entered private key is not valid for generated q: x should be positive and less than " + q);
				System.out.println(TERM);
				return;
			}
		}
		BigInteger y = a.modPow(x, p); // public key
		storePQAY(p, q, a, y);
		BigInteger h = readInputGetHash(inputFilename, q);
		if (h == null) {
			System.out.println(TERM);
			return;
		}
		if (argSet.containsKey("hash")) {
			System.out.println("hash = " + h);
			return;
		}
		
		BigInteger k, r, r_, s;
		do {
			do {
				k = new BigInteger(q.bitLength() - 1, new Random()).add(BigInteger.ONE);
				r = a.modPow(k, p);
				r_ = r.mod(q);			
			} while (r_.equals(BigInteger.ZERO));
			s = x.multiply(r_).add(k.multiply(h)).mod(q); // (xr' + kh) mod q
		} while (s.equals(BigInteger.ZERO));
		
		String sgFilename = getSgFilename(inputFilename);
		File sgFile = new File(sgFilename);
		try {
			sgFile.createNewFile();
			FileOutputStream out = new FileOutputStream(sgFile);
			out.write(addLeadingZeroes256(r_.toByteArray()));
			out.write(addLeadingZeroes256(s.toByteArray()));
		} catch (Exception e) {
			System.out.println("Something went wrong. Check whether the current dir is write accessable.");
			System.out.println(TERM);
		}		
	}
	
	static byte[] addLeadingZeroes256(byte[] b) throws Not256BitNumberException {
		if (b.length > bits_256) {
			throw new Not256BitNumberException();
		}
		if (b.length == bits_256) {
			return b;
		}
		byte[] b_ = new byte[bits_256];
		for (int i = 0; i < bits_256 - b.length; i++) {
			b_[i] = 0;
		}
		for (int i = bits_256 - b.length; i < bits_256; i++) {
			b_[i] = b[i - (bits_256 - b.length)];
		}
		return b_;
	}
	
	static String getSgFilename(String filename) {
		return filename + ".sg";
	}
	
	static void printHelp() {
		String[] list = {
				"Implementation of a modified ГОСТ Р 34-10.94 digital signature algorithm.",
				"Usage:",
				"<filename> [options], where <filename> is a path to the file you want to form a signature for",
				"Signature is stored to and loaded from <filename>.sg",
				"Options:",
				"\t--hash only calculate hash",
				"\t--help show this help",
				"\t--check[=<filename>] check whether the signature for the given file is correct. If filename is specified, it looks there for parameters such as public key. If it is ommitted, .info is used by default.",
				"\t--private_key=<key> set private key manually"
		};
		for (String s: list) {
			System.out.println(s);
		}
	}
}

class Not256BitNumberException extends Exception {}
class BadInfoException extends Exception {} 