package com.bwfcwalshy.jarchecker.jfx_gui.suspicious_class;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class SuspiciousClassController {

	@FXML
	private TextArea classArea;
	
	void setClass(String clazz) {
		classArea.setText(clazz);
	}
}
