package cn.tf.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Label;

public class Server {

	protected Shell shell;
	
	private Text text;
	private Table table;
	private Display display;
	private boolean  isRun=false;  //服务器启停标志
	private ServerSocket  ssk;  //服务器

	private List<OnLineClient>  clients=new ArrayList<OnLineClient>();
	
	private Button btnNewButton;
	private String port;

	
	private List<String> userList=new ArrayList<String>();
	
	
	
	
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Server window = new Server();
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
		shell=new Shell(SWT.MIN);	
		
		shell = new Shell();
		shell.setSize(832, 565);
		shell.setText("网络聊天服务器");
		
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setBounds(10, 10, 814, 450);
		
		text = new Text(composite, SWT.BORDER);
		text.setText("12345");
		text.setBounds(170, 48, 234, 26);
		
		btnNewButton = new Button(composite, SWT.NONE);
	
		btnNewButton.setBounds(430, 46, 98, 30);
		btnNewButton.setText("启动服务器");
		
		Button btnNewButton_1 = new Button(composite, SWT.NONE);
	
		btnNewButton_1.setBounds(593, 46, 98, 30);
		btnNewButton_1.setText("关闭服务器");
		
		table = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
		table.setBounds(100, 119, 620, 296);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		TableColumn tblclmnIp = new TableColumn(table, SWT.NONE);
		tblclmnIp.setWidth(305);
		tblclmnIp.setText("ip地址");
		
		TableColumn tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setWidth(519);
		tableColumn.setText("用户名");
		
		Label label = new Label(composite, SWT.NONE);
		label.setBounds(46, 48, 76, 20);
		label.setText("端口号：");
		
		
		//关闭服务器
		btnNewButton_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				isRun=false;
				btnNewButton.setEnabled(true);
			}
		});
		
		//启动服务器
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				port=text.getText().trim();
				new Thread(){
					@Override
					public void run() {
						startServer();
					}
				}.start();
				btnNewButton.setEnabled(false);
				System.out.println("服务器启动了");
			}
		});
	}
	
	
	
	public void startServer(){
		
		try {
			ssk=new ServerSocket(Integer.parseInt(port));
			isRun=true;
		} catch (Exception e1) {
			isRun=false;
			System.out.println("服务器启动失败，请检查端口是否被占用..");
		}
		
		while(isRun){
			try {
				Socket sk=ssk.accept();  //当有客户端连接上来时
				System.out.println(sk.getInetAddress().getHostAddress()+"连接上来了");
				new Thread(new OnLineClient(sk)).start();
				
			} catch (Exception e1) {
				e1.printStackTrace();
				isRun=false;
				MessageDialog.openError(shell, "失败","服务器已宕机..");
				btnNewButton.setEnabled(true);
			}
		}
	}
	
	
	//在线用户对象
	
	public class OnLineClient  implements  Runnable{
		
		private Socket  sk;   //
		private String name;  //当前在线用户的用户名
		private String ip;   //当前在线用户使用的ip地址
		private boolean connected=false;  //当前用户的在线状态
		private DataInputStream  dis;  //用户读取当前用户发送的信息
		private DataOutputStream dos;  //用户向当前用户发送信息
		 
		
		public String getIp() {
			return this.ip;
		}
		
		public String getName() {
			return this.name;
		}

		public OnLineClient(Socket sk){
			try {
				this.sk=sk;
				ip=sk.getInetAddress().getHostAddress();
				
				dis=new DataInputStream(sk.getInputStream());
				dos=new DataOutputStream(sk.getOutputStream());
				
				connected=true;  //连接成功
			} catch (IOException e) {
				connected=false;
				MessageDialog.openError(shell, "失败", name+"与服务器失去连接..");
				e.printStackTrace();
			}
			
		}
		
		
		//服务器向用户发送信息的方法
		public void send(String msg){
			try {
				dos.writeUTF(msg);
			} catch (IOException e) {
				connected=false;
				MessageDialog.openError(shell, "失败", name+"已经下线了...");
				e.printStackTrace();
				clients.remove(this); 
			}
		}
		
		
		
		@Override
		public void run() {
		
				try {
					while(connected){
					//如果当前用户还在线
					String info=dis.readUTF(); //读取用户发送的信息,服务器之负责转发数据
					
					boolean isExist=false;
					OnLineClient  isRemoveObj=null;
					if(info.startsWith("login ")){  //说明是用户的登录请求 
						name=info.substring(info.indexOf(" ")+1);
						
						for(OnLineClient  olc:clients){
							if(name.equals(olc.getName())){
								//说明已经登录了
								isExist=true;
								
								isRemoveObj=olc;
								olc.send("again ");
								break;
							}
						}
					
						
						if(isExist){
							clients.remove(isRemoveObj);
							userList.remove(name);
							
							//将当前用户从服务器列表中移除
							display.asyncExec(new Runnable(){
								@Override
								public void run() {
									TableItem[] tis=table.getItems();
									for (int i=0,len=tis.length;i<len;i++) {
										if(name.equals(tis[i].getText(1).trim())){
											table.remove(i);
											
											StringBuffer sb=new StringBuffer();
											for(String str:userList){
												sb.append(str+",");
											}
											//将当前用户发送给所有在线用户
											for(OnLineClient  ct:clients){
												ct.send("onLineUser "+sb.toString());
											}
											
											break;
										}
									}
								}
							});
						}
						
							//将当前用户加入到在线用户列表中
							clients.add(this);
							userList.add(name);  //添加到用户列表
							
							//在服务器列表中显示当前用户
							display.asyncExec(new Runnable(){
								@Override
								public void run() {
									TableItem ti=new TableItem(table, SWT.NONE);
									ti.setText(new String[]{ip,name});
								}
							});
							
							StringBuffer sb=new StringBuffer();
							for(String str:userList){
								sb.append(str+",");
							}
							//将当前用户发送给所有在线用户
							for(OnLineClient  ct:clients){
								ct.send("onLineUser "+sb.toString());
							}
						
						
					}else if(info.startsWith("end ")){  //说明是用户的下线请求
						final String outName=info.substring(info.indexOf(" ")+1);
						
						clients.remove(this); 
						userList.remove(outName);
						
						StringBuffer sb=new StringBuffer();
						for(String str:userList){
							sb.append(str+",");
						}
						//将当前用户发送给所有在线用户
						for(OnLineClient  ct:clients){
							ct.send("onLineUser "+sb.toString());
						}
						
						//通知用户下线
						this.send("end ");
						connected=false;
						//将当前用户从服务器列表中移除
						display.asyncExec(new Runnable(){
							@Override
							public void run() {
								TableItem[] tis=table.getItems();
								for (int i=0,len=tis.length;i<len;i++) {
									if(outName.equals(tis[i].getText(1).trim())){
										table.remove(i);
										break;
									}
								}
							}
						});
						
						
					}else if(info.startsWith("msg ")){  //用户的聊天记录请求
						
						//向所有的在线用户发送的信息
						for(OnLineClient  olc:clients){
							olc.send(info);
						}
					  }
				   }
				} catch (IOException e) {
					connected=false;
					System.out.println("已经下线了...");
					e.printStackTrace();	
					clients.remove(this);  //如果当前用户已经下线，需要将其从在线用户列表中删除
				}finally{
					if(sk!=null){
						try {
							sk.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
