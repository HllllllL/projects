import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

public class Server {

private static Hashtable<String, String> users = new Hashtable<String, String>();
private static Hashtable<String, String> lastIP = new Hashtable<String, String>();
private static ArrayList<String> CurrentlyOnline = new ArrayList<String>();
private static Hashtable<String, PrintWriter> pairs = new Hashtable<String, PrintWriter>();
private static Hashtable<String, Integer> loginHistory = new Hashtable<String, Integer>();
private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();
private static ArrayList<String[]> TimeTable = new ArrayList<String[]>();
private static Hashtable<String, LinkedList<String>> offLineMsg = new Hashtable<String, LinkedList<String>>();

private static final int LAST_HOUR = 3600; //in seconds
private static final int TIME_OUT = 1800;
private static final int BLOCK_TIME = 60;

private static void buildUserDB(String filePath) throws IOException{
	BufferedReader buffer = new BufferedReader(new FileReader(filePath));
	String line;
	while ((line = buffer.readLine()) != null){
		StringTokenizer token = new StringTokenizer(line, " ");
		String[] userinfo = new String[2];
		int counter = 0;
		while(token.hasMoreTokens()){
	    		userinfo[counter] = token.nextToken();      		
	    		counter++;
	    }
	    users.put(userinfo[0], userinfo[1]);
	    LinkedList<String> offLineMsgRec = new LinkedList<String>();
	    offLineMsg.put(userinfo[0], offLineMsgRec);
	}
	buffer.close();
}

public static void main(String[] args) throws Exception {
	String filePath = "/Users/haominglu/Desktop/user_pass.txt";
	buildUserDB(filePath);
	
	int PORT = Integer.parseInt(args[0]);
	
	ServerSocket listener = new ServerSocket(PORT);
	System.out.println("The chat server is running on port "+ PORT);
	try {
		while (true) {
			new Handler(listener.accept()).start();
		}
	} finally {
		listener.close();
	}
}
    
private static class Handler extends Thread {
        private String userName;
        private String psw;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String[] log;//log[0] is username, log[1] is lastCommandTime, log[2] is logoutTime.

	public Handler(Socket socket) {
            this.socket = socket;
	}

	public void run() {
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
			log = new String[3];
			int loginCounter=0;
			//user identity authentication
			while (true) {
				/* This is an extra feature.
				 * Ask if current user is the one last logged off from this IP address, 
				 * in this way we can save time for frequent users.
				*/
				String ip = socket.getInetAddress().toString();
				if (lastIP.containsKey(ip)){
					PrintWriter asker = new PrintWriter(socket.getOutputStream(), true);
					String lastUserFromThisIP = lastIP.get(ip);
					asker.println("Welcome back! Are you "+lastUserFromThisIP+"? (y/n)");
					if (in.readLine().equals("y")) userName = lastUserFromThisIP;
					else {
						lastIP.remove(ip);
						continue;
					}
				}else {
					out.println("Username: ");
					userName = in.readLine();
				}
                    
                    if(!loginHistory.containsKey(userName))loginHistory.put(userName, 0);
                    if (userName == null) {
                        return;
                    }
                    if (users.containsKey(userName)&& !CurrentlyOnline.contains(userName)) {
                    	out.println("Password: ");
                    	psw = in.readLine();
                    	if(checkPassword(userName,psw) ){
                    		CurrentlyOnline.add(userName);
                    		log[0] = userName;
                    		log[1] = currentTimeSeconds();
                    		lastIP.put(ip, userName);
                    		out.println("Login successfully!");
                    		break;
                    	}else if(loginHistory.get(userName)<2){
                    		loginCounter++;
                    		loginHistory.remove(userName);
                    		loginHistory.put(userName, loginCounter);
                    		out.println("Login failed! Try again. Username: ");
                    	}else{
                    		loginCounter=0;
                    		loginHistory.remove(userName);
                    		out.println("Failed 3 consecutive times, try later.");
                    		Thread.sleep(BLOCK_TIME*1000);
                    	}
                    }else if(CurrentlyOnline.contains(userName)){
                		out.println("User already online.Try another username: ");
                    }
                    else{
                    	out.println("No such user!");
                    }
			}

                //
			pairs.put(userName,out);
			writers.add(out);
			TimeTable.add(log);
			
			if(!offLineMsg.get(userName).isEmpty()){
				PrintWriter prOffLine = new PrintWriter(socket.getOutputStream(), true);
				prOffLine.println("You have unread messages:");
				prOffLine.println(offLineMsg.get(userName).toString());
				offLineMsg.get(userName).clear();
			}
	        
			while (true) {
				TimerTask timeout = new TimeOut();
				Timer timer = new Timer();
		        timer.schedule(timeout, TIME_OUT*1000);
				String input = in.readLine();
				PrintWriter feedback = new PrintWriter(socket.getOutputStream(), true);
				if (input == null) return;
				if (input != null) {
					timer.cancel();
					log[1] = currentTimeSeconds();
				}
				
				if (input.equals("logout")) {
					break;
				}
				
				else if (input.equals("whoelse")) {
					feedback.println(CurrentlyOnline.toString());
				}
				
				else if(input.startsWith("broadcast")) {
					for (PrintWriter writer : writers) {
						writer.println(userName + ": " + input.substring(10));
					}
				}
				
				else if(input.startsWith("message")){
					String[] message = input.split(" ",3);
					String ThisUser = message[1];
					PrintWriter toThisUser = pairs.get(ThisUser);
					if (CurrentlyOnline.contains(ThisUser)) 
						toThisUser.println(userName + ": " + message[2]);
					//keep the offline message in the hashtable, append it to the specified linkedlist
					else {
						LinkedList<String> offLineMsgRecorder = offLineMsg.get(ThisUser);
						offLineMsgRecorder.add(userName + ": " + message[2]);
					}
				} 
				
				else if (input.equals("wholasthr")) {
					ArrayList<String> lasthrUser = new ArrayList<String>();
					int currentTime =(int) (System.currentTimeMillis()/1000);//in seconds
					for(String[] userlog: TimeTable){
						if(userlog[2]==null || Integer.parseInt(userlog[2])+LAST_HOUR>=currentTime){
							if (!lasthrUser.contains(userlog[0]))lasthrUser.add(userlog[0]);
						}
					}
					feedback.println(lasthrUser.toString());
				}
				
				else{
					feedback.println("Unidentified Command, please check.");
				}
			}
			System.out.println(userName + " logged out");
			CurrentlyOnline.remove(userName);
			log[2] = currentTimeSeconds();
			writers.remove(out);
			pairs.remove(userName);
			socket.close();
			
		} catch (Exception e) {
                System.out.println(e);
			} 
		}
        
		private boolean checkPassword(String userName,String psw) {
			String password = users.get(userName);
			if(password.equals(psw)) return true;
			return false;
		}
		
		private String currentTimeSeconds(){
			int t =(int) (System.currentTimeMillis()/1000);
			String time = Integer.toString(t);
			return time;
		}

		private class TimeOut extends TimerTask {
		    public void run() {
		    	try {
					PrintWriter feedback = new PrintWriter(socket.getOutputStream(), true);
					feedback.println("timeout");
					System.out.println(userName + " timed out");
					CurrentlyOnline.remove(userName);
					log[2] = currentTimeSeconds();
					writers.remove(out);
					pairs.remove(userName);
					socket.close();
				} catch (IOException e) {
				}
		    }
		}
		//
    }
}