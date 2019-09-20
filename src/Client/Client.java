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
	//��ȡ�������˽ӿڴ������
	//service:����Ľӿ�
	//addr:���������˵�ip:�˿�
	@SuppressWarnings("unchecked")
	public static <T> T getRemoteProxyObj(Class serviceInterface,InetSocketAddress addr) {
		/*
		 * newProxyInstance(a,b,c)
		 * a:�����������Ҫ��������������
		 * b:��Ҫ�������߱���Щ�������ýӿڻ�ȡ����
		 */
		return (T)Proxy.newProxyInstance(serviceInterface.getClassLoader(), new Class<?>[] {serviceInterface}, new InvocationHandler() {

			@Override
			/*
			 * proxy:�������
			 * method:�ĸ�����
			 * args:��������
			 */
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				// TODO �Զ����ɵķ������
				Socket socket = new Socket();
				ObjectOutputStream output = null;
				ObjectInputStream input = null;
				try {
					socket.connect(addr);
					
					//��������
				    output = new ObjectOutputStream(socket.getOutputStream());
					//�ӿ�������������writeUTF
					output.writeUTF(serviceInterface.getName());
					output.writeUTF(method.getName());
					//�������������͡���������Object
					output.writeObject(method.getParameterTypes());
					output.writeObject(args);
					
					//���շ���˵ķ���ֵ
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
						// TODO �Զ����ɵ� catch ��
						e.printStackTrace();
					}
				}
			}	
		});
	}

}
