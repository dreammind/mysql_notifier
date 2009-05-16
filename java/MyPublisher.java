import gen_java.*;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import java.util.ArrayList;

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

import net.java.ao.EntityManager;
import net.java.ao.schema.UnderscoreFieldNameConverter;
import net.java.ao.schema.UnderscoreTableNameConverter;

import java.sql.SQLException;

public class MyPublisher {
    public static class PublisherHandler implements Publisher.Iface, Runnable {
	private EntityManager entityManager;
	private long subscriptionId;
	private Map<Integer, Map<Long,Peer>> subscribers = 
	    new HashMap<Integer, Map<Long,Peer>>();

	public List<TUserAttribute> get_user_attributes(int user_id) 
	    throws TException {
	    try {
		UserAttribute[] attrs = entityManager.find(
			   UserAttribute.class, "user_id = ?", user_id);
		List<TUserAttribute> tattrs = new ArrayList<TUserAttribute>();
		for (UserAttribute attr : attrs) {
		    tattrs.add(
		       new TUserAttribute(attr.getK(), attr.getV(), 
					  attr.getUser().getId()));
		}
	    } catch(SQLException e) {
		throw new TException(e);
	    }

	    return new ArrayList<TUserAttribute>();
	}

	public long subscribe(int user_id, String peer_host, int peer_port) 
	    throws TException {
	    if (user_id  < 0) {
		return 0;
	    }

	    Peer peer = new Peer(peer_host, peer_port);
	    peer.open();

	    ++subscriptionId;
	    if (subscribers.get(user_id) == null) {
		subscribers.put(user_id, new HashMap<Long, Peer>());
	    }
	    Map<Long,Peer> map = subscribers.get(user_id);
	    map.put(subscriptionId, peer);
	    System.out.println("[debug] subscribed. subscriptionId:" + subscriptionId);
	    return subscriptionId;
	}

	public void publish(List<TUserAttribute> user_attributes) 
	    throws TException {
	    try {
		for (TUserAttribute tattr : user_attributes) {
		    User[] users = entityManager.find(
			      User.class, "id = ?", tattr.user_id);
		    if (users.length == 0) {
			continue;
		    }
		    Map<String,Object> map = new HashMap<String,Object>();
		    map.put("k", tattr.getK());
		    map.put("v", tattr.getV());
		    map.put("user_id", tattr.user_id);
		    UserAttribute attr = entityManager.create(UserAttribute.class, map);
		    attr.save();
		}
	    } catch(SQLException e) {
		throw new TException(e);
	    }
	}

	public void run() {
	    while(true) {
		try {
		    Thread.sleep(1 * 1000);
		    EventQueue[] events = entityManager.find(EventQueue.class);
		    if (events.length > 0) {
			notifies(events);
			entityManager.delete(events);
		    }
		} catch (InterruptedException e) {
		    e.printStackTrace();
		    break;
		} catch (SQLException e) {
		    e.printStackTrace();
		}
	    }
	}

	private void notifies(EventQueue[] events) {
	    Map<Integer,List<TUserAttribute>> uMap = 
		new HashMap<Integer,List<TUserAttribute>>();
	    List<TUserAttribute> allAttrs = new ArrayList<TUserAttribute>();
	    for (EventQueue event : events) {
		UserAttribute uattr = event.getUserAttribute();
		TUserAttribute tattr = 
		    new TUserAttribute(uattr.getK(), uattr.getV(), 
				       uattr.getUser().getId());
		if (uMap.get(tattr.user_id) == null) {
		    uMap.put(tattr.user_id, new ArrayList<TUserAttribute>());
		}
		uMap.get(tattr.user_id).add(tattr);

		if (subscribers.get(0) != null) {
		    allAttrs.add(tattr);
		}
	    }
	    if (subscribers.get(0) != null) {
		notifiesSub(subscribers.get(0), allAttrs);
	    }

	    for (List<TUserAttribute> list : uMap.values()) {
		Map<Long, Peer> pMap;
		if ((pMap = subscribers.get(list.get(0))) == null) {
		    continue;
		}
		notifiesSub(pMap, list);
	    }

	}

	private void notifiesSub(Map<Long,Peer> map, List<TUserAttribute> tattrs) {
	    List<Long> closed = new ArrayList<Long>();
	    for (Map.Entry<Long,Peer> entry : map.entrySet()) {
		try {
		    entry.getValue().client.notify(tattrs);
		} catch(Exception e) {
		    closed.add(entry.getKey());
		}
	    }
	    for (Long subId : closed) {
		Peer peer = map.remove(subId);
		peer.close();
	    }
	}

	public PublisherHandler(EntityManager manager) {
	    this.entityManager = manager;
	}
    }

    public static void main(String[] args) throws Exception {
	EntityManager manager = 
	    new EntityManager("jdbc:mysql://localhost/test", "foo", "foofoo");
	manager.setFieldNameConverter(new UnderscoreFieldNameConverter(false));
	manager.setTableNameConverter(new UnderscoreTableNameConverter(false));

	// For ActiveObjects Debug
	//Logger logger = Logger.getLogger("net.java.ao");
	//logger.setLevel(Level.FINE);

	TServerTransport transport = new TServerSocket(9090);

	PublisherHandler handler = new PublisherHandler(manager);
	Publisher.Processor processor = new Publisher.Processor(handler);
	TServer server = new TThreadPoolServer(processor, transport);

	new Thread(handler).start();

	System.out.println("Starting the Publisher server...");
	server.serve();
    }

    public static class Peer {
	String peerHost;
	int peerPort;
	TTransport transport;
	Subscriber.Client client;

	public Peer(String peerHost, int peerPort) {
	    this.peerHost = peerHost;
	    this.peerPort = peerPort;
	}

	void open() throws TTransportException {
	    transport = new TSocket(peerHost, peerPort);
	    TProtocol protocol = new TBinaryProtocol(transport);
	    client = new Subscriber.Client(protocol);

	    transport.open();
	}

	void close() {
	    transport.close();
	}
    }

}
