package com.bwfcwalshy.jarchecker.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;

public class SuspiciousClassDisplay extends JFrame {

    /**
     * 
     */
    private static final long serialVersionUID = 3663577451403352639L;
    private final JList<String> classes = new JList<String>(new DefaultListModel<String>());
    private Map<String, ShowSource> ss = new HashMap<>();
    
    public SuspiciousClassDisplay(Map<String, String> res) {
    	setResizable(false);
	setBounds(250, 250, 300, 350);
	getContentPane().setLayout(null);
	classes.setBounds(0, 34, 294, 287);
	getContentPane().add(classes);
	
	JLabel lblClasses = new JLabel("Classes");
	lblClasses.setBounds(10, 9, 274, 14);
	getContentPane().add(lblClasses);
	for(Entry<String, String> e : res.entrySet()) {
	    ss.put(e.getKey(), new ShowSource(e.getValue(), e.getKey()));
	    ((DefaultListModel<String>)classes.getModel()).addElement(e.getKey());
	}
	
	classes.addMouseListener(new MouseAdapter() {
	    public void mouseClicked(MouseEvent e) {
	        if(e.getClickCount() % 2 == 0) {
	            ss.get(classes.getSelectedValue()).setVisible(true);
	        }
	    }
	});
    }
}
