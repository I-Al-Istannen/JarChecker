package com.bwfcwalshy.jarchecker.gui;

import java.awt.Font;
import java.awt.Label;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;

import com.bwfcwalshy.jarchecker.Logger;
import com.bwfcwalshy.jarchecker.Main;

/**
 * The main window for the GUI
 */
public class MainWindow extends JFrame {

    private static final long serialVersionUID = -3819423569120370386L;
    private final MainWindow inst = this;
    private JButton check;
    private JMenuItem MenuCheck;

    private TextArea log;
    private JButton ssc;
    private Map<String, String> res;
    private JProgressBar decompilingProgressBar;

    /**
     * A new instance with the default parameters
     */
    public MainWindow() {
	try {
	    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
		| UnsupportedLookAndFeelException e1) {
	}
	setResizable(false);
	setDefaultCloseOperation(EXIT_ON_CLOSE);
	setTitle("JarChecker " + Main.getVersion() + " by bwfcwalshy");
	setBounds(100, 100, 450, 358);

	JProgressBar work = new JProgressBar();
	work.setEnabled(false);
	work.setBounds(10, 8, 146, 14);
	getContentPane().add(work);

	JMenuBar menuBar = new JMenuBar();
	setJMenuBar(menuBar);

	JMenu FileMenu = new JMenu("File");
	menuBar.add(FileMenu);

	MenuCheck = new JMenuItem("Check JAR");
	FileMenu.add(MenuCheck);
	MenuCheck.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		begin(check, MenuCheck, ssc, work);
	    }
	});

	JSeparator separator = new JSeparator();
	FileMenu.add(separator);

	JMenuItem MenuExit = new JMenuItem("Exit");
	MenuExit.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		inst.dispose();
		System.exit(0);
	    }
	});
	FileMenu.add(MenuExit);
	
	JMenu mnUtils = new JMenu("Utils");
	menuBar.add(mnUtils);
	
	JMenuItem mntmImportFileCreator = new JMenuItem("Import file creator");
	mntmImportFileCreator.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    new ImportFileMaker().setVisible(true);
		}
	});
	mnUtils.add(mntmImportFileCreator);

	JMenu mnHelp = new JMenu("Help");

	menuBar.add(mnHelp);

	JMenuItem HelpMenu = new JMenuItem("About");
	HelpMenu.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		AboutWindow aw = new AboutWindow();
		aw.setVisible(true);
	    }
	});
	mnHelp.add(HelpMenu);

	JCheckBox debug = new JCheckBox("Debug");
	debug.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		if (debug.isEnabled())
		    Main.setDebug(debug.isSelected());
	    }
	});
	menuBar.add(debug);
	getContentPane().setLayout(null);

	if (Main.isPrintDebug()) {
	    debug.setSelected(true);
	    debug.setEnabled(false);
	}

	check = new JButton("Check");
	check.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		begin(check, MenuCheck, ssc, work);
	    }
	});
	check.setBounds(0, 274, 444, 34);
	getContentPane().add(check);

	log = new TextArea();
	log.setFont(new Font("Monospaced", Font.PLAIN, 14));
	log.setEditable(false);
	log.setBounds(0, 28, 444, 206);
	getContentPane().add(log);

	Label label = new Label("Log");
	label.setBounds(210, 0, 32, 22);
	getContentPane().add(label);

	ssc = new JButton("Show suspicious classes");
	ssc.setEnabled(false);
	ssc.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		new SuspiciousClassDisplay(res).setVisible(true);
	    }
	});
	ssc.setBounds(0, 240, 444, 34);
	getContentPane().add(ssc);

	decompilingProgressBar = new JProgressBar();
	decompilingProgressBar.setBounds(288, 8, 146, 14);
	decompilingProgressBar.setEnabled(false);
	getContentPane().add(decompilingProgressBar);
    }

    /**
     * @param checkButton
     *            The check button. Will be disabled while it decompiles.
     * @param checkMenuItem
     *            The same just with the menu item
     * @param showSourceButton
     *            The showSource button
     * @param workBar
     *            Shows the decompilation process
     */
    private void begin(JButton checkButton, JMenuItem checkMenuItem, JButton showSourceButton, JProgressBar workBar) {

	JFileChooser fc = new JFileChooser();
	fc.setAcceptAllFileFilterUsed(false);
	fc.setFileFilter(new FileFilter() {

	    @Override
	    public boolean accept(File f) {
		return f.getAbsolutePath().endsWith(".java") || f.isDirectory();
	    }

	    @Override
	    public String getDescription() {
		return "Java source file";
	    }

	});

	fc.setFileFilter(new FileFilter() {

	    @Override
	    public boolean accept(File f) {
		return f.getAbsolutePath().endsWith(".jar") || f.isDirectory();
	    }

	    @Override
	    public String getDescription() {
		return "JAR File";
	    }

	});

	fc.setMultiSelectionEnabled(false);
	fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

	if (fc.showOpenDialog(inst) == JFileChooser.APPROVE_OPTION) {
	    Logger.print("Beginning scan of " + fc.getSelectedFile().getAbsolutePath());
	    new Thread("Scan") {
		public void run() {
		    checkButton.setEnabled(false);
		    decompilingProgressBar.setEnabled(true);
		    checkMenuItem.setEnabled(false);
		    showSourceButton.setEnabled(false);
		    workBar.setEnabled(true);
		    workBar.setIndeterminate(true);
		    res = Main.decompilerStart(fc.getSelectedFile().getAbsolutePath());
		    if (res != null && res.size() > 0)
			showSourceButton.setEnabled(true);
		    workBar.setIndeterminate(false);
		    decompilingProgressBar.setEnabled(false);
		    decompilingProgressBar.setMaximum(100);
		    decompilingProgressBar.setMinimum(0);
		    decompilingProgressBar.setValue(0);
		    workBar.setEnabled(false);
		    checkButton.setEnabled(true);
		    checkMenuItem.setEnabled(true);
		}
	    }.start();
	} else
	    Logger.print("Scan cancelled!");

    }

    /**
     * @param line
     *            The line to add.
     */
    public void appendToLog(String line) {
	log.append(line);
    }

    /**
     * @param max
     *            The maximum value for the progress bar
     */
    public void setProgressbarMax(int max) {
	decompilingProgressBar.setMaximum(max);
    }

    /**
     * @param value
     *            The new value
     */
    public void setProgressbarValue(int value) {
	decompilingProgressBar.setValue(value);
    }
}
