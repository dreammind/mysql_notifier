import gen_java.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.apache.thrift.TException;
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

public class MyPublisher {
    public static class Peer {
	public Peer(String host, int port, TTransport transport) {
	    this.host = host;
	    this.port = port;
	    this.transport = transport;
	}
	public String host;
	public int port;
	public TTransport transport;
    }

    public static class PublisherHandler implements Publisher.Iface {
	private long subscriptionId;
	private Map<Integer, Map<Long,Peer>> subscribers = 
	    new HashMap<Integer, Map<Long,Peer>>();

	public List<TUserAttribute> get_user_attributes(int user_id) 
	    throws TException {
	    // TODO implement
	    return null;
	}

	public long subscribe(int user_id, String peer_host, int peer_port) 
	    throws TException {
	    if (user_id  < 0) {
		return 0;
	    }

	    TTransport sub_transport = new TSocket(peer_host, peer_port);
	    TProtocol protocol = new TBinaryProtocol(sub_transport);
	    Subscriber.Client client = new Subscriber.Client(protocol);
	    sub_transport.open();

	    ++subscriptionId;
	    if (subscribers.get(user_id) == null) {
		subscribers.put(user_id, new HashMap<Long, Peer>());
	    }
	    Peer peer = new Peer(peer_host, peer_port, sub_transport);
	    Map<Long,Peer> map = subscribers.get(user_id);
	    map.put(subscriptionId, peer);
	    return subscriptionId;
	}

	public void publish(List<TUserAttribute> user_attributes) 
	    throws TException {
	    // TODO implement
	}
    }

    public static void main(String[] args) throws Exception {
	TServerTransport transport = new TServerSocket(9090);

	PublisherHandler handler = new PublisherHandler();
	Publisher.Processor processor = new Publisher.Processor(handler);
	TServer server = new TThreadPoolServer(processor, transport);

	System.out.println("Starting the Publisher server...");
	server.serve();
    }
}
