package pureplus;

import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.core.MessageFormat;
import org.msgpack.value.ValueType;

import java.io.*;
import java.util.HashMap;

public class NvimReceiveThread extends Thread
{
    private MessageUnpacker unpacker;
    private NvimDrawModel   dmodel;

    public NvimReceiveThread(InputStream in) {
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
            		throw new IOException( "Unexpected type: " + vtype);
                }
            }
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    public void setDrawModel(NvimDrawModel model) {
        dmodel = model;
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

        if (method.equals("redraw")) { 
            for (int i=0; i<size-1; i++) {
                int ary_size = unpacker.unpackArrayHeader();
                for (int j=0; i<ary_size; i++) {
                    int  cmd_size = unpacker.unpackArrayHeader();
                    String cmd = unpacker.unpackString();
                    parseDrawEvent(cmd, cmd_size, unpacker);
                }
            }
        } else {
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

    private String parseArrayString(int size) throws IOException {
        StringBuilder  sb = new StringBuilder();
        for (int i=0; i<size; i++) {
            int  str_size = unpacker.unpackArrayHeader();
            String str = unpacker.unpackString();
            sb.append(str);
        }
        return sb.toString();
    }

    private String parseArrayString() throws IOException {
        int  ary_size = unpacker.unpackArrayHeader();
        return parseArrayString(ary_size);
    }

    private void parseCell(int row, int col, MessageUnpacker unpacker) throws IOException {
        int  ary_size = unpacker.unpackArrayHeader();
        int  hl_id=-1;
        for (int i=0; i<ary_size; i++) {
            int cell_size = unpacker.unpackArrayHeader();
            String text = unpacker.unpackString();
            int repeat=1;
            if (cell_size>1) {
                hl_id = unpacker.unpackInt();
                if (cell_size>2) {
                    repeat = unpacker.unpackInt();
                }
            }
            //System.out.println("cell: text=\""+text+"\" hl_id="+hl_id+" rep="+repeat);
            if (text.length()>1) { System.out.println("Warning!!: long text??"); }
            for (int ic=0; ic<repeat; ic++) {
                dmodel.setCell(row, col, text, hl_id);
                col++;
            }
        }
    }

    private void parseDrawEvent(String cmd, int size, MessageUnpacker unpacker) throws IOException {
        if (cmd.equals("grid_line")) {
            for (int i=0; i<size-1; i++) {
                int cmd_size = unpacker.unpackArrayHeader();
                int grid = unpacker.unpackInt();
                int row  = unpacker.unpackInt();
                int col  = unpacker.unpackInt();
                System.out.println("Start Grid: grid="+grid+" row="+row+" col="+col);
                //dlistener.startGridLine(grid, row, col);

                parseCell(row, col, unpacker);

                boolean wrap = unpacker.unpackBoolean();
                System.out.println("end Grid: wrap="+wrap);
                //dlistener.endGridLine(wrap);
            }
        } else if (cmd.equals("hl_attr_define")) {
            System.out.println("hl_attr");
            for (int i=0; i<size-1; i++) {
                int attr_size = unpacker.unpackArrayHeader();
                int id = unpacker.unpackInt();
                int map_size = unpacker.unpackMapHeader();
                NvimDrawModel.Hilight hl = dmodel.getDefaultHilight();
                for (int mi=0; mi<map_size; mi++) {
                    String  key = unpacker.unpackString();
                    if (key.equals("bold")) {
                        boolean bold = unpacker.unpackBoolean();
                        hl.setBold(bold);
                    } else if (key.equals("italic")) {
                        boolean italic = unpacker.unpackBoolean();
                        hl.setItalic(italic);
                    } else if (key.equals("foreground")) {
                        int fgcolor = unpacker.unpackInt();
                        hl.setForeground(fgcolor);
                    } else if (key.equals("background")) {
                        int bgcolor = unpacker.unpackInt();
                        hl.setBackground(bgcolor);
                    } else { 
                        Object  value = unpacker.unpackValue();
                        System.out.println("id:"+id+" key:"+key+" val:"+value);
                    }
                    dmodel.setHilight(id, hl);
                }
                Object cattr = unpacker.unpackValue();	//nouse
                Object info = unpacker.unpackValue();   //nouse
            }
        } else {
            System.out.print( "DrawEvent: " + cmd);
            for (int i=0; i<size-1; i++) {
                var note_args = unpacker.unpackValue();
			    System.out.print("," + note_args);
            }
            System.out.println();
        }
    }

    private void parseOldDrawEvent(String cmd, int size, MessageUnpacker unpacker) throws IOException {
        if (cmd.equals("cursor_goto")) {
            int  row, col;
            int  arg_size = unpacker.unpackArrayHeader();
            
            row = unpacker.unpackInt();
            col = unpacker.unpackInt();

            System.out.println("cursor_goto: " + row + "," + col);
            //if (dlistener != null) dlistener.cursorGoto(row, col);
        } else if (cmd.equals("put")) {
            StringBuilder  sb = new StringBuilder();
            for (int i=0; i<size-1; i++) {
                int  str_size = unpacker.unpackArrayHeader();
                String str = unpacker.unpackString();
                sb.append(str);
            }
            System.out.println("put: \"" + sb.toString() + "\"");
            //if (dlistener != null) dlistener.put(sb.toString());
        } else if (cmd.equals("highlight_set")) {
            int  arg_size = unpacker.unpackArrayHeader();
            int  attr_size = unpacker.unpackMapHeader();
            HashMap<String,Object>   attrs = new HashMap<String,Object>();
            for (int i=0; i<attr_size; i++) {
                String key = unpacker.unpackString();
                Object val = unpacker.unpackValue();
                attrs.put(key,val);
            }
            System.out.println("hilight_set:" + attrs.toString());
            // if (dlistener != null) dlistener.put(sb.toString());
        } else {
            System.out.print( "DrawEvent: " + cmd);
            for (int i=0; i<size-1; i++) {
                var note_args = unpacker.unpackValue();
			    System.out.print("," + note_args);
            }
            System.out.println();
        }
    }
}

