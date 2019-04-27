
import java.net.*;
import java.nio.*;
import java.nio.channels.*;

class Client {

	SelectionKey selkey;
	SocketChannel chan;
	ByteBuffer buf;

	Client(SelectionKey selkey, SocketChannel chan) throws Throwable {
		this.selkey = selkey;
		this.chan = (SocketChannel) chan.configureBlocking(false); // asynchronous/non-blocking
		buf = ByteBuffer.allocateDirect(64); // 64 byte capacity
	}

	void disconnect() {
		Server.clientMap.remove(selkey);
		try {
			if (selkey != null)
				selkey.cancel();

			if (chan == null)
				return;

			System.out.println("bye bye " + (InetSocketAddress) chan.getRemoteAddress());
			chan.close();
		} catch (Throwable t) {
			/** quietly ignore */
		}
	}

	void read() {
		try {
			int amount_read = -1;

			try {
				amount_read = chan.read((ByteBuffer) buf.clear());
			} catch (Throwable t) {
			}

			if (amount_read == -1)
				disconnect();

			if (amount_read < 1)
				return; // if zero

			System.out.println("sending back " + buf.position() + " bytes");

			// turn this bus right around and send it back!
			buf.flip();
			chan.write(buf);
		} catch (Throwable t) {
			disconnect();
			t.printStackTrace();
		}
	}

}