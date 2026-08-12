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

	    NvimReceiveThread th = new NvimReceiveThread(in);
        th.setDrawModel(model);
	    th.start();

        NvimApi   api = new NvimApi(out); 
        api.uiAttach(80,24);
    }
}

