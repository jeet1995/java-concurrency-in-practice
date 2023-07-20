package ch7_cancellation_and_shutdown.responding_to_interruption;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class NonStandardCancellation {}

// Ex 7.11
class ReaderThread extends Thread {
    private final Socket socket;
    private final InputStream inputStream;

    ReaderThread(Socket socket) throws IOException {
        // reading from the socket
        // is not responsive to interruption
        // and therefore cannot be cancelled
        // hence a non-standard way of cancellation is needed
        this.socket = socket;
        this.inputStream = socket.getInputStream();
    }

    // override the interrupt here
    // to close the socket and not
    // just set the interrupt status
    @Override
    public void interrupt() {
        try {
            socket.close();
        }
        catch (IOException ignore) {}
        finally {
            super.interrupt();
        }
    }
}
