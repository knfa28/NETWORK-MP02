package newmp;

import java.io.*;
import java.util.*;

public class receiverWindow{
    private int size;
    private short expectedSeqno = 0;
    private ArrayList<byte[]> byteArrayList = new ArrayList(); 
    private ArrayList<Short> seqnoinbyteArray = new ArrayList();
    private UDPClient udc;
    
    public receiverWindow(int size, int byteSize, UDPClient udc){
        this.size = size / byteSize;
        this.udc = udc;
    }
    
    public void check(){
        try{
            if(seqnoinbyteArray.size() != 0){
                for(int i = 0; i < seqnoinbyteArray.size(); i++){
                    if(expectedSeqno == seqnoinbyteArray.get(i)){
                        udc.writeToFile(extractSeqNo(seqnoinbyteArray.get(i)));
                        udc.printInfo(true, false, false);
                        expectedSeqno++;
                    }
                }
            }  
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public boolean addArray(byte[] tobeAdded, short seqno) throws Exception{
        if(byteArrayList.size() <= this.size && !seqnoinbyteArray.contains(seqno)){
            byteArrayList.add(tobeAdded);
            seqnoinbyteArray.add(seqno);
            check();         
            return false;
        }else if(seqnoinbyteArray.contains(seqno)){
            check();
            return false;
        }else{
            check();
            return true;
        }      
    }
    
    public byte[] extractSeqNo(short seqno){
        byte[] temp = byteArrayList.get(seqnoinbyteArray.indexOf(seqno));
        byteArrayList.remove(seqnoinbyteArray.indexOf(seqno));
        seqnoinbyteArray.remove(seqnoinbyteArray.indexOf(seqno));
        return temp;
    }
    
    public void updateExpectedSeqno(short seqno){
        this.expectedSeqno = seqno;
    }
}