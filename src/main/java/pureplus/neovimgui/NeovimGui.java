package pureplus.neovimgui;

import java.io.OutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import pureplus.neovimgui.swinggui.NeovimKeyAdapter;
import pureplus.neovimgui.swinggui.NeovimView;
import pureplus.neovimgui.neovimif.NeovimApi;
import pureplus.neovimgui.neovimif.NeovimDrawModel;
import pureplus.neovimgui.neovimif.NeovimReceiveThread;

import java.io.File;
import java.io.Reader;
import java.io.Writer;

public class NeovimGui
{
    public static void main(String[] args) {
        Properties   config = new Properties();
        Properties  sysprop = System.getProperties();
        File  configdir  = new File(sysprop.getProperty("user.home"),".config");
        File  configfile = new File(configdir, "nvimgui.properties");
        System.out.println("configfile:"+configfile.getPath());

        if (configfile.exists()) {
            try (Reader rd = new java.io.FileReader(configfile)) {
                config.load(rd);
            } catch (java.io.IOException ex) {
                ex.printStackTrace();
            }
        }

        ArrayList<String> argList = new ArrayList<String>();

        argList.add(config.getProperty("neovim","nvim"));
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

            NeovimDrawModel   model = new NeovimDrawModel();
            model.setSize(80,24);

            NeovimReceiveThread th = new NeovimReceiveThread(in);
            th.setDrawModel(model);
            th.start();

            NeovimApi   api = new NeovimApi(out); 

            NeovimView   view = new NeovimView(model,api);
            view.setConfig(config);
            model.addDrawEventListener(view);
            view.createFrame();
            NeovimKeyAdapter  keyAdapter = new NeovimKeyAdapter(api);
            view.addKeyListener(keyAdapter);

            th.addViewEventListener(view);

            api.uiAttach(80,24);

            result = process.waitFor();
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        if (!configdir.exists()) {
            configdir.mkdir();
        }
        try (Writer wt = new java.io.FileWriter(configfile)) {
            config.store(wt, "nvimgui configs");
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        }

        System.exit(result);
    }
}

