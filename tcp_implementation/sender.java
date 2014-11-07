import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.util.*;

public class sender { 
	private static final int MSS = 72;
	private static int RTT = 100;//ms
	private static byte[] currentSegment;
	private static InetAddress ip;
	private static int receiverPORT;
	private static int retransmitCounter;
	static DatagramSocket senderSock;
	
	public static byte[] affixHeader(header hd,byte[] data){
		byte[] output = new byte[MSS];
		byte[] h = hd.h;
		for(int i=0;i<20;i++){
			output[i]=h[i];
		}
		for(int i=20;i<MSS;i++){
			output[i]=data[i-20];
		}
		return output;
	}
	
	public static short checksum(byte[] arr){
		//arr.length == MSS
		short cs=0;
		for(int i=3;i<MSS;i+=4){
			short left = (short) (arr[i-3] +arr[i-2]*256);//(short) (((arr[i-3] & 0xFF)<<8) | (arr[i-2] & 0xFF));
			short right = (short) (arr[i-1] +arr[i]*256);//(short) (((arr[i-1] & 0xFF)<<8) | (arr[i] & 0xFF));
			if(i-3==16) left = 0;
			cs += left + right;
		}
		if(MSS%4==1) cs+=arr[MSS-1];
		if(MSS%4==2) cs+=arr[MSS-2]+arr[MSS-1]*256;
		if(MSS%4==3) cs+=arr[MSS-3]+arr[MSS-2]*256+arr[MSS-1];
		return cs;
	}
	
	public static void setChecksum(short checksum,byte[] b) throws IOException{
		byte[] arr = ToByteArrTwo(checksum);
		b[16] = arr[0];
		b[17] = arr[1];
	}
	
	public static byte[] ToByteArrTwo(short number) throws IOException {
		byte[] b = new byte[2];
		b[0] = (byte)(number & 0xff);
		b[1] = (byte)((number >> 8) & 0xff);
		return b;
	}
	public static int byteArrayToInt(byte[] b) {
	    int a =   b[3] & 0xFF | (b[2] & 0xFF) << 8 | (b[1] & 0xFF) << 16 | (b[0] & 0xFF) << 24;
	    return a;
	}
	
	public static byte[] getByteStream(String filePath) {
		String s;
		StringBuffer sb = new StringBuffer();
		try{
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			while ((s = br.readLine()) != null) {
				sb.append(s);
				sb.append("\n");
			}
			br.close();
		}catch(Exception e){
			System.out.println("file not found");
			System.exit(0);
		}
		String str = sb.toString();
		byte[] barr = str.getBytes();
		return barr;
	}
	
	public static void main( String args[] ) throws Exception {
		String file = args[0];//"/home/haominglu/test/test.txt";//args[0];
		String remote_IP =args[1];// "localhost";//args[1];
		int proxyInPORT = Integer.parseInt(args[2]);//5000;//args[2];
		int senderPORT =Integer.parseInt(args[3]);// 9988;//args[3];
		String logfile = args[4];//"/home/haominglu/Desktop/s_logfile.txt";//args[4];
		String window_size =args[5];// "1";//args[5];
		receiverPORT = proxyInPORT;//replace this to proxyInPORT; later...
		ip = InetAddress.getByName(remote_IP);
	    	PrintWriter logWriter = new PrintWriter(logfile, "UTF-8");
		logWriter.println("timestamp, source, destination, Sequence #, ACK #, FIN,estimate RTT");
	    
		senderSock = new DatagramSocket(senderPORT);
		byte arr[] = getByteStream(file);
		byte[] receiveData = new byte[20];
		DatagramPacket dpack = new DatagramPacket(arr, arr.length, ip, receiverPORT);
		DatagramPacket feedback = new DatagramPacket(receiveData, receiveData.length, ip, receiverPORT);
  
		int arrPointer = 0;
		int segmentCounter = 0;
		retransmitCounter=0;
		int FIN =0;
		int step = MSS-20;
		int totalSize = arr.length;
		System.out.println("Delivering...");
		while(arrPointer!=totalSize){
			byte[] packet = new byte[MSS-20];
			int seqNum = arrPointer;
			int ackNum = seqNum+step;
			for(int i=0;i<MSS-20;i++){
				packet[i]=arr[arrPointer];arrPointer++;
				if(arrPointer==totalSize) {
					FIN = 1;
					break;
				}
			}
			
			header hd =new header();
			hd.setFIN(FIN);
			hd.setSeqNum(seqNum);
			byte[] segment = affixHeader(hd,packet);
			
			segmentCounter++;
			currentSegment = segment;
			setChecksum(checksum(segment),segment);
			dpack = new DatagramPacket(segment, MSS, ip, receiverPORT);
			senderSock.send(dpack); // send the packet
			Date sendTime = new Date( );
			logWriter.println(new Date( ) +" "+ remote_IP+":"+proxyInPORT+" "+
                    dpack.getAddress().toString()+":"+senderPORT 
                    + " "+ seqNum+ " "+ackNum+" "+FIN);
			TimerTask timeout = new TimeOut();
			Timer timer = new Timer();
		    timer.scheduleAtFixedRate(timeout, RTT,RTT);
			
			senderSock.receive(feedback);
			timer.cancel(); // receive the packet 
			int receivedACK = byteArrayToInt(feedback.getData());
			Date receiveTime = new Date( );
			long estimatedRTT = receiveTime.getTime() - sendTime.getTime();
			logWriter.println(new Date( ) +" "+ remote_IP+":"+proxyInPORT+" "+
                    dpack.getAddress().toString()+":"+senderPORT 
                    + " "+ seqNum+ " "+receivedACK+" "+FIN+" "+estimatedRTT+"ms");
		}
		senderSock.close();
		logWriter.close();
		System.out.println("Delivery completed successfully");
		System.out.println("Total bytes sent = "+ totalSize);
		System.out.println("Segment sent = "+ segmentCounter);
		System.out.println("Segment retransmitted = "+ retransmitCounter);
	}
	
	private static class TimeOut extends TimerTask {
	    public void run() {
	    	try {
	    		DatagramPacket pack = new DatagramPacket(currentSegment, MSS, ip, receiverPORT);
				senderSock.send(pack);
				retransmitCounter++;
			} catch (IOException e) {
			}
	    }
	}
} 
