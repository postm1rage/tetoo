package com.example.tetoo.gui.controllers;

import com.example.tetoo.model.Chat;
import com.example.tetoo.model.Message;
import com.example.tetoo.model.UserProfile;
import com.example.tetoo.service.SupabaseService;
import com.example.tetoo.auth.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.input.KeyCode;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ChatController implements Initializable {
    
    // UI Elements
    @FXML private ListView<Chat> chatsListView;
    @FXML private ListView<Message> messagesListView;
    @FXML private TextArea messageInputArea;
    @FXML private Button sendMessageButton;
    @FXML private Label chatPartnerNameLabel;
    @FXML private Label chatPartnerInfoLabel;
    @FXML private VBox chatArea;
    @FXML private VBox noChatSelectedPane;
    @FXML private Label chatsTitleLabel;
    
    // Services and State
    private SupabaseService supabaseService;
    private Chat selectedChat;
    private ScheduledExecutorService scheduler;
    
    // Constants
    private static final DateTimeFormatter TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("HH:mm");
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("ChatController initialized");
        
        // Initialize services
        supabaseService = new SupabaseService();
        
        // Setup UI
        setupChatUI();
        
        // Load initial data if user is logged in
        if (SessionManager.getInstance().isLoggedIn()) {
            loadUserChats();
        }
        
        // Start auto-updates
        startChatUpdates();
    }
    
    private void setupChatUI() {
        // Setup chats list view
        chatsListView.setCellFactory(param -> new ListCell<Chat>() {
            @Override
            protected void updateItem(Chat chat, boolean empty) {
                super.updateItem(chat, empty);
                if (empty || chat == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    HBox hbox = new HBox(10);
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    
                    // Avatar
                    Circle avatar = new Circle(20);
                    avatar.setFill(Color.LIGHTGRAY);
                    
                    VBox vbox = new VBox(2);
                    
                    // Partner name
                    Label nameLabel = new Label(chat.getOtherUser().getFullName());
                    nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                    
                    // Last message preview
                    String lastMessage = "No messages";
                    if (chat.getLastMessage() != null) {
                        String msgText = chat.getLastMessage().getMessageText();
                        lastMessage = msgText.length() > 30 ? 
                                     msgText.substring(0, 30) + "..." : msgText;
                    }
                    
                    Label lastMsgLabel = new Label(lastMessage);
                    lastMsgLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px;");
                    
                    // Unread badge and time
                    HBox statusBox = new HBox(5);
                    if (chat.getUnreadCount() > 0) {
                        Circle badge = new Circle(8);
                        badge.setFill(Color.RED);
                        Label countLabel = new Label(String.valueOf(chat.getUnreadCount()));
                        countLabel.setTextFill(Color.WHITE);
                        countLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;");
                        
                        StackPane badgePane = new StackPane(badge, countLabel);
                        statusBox.getChildren().add(badgePane);
                    }
                    
                    // Last message time
                    if (chat.getLastMessage() != null && chat.getLastMessage().getCreatedAt() != null) {
                        LocalDateTime time = chat.getLastMessage().getCreatedAt();
                        String timeStr = time.format(TIME_FORMATTER);
                        Label timeLabel = new Label(timeStr);
                        timeLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 11px;");
                        statusBox.getChildren().add(timeLabel);
                    }
                    
                    vbox.getChildren().addAll(nameLabel, lastMsgLabel, statusBox);
                    hbox.getChildren().addAll(avatar, vbox);
                    
                    setGraphic(hbox);
                    setText(null);
                    
                    // Highlight if unread
                    if (chat.getUnreadCount() > 0) {
                        setStyle("-fx-background-color: #EDE7F6;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
        
        // Setup messages list view
        messagesListView.setCellFactory(param -> new ListCell<Message>() {
            @Override
            protected void updateItem(Message message, boolean empty) {
                super.updateItem(message, empty);
                if (empty || message == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Integer currentUserId = SessionManager.getInstance().getCurrentUserId();
                    boolean isMyMessage = message.isFromUser(currentUserId);
                    
                    HBox messageBox = new HBox(10);
                    messageBox.setAlignment(isMyMessage ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
                    
                    VBox contentBox = new VBox(3);
                    
                    // Message text
                    Label textLabel = new Label(message.getMessageText());
                    textLabel.setWrapText(true);
                    textLabel.setMaxWidth(300);
                    textLabel.setPadding(new Insets(8, 12, 8, 12));
                    
                    // Message time
                    String timeStr = "";
                    if (message.getCreatedAt() != null) {
                        timeStr = message.getCreatedAt().format(TIME_FORMATTER);
                    }
                    Label timeLabel = new Label(timeStr);
                    timeLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 10px;");
                    
                    if (isMyMessage) {
                        // My message (right)
                        textLabel.setStyle(
                            "-fx-background-color: #8A2BE2; " +
                            "-fx-text-fill: white; " +
                            "-fx-background-radius: 15px; " +
                            "-fx-border-radius: 15px;"
                        );
                        timeLabel.setAlignment(Pos.CENTER_RIGHT);
                        contentBox.setAlignment(Pos.CENTER_RIGHT);
                    } else {
                        // Their message (left)
                        textLabel.setStyle(
                            "-fx-background-color: #F0F0F0; " +
                            "-fx-text-fill: #333333; " +
                            "-fx-background-radius: 15px; " +
                            "-fx-border-radius: 15px;"
                        );
                        timeLabel.setAlignment(Pos.CENTER_LEFT);
                        contentBox.setAlignment(Pos.CENTER_LEFT);
                    }
                    
                    contentBox.getChildren().addAll(textLabel, timeLabel);
                    messageBox.getChildren().add(contentBox);
                    
                    setGraphic(messageBox);
                    setText(null);
                }
            }
        });
        
        // Chat selection listener
        chatsListView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                selectedChat = newValue;
                if (newValue != null) {
                    loadChatMessages(newValue);
                    showChatArea();
                } else {
                    showNoChatSelected();
                }
            }
        );
        
        // Message input handling
        messageInputArea.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && event.isShiftDown()) {
                // Shift+Enter - new line
                messageInputArea.appendText("\n");
                event.consume();
            } else if (event.getCode() == KeyCode.ENTER) {
                // Enter without Shift - send
                handleSendMessage();
                event.consume();
            }
        });
        
        // Send button action
        sendMessageButton.setOnAction(event -> handleSendMessage());
        
        // Show "no chat selected" by default
        showNoChatSelected();
    }
    
    // Public method to load chats (called from DashboardController)
    public void loadUserChats() {
        Integer currentUserId = SessionManager.getInstance().getCurrentUserId();
        if (currentUserId != null) {
            System.out.println("Loading chats for user: " + currentUserId);
            List<Chat> chats = supabaseService.getUserChats(currentUserId);
            
            Platform.runLater(() -> {
                chatsListView.getItems().setAll(chats);
                System.out.println("Loaded " + chats.size() + " chats");
                
                // Auto-select first chat if none selected
                if (!chats.isEmpty() && selectedChat == null) {
                    chatsListView.getSelectionModel().selectFirst();
                }
            });
        }
    }
    
    private void loadChatMessages(Chat chat) {
        if (chat != null) {
            Integer currentUserId = SessionManager.getInstance().getCurrentUserId();
            Integer otherUserId = chat.getOtherUser().getUserId();
            
            System.out.println("Loading messages with user: " + otherUserId);
            
            // Update chat header
            chatPartnerNameLabel.setText(chat.getOtherUser().getFullName());
            chatPartnerInfoLabel.setText(chat.getOtherUser().getLocation());
            
            // Load messages
            List<Message> messages = supabaseService.getChatHistory(currentUserId, otherUserId);
            
            Platform.runLater(() -> {
                messagesListView.getItems().setAll(messages);
                System.out.println("Loaded " + messages.size() + " messages");
                
                // Scroll to last message
                if (!messages.isEmpty()) {
                    messagesListView.scrollTo(messages.size() - 1);
                }
                
                // Mark messages as read
                supabaseService.markMessagesAsRead(currentUserId, otherUserId);
                
                // Update unread count in chats list
                chat.resetUnreadCount();
                refreshChatsList();
            });
        }
    }
    
    @FXML
    private void handleSendMessage() {
        if (selectedChat != null && !messageInputArea.getText().trim().isEmpty()) {
            Integer currentUserId = SessionManager.getInstance().getCurrentUserId();
            String messageText = messageInputArea.getText().trim();
            
            System.out.println("Sending message: " + messageText);
            
            // Send message
            boolean success = supabaseService.sendMessage(
                currentUserId,
                selectedChat.getOtherUser().getUserId(),
                messageText
            );
            
            if (success) {
                // Clear input
                messageInputArea.clear();
                
                // Reload messages
                loadChatMessages(selectedChat);
                
                // Reload chats list
                loadUserChats();
            } else {
                System.out.println("Failed to send message");
                showErrorMessage("Failed to send message");
            }
        }
    }
    
    private void showChatArea() {
        chatArea.setVisible(true);
        noChatSelectedPane.setVisible(false);
    }
    
    private void showNoChatSelected() {
        chatArea.setVisible(false);
        noChatSelectedPane.setVisible(true);
    }
    
    private void refreshChatsList() {
        chatsListView.refresh();
    }
    
    private void showErrorMessage(String message) {
        // Could be enhanced with dialog or status label
        System.err.println("Chat Error: " + message);
    }
    
    private void startChatUpdates() {
        scheduler = Executors.newScheduledThreadPool(1);
        
        // Update chats every 10 seconds
        scheduler.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {
                if (SessionManager.getInstance().isLoggedIn()) {
                    loadUserChats();
                    
                    // Refresh selected chat messages
                    if (selectedChat != null) {
                        loadChatMessages(selectedChat);
                    }
                }
            });
        }, 5, 10, TimeUnit.SECONDS);
    }
    
    // Cleanup method
    public void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("ChatController shutdown completed");
    }
}