package com.bwfcwalshy.jarchecker.gui;

import java.awt.Button;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import com.bwfcwalshy.jarchecker.Logger;
import com.bwfcwalshy.jarchecker.symbol_tables.ImportFileCreationUtil;

/**
 * GUI utility for using
 * {@link com.bwfcwalshy.jarchecker.symbol_tables.ImportFileCreationUtil
 * ImportFileCreationUtil}
 */
public class ImportFileMaker extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6161855705604709387L;
	private JTextField libraryPath;
	private JTextField outputPath;
	private ImportFileMaker inst;

	public ImportFileMaker() {
		setTitle("Import file maker");
		inst = this;
		setBounds(150, 150, 450, 170);
		setResizable(false);
		getContentPane().setLayout(null);

		libraryPath = new JTextField();
		libraryPath.setEditable(false);
		libraryPath.setBounds(10, 30, 350, 20);
		getContentPane().add(libraryPath);
		libraryPath.setColumns(10);

		Label libLabel = new Label("Library");
		libLabel.setBounds(10, 2, 62, 22);
		getContentPane().add(libLabel);

		Button libBrowse = new Button("Browse..");
		libBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooseLib = new JFileChooser();
				chooseLib.setAcceptAllFileFilterUsed(false);
				chooseLib.setFileFilter(new FileFilter() {

					@Override
					public boolean accept(File pathname) {
						return pathname.isDirectory() || pathname.getAbsolutePath().endsWith(".jar");
					}

					@Override
					public String getDescription() {
						return "JAR File";
					}

				});
				chooseLib.setMultiSelectionEnabled(false);
				chooseLib.setFileSelectionMode(JFileChooser.FILES_ONLY);
				if (chooseLib.showOpenDialog(inst) == JFileChooser.APPROVE_OPTION) {
					File library = chooseLib.getSelectedFile();
					libraryPath.setText(library.getAbsolutePath());
				}
			}
		});
		libBrowse.setBounds(366, 30, 70, 22);
		getContentPane().add(libBrowse);

		outputPath = new JTextField();
		outputPath.setEditable(false);
		outputPath.setColumns(10);
		outputPath.setBounds(10, 82, 348, 20);
		getContentPane().add(outputPath);

		Button outBrowse = new Button("Browse..");
		outBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooseLib = new JFileChooser();
				chooseLib.setAcceptAllFileFilterUsed(false);
				chooseLib.setFileFilter(new FileFilter() {

					@Override
					public boolean accept(File pathname) {
						return pathname.isDirectory() || pathname.getAbsolutePath().endsWith(".txt");
					}

					@Override
					public String getDescription() {
						return "TXT File";
					}

				});
				chooseLib.setMultiSelectionEnabled(false);
				chooseLib.setFileSelectionMode(JFileChooser.FILES_ONLY);
				if (chooseLib.showSaveDialog(inst) == JFileChooser.APPROVE_OPTION) {
					File output = chooseLib.getSelectedFile();
					outputPath.setText(output.getAbsolutePath().endsWith(".txt") ? output.getAbsolutePath()
							: output.getAbsolutePath() + ".txt");
				}
			}
		});
		outBrowse.setBounds(366, 80, 70, 22);
		getContentPane().add(outBrowse);

		Label outLabel = new Label("Output");
		outLabel.setBounds(10, 54, 62, 22);
		getContentPane().add(outLabel);

		Button go = new Button("Go");
		go.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File to = new File(outputPath.getText());
				try {
					to.createNewFile();
				} catch (IOException e1) {
					Logger.error(e1);
				}
				File lib = new File(libraryPath.getText());
				if (lib.exists()) {
					ImportFileCreationUtil.writeJarImportsToFile(lib, to.toPath());
				} else
					Logger.error("Library does not exist!");
			}
		});
		go.setBounds(10, 110, 70, 22);
		getContentPane().add(go);

		Button exit = new Button("Exit");
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				inst.dispose();
			}
		});
		exit.setBounds(364, 112, 70, 22);
		getContentPane().add(exit);

	}
}
