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
	//���ӳأ����ӳ��д��ڶ�����Ӷ���ÿ�����Ӷ��󶼿��Դ���һ���ͻ�����
	private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	
	public ServerCenter(int port) {
		this.port = port;
	}

	@SuppressWarnings("deprecation")
	@Override
	//��������
	public void start() {
		// TODO �Զ����ɵķ������
		ServerSocket server = null ;
		try {
			server = new ServerSocket();
			//���ö˿�
			server.bind(new InetSocketAddress(port));
		} catch (IOException e1) {
			// TODO �Զ����ɵ� catch ��
			e1.printStackTrace();
		}
		
		isRunning = true;
		while(isRunning) {
			System.out.println("Start Server...");
			Socket socket = null;
			try {
				socket = server.accept();//�ȴ��ͻ�������
			}catch (IOException e) {
				e.printStackTrace();
			}
			executor.execute(new ServiceTask(socket));
		}
		
		
		

	}

	@Override
	public void stop() {
		// TODO �Զ����ɵķ������
		isRunning = false;
		executor.shutdown();

	}

	@Override
	public void register(Class service,Class serviceImpl) {
		// TODO �Զ����ɵķ������
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
				//ObjectInputStream�Է������ݵ�˳�����ϸ�Ҫ�����԰����͵�˳���ȡ�������
				String serviceName = input.readUTF();
				String methodName = input.readUTF();
				Class[] parameterTypes = (Class[])input.readObject();
				Object[] arguments = (Object[])input.readObject();
				//��map�в��Ҷ�Ӧ�ľ���ӿ�
				Class ServiceClass = serviceRegiser.get(serviceName);
				Method method = ServiceClass.getMethod(methodName, parameterTypes);
				//����ִ��
				Object result = method.invoke(ServiceClass.getDeclaredConstructor().newInstance(), arguments);
				//����������ֵ���ظ��ͻ���
				output = new ObjectOutputStream(socket.getOutputStream());
				output.writeObject(result);
				
			} catch (Exception e) {
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
			}finally {		
				try {
					if(output != null)
						output.close();
					if(input != null)
						input.close();
				} catch (IOException e) {
						// TODO �Զ����ɵ� catch ��
					e.printStackTrace();
				}
				
			}
		}
		
	}

}
