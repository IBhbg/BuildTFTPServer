import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.function.Function;

public class server {
    public static final String READ_From_Folder= "/home/t-rex/Skrivbord/";
    public static final int TFTPPORT = 4970;
    public static final int BUFSIZE = 516;
    public static final int OP_RREAD_REQUEST = 1;
    public static final int OP_WRQ = 2;
    public static final short OP_DAT = 3;
    public static final int OP_ACK = 4;
    public static final int OP_ERR = 5;

    public static void main(String[] args) throws SocketException {
        System.out.println("Hello, World!");
        server ser = new server();
        ser.start();



    }

    private void start() throws SocketException {
        // build TFTP server
        DatagramSocket socket = new DatagramSocket(null);
        InetSocketAddress localBindPoint = new InetSocketAddress(TFTPPORT);
        socket.bind(localBindPoint);
        byte [] buffer = new byte[BUFSIZE];

        while (true) {
           // send socket and buffer to a method that it will return ClientIPAdress

            final InetSocketAddress clientAddress =   extractIPFromBuffer(socket, buffer);
            System.out.println(clientAddress); //127.0.0.1:56094

            // to hold file name
            final StringBuffer holdFilename = new StringBuffer();

            // create a new method to read the opcode. Note that the opcode returns a number between 1-2
            // readOpcode method give the file name and return a number to indicate if it is a read or a write
            final int  opCode = readOpcode(buffer,holdFilename);

            new Thread(() -> {
                try {

                    DatagramSocket sendSocket = new DatagramSocket(0);
                    sendSocket.connect(clientAddress);

                    if (opCode == OP_RREAD_REQUEST) {
                        holdFilename.insert(0, READ_From_Folder);
                        String str = holdFilename.toString();
                        System.out.println("str = " + str);

                        requestHandling(socket, str);
                    }

                    sendSocket.close();
                }  catch (SocketException e) {
                    sending_ERROR(socket, 0, "Unknown error occurred.");
            }
        }).start();
        }

    }

    private void requestHandling(DatagramSocket socket_Sending, String str) {
        try {
            File file = new File(str);
            if (!file.exists()) {
                sending_ERROR(socket_Sending, 1, "File not found.");
                System.err.println("File not found. Sending error message.");
                return;
            }

            // Create a new FileInputStream from the file
            InputStream is = new FileInputStream(file);
            byte[] dataBuf = new byte[512];

            int readBytes=0;
            int blockNum = 1;

            while (readBytes != -1) {

                readBytes = is.read(dataBuf);
                if(readBytes == -1){
                    break;
                }
                byte[] sendData = Arrays.copyOfRange(dataBuf, 0, readBytes);

                sending_Data(socket_Sending, sendData, blockNum);
                blockNum++;
            }
        } catch (IOException e) {
            System.err.println("Access violation.. Sending error message.");
            sending_ERROR(socket_Sending, 2, "Access violation.");
        }
    }

    private void sending_Data(DatagramSocket socketSending, byte[] sendData, int blockNum) {
        try {
            ByteBuffer bb = ByteBuffer.allocate(sendData.length + 4);
            bb.putShort(OP_DAT);
            bb.putShort((short) blockNum);
            bb.put(sendData);

            byte[] embed = bb.array();
            DatagramPacket send = new DatagramPacket(embed, embed.length);
            socketSending.send(send);
            System.out.println("Sending " + sendData.length + " bytes. Block num " + blockNum);
        } catch (IOException e) {
            sending_ERROR(socketSending, 2, "Access violation.");
        }

    }

    private void sending_ERROR(DatagramSocket socketSending, int i, String s) {
        try {
            int extraBytes = s.length();
            byte[] ackBuf = new byte[5 + extraBytes];
            ByteBuffer ack = ByteBuffer.wrap(ackBuf);

            ack.putShort(OP_DAT);
            ack.putShort((short) i);
            ack.put(s.getBytes());
            ack.put((byte) 0);

            socketSending.send(new DatagramPacket(ackBuf, ackBuf.length));
        } catch (IOException e) {
            System.err.println("Could not send error message.");
        }

    }

    private int readOpcode(byte[] buffer, StringBuffer stringBuffer) {

        // Each number stored in the buffer is 8 bits and represent a number see ASICII table.
        // Now we have to convert those numbers to characters
        // Later we have to convert the characters to a string.

        /**
         * The function extractStringUntilNullCharacter takes a byte array as input and returns a string.
         */

        Function<byte[], String> createString = (bufferInputFromMethod) ->  {
            StringBuffer strBuffer = new StringBuffer();
            for(int i =2; i<bufferInputFromMethod.length;i++){
                //To convert values (numbers to charachter use casting
                /**
                 *  This cut 000 from the file name
                 *  ex file0000000
                 */
                if ((char) bufferInputFromMethod[i] == '\0')
                    break;;
                strBuffer.append((char) bufferInputFromMethod[i]);
            }
            return strBuffer.toString();
        };

        // Now our creating method expressed as lambda will retun a string of th buffer you
        // get from socket. Let's attached/ append it to stringBuffer

        stringBuffer.append(createString.apply(buffer));


        return 0;
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
