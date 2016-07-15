package com.bwfcwalshy.jarchecker.gui;

import javax.swing.JFrame;
import java.awt.TextArea;
import java.awt.BorderLayout;

public class ShowSource extends JFrame {

    /**
     * 
     */
    private static final long serialVersionUID = -2419068241345357741L;

    public ShowSource(String source, String className) {
	//setResizable(false);
	setBounds(250, 250, 250, 250);
	setTitle(className + " source");
	setDefaultCloseOperation(HIDE_ON_CLOSE);
	TextArea src = new TextArea();
	getContentPane().add(src, BorderLayout.CENTER);
	src.setText(source);
    }
}
