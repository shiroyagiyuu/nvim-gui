package pureplus;

import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.core.MessageFormat;
import org.msgpack.value.ValueType;

import java.io.*;
import java.util.Map;

public class NeovimTest {

    public static void main(String[] args) throws Exception {

        ProcessBuilder pb = new ProcessBuilder(
                "nvim",
                "--embed",
                "--headless"
        );

        Process process = pb.start();

        OutputStream out = process.getOutputStream();
        InputStream in = process.getInputStream();

        // nvim_ui_attach(80, 24, {})
        call(
                out,
                1,
                "nvim_ui_attach",
                80,
                24
        );

        // Responseを読む
      	MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(in);


		while (unpacker.hasNext()) {
			MessageFormat  format = unpacker.getNextFormat();
			ValueType  vtype = format.getValueType();
			System.out.println("type: "+vtype);

			switch (vtype) {
			case ARRAY:
				int ary_size = unpacker.unpackArrayHeader();
				System.out.println("array size=" + ary_size);
				break;
			case INTEGER:

            int messageType = unpacker.unpackInt();

            switch (messageType) {

                // Response
                case 1 -> {
                    int msgid = unpacker.unpackInt();

                    System.out.println(
                            "Response id = " + msgid
                    );

                    // error
                    var error = unpacker.unpackValue();
                    // result
                    var result = unpacker.unpackValue();

                    System.out.println(
                            "error  = " + error
                    );
                    System.out.println(
                            "result = " + result
                    );
                }

                // Notification
                case 2 -> {
                    String method =
                            unpacker.unpackString();

                    var note_args =
                            unpacker.unpackValue();

                    System.out.println(
                            "Notification: " +
                            method +
                            " " +
                            note_args
                    );
                }

                default ->
                    throw new IOException(
                            "Unknown message type: " +
                            messageType
                    );
            }
			break;
			}
        }
    }

    static void call(
            OutputStream out,
            int msgid,
            String method,
            int width,
            int height
    ) throws IOException {

        MessagePacker packer =
                MessagePack.newDefaultPacker(out);

        // [0, msgid, method, params]
        packer.packArrayHeader(4);

        // REQUEST
        packer.packInt(0);

        // msgid
        packer.packInt(msgid);

        // method
        packer.packString(method);

        // params = [width, height, options]
        packer.packArrayHeader(3);

        packer.packInt(width);
        packer.packInt(height);

        // {}
        packer.packMapHeader(0);

        packer.flush();
    }
}

