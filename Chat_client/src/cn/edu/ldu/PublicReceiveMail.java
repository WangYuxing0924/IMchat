package cn.edu.ldu;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.security.KeyStore;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

public class PublicReceiveMail extends Thread{
	
	ClientUI clientUI;
	private int baseHostPort;
	public int hostPort=0;
	
	public PublicReceiveMail(ClientUI clientUI,int hostport){
		this.clientUI=clientUI;
		this.baseHostPort=hostport;
	}
   public void run() {
	   try {
           //获取客户机证书库
           InputStream key =PrivateClientUI.class.getResourceAsStream("/cn/edu/ldu/keystore/server.keystore");//私钥库
           InputStream tkey =PrivateClientUI.class.getResourceAsStream("/cn/edu/ldu/keystore/tserver.keystore");//公钥库
           String SERVER_KEY_STORE_PASSWORD = "123456"; //server.keystore密码
           String SERVER_TRUST_KEY_STORE_PASSWORD = "123456";//tserver.keystore密码

           SSLContext ctx = SSLContext.getInstance("SSL");//SSL上下文
           KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
           TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
           KeyStore ks = KeyStore.getInstance("JKS");
           KeyStore tks = KeyStore.getInstance("JKS");
           //加载私钥证书库
           ks.load(key, SERVER_KEY_STORE_PASSWORD.toCharArray());
           //加载公钥证书库
           tks.load(tkey, SERVER_TRUST_KEY_STORE_PASSWORD.toCharArray());
           kmf.init(ks, SERVER_KEY_STORE_PASSWORD.toCharArray());
           tmf.init(tks);
           ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
           //服务器侦听安全连接
           SSLServerSocket sslListenSocket = null;
           
           while(true) {
  		     try {
  			   sslListenSocket = (SSLServerSocket) ctx.getServerSocketFactory().createServerSocket(baseHostPort);
  			   break;
  		     } catch (IOException e) {
  			   // TODO Auto-generated catch block
  			   //e.printStackTrace();
  			   baseHostPort=baseHostPort+1;
  			  }   
  		   }
              hostPort = baseHostPort;
           int processors=Runtime.getRuntime().availableProcessors();//CPU数
           ExecutorService fixedPool=Executors.newFixedThreadPool(processors*2);//创建固定大小线程池     
           while (true) { //处理所有客户机连接
               SSLSocket fileSocket=(SSLSocket)sslListenSocket.accept();//如果无连接，则阻塞，否则接受连接并创建新的会话套接字
               //文件接收线程为SwingWorker类型的后台工作线程
               
               SwingWorker<List<String>,String> recver=new PublicRecvFile(fileSocket,clientUI,tks,ks); //创建客户线程
               //********xfxf 加上进度条**********
               recver.addPropertyChangeListener(new PropertyChangeListener() {
                   public  void propertyChange(PropertyChangeEvent evt) {
                       if ("progress".equals(evt.getPropertyName())) {
                    	   clientUI.progressBar.setValue((Integer)evt.getNewValue());
                       }
                   }
               });
               fixedPool.execute(recver); //用线程池调度客户线程运行
           }//end while 
                 
           
       }   catch (Exception e) {
		// TODO: handle exception
    	   JOptionPane.showMessageDialog(null, e.getMessage(), "错误提示", JOptionPane.ERROR_MESSAGE);
	}
   }
  
   }
	


