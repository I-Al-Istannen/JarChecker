package com.bwfcwalshy.jarchecker.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextPane;

import com.bwfcwalshy.jarchecker.Main;

/**
 * Window showing information about this program
 */
public class AboutWindow extends JFrame {

    /**
     * Initializes the default About Window.
     */
    public AboutWindow() {
	setResizable(false);
	setBounds(200, 200, 351, 192);
	setTitle("About JarChecker "+Main.getVersion());

	JTextPane lblJarcheckerByBwfcwalshy = new JTextPane();
	lblJarcheckerByBwfcwalshy.setEnabled(false);
	lblJarcheckerByBwfcwalshy.setText("JarChecker\n\n" +

		"JarChecker version " + Main.getVersion() + "\n"+

		"JarChecker was created by bwfcwalshy with help and input from ArsenArsen and I Al Istannen. JarChecker is a program created to check jar files for malicious content. This project was made for use in the Bukkit Forums to protect server owners and people in need of plugins from malicious content." +
		"The program is still very much in development and has much to go before it is done.");
	lblJarcheckerByBwfcwalshy.setEditable(false);
	getContentPane().add(lblJarcheckerByBwfcwalshy, BorderLayout.CENTER);

	JButton btnNewButton = new JButton("Exit");
	btnNewButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		dispose();
	    }
	});
	getContentPane().add(btnNewButton, BorderLayout.SOUTH);
    }

    /**
     * 
     */
    private static final long serialVersionUID = -4417622926930031790L;


}