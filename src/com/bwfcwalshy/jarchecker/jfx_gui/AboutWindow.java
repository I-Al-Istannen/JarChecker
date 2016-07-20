package com.bwfcwalshy.jarchecker.jfx_gui;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Shows the about window
 */
public class AboutWindow {

	/**
	 * Shows the about window
	 * 
	 * @param parent The parent stage
	 */
	public static void show(Stage parent) {
		Alert alert = new Alert(AlertType.INFORMATION, "", ButtonType.CLOSE);
		alert.initModality(Modality.APPLICATION_MODAL);
		alert.initOwner(parent);
		alert.setHeaderText("About JarChecker");
		
		// not so nice way of handling links. ControlsFX may be a nice alternatice
		// http://controlsfx.bitbucket.org/org/controlsfx/control/HyperlinkLabel.html
		TextFlow contentText = new TextFlow();
		contentText.getChildren().add(new Text("JarChecker version " + AppMain.getInstance().getVersion() + "\n\n" +
				"JarChecker was created by"));
		
		contentText.getChildren().add(getLink("bwfcwalshy", "https://bukkit.org/members/bwfcwalshy.90927090/"));
		
		contentText.getChildren().add(new Text("with help and input from"));
		
		contentText.getChildren().add(getLink("ArsenArsen", "https://bukkit.org/members/arsenarsen.90959439/"));
		
		contentText.getChildren().add(new Text("and"));
		
		contentText.getChildren().add(getLink("I Al Istannen", "https://bukkit.org/members/i-al-istannen.91064682/"));
		
		contentText.getChildren().add(new Text(".\n\n" + 
				"JarChecker is a program created to check jar files for malicious content. This project was made for use in"));
		
		contentText.getChildren().add(getLink("the Bukkit Forums", "http://www.bukkit.org/forums"));

		contentText.getChildren().add(new Text("to protect server owners and people in need of plugins from malicious content."
					+ "\n\nThe program is still very much under development and has much to go before it is done."));
		
		contentText.setPrefWidth(550);
		
		alert.getDialogPane().setContent(contentText);
		
		alert.setResizable(true);
		alert.show();
	}
	
	/**
	 * @param text The text of the link
	 * @param url The URL the link should open
	 * @return The resulting Hyperlink
	 */
	private static Hyperlink getLink(String text, String url) {
		Hyperlink link = new Hyperlink(text);
		link.setOnAction(event -> {
			AppMain.getInstance().getHostServices().showDocument(url);
		});
		return link;
	}
}
