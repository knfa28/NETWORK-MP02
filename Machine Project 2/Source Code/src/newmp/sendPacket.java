package newmp;

import java.io.*;
import java.nio.*;
import java.net.*;
import java.util.*;

public class sendPacket extends Thread{
    int port, length;
    short seqno, ackno;
    byte[] packetArray, ackArray;
    InetAddress IPAddress;
    DatagramSocket serversocket;
    UDPServer udp;
    DatagramPacket sendPacket = null;
    boolean isPacketLost, isAckReceived;
    int lostProbable, verbLevel;
    Random random;
    
    public sendPacket(int port, short seqno, short ackno, byte[] packetArray, InetAddress IPAddress, DatagramSocket serversocket, 
                      UDPServer udp, int length, int lostProbable, int verbLevel){
        this.port = port;
        this.seqno = seqno;
        this.ackno = ackno;
        this.packetArray = Arrays.copyOfRange(packetArray, 0, packetArray.length);
        this.IPAddress = IPAddress;
        this.serversocket = serversocket;
        this.udp = udp;
        this.ackArray = new byte[2];
        this.isPacketLost = false;
        this.isAckReceived = false;
        this.length = length;
        this.lostProbable = lostProbable;
        this.verbLevel = verbLevel;
        sendPacket = new DatagramPacket(packetArray, length, IPAddress, port);
        random = new Random(System.currentTimeMillis());
    }
    
    public void run(){
        do{
            try{
                isAckReceived = false; isPacketLost = false;
                int nRand = random.nextInt(100);
                
                if(nRand >= lostProbable){
                    serversocket.send(sendPacket);
                    udp.printInfo(verbLevel, isPacketLost, isAckReceived, ackno, seqno);
                }else isPacketLost = true;
                
                try{
                    DatagramPacket ackPacket = new DatagramPacket(ackArray, ackArray.length);
                    serversocket.receive(ackPacket);
                    System.out.println("Received acknowledgement: " + ByteBuffer.wrap(ackArray).getShort(0));
                    
                    if(ackno == ByteBuffer.wrap(ackArray).getShort(0)){
                        this.isAckReceived = true;
                        stoppingProcedure();
                        this.stop();
                    }else udp.checkOthers(ByteBuffer.wrap(ackArray).getShort(0));
                    
                }catch(SocketTimeoutException e){
                    serversocket.send(sendPacket);
                }
            }catch(Exception e){
                    e.printStackTrace();
            }
        }while(true);
    }
    
    public void stopIf(short ackno){
        if(this.ackno < ackno)
            stoppingProcedure();
    }
    
    public void stoppingProcedure(){
        udp.decreaserunningPackets();
        udp.printInfo(verbLevel, isPacketLost, isAckReceived, ackno, seqno);
        udp.doneSending(this, ackno);
        this.stop();
    }
    
    public void sendP() throws Exception{
       serversocket.send(sendPacket);
       udp.printInfo(verbLevel, isPacketLost, isAckReceived, ackno, seqno);
    }
}
