package newmp;

import java.io.*; 
import java.math.BigInteger;
import java.net.*;
import java.util.*;
import java.nio.*;

public class UDPServer {
    private short ackNo, seqNo;
    private int runningPackets;
    private ArrayList<sendPacket> spArray = new ArrayList();
    private ArrayList<Short> ackShort = new ArrayList();
    private timeCounter ackCounter;
    
    public static void main(String args[]) throws Exception{
        UDPServer udp = new UDPServer();
        udp.start();
    }
     
    public void start() throws Exception{
        Scanner s = new Scanner(System.in);
        System.out.println("Name of file to be sent?");
        String input = "C:\\Users\\kurta\\Downloads\\tempSched.png";//s.nextLine();
        System.out.println("Chunksize in bytes?");
        int byteSize = 1024;//s.nextInt();
        System.out.println("Sender window size?");
        int senderWindow = 4000;//s.nextInt();
        senderWindow /= byteSize;
        System.out.println("Initial sequence number?");
        seqNo = 0;//s.nextInt();
        System.out.println("Loss probability for the ack?");
        int lossProbable = 90;//s.nextInt();
        System.out.println("timeout length?");
        int timeout = 2000;//s.nextInt();
        System.out.println("Delay?");
        int delay = 100;//s.nextInt();
        //s.nextLine();
        System.out.println("IP address of receiver?");
        String ipAddress = "10.100.217.25";//s.nextLine();
        System.out.println("Port number?");
        int portno = 9876;//s.nextInt();
        System.out.println("Verbosity level?");
        int verblevel = 3;//s.nextInt();
        
        int curVal = 0, bytesRead = 0, timetaken = 0, headersize = 4;
        runningPackets = 0;
	DatagramSocket serverSocket = new DatagramSocket(portno);
        //serverSocket.setSoTimeout(timeout);
        timeCounter timecounter = new timeCounter();
        ackCounter = new timeCounter(timeout/1000, this);
        
        File myFile = new File(input);
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myFile));                   
        InetAddress IPAddress = InetAddress.getByName(ipAddress);                   
        int port = portno;
        ackNo = 1;
        
        timecounter.start();
        ackCounter.start();
        while(curVal != -1){
            byte[] myFileArray = new byte[byteSize];
            byte[] tobeSent = null;
            curVal = bis.read(myFileArray, 0, myFileArray.length);
            
            if(curVal!= -1){
                try{
                    tobeSent = addHeader(headersize, seqNo, ackNo, myFileArray);
                    seqNo++;
                }catch(Exception e){
                    e.printStackTrace();
                }

                sendPacket sp = new sendPacket(port, seqNo, ackNo, tobeSent, IPAddress, serverSocket, this, curVal+headersize, lossProbable, verblevel);
                spArray.add(sp);
                sp.start();
                runningPackets++;
                bytesRead += curVal;
                ackNo++;
            }else timetaken = timecounter.stopCounting();
            
            
            while(runningPackets == senderWindow)
            Thread.sleep(delay);
            Thread.sleep(delay);
	}
        System.out.println("Last sequence number? " + seqNo);
        System.out.println(timetaken + " seconds to send the file.");
        
        do{
            if(runningPackets == 0)
                serverSocket.close();
        }while(runningPackets!= 0);
    }
     
    public int getSeqNo(){
        return this.seqNo;
    }
     
    public int getackNo(){
        return this.ackNo;
    }
     
    public void decreaserunningPackets(){
        this.runningPackets--;
    }
    
    public byte[] addHeader(int headerSize, short seqno, short ackno, byte[] arrayLength) throws Exception{
        byte[] header;
        header = concatenateByteArray(ByteBuffer.allocate(headerSize).putShort(seqno).putShort(ackno).array(), arrayLength);
        return header;
    }
     
    public byte[] concatenateByteArray(byte[] a, byte[] b) throws Exception{
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(a);
        outputStream.write(b);
        
        return outputStream.toByteArray();
    }
     
    public void printInfo(int level, boolean isPacketLost, boolean isAckReceived, short ackno, short seqno){
        if(level == 1){
            if(isPacketLost == true)
                System.out.println("Sequence number: " + seqno + " lost!");
        }
        
        else if(level == 2){
            if(isPacketLost == true){
                System.out.println("Sequence number: " + seqno + " lost!");
            }else System.out.println("Sequence number: " + seqno + " sent!");
          
            if(isAckReceived == true)
                System.out.println("Acknowledgement number: " + ackno + " received.");
        }
        
        else if(level == 3){
            Calendar cal = Calendar.getInstance();
            
            if(isPacketLost == true){
                System.out.println("Sequence number: " + seqno + " lost at " + cal.getTime() + "!");
            }else System.out.println("Sequence number: " + seqno + " sent at " + cal.getTime() + "!");
            
            if(isAckReceived == true)
                System.out.println("Acknowledgement number: " + ackno + " received at " + cal.getTime() + "!");
        }     
    }
     
    public void doneSending(sendPacket e, short ackNo){
        if(spArray.indexOf(e) == 0)
            ackCounter.restartCount();      
        
        spArray.remove(e);
        
        for(int i = 0; i < spArray.size(); i++)
           spArray.get(i).stopIf(ackNo);    
    }
     
     public void checkOthers(short ackNo){
         for(int i = 0; i < spArray.size(); i++){
           spArray.get(i).stopIf(ackNo);
        }
     }
     
     public void sendAllPackets() throws Exception{
         for(int i = 0; i < spArray.size(); i++){
           spArray.get(i).sendP();
        }
     }
}