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
        request("nvim_input", keycode);
    }

    public void uiTryResize(int width, int height) throws IOException {
        request("nvim_ui_try_resize", width, height);
    }

    public void request(String method, Object... params) throws IOException {
        this.msgid++;

        MessagePacker packer = MessagePack.newDefaultPacker(out);

        // [0, msgid, method, params]
        packer.packArrayHeader(4);

        // REQUEST
        packer.packInt(0);

        // msgid
        packer.packInt(msgid);
        
        // method
        packer.packString(method);

        // params
        packer.packArrayHeader(params.length);

        for (int i=0; i<params.length; i++) {
            if (params[i] instanceof Integer) {
                packer.packInt((int)params[i]);
            }
            else if (params[i] instanceof String) {
                packer.packString((String)params[i]);
            }
            else {
                System.out.println("Not Supported Now");
            }
        }

        packer.flush();
    }
}
