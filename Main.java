import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ParkingManagementApp extends Application {
    
    private static final int TOTAL_SLOTS = 20;
    private static final int RATE_CAR = 20;
    private static final int RATE_BIKE = 10;
    private static final int RATE_TRUCK = 40;
    
    private List<ParkingSlot> parkingSlots;
    private double totalRevenue = 0.0;
    
    // UI Components
    private Label totalSlotsLabel;
    private Label availableSlotsLabel;
    private Label occupiedSlotsLabel;
    private Label revenueLabel;
    private Label availablePercentLabel;
    private Label occupiedPercentLabel;
    private GridPane parkingGrid;
    private ComboBox<String> slotComboBox;
    private TextField vehicleNumberField;
    private ComboBox<String> vehicleTypeComboBox;
    private ComboBox<String> actionComboBox;
    private VBox vehicleListBox;
    private VBox alertContainer;

    @Override
    public void start(Stage primaryStage) {
        initializeData();
        
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");
        
        // Header
        VBox header = createHeader();
        root.setTop(header);
        
        // Main Content
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20));
        
        // Stats Grid
        HBox statsGrid = createStatsGrid();
        
        // Content Area (Parking Grid + Form)
        HBox contentArea = new HBox(20);
        contentArea.setPadding(new Insets(0, 20, 20, 20));
        
        // Parking Layout Panel
        VBox parkingPanel = createParkingPanel();
        HBox.setHgrow(parkingPanel, Priority.ALWAYS);
        
        // Right Side Panels
        VBox rightPanels = new VBox(20);
        rightPanels.setPrefWidth(400);
        rightPanels.setMinWidth(400);
        rightPanels.setMaxWidth(400);
        
        VBox formPanel = createFormPanel();
        VBox vehicleListPanel = createVehicleListPanel();
        
        rightPanels.getChildren().addAll(formPanel, vehicleListPanel);
        
        contentArea.getChildren().addAll(parkingPanel, rightPanels);
        
        mainContent.getChildren().addAll(statsGrid, contentArea);
        root.setCenter(mainContent);
        
        Scene scene = new Scene(root, 1400, 900);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        
        primaryStage.setTitle("Parking Management System");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        updateUI();
    }
    
    private void initializeData() {
        parkingSlots = new ArrayList<>();
        for (int i = 1; i <= TOTAL_SLOTS; i++) {
            parkingSlots.add(new ParkingSlot(i));
        }
    }
    
    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(30, 20, 30, 20));
        header.getStyleClass().add("header");
        
        Label title = new Label("🚗 Parking Management System");
        title.getStyleClass().add("header-title");
        
        Label subtitle = new Label("Smart Real-Time Parking Space Monitoring & Management");
        subtitle.getStyleClass().add("header-subtitle");
        
        header.getChildren().addAll(title, subtitle);
        return header;
    }
    
    private HBox createStatsGrid() {
        HBox statsGrid = new HBox(20);
        statsGrid.setPadding(new Insets(0, 20, 0, 20));
        statsGrid.setAlignment(Pos.CENTER);
        
        // Total Slots Card
        VBox totalCard = createStatCard("🅿", "Total Slots", "20", "total-stat");
        totalSlotsLabel = (Label) totalCard.getChildren().get(2);
        
        // Available Card
        VBox availableCard = createStatCard("✅", "Available", "20", "available-stat");
        availableSlotsLabel = (Label) availableCard.getChildren().get(2);
        availablePercentLabel = new Label("100%");
        availablePercentLabel.getStyleClass().add("percentage-label");
        availableCard.getChildren().add(availablePercentLabel);
        
        // Occupied Card
        VBox occupiedCard = createStatCard("🚙", "Occupied", "0", "occupied-stat");
        occupiedSlotsLabel = (Label) occupiedCard.getChildren().get(2);
        occupiedPercentLabel = new Label("0%");
        occupiedPercentLabel.getStyleClass().add("percentage-label");
        occupiedCard.getChildren().add(occupiedPercentLabel);
        
        // Revenue Card
        VBox revenueCard = createStatCard("💰", "Revenue Today", "₹0", "revenue-stat");
        revenueLabel = (Label) revenueCard.getChildren().get(2);
        
        HBox.setHgrow(totalCard, Priority.ALWAYS);
        HBox.setHgrow(availableCard, Priority.ALWAYS);
        HBox.setHgrow(occupiedCard, Priority.ALWAYS);
        HBox.setHgrow(revenueCard, Priority.ALWAYS);
        
        statsGrid.getChildren().addAll(totalCard, availableCard, occupiedCard, revenueCard);
        return statsGrid;
    }
    
    private VBox createStatCard(String icon, String label, String value, String styleClass) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().addAll("stat-card", styleClass);
        card.setPadding(new Insets(25));
        
        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("stat-icon");
        
        Label titleLabel = new Label(label);
        titleLabel.getStyleClass().add("stat-title");
        
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-value");
        
        card.getChildren().addAll(iconLabel, titleLabel, valueLabel);
        return card;
    }
    
    private VBox createParkingPanel() {
        VBox panel = new VBox(20);
        panel.getStyleClass().add("panel");
        panel.setPadding(new Insets(30));
        
        Label title = new Label("🅿 Parking Layout");
        title.getStyleClass().add("panel-title");
        
        alertContainer = new VBox(10);
        
        parkingGrid = new GridPane();
        parkingGrid.setHgap(15);
        parkingGrid.setVgap(15);
        parkingGrid.setAlignment(Pos.CENTER);
        
        // Create legend
        HBox legend = createLegend();
        
        panel.getChildren().addAll(title, alertContainer, parkingGrid, legend);
        return panel;
    }
    
    private HBox createLegend() {
        HBox legend = new HBox(30);
        legend.setAlignment(Pos.CENTER);
        legend.getStyleClass().add("legend");
        legend.setPadding(new Insets(20));
        
        HBox availableItem = new HBox(8);
        availableItem.setAlignment(Pos.CENTER);
        Region availableDot = new Region();
        availableDot.getStyleClass().addAll("legend-dot", "available-dot");
        availableDot.setPrefSize(16, 16);
        Label availableLabel = new Label("Available");
        availableLabel.getStyleClass().add("legend-label");
        availableItem.getChildren().addAll(availableDot, availableLabel);
        
        HBox occupiedItem = new HBox(8);
        occupiedItem.setAlignment(Pos.CENTER);
        Region occupiedDot = new Region();
        occupiedDot.getStyleClass().addAll("legend-dot", "occupied-dot");
        occupiedDot.setPrefSize(16, 16);
        Label occupiedLabel = new Label("Occupied");
        occupiedLabel.getStyleClass().add("legend-label");
        occupiedItem.getChildren().addAll(occupiedDot, occupiedLabel);
        
        legend.getChildren().addAll(availableItem, occupiedItem);
        return legend;
    }
    
    private VBox createFormPanel() {
        VBox panel = new VBox(20);
        panel.getStyleClass().add("panel");
        panel.setPadding(new Insets(30));
        panel.setPrefWidth(400);
        
        Label title = new Label("📝 Vehicle Entry/Exit");
        title.getStyleClass().add("panel-title");
        
        // Slot Selection
        VBox slotGroup = createFormGroup("Select Slot");
        slotComboBox = new ComboBox<>();
        slotComboBox.setPromptText("Choose a slot...");
        slotComboBox.setPrefWidth(Double.MAX_VALUE);
        slotComboBox.getStyleClass().add("form-control");
        slotGroup.getChildren().add(slotComboBox);
        
        // Vehicle Number
        VBox vehicleGroup = createFormGroup("Vehicle Number");
        vehicleNumberField = new TextField();
        vehicleNumberField.setPromptText("e.g., MH12AB1234");
        vehicleNumberField.getStyleClass().add("form-control");
        vehicleGroup.getChildren().add(vehicleNumberField);
        
        // Vehicle Type
        VBox typeGroup = createFormGroup("Vehicle Type");
        vehicleTypeComboBox = new ComboBox<>();
        vehicleTypeComboBox.getItems().addAll(
            "Car (₹20/hr)",
            "Bike (₹10/hr)",
            "Truck (₹40/hr)"
        );
        vehicleTypeComboBox.setValue("Car (₹20/hr)");
        vehicleTypeComboBox.setPrefWidth(Double.MAX_VALUE);
        vehicleTypeComboBox.getStyleClass().add("form-control");
        typeGroup.getChildren().add(vehicleTypeComboBox);
        
        // Action
        VBox actionGroup = createFormGroup("Action");
        actionComboBox = new ComboBox<>();
        actionComboBox.getItems().addAll("Vehicle Entry", "Vehicle Exit");
        actionComboBox.setValue("Vehicle Entry");
        actionComboBox.setPrefWidth(Double.MAX_VALUE);
        actionComboBox.getStyleClass().add("form-control");
        actionGroup.getChildren().add(actionComboBox);
        
        // Buttons
        HBox buttonBox = new HBox(12);
        Button submitBtn = new Button("✓ Submit");
        submitBtn.getStyleClass().addAll("btn", "btn-primary");
        submitBtn.setPrefWidth(170);
        submitBtn.setOnAction(e -> handleSubmit());
        
        Button resetBtn = new Button("↻ Reset");
        resetBtn.getStyleClass().addAll("btn", "btn-secondary");
        resetBtn.setPrefWidth(170);
        resetBtn.setOnAction(e -> resetForm());
        
        buttonBox.getChildren().addAll(submitBtn, resetBtn);
        
        panel.getChildren().addAll(title, slotGroup, vehicleGroup, typeGroup, actionGroup, buttonBox);
        return panel;
    }
    
    private VBox createFormGroup(String labelText) {
        VBox group = new VBox(8);
        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");
        group.getChildren().add(label);
        return group;
    }
    
    private VBox createVehicleListPanel() {
        VBox panel = new VBox(20);
        panel.getStyleClass().add("panel");
        panel.setPadding(new Insets(30));
        panel.setPrefWidth(400);
        
        Label title = new Label("🚗 Current Vehicles");
        title.getStyleClass().add("panel-title");
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("vehicle-scroll");
        scrollPane.setPrefHeight(400);
        
        vehicleListBox = new VBox(12);
        scrollPane.setContent(vehicleListBox);
        
        panel.getChildren().addAll(title, scrollPane);
        return panel;
    }
    
    private void updateUI() {
        updateStats();
        updateParkingGrid();
        updateSlotComboBox();
        updateVehicleList();
    }
    
    private void updateStats() {
        long available = parkingSlots.stream().filter(ParkingSlot::isAvailable).count();
        long occupied = TOTAL_SLOTS - available;
        double availablePercent = (available * 100.0) / TOTAL_SLOTS;
        double occupiedPercent = (occupied * 100.0) / TOTAL_SLOTS;
        
        totalSlotsLabel.setText(String.valueOf(TOTAL_SLOTS));
        availableSlotsLabel.setText(String.valueOf(available));
        occupiedSlotsLabel.setText(String.valueOf(occupied));
        revenueLabel.setText("₹" + String.format("%.0f", totalRevenue));
        availablePercentLabel.setText(String.format("%.0f%%", availablePercent));
        occupiedPercentLabel.setText(String.format("%.0f%%", occupiedPercent));
    }
    
    private void updateParkingGrid() {
        parkingGrid.getChildren().clear();
        int columns = 5;
        
        for (int i = 0; i < parkingSlots.size(); i++) {
            ParkingSlot slot = parkingSlots.get(i);
            VBox slotBox = createSlotBox(slot);
            
            int row = i / columns;
            int col = i % columns;
            parkingGrid.add(slotBox, col, row);
        }
    }
    
    private VBox createSlotBox(ParkingSlot slot) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        box.setPrefSize(100, 100);
        box.getStyleClass().add("parking-slot");
        
        if (slot.isAvailable()) {
            box.getStyleClass().add("available");
        } else {
            box.getStyleClass().add("occupied");
        }
        
        Label slotNumber = new Label(String.valueOf(slot.getSlotNumber()));
        slotNumber.getStyleClass().add("slot-number");
        
        Label status = new Label(slot.isAvailable() ? "Free" : "Occupied");
        status.getStyleClass().add("slot-status");
        
        box.getChildren().addAll(slotNumber, status);
        
        box.setOnMouseClicked(e -> {
            slotComboBox.setValue("Slot " + slot.getSlotNumber());
        });
        
        // Hover animation
        box.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), box);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });
        
        box.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), box);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
        
        return box;
    }
    
    private void updateSlotComboBox() {
        slotComboBox.getItems().clear();
        for (ParkingSlot slot : parkingSlots) {
            String status = slot.isAvailable() ? "Available" : "Occupied";
            slotComboBox.getItems().add("Slot " + slot.getSlotNumber() + " - " + status);
        }
    }
    
    private void updateVehicleList() {
        vehicleListBox.getChildren().clear();
        
        List<ParkingSlot> occupiedSlots = parkingSlots.stream()
            .filter(slot -> !slot.isAvailable())
            .toList();
        
        if (occupiedSlots.isEmpty()) {
            Label emptyLabel = new Label("No vehicles parked currently");
            emptyLabel.getStyleClass().add("empty-message");
            vehicleListBox.getChildren().add(emptyLabel);
            return;
        }
        
        for (ParkingSlot slot : occupiedSlots) {
            HBox vehicleItem = createVehicleItem(slot);
            vehicleListBox.getChildren().add(vehicleItem);
        }
    }
    
    private HBox createVehicleItem(ParkingSlot slot) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);
        item.getStyleClass().add("vehicle-item");
        item.setPadding(new Insets(15));
        
        VBox info = new VBox(4);
        Label vehicleLabel = new Label(slot.getVehicle().getVehicleNumber());
        vehicleLabel.getStyleClass().add("vehicle-label");
        
        long minutes = ChronoUnit.MINUTES.between(slot.getVehicle().getEntryTime(), LocalDateTime.now());
        String details = String.format("Slot %d • %s • %d min", 
            slot.getSlotNumber(), 
            slot.getVehicle().getType(), 
            minutes);
        Label detailsLabel = new Label(details);
        detailsLabel.getStyleClass().add("vehicle-details");
        
        info.getChildren().addAll(vehicleLabel, detailsLabel);
        HBox.setHgrow(info, Priority.ALWAYS);
        
        Button exitBtn = new Button("Exit");
        exitBtn.getStyleClass().add("exit-btn");
        exitBtn.setOnAction(e -> handleQuickExit(slot.getSlotNumber()));
        
        item.getChildren().addAll(info, exitBtn);
        return item;
    }
    
    private void handleSubmit() {
        String slotSelection = slotComboBox.getValue();
        String vehicleNumber = vehicleNumberField.getText().toUpperCase().trim();
        String vehicleType = vehicleTypeComboBox.getValue();
        String action = actionComboBox.getValue();
        
        if (slotSelection == null || slotSelection.isEmpty()) {
            showAlert("Please select a slot", "error");
            return;
        }
        
        if (vehicleNumber.isEmpty()) {
            showAlert("Please enter vehicle number", "error");
            return;
        }
        
        int slotNumber = Integer.parseInt(slotSelection.split(" ")[1]);
        ParkingSlot slot = parkingSlots.get(slotNumber - 1);
        
        String type = vehicleType.split(" ")[0].toLowerCase();
        
        if (action.equals("Vehicle Entry")) {
            if (!slot.isAvailable()) {
                showAlert("Slot " + slotNumber + " is already occupied!", "error");
                return;
            }
            
            Vehicle vehicle = new Vehicle(vehicleNumber, type, LocalDateTime.now());
            slot.parkVehicle(vehicle);
            showAlert("Vehicle " + vehicleNumber + " parked in Slot " + slotNumber, "success");
        } else {
            if (slot.isAvailable()) {
                showAlert("Slot " + slotNumber + " is already empty!", "warning");
                return;
            }
            
            double charge = calculateCharge(slot);
            totalRevenue += charge;
            slot.removeVehicle();
            showAlert("Vehicle exited from Slot " + slotNumber + ". Charge: ₹" + String.format("%.0f", charge), "success");
        }
        
        updateUI();
        resetForm();
    }
    
    private void handleQuickExit(int slotNumber) {
        ParkingSlot slot = parkingSlots.get(slotNumber - 1);
        
        if (slot.isAvailable()) {
            showAlert("Slot " + slotNumber + " is empty!", "error");
            return;
        }
        
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Exit");
        confirmDialog.setHeaderText("Exit Vehicle from Slot " + slotNumber);
        
        double charge = calculateCharge(slot);
        confirmDialog.setContentText(
            "Vehicle: " + slot.getVehicle().getVehicleNumber() + "
" +
            "Type: " + slot.getVehicle().getType() + "
" +
            "Charge: ₹" + String.format("%.0f", charge) + "

" +
            "Proceed with exit?"
        );
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            totalRevenue += charge;
            slot.removeVehicle();
            showAlert("Vehicle exited from Slot " + slotNumber + ". Charge: ₹" + String.format("%.0f", charge), "success");
            updateUI();
        }
    }
    
    private double calculateCharge(ParkingSlot slot) {
        Vehicle vehicle = slot.getVehicle();
        long hours = ChronoUnit.HOURS.between(vehicle.getEntryTime(), LocalDateTime.now());
        if (hours == 0) hours = 1; // Minimum 1 hour charge
        
        int rate = switch (vehicle.getType().toLowerCase()) {
            case "car" -> RATE_CAR;
            case "bike" -> RATE_BIKE;
            case "truck" -> RATE_TRUCK;
            default -> RATE_CAR;
        };
        
        return hours * rate;
    }
    
    private void showAlert(String message, String type) {
        alertContainer.getChildren().clear();
        
        HBox alert = new HBox(12);
        alert.setAlignment(Pos.CENTER_LEFT);
        alert.getStyleClass().addAll("alert", "alert-" + type);
        alert.setPadding(new Insets(16, 20, 16, 20));
        
        String icon = switch (type) {
            case "success" -> "✓";
            case "error" -> "✗";
            case "warning" -> "⚠";
            default -> "ℹ";
        };
        
        Label iconLabel = new Label(icon);
        Label messageLabel = new Label(message);
        alert.getChildren().addAll(iconLabel, messageLabel);
        
        alertContainer.getChildren().add(alert);
        
        FadeTransition fade = new FadeTransition(Duration.millis(300), alert);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
        
        // Auto-remove after 5 seconds
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.seconds(5));
        pause.setOnFinished(e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), alert);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(evt -> alertContainer.getChildren().remove(alert));
            fadeOut.play();
        });
        pause.play();
    }
    
    private void resetForm() {
        slotComboBox.setValue(null);
        vehicleNumberField.clear();
        vehicleTypeComboBox.setValue("Car (₹20/hr)");
        actionComboBox.setValue("Vehicle Entry");
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}