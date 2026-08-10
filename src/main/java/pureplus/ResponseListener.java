
package pureplus;

import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.core.MessageFormat;
import org.msgpack.value.ValueType;

import java.io.*;
import java.util.Map;

public class ResponseListener extends Thread
{
    MessageUnpacker unpacker;

    public ResponseListener(InputStream in) {
        unpacker = MessagePack.newDefaultUnpacker(in);
    }

    public void run() {
        int ary_size = 0;

		try {
            // Responseを読む
            while (unpacker.hasNext()) {
                ValueType  vtype = getNextType(unpacker);

                switch (vtype) {
                case ARRAY:
                    ary_size = unpacker.unpackArrayHeader();
                    System.out.println("array size=" + ary_size);
                    break;
                case INTEGER:
                    parseMessage(ary_size, unpacker);
                    break;
                default:
                    System.out.println("unknown type: "+vtype);
                    break;
                }
            }
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    private ValueType getNextType(MessageUnpacker unpacker) throws IOException {
        MessageFormat  format = unpacker.getNextFormat();
        ValueType  vtype = format.getValueType();
        System.out.println("type: "+vtype);

        return vtype;
    }

    public void parseMessage(int size, MessageUnpacker unpacker) throws IOException {
        int messageType = unpacker.unpackInt();

        switch (messageType) {
        case 1:
            // Response
            parseResponse(size, unpacker);
            break;
        case 2:
            // Notification
            parseNotifycation(size, unpacker);
            break;
        default:
            throw new IOException( "Unknown message type: " + messageType);
        }
    }

    private void parseResponse(int size, MessageUnpacker unpacker) throws IOException {
        int msgid = unpacker.unpackInt();

        System.out.println("Response id = " + msgid);

        // error
        getNextType(unpacker);
        var error = unpacker.unpackValue();
        // result
        getNextType(unpacker);
        var result = unpacker.unpackValue();

        System.out.println("error  = " + error);
        System.out.println("result = " + result);
    }

    private void parseNotifyArgs(int size, MessageUnpacker unpacker) throws IOException {

        for (int i=0; i<size; i++) {
            ValueType vtype = getNextType(unpacker);
            var note_args = unpacker.unpackValue();
            System.out.println( "args:" + note_args);
        }
    }

    private void parseNotifycation(int size, MessageUnpacker unpacker) throws IOException {
        String method = unpacker.unpackString();
 
        ValueType  vtype = getNextType(unpacker);
        if (vtype == ValueType.ARRAY) {
            System.out.println( "Notification: " + method );
            int ary_size = unpacker.unpackArrayHeader();
            parseNotifyArgs(ary_size, unpacker);
        } else {    
            var note_args = unpacker.unpackValue();
            System.out.println( "Notification: " + method + " " + note_args);
        }
    }
}

