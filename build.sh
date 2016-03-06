#!/bin/sh
cd digitalSignature
javac *.java
cd ..
jar cfm digitalSignature.jar META-INF/MANIFEST.MF digitalSignature
