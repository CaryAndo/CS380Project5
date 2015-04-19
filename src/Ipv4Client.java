import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Ipv4Client: take some data, encode in Ipv4 packet, send.
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
                    ioe.printStackTrace();
                }
            }
        }

        byte[] send = new byte[26];


        byte b = 4;
        b = (byte) (b << 4);
        b += 5;

        // send.add((byte) 0b01001100); // Version 4 and 12 words
        send[0] = b;
        send[1] = 0; // TOS (Don't implement)
        send[2] = 0; // Total length
        send[3] = 0; // Total length
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
        send[16] = (byte) 127; // 127.0.0.1 (destination address)
        send[17] = (byte) 0; // 127.0.0.1 (destination address)
        send[18] = (byte) 0; // 127.0.0.1 (destination address)
        send[19] = (byte) 1; // 127.0.0.1 (destination address)
        send[20] = (byte) 0; // Options (Don't implement)
        send[21] = (byte) 0; // Options (Don't implement)
        send[22] = (byte) 0; // Options (Don't implement)
        send[23] = (byte) 0; // Options (Don't implement)

        short checksum = calculateChecksum(send);

        //byte second = (byte) (checksum & 0xff);
        //byte first = (byte) ((checksum >> 8) & 0xff);
        System.out.println("HELLO CHECKSUM! 0x" + Integer.toString(checksum, 16));

        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.putShort(checksum);
        //byte first = buffer.array()[0];
        //byte second = buffer.array()[1] & 0xff;

        byte[] arr=new byte[]{(byte)((checksum>>8)&0xFF),(byte)(checksum&0xFF)};

        System.out.println("HELLO CHECKSUM! 0x" + Integer.toString(arr[0], 16));
        System.out.println("HELLO CHECKSUM! 0x" + Integer.toString(arr[1], 2));


        send[24] = (byte) 125; // Test data
        send[25] = (byte) 125; // Test data*/

        //send.add(());

      /*  byte[] toSend = new byte[send.length];

        for (int i = 0; i < send.length; i++) {
            toSend[i] = (byte) send[i]; // Copy over from arraylist
            //System.out.println("HEy beginning " + toSend[i]);
            //System.out.println(toSend[i]);
        }*/

        try {
            Socket socket = new Socket("45.50.5.238", 38003);
            Thread listenerThread = new Thread(new Listener(socket));
            listenerThread.start();

            OutputStream os = socket.getOutputStream();

            os.write(send);
            os.flush();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static byte strToByte(String str) {
        return (byte)(int) Integer.valueOf(str, 2);
    }

    private static long checksum(byte[] data) {
        long sum = 0;

        for (int i = 0; i < data.length; i++) {
            sum &= 0xFFFF;
            sum++;
        }

        return ~(sum & 0xFFFF);
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
     * Arg, I'm a pirate..
     *
     * */
    public static short calculateChecksum(byte[] buf) {
        int length = buf.length;
        int i = 0;

        long sum = 0;
        long data;

        // Handle all pairs
        while (length > 1) {
            // Corrected to include @Andy's edits and various comments on Stack Overflow
            data = (((buf[i] << 8) & 0xFF00) | ((buf[i + 1]) & 0xFF));
            sum += data;
            // 1's complement carry bit correction in 16-bits (detecting sign extension)
            if ((sum & 0xFFFF0000) > 0) {
                sum = sum & 0xFFFF;
                sum += 1;
            }

            i += 2;
            length -= 2;
        }

        // Handle remaining byte in odd length buffers
        if (length > 0) {
            // Corrected to include @Andy's edits and various comments on Stack Overflow
            sum += (buf[i] << 8 & 0xFF00);
            // 1's complement carry bit correction in 16-bits (detecting sign extension)
            if ((sum & 0xFFFF0000) > 0) {
                sum = sum & 0xFFFF;
                sum += 1;
            }
        }

        // Final 1's complement value correction to 16-bits
        sum = ~sum;
        sum = sum & 0xFFFF;
        return (short) sum;

    }
}

/*
u_short cksum(u_short *buf, int count)
{
    register u_long sum = 0;
    while (count--)
    {
        sum += *buf++;
        if (sum & 0xFFFF0000)
        {
            // carry occurred. so wrap around
            sum &= 0xFFFF;
            sum++;
        }
    }
    return ~(sum & 0xFFFF);
}
*/