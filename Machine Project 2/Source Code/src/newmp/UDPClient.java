package newmp;

import java.io.*; 
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;

public class UDPClient{
    private short seqno = -1, ackno;
    private short expectedSeqno =0;
    private boolean isSaved, isDiscarded, isLost;
    private int receiversize, verbLevel, lossProb;
    private String filename;
    private byte[] receiveData;
    private DatagramSocket clientSocket;
    private DatagramPacket receivePacket, sendPacket;
    private receiverWindow rw;
    private boolean firstRun = true;
    private Random random = new Random(System.currentTimeMillis());
    
    public static void main(String args[]) throws Exception{ 
        UDPClient udp = new UDPClient();
        udp.start();
    }
    
    public void start() throws Exception{
        Scanner s = new Scanner(System.in);
        
        System.out.println("Filename?");
        this.filename = "haha.jpg";//s.nextLine();
        System.out.println("Receiver window?");
        this.receiversize = 10000;//s.nextInt();
        System.out.println("UDP port no?");
        int portno = 9876;//s.nextInt();
        System.out.println("Delay?");
        int delay = 10;//s.nextInt();
        System.out.println("Loss prob?");
        this.lossProb = 0;//s.nextInt();
        System.out.println("Timeout?");
        int timeoutTime = 1000;//s.nextInt();
        System.out.println("Verbosity level?");
        this.verbLevel = 3;//s.nextInt();
        
	this.clientSocket = new DatagramSocket(portno);
        clientSocket.setSoTimeout(timeoutTime);
	this.receiveData = new byte[receiversize];
	this.receivePacket = new DatagramPacket(receiveData, receiveData.length); 
        this.sendPacket = null;
        int byteSize = 0;
        boolean isDone = false;
        
        do{ 
            try{
                clientSocket.receive(receivePacket);
                
                if(firstRun){
                    byteSize = receivePacket.getLength();
                    this.rw = new receiverWindow(receiversize, byteSize, this);
                    firstRun = false;
                }else if(receivePacket.getLength() != byteSize){
                    isDone = true;
                }
                
                executeSend();
            }catch(SocketTimeoutException e){
                if(sendPacket != null){
                    clientSocket.send(sendPacket);
                    System.out.println("Resent ack: " + ackno);
                }else {System.out.println("Sendpacket is null. unable to resend.");}
            }
            
            Thread.sleep(delay);
	}while(!isDone);
	
        clientSocket.close();
	System.exit(0);
    }
    
    public void writeToFile(byte[] array) throws Exception{
        FileOutputStream fos = new FileOutputStream(this.filename, true);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        bos.write(array, 0, array.length);
        bos.close();
        bos.flush();
        fos.close();
        fos.flush();
    }
    
    public void printInfo(boolean isSaved, boolean isLost, boolean isDiscarded){
        System.out.print("Sequence number: ");
        
        if(verbLevel == 1){
            if(isLost || isDiscarded)
                System.out.println(seqno + " was dropped.");
        }
        
        else if(verbLevel == 2){
            if(isLost){
                System.out.println(seqno + " was lost.");
            }else if(isDiscarded){
                System.out.println(seqno + " was discarded.");
            }else if(isSaved){
                System.out.println(seqno + " was saved.");
            }
        }
        
        else if(verbLevel == 3){
            Calendar cal = Calendar.getInstance();
            if(isLost){
                System.out.println(seqno + " was lost at " + cal.getTime());
            }else if(isDiscarded){
                System.out.println(seqno + " was discarded at " + cal.getTime());
            }else if(isSaved){
                System.out.println(seqno + " was saved at " + cal.getTime());
            }
        }
    }
    
    public void executeSend() throws Exception{
        seqno = ByteBuffer.wrap(receiveData).getShort(0);
        ackno = ByteBuffer.wrap(receiveData).getShort(2);
        System.out.println(seqno + ", " + ackno);
        byte[] toFile = Arrays.copyOfRange(receiveData, 4, receivePacket.getLength());
            
        isDiscarded = this.rw.addArray(toFile, seqno);
            
        if(expectedSeqno == seqno){        
            System.out.println("Packet Received.");
            byte[] tobeSent = ByteBuffer.allocate(2).putShort(ackno).array();
            sendPacket = new DatagramPacket(tobeSent, 2, receivePacket.getAddress(), receivePacket.getPort());
            int nRand = random.nextInt(100);
            
            if(nRand >= lossProb){
                clientSocket.send(sendPacket);
                System.out.println("Ack sent.");
            }
            
            expectedSeqno++;    
        }else if(seqno < expectedSeqno){
            clientSocket.send(sendPacket);
        }
        
        printInfo(isSaved, isLost, isDiscarded);
    }
    
    public void setisSaved(boolean val){
        this.isSaved = val;
    }
}    