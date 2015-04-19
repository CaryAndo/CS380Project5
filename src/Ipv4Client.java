import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Ipv4Client: take some data, encapsulate in Ipv4 packet, send.
 *
 * @author Cary Anderson
 */
public class Ipv4Client {

    public static void main(String[] args) {

        class Listener implements Runnable {

            Socket socket;

            public Listener(Socket socket) {
                this.socket = socket;
            }

            @Override
            public void run() {
                try {
                    InputStream is = socket.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader in = new BufferedReader(isr);
                    String readstring;
                    while (true) {
                        readstring = in.readLine();
                        if (readstring == null)
                            break;
                        System.out.println("Received: " + readstring);
                    }
                } catch (IOException ioe) {
                    //ioe.printStackTrace();
                }
            }
        }

        try {
            Socket socket = new Socket("45.50.5.238", 38003);
            Thread listenerThread = new Thread(new Listener(socket));
            listenerThread.start();

            /*
            * Just send the data every 1.5 seconds.
            * */
            Thread.sleep(500);
            sendLength(socket, 2);
            Thread.sleep(1500);
            sendLength(socket, 4);
            Thread.sleep(1500);
            sendLength(socket, 8);
            Thread.sleep(1500);
            sendLength(socket, 16);
            Thread.sleep(1500);
            sendLength(socket, 32);
            Thread.sleep(1500);
            sendLength(socket, 64);
            Thread.sleep(1500);
            sendLength(socket, 128);
            Thread.sleep(1500);
            sendLength(socket, 256);
            Thread.sleep(1500);
            sendLength(socket, 512);
            Thread.sleep(1500);
            sendLength(socket, 1024);
            Thread.sleep(1500);
            sendLength(socket, 2048);
            Thread.sleep(1500);
            sendLength(socket, 4096);

        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (Exception e) {
            // I don't give a shit
        }
    }

    /**
     * Send a packet with data length
     * The data is 125
     *
     * @param sock The socket to send the data
     * @param len The number of bytes to send as data
     * */
    private static void sendLength(Socket sock, int len) {

        byte[] send = new byte[20+len];

        byte b = 4;
        b = (byte) (b << 4);
        b += 5;

        send[0] = b; // Version 4 and 5 words
        send[1] = 0; // TOS (Don't implement)
        send[2] = 0; // Total length
        send[3] = 22; // Total length
        send[4] = 0; // Identification (Don't implement)
        send[5] = 0; // Identification (Don't implement)
        send[6] = (byte) 0b01000000; // Flags and first part of Fragment offset
        send[7] = (byte) 0b00000000; // Fragment offset
        send[8] = 50; // TTL = 50
        send[9] = 0x06; // Protocol (TCP = 6)
        send[10] = 0; // CHECKSUM
        send[11] = 0; // CHECKSUM
        send[12] = (byte) 127; // 127.0.0.1 (source address)
        send[13] = (byte) 0; // 127.0.0.1 (source address)
        send[14] = (byte) 0; // 127.0.0.1 (source address)
        send[15] = (byte) 1; // 127.0.0.1 (source address)
        send[16] = (byte) 0x4c; // 127.0.0.1 (destination address)
        send[17] = (byte) 0x5b; // 127.0.0.1 (destination address)
        send[18] = (byte) 0x7b; // 127.0.0.1 (destination address)
        send[19] = (byte) 0x61; // 127.0.0.1 (destination address)

        short length = (short) (22 + len - 2); // Quackulate the total length
        byte right = (byte) (length & 0xff);
        byte left = (byte) ((length >> 8) & 0xff);
        send[2] = left;
        send[3] = right;

        short checksum = calculateChecksum(send); // Quackulate the checksum

        byte second = (byte) (checksum & 0xff);
        byte first = (byte) ((checksum >> 8) & 0xff);
        send[10] = first;
        send[11] = second;


        for (int i = 0; i < len; i++) {
            send[i+20] = (byte) 125;
        }

        for (byte be : send) {
            System.out.println(be);
        }

        try {
            OutputStream os = sock.getOutputStream();

            os.write(send);
            os.flush();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }

    /**
     * Concatenate one array with another
     *
     * @param first First array
     * @param second Second array
     * */
    private static byte[] concatenateByteArrays(byte[] first, byte[] second) {
        int firstLength = first.length;
        int secondLength = second.length;

        byte[] ret = new byte[first.length + second.length];
        System.arraycopy(first, 0, ret, 0, first.length);
        System.arraycopy(second, 0, ret, first.length, second.length);

        return ret;
    }

    /**
     * Calculate internet checksum
     *
     * @param array Packet to compute the checksum
     * @return The checksum
     * */
    public static short calculateChecksum(byte[] array) {
        int length = array.length;
        int i = 0;

        int sum = 0;
        int data;

        // Count down
        while (length > 1) {
            data = (((array[i] << 8) & 0xFF00) | ((array[i + 1]) & 0xFF));
            sum += data;

            if ((sum & 0xFFFF0000) > 0) {
                sum = sum & 0xFFFF;
                sum += 1;
            }

            i = i + 2;
            length = length - 2;
        }

        if (length > 0) {
            sum += (array[i] << 8 & 0xFF00);
            if ((sum & 0xFFFF0000) > 0) {
                sum = sum & 0x0000FFFF;
                sum += 1;
            }
        }

        sum = ~sum;
        sum = sum & 0xFFFF;
        return (short) sum;
    }
}
