package pureplus.neovimgui.neovimif;

import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;

import java.io.OutputStream;
import java.io.IOException;

import java.util.Map;

public class NeovimApi
{
    OutputStream  out;
    int  msgid;

    public NeovimApi(OutputStream out) {
        this.out = out;
        this.msgid = 0;
    }

    public void uiAttach(int width, int height) throws IOException {
        Map<String, Object>  options = Map.of("ext_linegrid", true);

        request("nvim_ui_attach", width, height, options);
    }

    public void input(String keycode) throws IOException {
        request("nvim_input", keycode);
    }

    public void uiTryResize(int width, int height) throws IOException {
        request("nvim_ui_try_resize", width, height);
    }

    private void packMap(MessagePacker packer, Map<String, Object> map) throws IOException {
        packer.packMapHeader(map.size());

        for (Object keyobj : map.keySet()) {
            String key = "";
            if (keyobj instanceof String) {
                key = (String)keyobj;
                packer.packString(key);
            
                Object value = map.get(key);

                if (value instanceof Integer) {
                    packer.packInt((int)value);
                } else if (value instanceof Boolean) {
                    packer.packBoolean((boolean)value);
                } else if (value instanceof String) {
                    packer.packString((String)value);
                }
            }
            else {
                throw new ClassCastException("Map key nees String. key is "+keyobj.getClass());
            }
        }
    }

    private void request(String method, Object... params) throws IOException {
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
            else if (params[i] instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object>  map = (Map<String, Object>)params[i];
                packMap(packer, map);
            }
            else {
                throw new ClassCastException("Not Supported Now.. class is "+params[i].getClass());
            }
        }

        packer.flush();
    }
}
