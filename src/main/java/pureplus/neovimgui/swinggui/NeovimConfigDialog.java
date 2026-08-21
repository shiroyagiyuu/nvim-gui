package pureplus.neovimgui.swinggui;

import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JFrame;
import java.util.List;
import java.util.Properties;
import java.util.ArrayList;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GraphicsEnvironment;
import pureplus.neovimgui.NeovimGui;

public class NeovimConfigDialog extends JPanel
{
    JTextField      executePathField;
    JComboBox<String>   fontNameField;
    JTextField      fontSizeField;
    JCheckBox       boldBox, italicBox;
    JDialog         dialog;
 
    Properties   config;

    int   approve;

    public NeovimConfigDialog(Properties config) {
        this.config = config;
        approve = 0;
    }

    void initGui() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel exePane = new JPanel();
        exePane.add(new JLabel("Neovim path:"));
        executePathField = new JTextField(40);
        executePathField.setText(config.getProperty(NeovimGui.KEY_EXECUTE));
        exePane.add(executePathField);
        add(exePane);

        JPanel fontNamePane = new JPanel();
        fontNamePane.add(new JLabel("FontName:"));
        String[]  monofonts = getFontList();
        fontNameField = new JComboBox<>(monofonts);
        fontNameField.setSelectedItem(config.getProperty(NeovimGui.KEY_FONTNAME));
        fontNamePane.add(fontNameField);
        fontNamePane.add(new JLabel("size:"));
        fontSizeField = new JTextField(4);
        fontSizeField.setText(config.getProperty(NeovimGui.KEY_FONTSIZE));
        fontNamePane.add(fontSizeField);
        add(fontNamePane);

        JPanel  btnPane = new JPanel();
        JButton  ok_btn = new JButton("OK");
        ok_btn.addActionListener(e -> {
            approve = 1;
            setParamToConfig();
            dialog.dispose();
        });
        btnPane.add(ok_btn);
        JButton  cancel_btn = new JButton("Cancel");
        cancel_btn.addActionListener(e -> {
            approve = 0;
            dialog.dispose();
        });
        btnPane.add(cancel_btn);
        add(btnPane);
    }

    String[] getFontList() {
        GraphicsEnvironment  genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[]  allfonts = genv.getAvailableFontFamilyNames();
        List<String>  monofonts = new ArrayList<>();

        for (String fontname : allfonts) {
            Font   font = new Font(fontname, Font.PLAIN, 20);
            FontMetrics  fm = getFontMetrics(font);
            int  iwidth = fm.charWidth('I');
            int  wwidth = fm.charWidth('W');
            if (iwidth == wwidth) {
                monofonts.add(fontname);
            }
        }

        return monofonts.toArray(new String[monofonts.size()]);
    }
    
    public void setParamToConfig() {
        try {
            int  fontsize = Integer.parseInt(fontSizeField.getText());

            config.setProperty(NeovimGui.KEY_EXECUTE, executePathField.getText());
            config.setProperty(NeovimGui.KEY_FONTNAME, (String)fontNameField.getSelectedItem());
            config.setProperty(NeovimGui.KEY_FONTSIZE, Integer.toString(fontsize));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public int openDialog(JFrame parentFrame) {
        dialog = new JDialog(parentFrame, "Config", true);
        this.initGui();
        dialog.setContentPane(this);
        dialog.pack();
        dialog.setVisible(true);

        return approve;
    }

    public static void main(String[] args) {
        Properties  config = new Properties();
        config.setProperty(NeovimGui.KEY_EXECUTE, "nvim");
        config.setProperty(NeovimGui.KEY_FONTNAME, Font.MONOSPACED);
        config.setProperty(NeovimGui.KEY_FONTSIZE, "12");
        NeovimConfigDialog config_dialog = new NeovimConfigDialog(config);
        int  result = config_dialog.openDialog(null);

        System.out.println("result=" + result);
        System.exit(0);
    }
}
