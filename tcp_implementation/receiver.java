import java.net.*; 
import java.util.*; 
import java.io.*;  
public class receiver { 
  private static final int MSS = 72;
  private static int expectedSeqNum;
  //header: 0,1,2,3 port #; 4,5,6,7 seq#; 8,9,10,11 ack#; 12 ACK;13 FIN
  public static byte[] ripHeader(byte[] arr){
    byte[] data = new byte[MSS-20];
    for(int i=20;i<MSS;i++){
      data[i-20]=arr[i];
    }
    return data;
  }

  public static boolean checkChecksum(byte[] arr)throws IOException{
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
    byte[] ch = new byte[2];
    ch[0]=arr[16];
    ch[1]=arr[17];
    byte[] check = ToByteArrTwo(cs);
    if((check[0]==ch[0])&&(check[1]==ch[1])) return true;
    return false;
  }

  public static byte[] ToByteArrTwo(short number) {
    byte[] b = new byte[2];
    b[0] = (byte)(number & 0xff);
    b[1] = (byte)((number >> 8) & 0xff);
    return b;
  }

  public static int getSeqNum(byte[] arr) throws IOException {
    byte[] b = new byte[4];
    b[0]=arr[4];b[1]=arr[5];b[2]=arr[6];b[3]=arr[7];
    ByteArrayInputStream bis = new ByteArrayInputStream(b);
    DataInputStream o = new DataInputStream(bis);
    int x = o.readInt();
    o.close();
    return x;
  }

  public static byte[] intToByteArray(int a){
    byte[] b = new byte[4];
    b[3] = (byte) (a & 0xFF);   
    b[2] = (byte) ((a >> 8) & 0xFF);   
    b[1] = (byte) ((a >> 16) & 0xFF);   
    b[0] = (byte) ((a >> 24) & 0xFF);
    return b;
  }

  public static void main( String args[]) throws Exception {
    String file = args[0];//"/home/haominglu/Desktop/file.txt";//args[0];
    int receiverPORT = Integer.parseInt(args[1]);//4119;//args[1];
    String sender_IP = args[2];//"localhost";//args[2];
    int senderPORT = Integer.parseInt(args[3]);//9988;//args[3];
    String logfile = args[4];//"/home/haominglu/Desktop/r_logfile.txt";//args[4];
    PrintWriter logWriter = new PrintWriter(logfile, "UTF-8");
    PrintWriter fileWriter = new PrintWriter(file,"UTF-8");
    logWriter.println("timestamp, source, destination, Sequence #, ACK #, FIN");
    InetAddress ip = InetAddress.getByName(sender_IP); 


  DatagramSocket receiverSock = new DatagramSocket(receiverPORT); 
  byte arr1[] = new byte[MSS]; 
  DatagramPacket dpack = new DatagramPacket(arr1, arr1.length );
  System.out.println("Started");  
  expectedSeqNum = 0;
  int step = MSS-20;
  while(true) {
    receiverSock.receive(dpack); 
    byte[] segment = dpack.getData();
    boolean chk = checkChecksum(segment);
    if(chk==false) continue;
    byte[] data = ripHeader(segment);
    int packSize = dpack.getLength(); 
    int seqNum = getSeqNum(segment);
    if(seqNum!=expectedSeqNum) continue;
    String s2 = new String(data, 0, packSize-20);
    fileWriter.print(s2);
    expectedSeqNum += step;
    int ackNum = expectedSeqNum;
    int FIN = segment[13];
    logWriter.println( new Date( ) +" "+ sender_IP+":"+senderPORT+" "+
                      dpack.getAddress().toString()+":"+receiverPORT 
                      + " "+ seqNum+ " "+ackNum+" "+FIN);
    byte[] ac = intToByteArray(ackNum);
    DatagramPacket ackPack = new DatagramPacket(ac, ac.length,ip,senderPORT);
    receiverSock.send(ackPack);
    if(segment[13]==1) break;
  }
  System.out.println("Delivery completed successfully");
  receiverSock.close();
  logWriter.close();
  fileWriter.close();
 } 
}

