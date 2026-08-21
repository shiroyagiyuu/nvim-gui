package pureplus.neovimgui;

import java.io.OutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import pureplus.neovimgui.swinggui.NeovimConfigDialog;
import pureplus.neovimgui.swinggui.NeovimKeyAdapter;
import pureplus.neovimgui.swinggui.NeovimView;
import pureplus.neovimgui.neovimif.NeovimApi;
import pureplus.neovimgui.neovimif.NeovimDrawModel;
import pureplus.neovimgui.neovimif.NeovimReceiveThread;
import pureplus.neovimgui.neovimif.NeovimViewEventListener;

import java.io.File;
import java.io.Reader;
import java.io.Writer;

public class NeovimGui
{
    Properties   config;
    public final static String KEY_EXECUTE = "neovim";
    public final static String KEY_FONTNAME = "fontname";
    public final static String KEY_FONTSIZE = "fontsize";
    
    File   configdir;
    File   configfile;
    JFrame frame;

    NeovimGui() {
        config = new Properties();
        config.setProperty(KEY_EXECUTE, "nvim");
        config.setProperty(KEY_FONTNAME, java.awt.Font.MONOSPACED);
        config.setProperty(KEY_FONTSIZE, "12");
    }

    public String getExePath() {
        return config.getProperty(KEY_EXECUTE, "nvim");
    }

    public void loadConfig() {
        Properties  sysprop = System.getProperties();
        configdir  = new File(sysprop.getProperty("user.home"),".config");
        configfile = new File(configdir, "nvimgui.properties");
        System.out.println("configfile:"+configfile.getPath());

        if (configfile.exists()) {
            try (Reader rd = new java.io.FileReader(configfile)) {
                config.load(rd);
            } catch (java.io.IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void storeConfig() {
        if (!configdir.exists()) {
            configdir.mkdir();
        }
        try (Writer wt = new java.io.FileWriter(configfile)) {
            config.store(wt, "nvimgui configs");
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
    }

    public int run(List<String> argList) {
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
            createFrame(view);
            NeovimKeyAdapter  keyAdapter = new NeovimKeyAdapter(api);
            view.addKeyListener(keyAdapter);

            th.addViewEventListener(new NeovimViewEventListener() {
                @Override
                public void titleChanged(String title) {
                    if (frame != null) {
                        if (title.length()==0) {
                            title = "Neovim";
                        }
                        frame.setTitle(title);
                    }
                }
            });

            api.uiAttach(80,24);

            result = process.waitFor();
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        return result;
    }

    public void createFrame(NeovimView view) {
        frame = new JFrame("NVim");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        frame.setContentPane(view);
        view.setFrame(frame);

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent evt) {
                doQuit(0);
            }
        });

        /* Menu */
        JMenuBar  mbar = new JMenuBar();

        JMenu  nvimMenu = new JMenu("Neovim");
        JMenuItem   configItem = new JMenuItem("Config");
        configItem.addActionListener(e -> {
            SwingUtilities.invokeLater(()->{openConfigDialog();});
        });
        nvimMenu.add(configItem);

        JMenuItem  quitItem = new JMenuItem("quit");
        quitItem.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> doQuit(0));
        });
        nvimMenu.add(quitItem);

        mbar.add(nvimMenu);
        frame.setJMenuBar(mbar);

        /* Finish */
        frame.pack();
        frame.setVisible(true);
    }

    public void openConfigDialog() {
        NeovimConfigDialog  configDialog = new NeovimConfigDialog(config);
        configDialog.openDialog(frame);
    }

    public void doQuit(int result) {
        frame.dispose();
        storeConfig();
        System.exit(result);
    }

    public static void main(String[] args) {
        NeovimGui  app = new NeovimGui();
        app.loadConfig();        

        ArrayList<String> argList = new ArrayList<String>();

        argList.add(app.getExePath());
        argList.add("--embed");
        argList.add("--headless");
        if (args.length > 0) {
            for (String arg : args) {
                argList.add(arg);
            }
        }

        int result = app.run(argList);

        app.doQuit(result);
    }
}

