package com.bwfcwalshy.jarchecker.jfx_gui.log.create_log_filter;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Level;

import com.bwfcwalshy.jarchecker.jfx_gui.log.LogFilter;
import com.bwfcwalshy.jarchecker.jfx_gui.log.LogFilter.FilterType;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

/**
 * Let's you create a filter
 */
public class CreateLogFilterController {

	private Stage thisStage;
	private LogFilter filter;
	
    @FXML
    private ComboBox<String> operatorComboBox;

    @FXML
    private ComboBox<String> typeComboBox;

    @FXML
    private ComboBox<String> targetComboBox;

    @FXML
    private CheckBox andCheckbox;
    
    @FXML
    private void initialize() {
    	operatorComboBox.getItems().addAll("Equals", "Equals not", "Is bigger", "Is smaller");
    	typeComboBox.getItems().addAll("Level");
    	
		targetComboBox.getItems().addAll("ALL (-2147483648)", "FINEST (300)", "FINER (400)", "FINE (500)",
				"CONFIG (700)", "INFO (800)", "WARNING (900)", "SEVERE (1000)", "OFF (2147483647)");
		
		operatorComboBox.getSelectionModel().select(0);
		typeComboBox.getSelectionModel().select(0);
		targetComboBox.getSelectionModel().select(0);
    }
    
    /**
     * @param thisStage This stage
     */
    public void setThisStage(Stage thisStage) {
		this.thisStage = thisStage;
	}


    @FXML
    void onApply(ActionEvent event) {
    	Level level = Level.parse(targetComboBox.getSelectionModel().getSelectedItem().replaceAll(" .+", ""));
    	Predicate<Level> predicate;
    	String description = "Level";
    	
    	switch(operatorComboBox.getSelectionModel().getSelectedItem()) {
    	case "Equals": {
    		predicate = arg -> arg.equals(level);
    		description += " equals ";
    		break;
    	}
    	case "Equals not": {
    		predicate = arg -> !arg.equals(level);
    		description += " equals not ";
    		break;
    	}
    	case "Is bigger": {
    		predicate = arg -> arg.intValue() > level.intValue();
    		description += " is bigger as ";
    		break;
    	}
    	case "Is smaller": {
    		predicate = arg -> arg.intValue() < level.intValue();
    		description += " is smaller as ";
    		break;
    	}
    	default: {
    		System.out.println("Error: " + operatorComboBox.getSelectionModel().getSelectedItem());
    		return;
    	}
    	}
    	
    	FilterType filterType = andCheckbox.isSelected() ? FilterType.AND : FilterType.OR;
    	description += level.toString() + " (" + filterType.name() + ")";
    	
    	filter = new LogFilter(filterType, predicate, description);
    	
    	thisStage.hide();
    }
    
    /**
     * @return The filter if there is any
     */
    public Optional<LogFilter> getFilter() {
		return Optional.ofNullable(filter);
	}

    @FXML
    void onClose(ActionEvent event) {
    	thisStage.hide();
    }
}
