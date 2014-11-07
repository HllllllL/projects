README

This is a simple implementation of TCP protocol over UDP. I used a 20-byte header to achieve reliable data transfer. In the header, sequence number is used to solve out—of-order, missing segment issue; checksum is used to deal with data corruption. The window size should be fixed to 1.

You can run the receiver program like:
java receiver.java
java receiver /home/haominglu/Desktop/file.txt 4119 localhost 9988 /home/haominglu/Desktop/r_logfile.txt

And run the sender program like:
javac header.java
javac sender.java
java sender /home/haominglu/test/test.txt localhost 5000 9988 /home/haominglu/Desktop/s_logfile.txt 1
The command line arguments are set exactly as the required order.

test.txt is the sample file I used to test the program. The content is from the IMDb top 250.

