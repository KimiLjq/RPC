package Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client {
	//获取代表服务端接口代理对象
	//service:请求的接口
	//addr:代请求服务端的ip:端口
	@SuppressWarnings("unchecked")
	public static <T> T getRemoteProxyObj(Class serviceInterface,InetSocketAddress addr) {
		/*
		 * newProxyInstance(a,b,c)
		 * a:类加载器：需要代理类的类加载器
		 * b:需要代理对象具备哪些方法，用接口获取方法
		 */
		return (T)Proxy.newProxyInstance(serviceInterface.getClassLoader(), new Class<?>[] {serviceInterface}, new InvocationHandler() {

			@Override
			/*
			 * proxy:代理对象
			 * method:哪个方法
			 * args:方法参数
			 */
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				// TODO 自动生成的方法存根
				Socket socket = new Socket();
				ObjectOutputStream output = null;
				ObjectInputStream input = null;
				try {
					socket.connect(addr);
					
					//发送数据
				    output = new ObjectOutputStream(socket.getOutputStream());
					//接口名，方法名：writeUTF
					output.writeUTF(serviceInterface.getName());
					output.writeUTF(method.getName());
					//方法参数的类型、方法参数Object
					output.writeObject(method.getParameterTypes());
					output.writeObject(args);
					
					//接收服务端的返回值
					input = new ObjectInputStream(socket.getInputStream());
					Object result = input.readObject();
					return result;
				}catch(Exception e) {
					e.printStackTrace();
					return null;
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
		});
	}

}
