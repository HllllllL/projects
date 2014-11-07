import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client implements Runnable {
    BufferedReader in1,in2;
    PrintWriter out;
    
    //two Threads, read and send spontaneously
    Thread t1,t2;
    
    String inMessage,outMessage;
    String serverAddress;
    static Socket socket;
 
public Client(int PORT, String IP) throws UnknownHostException, IOException {
	t1 = new Thread(this);
	t2 = new Thread(this);
	serverAddress = IP;
	socket = new Socket(serverAddress, PORT);
	t1.start();;
	t2.start();
}
    
public void run(){
	try {
		while(true){
			if (Thread.currentThread() == t2) {
				//read command line and send it to the socket
				in1 = new BufferedReader(new InputStreamReader(System.in));
				out = new PrintWriter(socket.getOutputStream(), true);
				outMessage = in1.readLine();
				out.println(outMessage);
				if(outMessage.equals("logout")) {
					socket.close();
					break;
				}
			} else {
				//read message from the socket and print it
				in2 = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				inMessage = in2.readLine();
				if(inMessage.equals("timeout")) {
					System.out.println("You are out of time, please re-login.");
					socket.close();
					break;
				}else{
					System.out.println(inMessage);
				}
			}
		}
	} catch (Exception e) {
	}
}

public static void main(String[] args) throws Exception {
	String serverAddress = args[0];
	int port = Integer.parseInt(args[1]);
	new Client(port,serverAddress);
	}
}