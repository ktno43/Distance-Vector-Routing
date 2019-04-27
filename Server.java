
import java.net.*;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;

class Server {

	ServerSocketChannel serverChannel;
	Selector selector;
	SelectionKey serverKey;
	int routingInterval;
	static HashMap<SelectionKey, Client> clientMap = new HashMap<SelectionKey, Client>();

	public Server(InetSocketAddress listenAddress, int rt) throws Throwable {
		serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);
		selector = Selector.open();
		serverKey = serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		serverChannel.bind(listenAddress);
		routingInterval = rt;

		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
			try {
				loop();
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}, routingInterval, routingInterval, TimeUnit.SECONDS);
	}

	void loop() throws Throwable {
		selector.selectNow();

		for (SelectionKey key : selector.selectedKeys()) {
			try {
				if (!key.isValid())
					continue;

				if (key == serverKey) {
					SocketChannel acceptedChannel = serverChannel.accept();

					if (acceptedChannel == null)
						continue;

					acceptedChannel.configureBlocking(false);
					SelectionKey readKey = acceptedChannel.register(selector, SelectionKey.OP_READ);
					clientMap.put(readKey, new Client(readKey, acceptedChannel));

					System.out.println("New client ip=" + acceptedChannel.getRemoteAddress() + ", total clients="
							+ Server.clientMap.size());
				}

				if (key.isReadable()) {
					Client sesh = clientMap.get(key);

					if (sesh == null)
						continue;

					sesh.read();
				}

			} catch (Throwable t) {
				t.printStackTrace();
			}
		}

		selector.selectedKeys().clear();
	}

}