package com.bwfcwalshy.jarchecker.gui;

import java.awt.Font;
import java.awt.Label;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;

import com.bwfcwalshy.jarchecker.Logger;
import com.bwfcwalshy.jarchecker.Main;

public class MainWindow extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3819423569120370386L;
	private final MainWindow inst = this;
	private JButton check;
	private JMenuItem MenuCheck;
	public TextArea log;
	private JButton ssc;
	private Map<String, String> res;

	public MainWindow() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {
			// This will never fire
			e1.printStackTrace();
		}
		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("JarChecker " + Main.getVersion()+" by bwfcwalshy");
		setBounds(100, 100, 450, 358);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu FileMenu = new JMenu("File");
		menuBar.add(FileMenu);
		
		MenuCheck = new JMenuItem("Check JAR");
		FileMenu.add(MenuCheck);
		MenuCheck.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				begin(check, MenuCheck, ssc);
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
		getContentPane().setLayout(null);
		
		check = new JButton("Check");
		check.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				begin(check, MenuCheck, ssc);
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
	}

	private void begin(JButton jb, JMenuItem jmi, JButton showSource) {
		
		JFileChooser fc = new JFileChooser();
		fc.setAcceptAllFileFilterUsed(false);
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
		
		fc.addChoosableFileFilter(new FileFilter() {

			@Override
			public boolean accept(File f) {
				return f.getAbsolutePath().endsWith(".jar");
			}

			@Override
			public String getDescription() {
				return "JAR File";
			}
			
		});
		fc.setMultiSelectionEnabled(false);
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		
		if(fc.showOpenDialog(inst) == JFileChooser.APPROVE_OPTION) {
			Logger.print("Beginning scan of " + fc.getSelectedFile().getAbsolutePath());
			new Thread("Scan") {
				public void run() {
					jb.setEnabled(false);
					jmi.setEnabled(false);
					showSource.setEnabled(false);
					res = Main.decompilerStart(fc.getSelectedFile().getAbsolutePath());
					showSource.setEnabled(true);
					jb.setEnabled(true);
					jmi.setEnabled(true);
				}
			}.start();
		} else Logger.print("Scan cancelled!");
		
		
	}

}
