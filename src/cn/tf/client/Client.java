package cn.tf.client;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class Client implements Runnable {

	protected Shell shell;
	private SashForm sashForm;
	private Text text;
	private Text text_1;
	private Text text_2;
	private Text text_3;
	private Table table;
	private Text text_4;

	private Socket sk;  //连接对象
	private DataInputStream dis;
	private DataOutputStream  dos;
	private boolean connected=false;
	private String msg;
	private String ip;
	private Integer  port;
	private String name;
	
	
	private Button button ;//登录按钮
	private Button button_2; //发送按钮
	private Display display;
	private boolean isForce=false ;
	
	
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Client window = new Client();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(1032, 562);
		shell.setText("网络聊天室");
		
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setBounds(10, 10, 983, 495);
		
		sashForm = new SashForm(composite, SWT.NONE);
		sashForm.setBounds(27, 84, 0, 0);

		
		Composite composite_4 = new Composite(composite, SWT.NONE);
		composite_4.setBounds(10, 10, 963, 55);
		
		Label label = new Label(composite_4, SWT.NONE);
		label.setText("服务器地址：");
		label.setBounds(10, 10, 97, 20);
		
		text = new Text(composite_4, SWT.BORDER);
		text.setText("12345");
		text.setBounds(355, 7, 73, 26);
		
		Label label_1 = new Label(composite_4, SWT.NONE);
		label_1.setText("用户名：");
		label_1.setBounds(463, 10, 76, 20);
		
		text_1 = new Text(composite_4, SWT.BORDER);
		text_1.setText("192.168.195.2");
		text_1.setBounds(114, 10, 137, 26);
		
		Button button_1 = new Button(composite_4, SWT.NONE);
		
		button_1.setText("退出");
		button_1.setBounds(855, 5, 98, 30);
		
		Label label_2 = new Label(composite_4, SWT.NONE);
		label_2.setBounds(273, 10, 76, 20);
		label_2.setText("端口号：");
		
		 button = new Button(composite_4, SWT.NONE);
	
		button.setBounds(725, 5, 98, 30);
		button.setText("登录");
		
		text_2 = new Text(composite_4, SWT.BORDER);
		text_2.setBounds(565, 7, 115, 26);
		
		SashForm sashForm_1 = new SashForm(composite, SWT.NONE);
		sashForm_1.setBounds(44, 107, 0, 0);
		
		Composite composite_1 = new Composite(composite, SWT.NONE);
		composite_1.setBounds(10, 90, 963, 334);
		
		Group group = new Group(composite_1, SWT.NONE);
		group.setText("聊天记录");
		group.setBounds(0, 0, 755, 324);
		
		text_3 = new Text(group, SWT.BORDER | SWT.MULTI);
		text_3.setBounds(10, 33, 735, 281);
		
		Group group_1 = new Group(composite_1, SWT.NONE);
		group_1.setText("在线用户列表");
		group_1.setBounds(748, 0, 205, 324);
		
		table = new Table(group_1, SWT.BORDER | SWT.FULL_SELECTION);
		table.setBounds(31, 26, 164, 288);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		TableColumn tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setWidth(152);
		tableColumn.setText("在线用户名");
		
		Label label_3 = new Label(composite, SWT.NONE);
		label_3.setBounds(10, 448, 57, 20);
		label_3.setText("内容：");
		
		text_4 = new Text(composite, SWT.BORDER);
		text_4.setBounds(81, 430, 684, 55);
		
		 button_2 = new Button(composite, SWT.NONE);
	
		button_2.setBounds(826, 438, 98, 30);
		button_2.setText("发送");
		
		
		//登录
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				ip=text_1.getText().trim() ;
				port=Integer.parseInt(text.getText().trim());
				name=text_2.getText().trim();
			
				connection();
				
				button.setEnabled(false);
				button_2.setEnabled(true);
				
				
				Thread t=new Thread(Client.this);
				t.start();
				
				
			}
		});
		
		//点击发送
		 button_2.addSelectionListener(new SelectionAdapter() {
			 	@Override
			 	public void widgetSelected(SelectionEvent e) {
			 		String content=text_4.getText().trim();
			 		if(content==null || "".equals(content)){
			 			return;
			 		}else{
			 			try {
							String info="msg "+name+" "+content ;//InetAddress.getLocalHost().getHostAddress();
							
							dos.writeUTF(info);
							dos.flush();
							
							text_4.setText("");
							text_4.setFocus();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
			 			
			 			
			 		}
			 		
			 	}
			 });
		 
		 
		 //点击退出
		 button_1.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						dos.writeUTF("end "+name);
						dos.flush();
						
					} catch (Exception e1) {
						e1.printStackTrace();	
					}
					connected=false;
				}
			});
		

	}
	
	//连接服务器的方法
	private  void  connection(){
		try {
			sk=new Socket(ip,port);
			dis=new DataInputStream(sk.getInputStream());
			dos=new DataOutputStream(sk.getOutputStream());
			
			System.out.println(name+"连接服务器成功");
			connected=true;
			
			//当连接成功后，将当前用户的用户名发送到服务器
			dos.writeUTF("login "+name);
			
			
			
		}  catch (Exception e) {
			connected=false;
			e.printStackTrace();
			
			
		}
	}
	
	
	@Override
	public void run(){
		
		
		while(true){
			try {
			if(connected && sk.isConnected()  &&  !sk.isClosed()){

						msg=dis.readUTF();
						 //获取服务器的回送信息
						if(msg.startsWith("onLineUser ")){
							display.asyncExec(new Runnable(){
								
								@Override
								public void run() {
									table.removeAll();
									msg=msg.substring(msg.indexOf(" ")+1);
									String[] users=msg.split(",");

									TableItem ti=null;
										for(int i=0,len=users.length;i<len;i++){
											ti=new TableItem(table, SWT.NONE);
											ti.setText(new String[]{users[i]});	
										}		
								}
							});		
						}else if(msg.startsWith("msg ")){
							
							final String[] str=msg.split(" ");
							display.asyncExec(new Runnable() {
								
								@Override
								public void run() {
									text_3.append("\n"+str[1]+ " 说:\n\t"+str[2]);
								}
							});
							
						}else if(msg.startsWith("end ")){	
							break;
						}else if(msg.startsWith("again ")){		
							isForce=true;
							connected=false;
							break;
							
						}	
						
					}
				} catch (IOException e) {
						e.printStackTrace();
				} 
			}
			
			display.asyncExec(new Runnable(){
				@Override
				public void run() {
					if(isForce){
						MessageDialog.openError(shell,"下线通知","您的账号已在其他地方登录，若非本人操作，请及时修改密码...");	
					}
					shell.dispose();	
				}
			});
			
		}
}
