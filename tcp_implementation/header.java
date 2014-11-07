import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class header{
		byte[] h;
		public header(){
			h = new byte[20];//0,1,2,3 port #; 4,5,6,7 seq#; 8,9,10,11 ack#; 12 ACK;13 FIN; 16,17 checksum
		}
		public void setSeqNum(int SeqNum) throws IOException{
			byte[] arr = intToByteArrFour(SeqNum);
			for(int i=0;i<4;i++){
				h[4+i]=arr[i];
			}
		}
		
		public void setAckNum(int AckNum) throws IOException{
			byte[] arr = intToByteArrFour(AckNum);
			for(int i=0;i<4;i++){
				h[8+i]=arr[i];
			}
		}
		
		public void setACK(int ACK){
			h[12]=(byte) ACK;
		}
		
		public void setFIN(int FIN){
			h[13]=(byte) FIN;
		}
		
		public static byte[] intToByteArrFour(int number) throws IOException {
		    ByteArrayOutputStream o = new ByteArrayOutputStream();
		    DataOutputStream out = new DataOutputStream(o);
		    out.writeInt(number);
		    out.close();
		    byte[] b = o.toByteArray();
		    o.close();
		    return b;
		}
		
	}
