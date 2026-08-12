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

    NvimDrawModel   model = new NvimDrawModel();
    model.setSize(24,80);

	ResponseListener th = new ResponseListener(in);
    th.setDrawModel(model);
	th.start();

        // nvim_ui_attach(80, 24, {})
        call(
                out,
                1,
                "nvim_ui_attach",
                80,
                24
        );
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
        packer.packMapHeader(1);
        packer.packString("ext_linegrid");
        packer.packBoolean(true);

        packer.flush();
    }
}

