package Service;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerCenter implements Server {
	private static HashMap<String,Class> serviceRegiser = new HashMap();
	public static int port;
	private static boolean isRunning = false;
	//连接池：连接池中存在多个连接对象，每个连接对象都可以处理一个客户请求
	private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	
	public ServerCenter(int port) {
		this.port = port;
	}

	@SuppressWarnings("deprecation")
	@Override
	//开启服务
	public void start() {
		// TODO 自动生成的方法存根
		ServerSocket server = null ;
		try {
			server = new ServerSocket();
			//设置端口
			server.bind(new InetSocketAddress(port));
		} catch (IOException e1) {
			// TODO 自动生成的 catch 块
			e1.printStackTrace();
		}
		
		isRunning = true;
		while(isRunning) {
			System.out.println("Start Server...");
			Socket socket = null;
			try {
				socket = server.accept();//等待客户端连接
			}catch (IOException e) {
				e.printStackTrace();
			}
			executor.execute(new ServiceTask(socket));
		}
		
		
		

	}

	@Override
	public void stop() {
		// TODO 自动生成的方法存根
		isRunning = false;
		executor.shutdown();

	}

	@Override
	public void register(Class service,Class serviceImpl) {
		// TODO 自动生成的方法存根
		serviceRegiser.put(service.getName(), serviceImpl);

	}
	
	private static class ServiceTask implements Runnable{
		private Socket socket;
		public ServiceTask() {
			
		}
		
		public ServiceTask(Socket socket) {
			this.socket = socket;
		}
		
		public void run() {
			ObjectInputStream input = null;
			ObjectOutputStream output = null;
			try {
				input = new ObjectInputStream(socket.getInputStream());
				//ObjectInputStream对发送数据的顺序有严格要求，所以按发送的顺序获取逐个接收
				String serviceName = input.readUTF();
				String methodName = input.readUTF();
				Class[] parameterTypes = (Class[])input.readObject();
				Object[] arguments = (Object[])input.readObject();
				//到map中查找对应的具体接口
				Class ServiceClass = serviceRegiser.get(serviceName);
				Method method = ServiceClass.getMethod(methodName, parameterTypes);
				//方法执行
				Object result = method.invoke(ServiceClass.getDeclaredConstructor().newInstance(), arguments);
				//将方法返回值返回给客户端
				output = new ObjectOutputStream(socket.getOutputStream());
				output.writeObject(result);
				
			} catch (Exception e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}finally {		
				try {
					if(output != null)
						output.close();
					if(input != null)
						input.close();
				} catch (IOException e) {
						// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
				
			}
		}
		
	}

}
