http://pydev.org/manual_101_install.html#pydev-certificate

PyDev Certificate

PyDev is built with a self-signed certificate, which means that when installed a dialog will be opened to ask if you trust the certificate (which should be OK for most users).

Now, if you don't want that dialog to appear, it's possible to import the certificate before starting the installation process (this is actually a requirement for those that want to install PyDev from the command line because of a bug in the Eclipse p2 director).

The first step for that is downloading the PyDev certificate.

The second step is discovering the java being used in Eclipse: go to Help > About > Installation details and look for 'java.home'

Then to actually import it, in the command line, go to the Eclipse 'java.home' directory and execute

bin\keytool.exe -import -file pydev_certificate.cer -keystore lib\security\cacerts
Note that if you never did anything here, your password when requested should be changeit

Reference: http://download.oracle.com/javase/1.4.2/docs/tooldocs/solaris/keytool.html#cacerts

