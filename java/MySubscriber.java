import gen_java.*;

import java.util.List;
import java.util.ArrayList;

import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;

public class MySubscriber {
    public static class SubscriberHandler implements Subscriber.Iface {
	public void notify(List<TUserAttribute> user_attributes) throws TException {
	    for (TUserAttribute attr : user_attributes) {
		System.out.println(attr.toString());
	    }
	}
    }

    public static void main(String[] args) throws Exception {
	TServerTransport transport = new TServerSocket(9091);
	SubscriberHandler handler = new SubscriberHandler();
	Subscriber.Processor processor = new Subscriber.Processor(handler);
	final TServer server = new TThreadPoolServer(processor, transport);

	new Thread() {
	    public void run() {
		try {
		    System.out.println("Starting the server...");
		    server.serve();
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	}.start();


	TTransport pub_transport = new TSocket("localhost", 9090);
	TProtocol protocol = new TBinaryProtocol(pub_transport);
	Publisher.Client client = new Publisher.Client(protocol);

	try {
	    pub_transport.open();
	    TUserAttribute attr = new TUserAttribute("a", "b", 1);

	    List<TUserAttribute> attributes = new ArrayList<TUserAttribute>();
	    attributes.add(attr);
	    client.publish(attributes);

	    client.subscribe(0, "localhost", 9091);
	    Thread.sleep(120 * 1000);

	} finally {
	    pub_transport.close();
	}
    }
}