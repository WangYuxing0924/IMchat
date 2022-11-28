package cn.edu.ldu;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;

public class Expression extends JFrame {

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Expression frame = new Expression();
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
	public Expression() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(2,8));
		JLabel []icon=new JLabel[5];
		JLabel lblNewLabel_1 = new JLabel("开心");
		lblNewLabel_1.setSize(120, 130);
		lblNewLabel_1.setIcon(new ImageIcon(Expression.class.getResource("/cn/edu/ldu/images/1.jpg")));
		contentPane.add(lblNewLabel_1);
		icon[0]=lblNewLabel_1;
		JLabel lblNewLabel_2 = new JLabel("喜欢");
		lblNewLabel_2.setIcon(new ImageIcon(Expression.class.getResource("/cn/edu/ldu/images/3.jpg")));
		contentPane.add(lblNewLabel_2);
		icon[1]=lblNewLabel_2;
		JLabel lblNewLabel = new JLabel("难过");
		lblNewLabel.setIcon(new ImageIcon(Expression.class.getResource("/cn/edu/ldu/images/2.jpg")));
		contentPane.add(lblNewLabel);
		icon[2]=lblNewLabel;
		JLabel lblNewLabel_3 = new JLabel("生气");
		lblNewLabel_3.setIcon(new ImageIcon(Expression.class.getResource("/cn/edu/ldu/images/4.jpg")));
		contentPane.add(lblNewLabel_3);
		icon[3]=lblNewLabel_3;
		JButton btnNewButton = new JButton("发送");
		
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				
				
				
				
				
			}
		});
		contentPane.add(btnNewButton);
		
		JLabel lblNewLabel_4 = new JLabel("赞同");
		lblNewLabel_4.setIcon(new ImageIcon(Expression.class.getResource("/cn/edu/ldu/images/5.jpg")));
		contentPane.add(lblNewLabel_4);
		icon[4]=lblNewLabel_4;
		for(int i=0;i<icon.length;i++) {
			icon[i].addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if(e.getButton()==1) {
						JLabel cubLabel=(JLabel)e.getSource();
						
					}
				}
			});
		}
	}


}
