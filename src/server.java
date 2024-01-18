import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class server {
    public final int BufferConst = 516;

    public static void main(String[] args) throws SocketException {
        System.out.println("Hello, World!");
        server ser = new server();
        ser.start();



    }

    private void start() throws SocketException {
        // build TFTP server
        DatagramSocket socket = new DatagramSocket(null);
        InetSocketAddress localBindPoint = new InetSocketAddress(4970);
        socket.bind(localBindPoint);
        byte [] buffer = new byte[BufferConst];

        while (true) {
           // send socket and buffer to a method that it will return ClientIPAdress

            final InetSocketAddress clientAddress =   extractIPFromBuffer(socket, buffer);
            System.out.println(clientAddress);
            // receive packet
            // parse packet
            // send packet
        }

    }

    private InetSocketAddress extractIPFromBuffer(DatagramSocket socket, byte[] buffer) {
        // receive packet
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        try {
            socket.receive(packet);
        } catch (Exception e) {
            System.out.println("Error in extractIPFromBuffer");
        }
        InetSocketAddress clientAddress = new InetSocketAddress(packet.getAddress(), packet.getPort());
        return clientAddress;
    }
}
