package com.prison.controller;

import com.prison.model.Inmate;
import com.prison.model.PhoneCallEligibility;
import com.prison.model.ShopItem;
import com.prison.util.ActivityLogService;
import com.prison.util.BalanceUpdateBus;
import com.prison.util.BackgroundLoader;
import com.prison.util.Database;
import com.prison.util.UiPerformanceUtil;
import com.prison.util.WindowManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CommissaryController {

    private static final double PAID_PHONE_CALL_PRICE = 50.0;

    @FXML private ComboBox<Inmate> inmateCombo;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private ScrollPane itemsScrollPane;
    @FXML private TilePane itemsTilePane;
    @FXML private Label selectedItemLabel;
    @FXML private Label inmateBalanceLabel;
    @FXML private Label countdownLabel;
    @FXML private Label statusLabel;

    private final Database database = Database.getInstance();
    private final List<ShopItem> catalog = buildCatalog();
    private ShopItem selectedItem;
    private Timeline phoneCallTimeline;

    private final ChangeListener<Number> balanceListener = (obs, oldVal, newVal) -> refreshInmatesKeepingSelection();

    @FXML
    public void initialize() {
        categoryCombo.getItems().add("All");
        categoryCombo.getItems().addAll(catalog.stream().map(ShopItem::getCategory).distinct().collect(Collectors.toList()));
        categoryCombo.setValue("All");
        categoryCombo.setOnAction(event -> renderItems());

        inmateCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> updateBalanceLabel());

        UiPerformanceUtil.optimizeScrollPane(itemsScrollPane);
        UiPerformanceUtil.enableBufferedRendering(itemsTilePane, inmateCombo);

        BalanceUpdateBus.versionProperty().addListener(balanceListener);

        renderItems();
        loadInmates();
    }

    @FXML
    private void purchaseSelectedItem() {
        Inmate selectedInmate = inmateCombo.getValue();
        if (selectedInmate == null) {
            showError("Select an inmate first.");
            return;
        }

        if (selectedItem == null) {
            showError("Select an item from the commissary grid.");
            return;
        }

        if (selectedItem.isPhoneCallService()) {
            handlePhoneCallPurchase(selectedInmate, selectedItem);
            return;
        }

        if (selectedInmate.getBalance() < selectedItem.getPrice()) {
            showError("Insufficient balance for " + selectedItem.getItemName() + ".");
            return;
        }

        BackgroundLoader.loadAsync(
            () -> database.purchaseCommissaryItem(selectedInmate.getInmateId(), selectedItem.getPrice()),
            success -> {
                if (success) {
                    statusLabel.setText("Purchased " + selectedItem.getItemName() + " successfully.");
                    statusLabel.setStyle("-fx-text-fill: #22c55e;");
                    ActivityLogService.log("Commissary Purchase", "Inmate " + selectedInmate.getName() + " Purchased " + selectedItem.getItemName());
                    BalanceUpdateBus.publish();
                } else {
                    showError("Purchase failed.");
                }
            },
            error -> showError("Purchase failed: " + error.getMessage())
        );
    }

    @FXML
    private void goBack() {
        BalanceUpdateBus.versionProperty().removeListener(balanceListener);
        if (phoneCallTimeline != null) {
            phoneCallTimeline.stop();
        }
        Stage stage = (Stage) itemsTilePane.getScene().getWindow();
        WindowManager.showDashboardForCurrentUser(stage, getClass());
    }

    @FXML
    private void requestPhoneCall() {
        Inmate inmate = inmateCombo.getValue();
        if (inmate == null) {
            showError("Select an inmate before requesting a phone call.");
            return;
        }

        if (phoneCallTimeline != null) {
            phoneCallTimeline.stop();
        }

        final int[] remainingMinutes = {30};
        countdownLabel.setText("Call Timer: 30:00 (simulated)");
        statusLabel.setText("Phone call simulation started. 1 second = 1 simulated minute.");
        statusLabel.setStyle("-fx-text-fill: #f59e0b;");

        phoneCallTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            remainingMinutes[0]--;
            countdownLabel.setText(String.format("Call Timer: %02d:00 (simulated)", Math.max(remainingMinutes[0], 0)));

            if (remainingMinutes[0] <= 0) {
                phoneCallTimeline.stop();
                database.markPhoneCallCompleted(inmate.getInmateId());
                ActivityLogService.log("Phone Call", "Inmate #" + inmate.getInmateId() + " completed a 30-minute call");
                statusLabel.setText("Phone call completed and logged.");
                statusLabel.setStyle("-fx-text-fill: #22c55e;");
            }
        }));

        phoneCallTimeline.setCycleCount(30);
        phoneCallTimeline.playFromStart();
    }

    private void handlePhoneCallPurchase(Inmate inmate, ShopItem phoneCallItem) {
        Optional<String> routingOption = new ChoiceDialog<>("Route Call Now", Arrays.asList("Route Call Now", "Schedule for Later")).showAndWait();
        if (routingOption.isEmpty()) {
            showError("Phone call action cancelled.");
            return;
        }

        PhoneCallEligibility eligibility = database.getPhoneCallEligibility(inmate.getInmateId());
        if (eligibility.isMaxWeeklyCallsReached()) {
            showError("Weekly call limit reached. Max two calls per 7 days.");
            return;
        }

        if (eligibility.isFreeCallAvailable()) {
            Alert freePrompt = new Alert(Alert.AlertType.CONFIRMATION);
            freePrompt.setTitle("Free Phone Call Available");
            freePrompt.setHeaderText("1 free call is available this week.");
            freePrompt.setContentText("Use free call for this request?");

            Optional<ButtonType> freeResult = freePrompt.showAndWait();
            if (freeResult.isPresent() && freeResult.get() == ButtonType.OK) {
                processFreeCall(inmate, routingOption.get());
                return;
            }

            if (!eligibility.isPaidCallAvailable()) {
                showError("Paid call already used this week. Only one paid call is allowed per week.");
                return;
            }

            Alert paidInsteadPrompt = new Alert(Alert.AlertType.CONFIRMATION);
            paidInsteadPrompt.setTitle("Purchase Required");
            paidInsteadPrompt.setHeaderText("You chose not to use the free call.");
            paidInsteadPrompt.setContentText("Proceed with a paid call for " + String.format("%.2f", phoneCallItem.getPrice()) + "?");
            Optional<ButtonType> paidInstead = paidInsteadPrompt.showAndWait();
            if (paidInstead.isEmpty() || paidInstead.get() != ButtonType.OK) {
                showError("Phone call action cancelled.");
                return;
            }
        } else {
            if (!eligibility.isPaidCallAvailable()) {
                showError("Weekly call limit reached. Paid call already used in this 7-day window.");
                return;
            }

            Alert purchaseRequired = new Alert(Alert.AlertType.CONFIRMATION);
            purchaseRequired.setTitle("Purchase Required");
            purchaseRequired.setHeaderText("Free call already used this week.");
            purchaseRequired.setContentText("A paid phone call is required. Continue?");
            Optional<ButtonType> requiredResult = purchaseRequired.showAndWait();
            if (requiredResult.isEmpty() || requiredResult.get() != ButtonType.OK) {
                showError("Phone call action cancelled.");
                return;
            }
        }

        if (inmate.getBalance() < phoneCallItem.getPrice()) {
            showError("Insufficient balance for paid phone call.");
            return;
        }

        Alert purchaseConfirm = new Alert(Alert.AlertType.CONFIRMATION);
        purchaseConfirm.setTitle("Confirm Purchase");
        purchaseConfirm.setHeaderText("Confirm paid call purchase");
        purchaseConfirm.setContentText("Deduct " + String.format("%.2f", phoneCallItem.getPrice()) + " from inmate wallet?");
        Optional<ButtonType> purchaseResult = purchaseConfirm.showAndWait();
        if (purchaseResult.isEmpty() || purchaseResult.get() != ButtonType.OK) {
            showError("Phone call purchase cancelled.");
            return;
        }

        BackgroundLoader.loadAsync(
            () -> database.bookPaidPhoneCall(inmate.getInmateId(), phoneCallItem.getPrice()),
            success -> {
                if (success) {
                    statusLabel.setText("Paid phone call " + routingOption.get().toLowerCase() + " successfully.");
                    statusLabel.setStyle("-fx-text-fill: #22c55e;");
                    BalanceUpdateBus.publish();
                } else {
                    showError("Phone call booking failed.");
                }
            },
            error -> showError("Phone call booking failed: " + error.getMessage())
        );
    }

    private void processFreeCall(Inmate inmate, String routingOption) {
        BackgroundLoader.loadAsync(
            () -> database.bookFreePhoneCall(inmate.getInmateId()),
            success -> {
                if (success) {
                    statusLabel.setText("Free phone call " + routingOption.toLowerCase() + " successfully.");
                    statusLabel.setStyle("-fx-text-fill: #22c55e;");
                    BalanceUpdateBus.publish();
                } else {
                    showError("Free call not available.");
                }
            },
            error -> showError("Free call booking failed: " + error.getMessage())
        );
    }

    private void loadInmates() {
        BackgroundLoader.loadAsync(
            database::getAllInmates,
            inmates -> {
                inmateCombo.setItems(FXCollections.observableArrayList(inmates));
                updateBalanceLabel();
            },
            error -> showError("Failed to load inmates: " + error.getMessage())
        );
    }

    private void refreshInmatesKeepingSelection() {
        Inmate selected = inmateCombo.getValue();
        Integer selectedId = selected != null ? selected.getInmateId() : null;

        BackgroundLoader.loadAsync(
            database::getAllInmates,
            inmates -> {
                inmateCombo.setItems(FXCollections.observableArrayList(inmates));
                if (selectedId != null) {
                    inmates.stream()
                        .filter(inmate -> inmate.getInmateId() == selectedId)
                        .findFirst()
                        .ifPresent(inmate -> inmateCombo.getSelectionModel().select(inmate));
                }
                updateBalanceLabel();
            },
            error -> showError("Failed to refresh inmate balances: " + error.getMessage())
        );
    }

    private void renderItems() {
        itemsTilePane.getChildren().clear();
        itemsTilePane.setPadding(new Insets(10));

        String category = categoryCombo.getValue();
        List<ShopItem> filtered = catalog;
        if (category != null && !"All".equalsIgnoreCase(category)) {
            filtered = catalog.stream()
                .filter(item -> item.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
        }

        for (ShopItem item : filtered) {
            Button itemButton = new Button(item.getItemName() + "\n$" + String.format("%.2f", item.getPrice()) + "\n" + item.getDescription());
            itemButton.setWrapText(true);
            itemButton.setPrefWidth(210);
            itemButton.setPrefHeight(120);
            itemButton.setStyle("-fx-background-color: #1f2937; -fx-text-fill: #f8fafc; -fx-background-radius: 10; -fx-font-size: 12px;");
            itemButton.setOnAction(event -> {
                selectedItem = item;
                selectedItemLabel.setText("Selected Item: " + item.getItemName() + " ($" + String.format("%.2f", item.getPrice()) + ")");
            });
            itemsTilePane.getChildren().add(itemButton);
        }
    }

    private void updateBalanceLabel() {
        Inmate inmate = inmateCombo.getValue();
        if (inmate == null) {
            inmateBalanceLabel.setText("Current Balance: --");
            return;
        }

        inmateBalanceLabel.setText("Current Balance: " + String.format("%.2f", inmate.getBalance()));
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #ef4444;");
    }

    private List<ShopItem> buildCatalog() {
        List<ShopItem> items = new ArrayList<>();
        items.add(new ShopItem("Extra Pillow", 25.0, "Comfort", "Memory foam support pillow"));
        items.add(new ShopItem("Comfortable Mattress", 120.0, "Comfort", "Upgraded sleep support mattress"));
        items.add(new ShopItem("Hygiene Kit", 30.0, "Essentials", "Soap, toothpaste, toothbrush, towel"));
        items.add(new ShopItem("Healthy Snack Pack", 18.0, "Food", "Nutrition-friendly snack bundle"));
        items.add(new ShopItem("Writing Materials", 12.0, "Education", "Notebook and pen set"));
        items.add(new ShopItem("Reading Light", 22.0, "Electronics", "Low-power bedside reading lamp"));
        items.add(new ShopItem("Phone Call (30 mins)", PAID_PHONE_CALL_PRICE, "Communication", "Weekly call service, with free-call policy", true));
        return items;
    }
}