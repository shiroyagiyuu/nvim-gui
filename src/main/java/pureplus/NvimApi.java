package pureplus;

import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;

import java.io.OutputStream;
import java.io.IOException;

public class NvimApi
{
    OutputStream  out;
    int  msgid;

    public NvimApi(OutputStream out) {
        this.out = out;
        this.msgid = 0;
    }

    public int uiAttach(int width, int height) throws IOException {
        this.msgid++;

        MessagePacker packer =
                MessagePack.newDefaultPacker(out);

        // [0, msgid, method, params]
        packer.packArrayHeader(4);

        // REQUEST
        packer.packInt(0);

        // msgid
        packer.packInt(msgid);

        // method
        packer.packString("nvim_ui_attach");

        // params = [width, height, options]
        packer.packArrayHeader(3);

        packer.packInt(width);
        packer.packInt(height);

        // {}
        packer.packMapHeader(1);
        packer.packString("ext_linegrid");
        packer.packBoolean(true);

        packer.flush();

        return msgid;
    }

    public void input(String keycode) throws IOException {
        this.msgid++;

        MessagePacker packer = MessagePack.newDefaultPacker(out);

        // [0, msgid, method, params]
        packer.packArrayHeader(4);

        // REQUEST
        packer.packInt(0);

        // msgid
        packer.packInt(msgid);
        
        // method
        packer.packString("nvim_input");

        // params = [keys]
        packer.packArrayHeader(1);

        // {}
        //packer.packMapHeader(1);
        packer.packString(keycode);

        packer.flush();

        System.out.println("Call input");
    }
}
