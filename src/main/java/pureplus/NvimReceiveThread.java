package pureplus;

import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.core.MessageFormat;
import org.msgpack.value.ValueType;

import java.io.InputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;

public class NvimReceiveThread extends Thread
{
    private MessageUnpacker unpacker;
    private NvimDrawModel   dmodel;
    private ArrayList<NvimViewEventListener> viewListeners;

    public NvimReceiveThread(InputStream in) {
        unpacker = MessagePack.newDefaultUnpacker(in);
        viewListeners = new ArrayList<>();
    }

    public void setDrawModel(NvimDrawModel model) {
        dmodel = model;
    }

    private ValueType getNextType(MessageUnpacker unpacker) throws IOException {
        MessageFormat  format = unpacker.getNextFormat();
        ValueType  vtype = format.getValueType();
        //System.out.println("type: "+vtype);

        return vtype;
    }

    public void run() {
        try {
            // Responseを読む
            while (unpacker.hasNext()) {
                int msg_size = unpacker.unpackArrayHeader();
                if (msg_size>0) {
                    int messageType = unpacker.unpackInt();

                    if (messageType==1) {
                        // Response
                        parseResponse(msg_size-1, unpacker);
                    } else if (messageType==2) {
                        // Notification
                        parseNotifycation(msg_size-1, unpacker);
                    } else {
            	        throw new IOException( "Unexpected message: " + messageType);
                    }
                }
            }
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    private void parseResponse(int size, MessageUnpacker unpacker) throws IOException {
        int msgid = unpacker.unpackInt();

        // error
        ValueType  err_type = getNextType(unpacker);
        var error = unpacker.unpackValue();
        // result
        //ValueType  res_type = getNextType(unpacker);
        var result = unpacker.unpackValue();

        if (err_type != ValueType.NIL) {
            System.out.println("Response id = " + msgid);
            System.out.println("error  = " + error);
            System.out.println("result = " + result);
        }
    }

    private void parseNotifycation(int size, MessageUnpacker unpacker) throws IOException {
        String method = unpacker.unpackString();

        if (method.equals("redraw")) { 
            for (int i=0; i<size-1; i++) {
                int ary_size = unpacker.unpackArrayHeader();
                for (int j=0; j<ary_size; j++) {
                    int  cmd_size = unpacker.unpackArrayHeader();
                    String cmd = unpacker.unpackString();
                    //System.out.println("cmd: "+cmd);
                    parseDrawEvent(cmd, cmd_size, unpacker);
                }
            }
        } else {
            System.out.println( "Notification: " + method );
            for (int i=0; i<size-1; i++) {
                var note_args = unpacker.unpackValue();
                System.out.println( "    " + note_args);
            }
        }
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
            if (text.length()>1) { System.out.println("Warning!!: long text?? ["+text+"]"); }
            for (int ic=0; ic<repeat; ic++) {
                dmodel.setCell(row, col, text, hl_id);
                col++;
            }
        }
    }

    private void parseDrawEvent(String cmd, int size, MessageUnpacker unpacker) throws IOException {
        switch (cmd) {
        case "grid_line" -> {
            for (int i=0; i<size-1; i++) {
                int cmd_size = unpacker.unpackArrayHeader();
                int grid = unpacker.unpackInt();
                int row  = unpacker.unpackInt();
                int col  = unpacker.unpackInt();
                //System.out.println("Start Grid: grid="+grid+" row="+row+" col="+col);
                //dlistener.startGridLine(grid, row, col);

                parseCell(row, col, unpacker);

                boolean wrap = unpacker.unpackBoolean();
                //System.out.println("end Grid: wrap="+wrap);
                //dlistener.endGridLine(wrap);
            }
        }
        case "hl_attr_define" -> {
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
                    } else if (key.equals("special")) {
                        int spcolor = unpacker.unpackInt();
                        hl.setSpecialColor(spcolor);
                    } else if (key.equals("reverse")) {
                        boolean reverse = unpacker.unpackBoolean();
                        hl.setReverse(reverse);
                    } else if (key.equals("underline")) {
                        boolean underline = unpacker.unpackBoolean();
                        hl.setUnderline(underline);
                    } else if (key.equals("strikethrough")) {
                        boolean strikethrough = unpacker.unpackBoolean();
                        hl.setStrikethrough(strikethrough);
                    } else { 
                        Object  value = unpacker.unpackValue();
                        System.out.println("id:"+id+" key:"+key+" val:"+value);
                    }
                    dmodel.setHilight(id, hl);
                }
                unpacker.unpackValue();	 //c_attr is nouse
                unpacker.unpackValue();  //info is nouse
            }
        }
        case "hl_group_set" -> {
            // do nothing
            for (int i=0; i<size-1; i++) {
                unpacker.unpackValue();
            }
        }
        case "default_colors_set" -> {
            int param_size = unpacker.unpackArrayHeader();
            int fgcolor = unpacker.unpackInt();
            int bgcolor = unpacker.unpackInt();
            int spcolor = unpacker.unpackInt();
            unpacker.unpackInt(); //ctermfg is nouse
            unpacker.unpackInt(); //ctermbg is nouse
            
            dmodel.setDefaultColor(fgcolor, bgcolor, spcolor);
        }
        case "grid_cursor_goto" -> {
            int param_size = unpacker.unpackArrayHeader();
            int grid = unpacker.unpackInt();
            int row  = unpacker.unpackInt();
            int col  = unpacker.unpackInt();

            dmodel.setCursor(grid, row, col);
        }
        case "grid_scroll" -> {
            int param_size = unpacker.unpackArrayHeader();
            int grid  = unpacker.unpackInt();
            int top   = unpacker.unpackInt();
            int bot   = unpacker.unpackInt();
            int left  = unpacker.unpackInt();
            int right = unpacker.unpackInt();
            int rows  = unpacker.unpackInt();
            int cols  = unpacker.unpackInt();

            dmodel.scroll(top, bot, left, right, rows, cols);
        }
        case "grid_resize" -> {
            int param_size = unpacker.unpackArrayHeader();
            int grid  = unpacker.unpackInt();
            int width = unpacker.unpackInt();
            int height= unpacker.unpackInt();

            dmodel.setSize(width, height);
        }
        case "grid_clear" -> {
            int param_size = unpacker.unpackArrayHeader();
            int grid  = unpacker.unpackInt();

            dmodel.clear();
        }
        case "mode_info_set" -> {
            //System.out.println("mode_info_set");
            int param_size = unpacker.unpackArrayHeader();
            boolean  cursor_style_enabled = unpacker.unpackBoolean();
            int map_ary = unpacker.unpackArrayHeader();
            for (int i=0; i<map_ary; i++) {
                //System.out.println("ary:"+i);
                NvimModeInfo  mode_info = new NvimModeInfo();
                int map_size = unpacker.unpackMapHeader();
                for (int m=0; m<map_size; m++) {
                    String key = unpacker.unpackString();
                    
                    switch (key) {
                    case "name" -> {
                        String name = unpacker.unpackString();
                        mode_info.setName(name);
                    }
                    case "short_name" -> {
                        String short_name = unpacker.unpackString();
                        mode_info.setShortName(short_name);
                    }
                    case "attr_id" -> {
                        int id = unpacker.unpackInt();
                        mode_info.setAttrId(id);
                    }
                    case "cursor_shape" -> {
                        String cursor_shape = unpacker.unpackString();
                        switch (cursor_shape) {
                        case "block" -> mode_info.setShape(NvimModeInfo.SHAPE_BLOCK);
                        case "horizontal" -> mode_info.setShape(NvimModeInfo.SHAPE_HORIZONTAL);
                        case "vertical" -> mode_info.setShape(NvimModeInfo.SHAPE_VERTICAL);
                        default -> System.out.println("Warning!!: unknown cursor_shape="+cursor_shape);
                        }
                    }
                    case "cell_percentage" -> {
                        int cell_percentage = unpacker.unpackInt();
                        mode_info.setCellPercentage(cell_percentage);
                    }
                    case "blinkwait" -> {
                        int blinkwait = unpacker.unpackInt();
                        mode_info.setBlinkWait(blinkwait);
                    }
                    case "blinkon" -> {
                        int blinkon = unpacker.unpackInt();
                        mode_info.setBlinkOn(blinkon);
                    }
                    case "blinkoff" -> {
                        int blinkoff = unpacker.unpackInt();
                        mode_info.setBlinkOff(blinkoff);
                    }
                    case "hl_id", "id_lm", "attr_id_lm" -> {
                        //not used
                        unpacker.unpackValue();
                    }
                    case "mouse_shape" -> {
                        int mouse_shape = unpacker.unpackInt();
                        //mode_info.setMouseShape(mouse_shape);
                    }
                    default -> {
                        Object value = unpacker.unpackValue();
                        System.out.println("Warning!!: unknown key="+key+" val="+value);
                    }
                    }
                }
                dmodel.setModeInfo(i, mode_info);
            }
            //System.out.println("mode_info_set end");
        }
        case "mode_change" -> {
            int param_size = unpacker.unpackArrayHeader();
            String  mode = unpacker.unpackString();
            int     mode_idx = unpacker.unpackInt();
            //System.out.println("mode_change to " + mode + "("+mode_idx+")");
            dmodel.setMode(mode_idx, mode);
        }
        case "busy_start" -> {
            int param_size = unpacker.unpackArrayHeader();
            dmodel.setBusy(true);
        }
        case "busy_stop" -> {
            int param_size = unpacker.unpackArrayHeader();
            dmodel.setBusy(false);
        }
        case "option_set" -> {
            for (int p=0; p<size-1; p++) {
                int subparam_size = unpacker.unpackArrayHeader();
                String option_name = unpacker.unpackString();
                Object val = unpacker.unpackValue();
                // do nothing
            }                        
        }
        case "flush" -> {
            dmodel.flush();
        }
        case "win_viewport" -> {
            // do nothing
            unpacker.unpackValue();
        }
        case "mouse_on","mouse_off" -> {
            unpacker.unpackValue();
            // do nothing
        }
        case "set_title" -> {
            int param_size = unpacker.unpackArrayHeader();
            String title = unpacker.unpackString();
            fireViewEventTitleChanged(title);
        }
        default -> {
            System.out.print( "Event: " + cmd);
            for (int i=0; i<size-1; i++) {
                var note_args = unpacker.unpackValue();
			    System.out.print("," + note_args);
            }
            System.out.println();
        }
        }
    }

    private void parseDrawCellEvent(String cmd, int size, MessageUnpacker unpacker) throws IOException {
        switch (cmd) {
        case "cursor_goto" -> {
            int  row, col;
            int  arg_size = unpacker.unpackArrayHeader();
            
            row = unpacker.unpackInt();
            col = unpacker.unpackInt();

            System.out.println("cursor_goto: " + row + "," + col);
            //if (dlistener != null) dlistener.cursorGoto(row, col);
        }
        case "put" -> {
            StringBuilder  sb = new StringBuilder();
            for (int i=0; i<size-1; i++) {
                int  str_size = unpacker.unpackArrayHeader();
                String str = unpacker.unpackString();
                sb.append(str);
            }
            System.out.println("put: \"" + sb.toString() + "\"");
            //if (dlistener != null) dlistener.put(sb.toString());
        }
        case "highlight_set" -> {
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
        }
        default -> {
            System.out.print( "DrawEvent: " + cmd);
            for (int i=0; i<size-1; i++) {
                var note_args = unpacker.unpackValue();
			    System.out.print("," + note_args);
            }
            System.out.println();
        }
        }
    }


    /**
     * add view event listener
     * @param listener
     */
    public void addViewEventListener(NvimViewEventListener listener) {
        if (!viewListeners.contains(listener)) {
            this.viewListeners.add(listener);
        }
    }

    /** 
     * remove view event listener
     * @param listener
     */
    public void removeViewEventListener(NvimViewEventListener listener) {
        if (viewListeners.contains(listener)) {
            this.viewListeners.remove(listener);
        }
    }

    /**
     * fire title changed event
     * @param title
     */
    private void fireViewEventTitleChanged(String title) {
        for (NvimViewEventListener listener : viewListeners) {
            listener.titleChanged(title);
        }
    }
}

