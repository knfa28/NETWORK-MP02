package newmp;

public class timeCounter extends Thread{
    private int seconds = 0, lastCounter;
    private boolean countdownTimer;
    private UDPServer udp;
    
    public timeCounter(){
        countdownTimer = false;
    }
    
    public timeCounter(int lastCounter, UDPServer udp){
        countdownTimer = true;
        this.lastCounter = lastCounter;
        this.udp = udp;
    }
    
    public void run(){
        while(true){
            try{
                if(countdownTimer){
                    if(lastCounter == this.seconds){
                        udp.sendAllPackets();
                        restartCount();
                    }
                }
                Thread.sleep(1000);
                this.seconds++;
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    
    public int stopCounting(){
        this.stop();
        return seconds;
    }
    
    public void restartCount(){
        this.seconds = 0;
    }
}
