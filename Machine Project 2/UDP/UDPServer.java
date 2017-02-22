import java.io.*; 
import java.net.*; 
class UDPServer
{    
	public static void main(String args[]) throws Exception       
	{          
		DatagramSocket serverSocket = new DatagramSocket(9876);
		byte[] receiveData = new byte[1024];
		byte[] sendData = new byte[1024];
		File myFile = new File("D:\MY STUFF\Series\The Legend of Korra Season 4\The.Legend.of.Korra.S04E04.The.Calling.720p.WEB-DL.x264.AAC.mp4");
		byte[] myFile = new byte[(int) myFile.length() + 1];
		
		 try{
         BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myFile));
         bis.read(myFile, 0, myFile.length());
         }catch(Exception e){
         e.printStackTrace();
         }
		while(true)                
		{
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);                   			
			serverSocket.receive(receivePacket);                   
			String sentence = new String( receivePacket.getData());                   
			System.out.println("RECEIVED: " + sentence);                   
			InetAddress IPAddress = receivePacket.getAddress();                   
			int port = receivePacket.getPort();                   
			String capitalizedSentence = sentence.toUpperCase();                   
			sendData = capitalizedSentence.getBytes();                   
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);                   		
			serverSocket.send(sendPacket);                
		}       
	} 
}