package com.bwfcwalshy.jarchecker.jfx_gui.log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.bwfcwalshy.jarchecker.Main;
import com.bwfcwalshy.jarchecker.jfx_gui.log.create_log_filter.CreateLogFilterController;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * The log
 */
public class LogPaneController {

	private Map<Level, List<String>> messages = new HashMap<>();
	
	private List<LogFilter> filters = new ArrayList<>();
	private Predicate<Level> resultingFilter = (level) -> true;
	
    @FXML
    private TextArea textArea;
    
    @FXML
    private ProgressBar progressBar;
    
    @FXML
    private MenuItem addFilterMenuItem;
    
    @FXML
    private MenuItem listFiltersMenuItems;

    @FXML
    private MenuItem deleteFiltersMenuItems;
    
    @FXML
    private MenuItem clearMenuItem;
    
    @FXML
    private MenuItem copyAllMenuItem;
    
    @FXML
    private CheckMenuItem wrapTextCheckMenuItem;
    
    @FXML
    private void initialize() {
		{
			Image copyAllIcon = new Image(Main.class.getResource("/resources/copy icon.png").toString(), 20, 20,
					true, true);
			copyAllMenuItem.setGraphic(new ImageView(copyAllIcon));
		}

		{
			Image clearIcon = new Image(Main.class.getResource("/resources/delete icon.png").toString(), 20, 20,
					true, true);
			clearMenuItem.setGraphic(new ImageView(clearIcon));
			deleteFiltersMenuItems.setGraphic(new ImageView(clearIcon));
		}

		{
			Image listIcon = new Image(Main.class.getResource("/resources/list icon.png").toString(), 20, 20,
					true, true);
			listFiltersMenuItems.setGraphic(new ImageView(listIcon));
		}
		
		{
			Image addFilterIcon = new Image(Main.class.getResource("/resources/filter icon.png").toString(), 20, 20,
					true, true);
			addFilterMenuItem.setGraphic(new ImageView(addFilterIcon));
		}
    }
    
    @FXML
    void onCopyAll(ActionEvent event) {
    	Clipboard clipboard = Clipboard.getSystemClipboard();
    	ClipboardContent content = new ClipboardContent();
    	content.putString(textArea.getText());
    	clipboard.setContent(content);
    }
    
    @FXML
    void onClear(ActionEvent event) {
    	removeAllMessages();
    }

    @FXML
    void onWrapText(ActionEvent event) {
   		textArea.setWrapText(wrapTextCheckMenuItem.isSelected());
    }

    @FXML
    void onFilterLogLevel(ActionEvent event) {
    	try {
			FXMLLoader loader = new FXMLLoader(CreateLogFilterController.class.getResource("CreateLogFilter.fxml"));
			GridPane pane = loader.load();
			CreateLogFilterController controller = loader.getController();
			
			Stage stage = new Stage();
			stage.initOwner(Main.getInstance().getPrimaryStage());
			stage.initModality(Modality.APPLICATION_MODAL);
			
			stage.setScene(new Scene(pane));
			
			controller.setThisStage(stage);
			
			stage.showAndWait();
			
			controller.getFilter().ifPresent(filter -> {
				addFilter(filter);
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    @FXML
    void onListFilters(ActionEvent event) {
    	Alert alert = new Alert(AlertType.INFORMATION);
    	alert.initOwner(Main.getInstance().getPrimaryStage());
    	alert.setHeaderText("Filters:");
    	
    	if(!filters.isEmpty()) {    	
        	ListView<String> filterView = new ListView<>();
        	filterView.getItems().addAll(filters.stream().map(LogFilter::getDescription).collect(Collectors.toList()));
        	filterView.setEditable(false);
        	
        	alert.getDialogPane().setContent(filterView);
    	}
    	else {
    		alert.setContentText("No filters :/");
    	}
    	
    	alert.setResizable(true);
    	alert.show();
    }

    @FXML
    void onRemoveAllFilters(ActionEvent event) {
    	removeAllFilters();
    }
    
    private void updateMessages() {
    	if(!Platform.isFxApplicationThread()) {
    		Platform.runLater(() -> updateMessages());
    		return;
    	}
    	textArea.clear();
    	for (Entry<Level, List<String>> entry : messages.entrySet()) {
    		if(resultingFilter.test(entry.getKey())) {
    			for (String string : entry.getValue()) {
					textArea.appendText(string + System.lineSeparator());
				}
    		}
		}
    }
    
    /**
     * Removes all the messages
     */
    public void removeAllMessages() {
    	messages.clear();
    	updateMessages();
    }
    
    /**
     * @param level The Level of the message
     * @param message The message to add
     */
    public void addMessage(Level level, String message) {
    	if(!messages.containsKey(level)) {
    		messages.put(level, new ArrayList<>());
    	}
    	messages.get(level).add(message);
    	
    	// could call updateMessages, but that would be overkill and too slow
    	if(resultingFilter.test(level)) {
    		// TODO: ugly fix. Implement a queue 
    		Platform.runLater(() -> {
    			textArea.appendText(message + "\n");
    		});
    	}
    }

    /**
     * @param filter The filter to add
     */
    public void addFilter(LogFilter filter) {
    	filters.add(filter);
    	
    	calculateResultingFilter();
    }
    
    /**
     * Removes all filters
     */
    public void removeAllFilters() {
    	filters.clear();
    	
    	calculateResultingFilter();
    }
    
    /**
     * Calculates the resulting filter
     */
    private void calculateResultingFilter() {
   		resultingFilter = (leve) -> true;
   		
   		for(LogFilter filter : filters) {
   			resultingFilter = filter.addAfter(resultingFilter);
   		}

   		updateMessages();
    }
    
    /**
     * Gets the decompilation progress bar
     * @return The progress bar
     */
    public ProgressBar getProgressBar() {
    	return progressBar;
    }

}
