README

a&e. This is a package of well functioning Java program, enabling all the required features and 2 extra features. The first extra feature is off-line message. If one user receives private message when he/she is not online, those message will pops out right after he/she login. The second extra feature is recognizing username by the IP. For example, if the user “facebook” logged in to the server in some IP, when the next user logs in from this IP, he/she will be asked “Are you Facebook?”, if yes, you only need to type in password to verify your identity and no need to type username again.


b. Development environment: Java 1.6 in Mac OS X.

c&d. Before compiling, you need to modify the file path of the user_pass.txt (in Server.java in line 51) to its current location. Also you need to know the IP of the machine running as server. If server and client are in the same machine, just use:
>java Client localhost 9000
to start the client.

	Sample commands:
	1.Server side
	>javac Server.java
	>java Server 9000
	>The chat server is running on port 9000

	2.Client side
	>javac Client.java
	>java Client 160.39.58.231 9000
	>Username:
	>facebook
	>Password:
	> wastingtime
	>Login successfully!
