This software generates and checks electronic signatures for arbitrary files.
The implementation is based on the Russian ГОСТ (GOST) Р 34-10.94 standard.
However it has simplifications and uses a custom hash funciton.

This is a school project and has only educational value. **Not to be used in productoin.**

To be able to run the program, first you will need to build it. Simply run
	./build.sh

This makes a jar file. Now the program can be run with
	java -jar  digitalSignature.jar	
