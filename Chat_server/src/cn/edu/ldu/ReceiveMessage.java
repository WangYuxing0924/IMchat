package cn.edu.ldu;

import cn.edu.ldu.db.DBUtils;
import cn.edu.ldu.db.beans.Member;
import cn.edu.ldu.db.tables.MemberManager;
import cn.edu.ldu.util.Message;
import cn.edu.ldu.util.Translate;
import cn.edu.ldu.util.User;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.MaskFormatter;
import javax.swing.text.html.HTMLDocument.HTMLReader.ParagraphAction;

/**
 * ReceiveMessage，服务器接收消息和处理消息的线程类
 * @author 董相志，版权所有2016--2018，upsunny2008@163.com
 */
public class ReceiveMessage extends Thread {
   public static  DatagramSocket serverSocket; //服务器套接字
   public  DatagramPacket packet;  //报文
   public static List<User> userList=new ArrayList<User>(); //用户列表
   public  byte[] data=new byte[8096]; //8K字节数组
    public static  ServerUI parentUI; //消息窗口  
    public static DefaultListModel listModel=new DefaultListModel(); //列表Model
    public static Manager par;
    /**
     * 构造函数
     * @param socket 会话套接字
     * @param parentUI 父类
     */
    public ReceiveMessage(DatagramSocket socket,ServerUI parentUI,Manager par) {
        serverSocket=socket;
        this.parentUI=parentUI;
        this.par=par;
    }
    @Override
    public void run() {  
        while (true) { //循环处理收到的各种消息
            try {
            	data=new byte[8096];
            packet=new DatagramPacket(data,data.length);//构建接收报文
            serverSocket.receive(packet);//接收客户机数据
            //收到的数据转为消息对象
            Message msg=(Message)Translate.ByteToObject(packet.getData());
            if(msg == null) {
            	 
            	  continue;
            }
            	  
            String userId=msg.getUserId();//当前消息来自用户的id 
           
            if (msg.getType().equalsIgnoreCase("M_LOGIN")) { //是M_LOGIN消息 
                Message backMsg=new Message();
                Member bean=new Member();
                bean.setId(Integer.parseInt(userId));
                bean.setPassword(msg.getPassword()); 
                System.out.println(bean.toString());
                if (!MemberManager.userLogin(bean)) {//登录不成功
                    backMsg.setType("M_FAILURE");
                    byte[] buf=Translate.ObjectToByte(backMsg);
                    DatagramPacket backPacket=new DatagramPacket(buf,buf.length,packet.getAddress(),packet.getPort());//向登录用户发送的报文
                    serverSocket.send(backPacket); //发送                  
                }else { //登录成功
                    backMsg.setType("M_SUCCESS");
                    ArrayList<String> userArrayList=new ArrayList<String>();
                    userArrayList=DBUtils.getUserlist();
                    backMsg.setUserArrayList(userArrayList);
                    byte[] buf=Translate.ObjectToByte(backMsg);
                   
                    DatagramPacket backPacket=new DatagramPacket(buf,buf.length,packet.getAddress(),packet.getPort());//向登录用户发送的报文
                    serverSocket.send(backPacket); //发送   
                    
                    User user=new User();
                    user.setUserId(userId); //用户名
                    user.setPacket(packet); //保存收到的报文
                    userList.add(user); //将新用户加入用户列表
                    
                    //更新服务器聊天室大厅
                    parentUI.txtArea.append(userId+" 登录！\n");
                    
                    listModel.add(listModel.getSize(), userId);
                   
                    par.userlist.setModel(listModel);
                   
                    //向所有其他在线用户发送M_LOGIN消息，向新登录者发送整个用户列表
                    for (int i=0;i<userList.size();i++) { //遍历整个用户列表                                       
                        //向其他在线用户发送M_LOGIN消息
                        if (!userId.equalsIgnoreCase(userList.get(i).getUserId())){
                            DatagramPacket oldPacket=userList.get(i).getPacket(); 
                            System.out.println("向用户"+userList.get(i).getUserId()+"发送用户"+userId+"登录消息");
                            DatagramPacket newPacket=new DatagramPacket(data,data.length,oldPacket.getAddress(),oldPacket.getPort());//向其他用户发送的报文
                            serverSocket.send(newPacket); //发送
                        }//end if
                        //向当前用户回送M_ACK消息，将第i个用户加入当前用户的用户列表
                        Message other=new Message();
                        other.setUserId(userList.get(i).getUserId());
                        other.setType("M_ACK");
                       
                        byte[] buffer=Translate.ObjectToByte(other);
                        DatagramPacket newPacket=new DatagramPacket(buffer,buffer.length,packet.getAddress(),packet.getPort());
                        serverSocket.send(newPacket);
                    }//end for
                    
                    
                    
                    
                }//end if                           
            }else if(msg.getType().equalsIgnoreCase("M_REGISTER")) {
            	Member beanMember=new Member();
            	beanMember.setId(Integer.parseInt(msg.getUserId()));
            	beanMember.setName(msg.getNameString());
            	beanMember.setPassword(msg.getPassword());
            	beanMember.setEmail(msg.getMailString());
            	beanMember.setHeadImage(msg.getPicture());
            	System.out.println(msg.getPassword());
            	MemberManager memberManager=new MemberManager();
            	memberManager.registerUser(beanMember);
            }else if(msg.getType().equalsIgnoreCase("M_DOWNLOAD_REQ")) {
            	//***************xfxf****************
            	System.out.println("服务器收到下载文件请求。。。");
            	File file=new File("upload");
            	System.out.println("file path:"+file.getAbsolutePath());
            	String fList = "";
            	String[] files = file.list();
            	ArrayList< String> fileArrayList=new ArrayList<String>();
            	if(files!=null&&files.length>0) {
            	  for(String f:files) {
            		  fList+=f+" ";
            		  //msg.setFileArrayList(fileArrayList);
            		  fileArrayList.add(f);
            	  }
             msg.setFileArrayList(fileArrayList);
            	System.out.println("服务端上传的文件列表："+fileArrayList);
            //msg.setText(fList);
		    msg.setType("M_DOWNLOAD_FILE_LIST");
            //msg.setType("M_MSG");
            
		    
			byte[] data=Translate.ObjectToByte(msg);
			
            	for (int i=0;i<userList.size();i++) { //遍历用户
            		System.out.println("userList length:"+userList.size()+"userList==="+userList.get(i).getUserId()+" 客户端userId:"+msg.getUserId());
           		 if(userList.get(i).getUserId().equals(msg.getUserId())) {
                    DatagramPacket oldPacket=userList.get(i).getPacket();
                    System.out.println("userid:"+userList.get(i).getUserId()+" oldPacket.getAddress():"+oldPacket.getAddress()+" oldPacket.getPort():"+oldPacket.getPort());
                    DatagramPacket newPacket=new DatagramPacket(data,data.length,oldPacket.getAddress(),oldPacket.getPort()); 
                    serverSocket.send(newPacket); //发送
                    System.out.println("发送列表到客户端："+fileArrayList);
                    
           		 }
           		
           	 }
            	}
            //	byte[] data=Translate.ObjectToByte(msg);
            
            }else if (msg.getType().equalsIgnoreCase("M_PRIVATE")||msg.getType().equalsIgnoreCase("F_PRIVATE")||msg.getType().equalsIgnoreCase("RF_PRIVATE")) { //是M_MSG消息
            	 for (int i=0;i<userList.size();i++) { //遍历用户
            		 if(userList.get(i).getUserId().equals(msg.getTargetId())) {
                     DatagramPacket oldPacket=userList.get(i).getPacket();
                     DatagramPacket newPacket=new DatagramPacket(data,data.length,oldPacket.getAddress(),oldPacket.getPort()); 
                     serverSocket.send(newPacket); //发送
                     
            		 }
            		
            	 }
            	
            		 
                 }
                
                   
                
            
            else if (msg.getType().equalsIgnoreCase("beg_PRIVATE")) { //是M_MSG消息
           	 for (int i=0;i<userList.size();i++) { //遍历用户
           		 if(userList.get(i).getUserId().equals(msg.getTargetId())) {
                    DatagramPacket oldPacket=userList.get(i).getPacket();
                    DatagramPacket newPacket=new DatagramPacket(data,data.length,oldPacket.getAddress(),oldPacket.getPort()); 
                    serverSocket.send(newPacket); //发送
                 
           		 }
                }   
               
           }
            else if (msg.getType().equalsIgnoreCase("O_PRIVATE")) { 
            	String pathString="C:\\schoolstudy2\\case_chapter7_client\\src\\AllFile\\"+msg.getTargetId();
            	//BufferedWriter out=new BufferedWriter(new OutputStreamWriter(new FileOutputStream("C:\\schoolstudy2\\case_chapter7_client\\src\\AllFile\\"+msg.getUserId())));
            	//File file=new File("C:\\schoolstudy2\\case_chapter7_client\\src\\AllFile\\"+msg.getTargetId());
            	FileOutputStream out=null;
            	out=new FileOutputStream(pathString,true);
            	byte []text=(msg.getText()).getBytes();
            	out.write(text);
            	out.write("\r\n".getBytes());
            	out.flush();
            	
              	
                  
              }
            else if (msg.getType().equalsIgnoreCase("D_FILE")) { 
            	System.out.println("选择的文件是： "+msg.getFilenameString());
            	
            	
            	 
                    //File file=new File("C:\\schoolstudy2\\case_chapter7_server\\upload\\"+msg.getFilenameString());
                    //File file=new File("upload\\"+msg.getFilenameString());
            	      File file=new File("upload"+File.separator+msg.getFilenameString());
            	       System.out.println("服务器上传文件路径："+file.getAbsolutePath());
                    //启动发送文件线程
                    SwingWorker<List<String>,String> sender=new FileSenderPrivate(file,msg,parentUI);
                    
                 sender.execute(); 
                 
              
            	  }
                  
              
            
else if (msg.getType().equalsIgnoreCase("OUT_GET")) { 
            	
	        System.out.println("查看离线消息");
	        BufferedReader w=new BufferedReader(new InputStreamReader(new FileInputStream("C:\\schoolstudy2\\case_chapter7_client\\src\\AllFile\\"+msg.getUserId())));
            	
				String x;
				String textString ="" ;
				while((x=w.readLine())!=null) {
					textString=textString+x;
					 
	                      
	              		 }
	              		 msg.setText(textString);
						 msg.setType("RO_GET");
						 byte[] data=Translate.ObjectToByte(msg);
		              	 for (int i=0;i<userList.size();i++) { //遍历用户
		              		 if(userList.get(i).getUserId().equals(msg.getUserId())) {
		                       DatagramPacket oldPacket=userList.get(i).getPacket();
		                       DatagramPacket newPacket=new DatagramPacket(data,data.length,oldPacket.getAddress(),oldPacket.getPort()); 
		                       serverSocket.send(newPacket); //发送
		                       System.out.println(msg.getUserId()+"消息已发送");
				}
				//System.out.println("textstring"+textString);
				//System.out.println(textString);
				/* msg.setText(textString);
				 msg.setType("RO_GET");
				 byte[] data=Translate.ObjectToByte(msg);
              	 for (int i=0;i<userList.size();i++) { //遍历用户
              		 if(userList.get(i).getUserId().equals(msg.getUserId())) {
                       DatagramPacket oldPacket=userList.get(i).getPacket();
                       DatagramPacket newPacket=new DatagramPacket(data,data.length,oldPacket.getAddress(),oldPacket.getPort()); 
                       serverSocket.send(newPacket); //发送
                       System.out.println(msg.getUserId()+"消息已发送");
                      
              		 }*/
                   }   
                  
              }
          /* else if (msg.getType().equalsIgnoreCase("RF_PRIVATE")) { //是M_MSG消息
              	 for (int i=0;i<userList.size();i++) { //遍历用户
              		 if(userList.get(i).getUserId().equals(msg.getTargetId())) {
                       DatagramPacket oldPacket=userList.get(i).getPacket();
                       DatagramPacket newPacket=new DatagramPacket(data,data.length,oldPacket.getAddress(),oldPacket.getPort()); 
                       serverSocket.send(newPacket); //发送
                      System.out.println("消息发送成功");
              		 }
                   }   
                  
              }*/
            
            
            else if (msg.getType().equalsIgnoreCase("M_MSG")) { //是M_MSG消息
                //更新显示
                parentUI.txtArea.append(userId+" 说："+msg.getText()+"\n");
                //转发消息
                for (int i=0;i<userList.size();i++) { //遍历用户
                    DatagramPacket oldPacket=userList.get(i).getPacket();
                    DatagramPacket newPacket=new DatagramPacket(data,data.length,oldPacket.getAddress(),oldPacket.getPort()); 
                    serverSocket.send(newPacket); //发送
                }
            }
              /*else if (msg.getType().equalsIgnoreCase("M_PRIVATE")||msg.getType().equalsIgnoreCase("F_PRIVATE")) { //是M_MSG消息
                    
                   
        	  for(int i=0;i<userList.size();i++) {
        		  if(userList.get(i).equals(msg.getTargetId())) {
        			  DatagramPacket oldPacket=userList.get(i).getPacket();
        		  }
        	  }
                        DatagramPacket newPacket=new DatagramPacket(data,data.length,msg.getToAddr(),msg.getToPort()); 
                        serverSocket.send(newPacket); //发送
                        System.out.println("请求消息发送成功");
                       
            }*/
              else if (msg.getType().equalsIgnoreCase("M_QUIT")) { //是M_QUIT消息
                //更新显示
                parentUI.txtArea.append(userId+" 下线！\n");
                //删除用户
                for(int i=0;i<userList.size();i++) {
                    if (userList.get(i).getUserId().equals(userId)) {
                        userList.remove(i);
                        break;
                    }
                }//end for
                //向其他用户转发下线消息
                for (int i=0;i<userList.size();i++) {
                    DatagramPacket oldPacket=userList.get(i).getPacket();
                    DatagramPacket newPacket=new DatagramPacket(data,data.length,oldPacket.getAddress(),oldPacket.getPort());
                    serverSocket.send(newPacket);
                }//end for
                listModel.remove(listModel.indexOf(userId));
                par.userlist.setModel(listModel);
            }//end if
            
            } catch (IOException | SQLException |  NumberFormatException ex) {  } 
        }//end while
    }//end run
   
}//end class
