
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLEncoder;

import javax.swing.*;

/*
 * function һ����ҳ��ȡ�Ŀͻ��ˣ��ṩ���󷽷���GET,DELETE,HEAD,OPTIONS,ERRORREQUEST
 * @param isDebug boolean��	 ���ڵ���
 * @author �����  2016/6/9
 */
public class WebClient {
	public static boolean isDebug = true;
	
	public static void main(String args[]){
		javax.swing.SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run(){
				createGUIAndShow();
			}
		});
	}
	
	//����GUI�İ�װ����
	public static void createGUIAndShow(){
		new NetWin();
	}
}


/*
 * function ͬʱ�̳�JFrame,ActionListener,Runnale���ڲ��Բ�ͬ������ʽ��ȡ��ҳ��Ϣ
 * @author  �����  2016/6/9
 */
class NetWin extends JFrame implements ActionListener,Runnable{
	JButton GETBtn;
	JButton DELETEBtn;
	JButton HEADBtn;
	JButton OPTIONSBtn;
	JButton ERRORBtn;
	
	JTextField urlInputField;
	JTextArea contentShowArea;
	
	static final String URLSTR_DEFAULT = "http://localhost/index.html";
	
	String method = "GET";
	String encode = "UTF-8";
	Thread thread = null;
	
	NetWin(){
		//��ť��
		GETBtn = new JButton("GET");
		GETBtn.addActionListener(this);
		DELETEBtn = new JButton("DELETE");
		DELETEBtn.addActionListener(this);
		HEADBtn = new JButton("HEAD");
		HEADBtn.addActionListener(this);
		OPTIONSBtn = new JButton("OPTIONS");
		OPTIONSBtn.addActionListener(this);
		ERRORBtn = new JButton("ERROR");
		ERRORBtn.addActionListener(this);
		//��ַ�����
		urlInputField = new JTextField(20);
		urlInputField.setText(URLSTR_DEFAULT);
		//������ʾ����
		contentShowArea = new JTextArea(12,12);
		
		//���ð�ť����ַ������Լ���ʾ����
		Box baseBox = Box.createVerticalBox();
		Box urlBox = Box.createHorizontalBox();
		urlBox.add(new JLabel("������ַ"));
		urlBox.add(urlInputField);
		Box btnBox = Box.createHorizontalBox();
		btnBox.add(GETBtn);
		btnBox.add(DELETEBtn);
		btnBox.add(HEADBtn);
		btnBox.add(OPTIONSBtn);
		btnBox.add(ERRORBtn);
		baseBox.add(urlBox);
		baseBox.add(Box.createVerticalStrut(5));
		baseBox.add(btnBox);
		
		JPanel pane = new JPanel();
		pane.add(baseBox);
		
		this.add(pane,BorderLayout.NORTH);
		this.add(new JScrollPane(contentShowArea),BorderLayout.CENTER);
		
		setBounds(60,60,540,490);
		this.setMaximumSize(new Dimension(540,490));
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		thread = new Thread(this);
	}
	
	/*
	 * function ͨ�������ť����ȡ��ͬ���������ͣ�������������ҳץȡ�߳�
	 */
	@Override
	public void actionPerformed(ActionEvent e){
		if(WebClient.isDebug){
			System.out.println("���");
		}
		if(e.getSource() == GETBtn){
			method = "GET";
		}else if(e.getSource() == DELETEBtn){
			method = "DELETE";
		}else if(e.getSource() == HEADBtn){
			method = "HEAD";
		}else if(e.getSource() == OPTIONSBtn){
			method = "OPTIONS";
		}else if(e.getSource() == ERRORBtn){
			method = "ERRORREQUEST";
		}else{
			contentShowArea.setText("�����ڲ�����");
			return;
		}
		if(WebClient.isDebug){
			System.out.println(method);
		}
		if(!thread.isAlive()){
			new Thread(this).start();
		}else{
			JOptionPane.showMessageDialog(this, "���ڼ�����Դ�����Ժ�");
		}
	}
	
	@Override
	public void run(){
		URL url;
		BufferedWriter wr = null;
		BufferedReader br = null;
		Socket socket = null;
		
		contentShowArea.setText(null);
		try {
			url =  new URL(urlInputField.getText().trim());
			String host = url.getHost();		 
			int port = 80;
			
			socket = new Socket();
			SocketAddress address = new InetSocketAddress(host,port);
			
			socket.connect(address);
			socket.setSoTimeout(10000);//����10��ȴ�ʱ��
			
			String path = url.getPath() == null?"/":url.getPath();
			
			wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "ASCII"));
			
			if(WebClient.isDebug){
				System.out.println("host: " + host);
				System.out.println("port: " + port);
				System.out.println("method: " + method);
			}
			
			//����������Ϣ����
			String params = URLEncoder.encode("param1", encode) + "=" + URLEncoder.encode("value1", encode);
			params += "&" + URLEncoder.encode("param2", encode) + "=" + URLEncoder.encode("value2", encode);
			
			//������Ϣͷ��
			wr.write(method + " " + path + " HTTP/1.0\r\n");
			wr.write("HOST:" + host + "\r\n");
			wr.write("Content-Length: " + params.length() + "\r\n");
			wr.write("Content-Type: application/x-www-form-urlencoded\r\n");
			wr.write("\r\n");
			//����������Ϣ����
			wr.write(params);
			wr.flush();
			if(WebClient.isDebug){
				System.out.println("ָ������");
			}
			
			//��ʼ����
			br = new BufferedReader(new InputStreamReader(socket.getInputStream(), encode));
			String line = null;
			
			while ((line = br.readLine()) != null) {
				contentShowArea.append(line);
				contentShowArea.append("\r\n");
//				System.out.println(line);
			}
			
			if(WebClient.isDebug){
				System.out.println("�������");
			}
		} catch (IOException e) {
			e.printStackTrace();
			contentShowArea.setText("" + e);
		} finally{
			if(socket != null && !socket.isClosed() && socket.isConnected()){
				try {
					socket.close();
				} catch (IOException e) {
					//�����������Ŷ�
					e.printStackTrace();
				}
			}
			if(wr != null){
				try {
					wr.close();
				} catch (IOException e) {
					//�����������Ŷ�
					e.printStackTrace();
				}
			}
		}
	}	
	
}