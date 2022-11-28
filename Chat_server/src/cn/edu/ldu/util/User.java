package cn.edu.ldu.util;

import java.net.DatagramPacket;


public class User {
    private String userId=null; //用户id
    private DatagramPacket packet=null; //报文
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public DatagramPacket getPacket() {
        return packet;
    }
    public void setPacket(DatagramPacket packet) {
        this.packet = packet;
    }   
}
