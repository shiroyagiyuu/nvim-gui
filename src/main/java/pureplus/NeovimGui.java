package pureplus;

import java.io.OutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class NeovimGui
{
    public static void main(String[] args) {

        ArrayList<String> argList = new ArrayList<String>();

        argList.add("nvim");
        argList.add("--embed");
        argList.add("--headless");
        if (args.length > 0) {
            for (String arg : args) {
                argList.add(arg);
            }
        }
        ProcessBuilder pb = new ProcessBuilder(argList);

        int  result = -1;
        try {
            Process process = pb.start();

            OutputStream out = process.getOutputStream();
            InputStream in = process.getInputStream();

            NvimDrawModel   model = new NvimDrawModel();
            model.setSize(80,24);

            NvimView   view = new NvimView(model);
            model.addDrawEventListener(view);
            view.createFrame();

            NvimReceiveThread th = new NvimReceiveThread(in);
            th.setDrawModel(model);
            th.start();

            NvimApi   api = new NvimApi(out); 
            NvimKeyAdapter  keyAdapter = new NvimKeyAdapter(api);
            view.addKeyListener(keyAdapter);
            view.setApi(api);

            api.uiAttach(80,24);

            result = process.waitFor();
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        System.exit(result);
    }
}

