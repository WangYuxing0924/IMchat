package cn.edu.ldu;

import cn.edu.ldu.util.Message;
import cn.edu.ldu.util.Translate;
import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * 功能ReceiveMessage客户机接收消息和处理消息的线程类
 * @author  董相志，版权所有2016--2018，upsunny2008@163.com
 */
public class ReceiveMessage extends Thread{
    private DatagramSocket clientSocket; //会话套接字
//    private ClientUI parentUI; //父类
//    private PrivateClientUI privateClientUI;
    private byte[] data=new byte[8096]; //8K字节数组
    private DefaultListModel listModel=new DefaultListModel(); //列表Model
    private DefaultListModel FilelistModel;
    private HashMap<String,JFrame> clientUIMap;
    //构造函数
    public ReceiveMessage(DatagramSocket socket,HashMap<String,JFrame> clientUIMap) {
        clientSocket=socket; //会话套接字
        //this.parentUI=parentUI; //父类
        this.clientUIMap = clientUIMap;
        //System.out.println("===ReceiveMessage== 创建公聊窗口"+parentUI+this);
    } 
//    public ReceiveMessage(DatagramSocket socket,ClientUI parentUI) {
//        clientSocket=socket; //会话套接字
//        this.parentUI=parentUI; //父类
//        System.out.println("===ReceiveMessage== 创建公聊窗口"+parentUI+this);
//    }   
//    public ReceiveMessage(DatagramSocket socket, PrivateClientUI privateClientUI) {
//		// TODO Auto-generated constructor stub
//    	clientSocket=socket; //会话套接字
//        this.privateClientUI=privateClientUI; //父类
//        System.out.println("===ReceiveMessage== 创建私聊窗口==="+this);
//	}
	@Override
    public void run() {
        while (true) { //无限循环，处理收到的各类消息
          try {
        	   data=new byte[8096];
            DatagramPacket packet=new DatagramPacket(data,data.length); //构建接收报文
            //System.out.println("clientSocket is closed? "+clientSocket.isClosed());
            if(clientSocket.isClosed()) {
                      break;
            }
            clientSocket.receive(packet); //接收
                  
            Message msg=(Message)Translate.ByteToObject(data);//还原消息对象
            String userId=msg.getUserId(); //当前用户id
            ClientUI parentUI = (ClientUI)clientUIMap.get("0");
          // System.out.println(msg.getType());
            //根据消息类型分类处理
            if (msg.getType().equalsIgnoreCase("M_LOGIN")) { //是其他用户的登录消息
                playSound("/cn/edu/ldu/sound/fadeIn.wav");//上线提示音  
                //更新消息窗口
                parentUI.txtArea.append(userId+" 昂首挺胸进入聊天室...\n");
                //新上线用户加入列表
               /* for(String userString:msg.getUserArrayList()) {
                	System.out.println(userString);
                listModel.add(listModel.getSize(), userString);
                }
                parentUI.userList.setModel(listModel);*/
                listModel=(DefaultListModel) parentUI.userList.getModel();
                for(int i=0;i<listModel.size();i++) {
                	
                	//System.out.println(listModel.get(i));
                if(((String)listModel.get(i)).startsWith(userId+"(")) {
                	// System.out.println(listModel.get(i));
                	listModel.set(i, userId+"(在线)");
                }
                }
                
                
                parentUI.userList.setModel(listModel);
            }else if (msg.getType().equalsIgnoreCase("M_ACK")) { //是服务器确认消息
                //登录成功，将自己加入用户列表
            	
            	/*for(String userString:msg.getUserArrayList()) {
                	
               listModel.add(listModel.getSize(), userString);
                }
               parentUI.userList.setModel(listModel);*/
            	listModel=(DefaultListModel) parentUI.userList.getModel();
            	 for(int i=0;i<listModel.size();i++) {
            		 
                     if(listModel.get(i).equals(userId+"(离线)")) {
                    	 System.out.println(listModel.get(i));
                     	listModel.set(i, userId+"(在线)");
                     }
                     }
                     parentUI.userList.setModel(listModel);
            }
            
            
            else if (msg.getType().equalsIgnoreCase("M_MSG")) { //是普通会话消息
                playSound("/cn/edu/ldu/sound/msg.wav");//消息提示音  
                //更新消息窗口
                //parentUI = (ClientUI)clientUIMap.get("0");
                parentUI.txtArea.append(userId+" 说："+msg.getText()+"\n");
  /*修改过*/ }
            else if (msg.getType().equalsIgnoreCase("RO_GET")) { 
            	System.out.println("得到离线消息");
                playSound("/cn/edu/ldu/sound/msg.wav");//消息提示音  
                //更新消息窗口
                //parentUI = (ClientUI)clientUIMap.get("0");
                parentUI.txtArea.append("在你离线的时候："+msg.getText()+"\n");
  /*修改过*/ }
            else if(msg.getType().equalsIgnoreCase("M_PRIVATE")) {
	           // PrivateClientUI client=new PrivateClientUI(clientSocket,msg);
	            
	            //client.setTitle("正在与 "+msg.getUserId()+" 进行私聊"); //设置标题
				 // client.setVisible(true); //显示会话窗体 
	           PrivateClientUI privateClientUI = (PrivateClientUI)clientUIMap.get(userId);
	           System.out.println("privateClientUI======"+this);
	           System.out.println("txtArea======"+privateClientUI.txtArea);
	           privateClientUI.txtArea.append(userId+" 说："+msg.getText()+"\n");
              }else if(msg.getType().equalsIgnoreCase("beg_PRIVATE")) {
				  String idString=msg.getTargetId();
				 msg.setTargetId(userId);
				 msg.setUserId(idString);
				 PrivateClientUI privateClientUI=new PrivateClientUI(clientSocket,msg);
				 System.out.println("beg_PRIVATE====="+privateClientUI);
				 
				 privateClientUI.setTitle("正在与 "+msg.getTargetId()+" 进行私聊"); //设置标题
				 privateClientUI.setVisible(true); //显示会话窗体 
				 clientUIMap.put(userId, privateClientUI);
			}
			  else if(msg.getType().equalsIgnoreCase("F_PRIVATE")) {
				  int port=9998;
				 
				  String idString=msg.getTargetId();
				  //ServerSocket serverSocket=new ServerSocket(port);
				
				 msg.setPort(port);
				msg.setAddrString("0.0.0.0");
				 msg.setTargetId(userId);
				 msg.setUserId(idString);
				 msg.setType("RF_PRIVATE");
				 System.out.println(idString+"即将给你发送文件");
				 
				 JOptionPane.showMessageDialog(null, userId+"即将给你发送文件", "发送文件提示", JOptionPane.OK_CANCEL_OPTION);
				 //System.out.println(idString+"给你发送文件");
				 PrivateClientUI privateClientUI=(PrivateClientUI) clientUIMap.get(userId);
				 ReceiveMail receiveMail=new ReceiveMail(privateClientUI, port);
				 receiveMail.start();
				 //*************xfxf*****hostPort可能已经加1了所以要重新设置**************
				 while(true) {
					 Thread.sleep(1000);
					 if(receiveMail.hostPort!=0)
						 break;
				 }
				 System.out.println("启动接受文件线程，端口是"+receiveMail.hostPort);
				 msg.setPort(receiveMail.hostPort);
				 data=Translate.ObjectToByte(msg); //消息对象序列化
				    //构建发送
				 packet=new DatagramPacket(data,data.length,msg.getToAddr(),msg.getToPort());  
				clientSocket.send(packet);
				 
				System.out.println("得到请求消息");
			}
			else if(msg.getType().equalsIgnoreCase("RF_PRIVATE")) {
				String idString=msg.getTargetId();
				System.out.println(idString+"收到同意发文件消息准备发文件给"+msg.getUserId());
				int port=msg.getPort();
				String addrString=msg.getAddrString();
				//PrivateClientUI privateClientUI=new PrivateClientUI(clientSocket,msg);
				 msg.setTargetId(userId);
				 msg.setUserId(idString);
				
				 LoginUI.isSendFileAllow.put(userId, msg);
				//System.out.println("服务端端口"+msg.getAddrString()+" "+msg.getPort());
				
			}
            
            
            else if (msg.getType().equalsIgnoreCase("M_QUIT")) { //是其他用户下线消息
                playSound("/cn/edu/ldu/sound/leave.wav");//消息提示音  
                //更新消息窗口
                parentUI.txtArea.append(userId+" 大步流星离开聊天室...\n");
                //下线用户从列表删除
                //listModel.remove(listModel.indexOf(userId));
                //parentUI.userList.setModel(listModel);
                listModel=(DefaultListModel) parentUI.userList.getModel();
                for(int i=0;i<listModel.size();i++) {
                	
                if(listModel.get(i).equals(userId+"(在线)")) {
                	
                	// System.out.println(listModel.get(i));
                	listModel.set(i, userId+"(离线)");
                }
                }
                parentUI.userList.setModel(listModel);
                clientUIMap.remove(userId);
            }//end if  
            else if (msg.getType().equalsIgnoreCase("F_QUIT")) { //是其他用户下线消息
                playSound("/cn/edu/ldu/sound/leave.wav");//消息提示音  
                //更新消息窗口
                parentUI.txtArea.append(userId+"被踢出聊天室...\n");
                listModel=(DefaultListModel) parentUI.userList.getModel();
                for(int i=0;i<listModel.size();i++) {
                	
                if(listModel.get(i).equals(userId+"(在线)")) {
                	 //System.out.println(listModel.get(i));
                	listModel.set(i, userId+"(离线)");
                }
                }
                parentUI.userList.setModel(listModel);
               System.exit(0); 
                //下线用户从列表删除
                //listModel.remove(listModel.indexOf(userId));
                //parentUI.userList.setModel(listModel);
               // clientUIMap.remove(userId);
            }//end if  
            else if (msg.getType().equalsIgnoreCase("MF_QUIT")) { 
                playSound("/cn/edu/ldu/sound/leave.wav");//消息提示音  
                //更新消息窗口
                parentUI.txtArea.append(userId+"被踢出聊天室...\n");
                listModel=(DefaultListModel) parentUI.userList.getModel();
                for(int i=0;i<listModel.size();i++) {
                	
                if(listModel.get(i).equals(userId+"(在线)")) {
                	// System.out.println(listModel.get(i));
                	listModel.set(i, userId+"(离线)");
                }
                }
                parentUI.userList.setModel(listModel);
                //下线用户从列表删除
                //listModel.remove(listModel.indexOf(userId));
                parentUI.userList.setModel(listModel);
                
                if(clientUIMap.containsKey(userId)){
                	clientUIMap.get(userId).dispose();
                	clientUIMap.remove(userId);
                	
                }
            }else if(msg.getType().equalsIgnoreCase("M_DOWNLOAD_FILE_LIST")) {
            	
            	  int port=9997;
            	  FilelistModel=new DefaultListModel<String>();
            	  System.out.println("客户端收到服务端的文件列表:"+msg.getFileArrayList());
            	  //弹出用户选文件的窗口，启动接受文件线程
            	  AllFileList allFileList=new AllFileList();
            	  for(String fileString:msg.getFileArrayList()) {
                  	
                      FilelistModel.add(FilelistModel.getSize(), fileString);
                       }
                     
                  
            	  allFileList.FileList.setModel(FilelistModel);
            	  allFileList.setVisible(true);
            	  ClientUI parentUI1 = (ClientUI)clientUIMap.get("0");
            	  PublicReceiveMail receiveMail=new PublicReceiveMail(parentUI1, port);
 			      receiveMail.start();
 			     while(true) {
					 Thread.sleep(1000);
					 if(receiveMail.hostPort!=0)
						 break;
				 }
 			      AllFileList.btnNewButton.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						msg.setType("D_FILE");
 						msg.setFilenameString(allFileList.FileList.getSelectedValue());
 						 msg.setPort(receiveMail.hostPort);
 						msg.setAddrString("0.0.0.0");
 						data=Translate.ObjectToByte(msg); //消息对象序列化
 					    //构建发送
 					 DatagramPacket packet=new DatagramPacket(data,data.length,msg.getToAddr(),msg.getToPort());  
 					  try {
						clientSocket.send(packet);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
 					allFileList.dispose();	
 						
 						
 					}
 				});
            	  
            	}
            
            
            
            
          }catch (Exception ex) {
        	 // ex.printStackTrace();
       	  if(!ex.getMessage().contains("socket closed")) {
//        		  ex.printStackTrace();
//        		
//        		  JOptionPane.showMessageDialog(null, ex.getMessage(),"错误提示",JOptionPane.ERROR_MESSAGE);
        	  }
        	 // ex.printStackTrace();
        	  //System.out.println("Something wrong");
              
          }//end try
        } //end while
    }//end run
    /**
     * 播放声音文件
     * @param filename 声音文件路径和名称
     */
    private void playSound(String filename) {
        URL url = AudioClip.class.getResource(filename);
        AudioClip sound;
        sound = Applet.newAudioClip(url);
        sound.play();
    }
}//end class
