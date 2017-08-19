Steganography Project

In this folder should be a steg.jar, R2.png and secretFile.txt.
You can utilize these to test the program.

My program only works with png images.

To run the program:
	Open up the command line
	Move to the directory that steg.jar is in.
	run:
		java -jar steg.jar -e <original image name> <modified image name> [input ASCII text file name]
	to encode an original image with a ASCII text file
	run:
		java -jar steg.jar -d <modified image name> [output ASCII text file name]
	to decode the modified image and put the ASCII text into an ouput file

	An Example of this that you should be able to run now if you are running it in this same file

	ex.
		java -jar steg.jar -e R2.png modified.png secretFile.txt
		java -jar steg.jar -d modified.png output.txt

Thanks!
Noah Bragg