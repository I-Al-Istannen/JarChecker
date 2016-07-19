package com.bwfcwalshy.jarchecker.jfx_gui.log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.bwfcwalshy.jarchecker.jfx_gui.AppMain;
import com.bwfcwalshy.jarchecker.jfx_gui.log.LogFilter.FilterType;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

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
    void onCopyAll(ActionEvent event) {
    	Clipboard clipboard = Clipboard.getSystemClipboard();
    	ClipboardContent content = new ClipboardContent();
    	content.putString(textArea.getText());
    	clipboard.setContent(content);
    	
    	new Thread(() -> {
    		try {
				Thread.sleep(2000);
			} catch (Exception e) {
				e.printStackTrace();
			}
    		Platform.runLater(() -> {
    			addMessage(Level.WARNING, "[WARN] LOL");
    		});
    		
    		try {
				Thread.sleep(2000);
			} catch (Exception e) {
				e.printStackTrace();
			}
    		
   			addFilter(new LogFilter(FilterType.AND, level -> level != Level.WARNING, "Not warning"));
   			
    		try {
				Thread.sleep(2000);
			} catch (Exception e) {
				e.printStackTrace();
			}
    		
    		addFilter(new LogFilter(FilterType.OR, level -> level == Level.WARNING, "Warning"));
    	}, "Test").start();
    }

    @FXML
    void onFilterLogLevel(ActionEvent event) {
    	// TODO: Continue here
    }
    
    @FXML
    void onListFilters(ActionEvent event) {
    	Alert alert = new Alert(AlertType.INFORMATION);
    	alert.initOwner(AppMain.getInstance().getPrimaryStage());
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
     * @param level The Level of the message
     * @param message The message to add
     */
    public void addMessage(Level level, String message) {
    	if(!messages.containsKey(level)) {
    		messages.put(level, new ArrayList<>());
    	}
    	messages.get(level).add(message);
    	
    	updateMessages();
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
}
