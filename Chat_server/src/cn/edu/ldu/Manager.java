package cn.edu.ldu;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.IOException;
import java.net.DatagramPacket;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.derby.iapi.error.PublicAPI;

import cn.edu.ldu.util.Message;
import cn.edu.ldu.util.Translate;

import javax.swing.JScrollPane;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JList;
import java.awt.Color;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Manager extends JFrame {

	private JPanel contentPane;
    public JList<String> userlist;
    public  byte[] data=new byte[8096];
    
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Manager frame = new Manager();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Manager() {
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		JScrollPane scrollPane = new JScrollPane();
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 426, GroupLayout.PREFERRED_SIZE)
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 253, GroupLayout.PREFERRED_SIZE)
		);
		
		JLabel lblNewLabel = new JLabel("在线用户列表");
		lblNewLabel.setBackground(Color.PINK);
		scrollPane.setColumnHeaderView(lblNewLabel);
		
		 userlist = new JList();
		 userlist.setToolTipText("");
		userlist.setBackground(Color.CYAN);
		scrollPane.setViewportView(userlist);
		
		JButton btnNewButton = new JButton("服务器踢人");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					
				
				 //更新显示
				 Message msg=new Message();
				String userId=userlist.getSelectedValue();
                ReceiveMessage.parentUI.txtArea.append(userId+"被踢下线！\n");
                
                //删除用户
                for(int i=0;i<ReceiveMessage.userList.size();i++) {
                    if (ReceiveMessage.userList.get(i).getUserId().equals(userId)) {
                    	 DatagramPacket oldPacket=ReceiveMessage.userList.get(i).getPacket();
                    	 
                    	 msg.setUserId(userId);
                    	 msg.setType("F_QUIT");
                    	 data=Translate.ObjectToByte(msg);
                         DatagramPacket newPacket=new DatagramPacket(data,data.length,oldPacket.getAddress(),oldPacket.getPort());
                         ReceiveMessage.serverSocket.send(newPacket);
                        ReceiveMessage.userList.remove(i);
                        break;
                    }
                }//end for
                //向其他用户转发下线消息
                for (int i=0;i<ReceiveMessage.userList.size();i++) {
                    DatagramPacket oldPacket=ReceiveMessage.userList.get(i).getPacket();
                    msg.setType("MF_QUIT");
                    data=Translate.ObjectToByte(msg);
                    DatagramPacket newPacket=new DatagramPacket(data,data.length,oldPacket.getAddress(),oldPacket.getPort());
                    ReceiveMessage.serverSocket.send(newPacket);
                }//end for
                ReceiveMessage.listModel.remove(ReceiveMessage.listModel.indexOf(userId));
                ReceiveMessage.par.userlist.setModel(ReceiveMessage.listModel);
				
				} catch (Exception e2) {
					// TODO: handle exception
				}
				
				
				
				
				
			}
		});
		scrollPane.setRowHeaderView(btnNewButton);
		contentPane.setLayout(gl_contentPane);
	
	
	
	 
	}
}
