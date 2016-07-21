package com.bwfcwalshy.jarchecker.jfx_gui.log;

import java.io.IOException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.bwfcwalshy.jarchecker.Main;
import com.bwfcwalshy.jarchecker.jfx_gui.log.create_log_filter.CreateLogFilterController;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * The log
 */
public class LogPaneController {

	private Map<Level, List<String>> messages = new HashMap<>();

	private List<LogFilter> filters = new ArrayList<>();
	private Predicate<Level> resultingFilter = (level) -> true;

	private LinkedBlockingDeque<String> messageQueue = new LinkedBlockingDeque<>();
	private MessageQueueProcessor queueProcessor = new MessageQueueProcessor();

	@FXML
	private ListView<String> logList;

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
			Image copyAllIcon = new Image(Main.class.getResource("/resources/copy icon.png").toString(), 20, 20, true,
					true);
			copyAllMenuItem.setGraphic(new ImageView(copyAllIcon));
		}

		{
			Image clearIcon = new Image(Main.class.getResource("/resources/delete icon.png").toString(), 20, 20, true,
					true);
			clearMenuItem.setGraphic(new ImageView(clearIcon));
			deleteFiltersMenuItems.setGraphic(new ImageView(clearIcon));
		}

		{
			Image listIcon = new Image(Main.class.getResource("/resources/list icon.png").toString(), 20, 20, true,
					true);
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
		content.putString(logList.getItems().stream().collect(Collectors.joining(System.lineSeparator())));
		clipboard.setContent(content);
	}

	@FXML
	void onClear(ActionEvent event) {
		removeAllMessages();
	}

	@FXML
	void onWrapText(ActionEvent event) {
		installWrappingCellFactory(wrapTextCheckMenuItem.isSelected());
	}

	private void installWrappingCellFactory(boolean wrap) {
		logList.setCellFactory(param -> {
			return new ListCell<String>() {
				@Override
				public void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);
					Text text = new Text();
					if (wrap) {
						// 30 to not even remotely touch the edges
						text.wrappingWidthProperty().bind(param.widthProperty().subtract(30));
					} else {
						text.setWrappingWidth(-1);
					}
					text.textProperty().bind(itemProperty());

					setGraphic(text);
				}
			};
		});
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

		if (!filters.isEmpty()) {
			ListView<String> filterView = new ListView<>();
			filterView.getItems().addAll(filters.stream().map(LogFilter::getDescription).collect(Collectors.toList()));
			filterView.setEditable(false);

			alert.getDialogPane().setContent(filterView);
		} else {
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
		if (!Platform.isFxApplicationThread()) {
			Platform.runLater(() -> updateMessages());
			return;
		}
		logList.getItems().clear();
		for (Entry<Level, List<String>> entry : messages.entrySet()) {
			if (resultingFilter.test(entry.getKey())) {
				logList.getItems().addAll(entry.getValue());
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
	 * @param level
	 *            The Level of the message
	 * @param message
	 *            The message to add
	 */
	public void addMessage(Level level, String message) {
		if (!messages.containsKey(level)) {
			messages.put(level, new ArrayList<>());
		}
		messages.get(level).add(message);

		// could call updateMessages, but that would be overkill and too slow
		if (resultingFilter.test(level)) {
			messageQueue.add(message);
			if (!queueProcessor.isRunning()) {
				queueProcessor.start();
			}
		}
	}

	/**
	 * @param filter
	 *            The filter to add
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

		for (LogFilter filter : filters) {
			resultingFilter = filter.addAfter(resultingFilter);
		}

		updateMessages();
	}

	/**
	 * Gets the decompilation progress bar
	 * 
	 * @return The progress bar
	 */
	public ProgressBar getProgressBar() {
		return progressBar;
	}

	/**
	 * Processes the message queue
	 */
	private class MessageQueueProcessor extends AnimationTimer {

		private LocalTime lastUpdate = LocalTime.now();
		private boolean running = false;

		// mainly to pull it on the fx thread during pulses, but also to limit
		// the count of items added each time. Although the latter shouldn't
		// make a difference.
		@Override
		public void handle(long now) {
			if (!messageQueue.isEmpty()) {
				messageQueue.drainTo(logList.getItems(), 100);
				lastUpdate = LocalTime.now();
			} else {
				if (ChronoUnit.SECONDS.between(lastUpdate, LocalTime.now()) > 1) {
					stop();
				}
			}
		}

		@Override
		public void stop() {
			super.stop();
			running = false;
		}

		@Override
		public void start() {
			super.start();
			running = true;
		}

		/**
		 * @return True if this processor is running
		 */
		public boolean isRunning() {
			return running;
		}
	}
}
