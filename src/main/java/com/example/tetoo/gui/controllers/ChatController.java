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
import javafx.scene.layout.Priority;
import javafx.scene.shape.Circle;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javafx.scene.control.ListView;

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
    @FXML private StackPane mainChatPane;
    @FXML private HBox chatMainContainer;
    // УДАЛИЛИ: @FXML private HBox messageInputWrapper;
    
    // Services and State
    private SupabaseService supabaseService;
    private Chat selectedChat;
    private ScheduledExecutorService scheduler;
    
    // Constants
    private static final DateTimeFormatter TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("dd MMM, HH:mm");
    
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
        
        // Устанавливаем растягивание
        HBox.setHgrow(chatArea, Priority.ALWAYS);
        HBox.setHgrow(noChatSelectedPane, Priority.ALWAYS);
    }
    
    private void setupChatUI() {
        // Настраиваем основной контейнер
        chatMainContainer.setSpacing(0);
        chatMainContainer.getStyleClass().add("chat-main-container");
        
        // Setup chats list view
        chatsListView.setCellFactory(param -> new ListCell<Chat>() {
            @Override
            protected void updateItem(Chat chat, boolean empty) {
                super.updateItem(chat, empty);
                if (empty || chat == null) {
                    setText(null);
                    setGraphic(null);
                    getStyleClass().removeAll("chat-cell-selected", "chat-cell-unread");
                } else {
                    HBox hbox = new HBox(12);
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    hbox.setPadding(new Insets(12, 16, 12, 16));
                    hbox.setPrefHeight(70);
                    
                    // Аватар с CSS классом
                    Circle avatar = new Circle(25);
                    avatar.getStyleClass().add("chat-list-avatar");
                    
                    VBox vbox = new VBox(4);
                    vbox.setAlignment(Pos.CENTER_LEFT);
                    vbox.setPrefHeight(60);
                    
                    HBox nameTimeBox = new HBox();
                    nameTimeBox.setAlignment(Pos.CENTER_LEFT);
                    HBox.setHgrow(nameTimeBox, Priority.ALWAYS);
                    
                    // Имя собеседника с CSS классом
                    Label nameLabel = new Label(chat.getOtherUser().getFullName());
                    nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
                    nameLabel.setMaxWidth(Double.MAX_VALUE);
                    HBox.setHgrow(nameLabel, Priority.ALWAYS);
                    nameLabel.getStyleClass().add("chat-list-name");
                    
                    // Время последнего сообщения с CSS классом
                    String timeStr = "";
                    if (chat.getLastMessage() != null && chat.getLastMessage().getCreatedAt() != null) {
                        LocalDateTime time = chat.getLastMessage().getCreatedAt();
                        timeStr = time.format(TIME_FORMATTER);
                    }
                    Label timeLabel = new Label(timeStr);
                    timeLabel.setFont(Font.font("Segoe UI", 12));
                    timeLabel.getStyleClass().add("chat-list-time");
                    
                    nameTimeBox.getChildren().addAll(nameLabel, timeLabel);
                    
                    // Последнее сообщение с CSS классом
                    String lastMessage = "No messages yet";
                    if (chat.getLastMessage() != null) {
                        String msgText = chat.getLastMessage().getMessageText();
                        lastMessage = msgText.length() > 35 ? 
                                    msgText.substring(0, 35) + "..." : msgText;
                    }
                    
                    Label lastMsgLabel = new Label(lastMessage);
                    lastMsgLabel.setFont(Font.font("Segoe UI", 13));
                    lastMsgLabel.getStyleClass().add("chat-list-preview");
                    
                    // Непрочитанные с CSS классами
                    StackPane badgeContainer = new StackPane();
                    if (chat.getUnreadCount() > 0) {
                        Circle badge = new Circle(8);
                        badge.getStyleClass().add("chat-unread-badge");
                        
                        Label countLabel = new Label(String.valueOf(chat.getUnreadCount()));
                        countLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
                        countLabel.getStyleClass().add("chat-unread-count");
                        
                        badgeContainer.getChildren().addAll(badge, countLabel);
                        StackPane.setAlignment(badge, Pos.CENTER);
                        StackPane.setAlignment(countLabel, Pos.CENTER);
                    }
                    
                    vbox.getChildren().addAll(nameTimeBox, lastMsgLabel);
                    hbox.getChildren().addAll(avatar, vbox, badgeContainer);
                    
                    setGraphic(hbox);
                    setText(null);
                    
                    // Очищаем все стили
                    getStyleClass().removeAll("chat-cell-selected", "chat-cell-unread");
                    
                    // Добавляем соответствующие CSS классы
                    if (isSelected()) {
                        getStyleClass().add("chat-cell-selected");
                    } else if (chat.getUnreadCount() > 0) {
                        getStyleClass().add("chat-cell-unread");
                    }
                    
                    // Hover эффект обрабатывается в CSS
                    setOnMouseEntered(e -> {
                        if (!isSelected() && !getStyleClass().contains("chat-cell-unread")) {
                            getStyleClass().add("chat-cell-hover");
                        }
                    });
                    
                    setOnMouseExited(e -> {
                        getStyleClass().remove("chat-cell-hover");
                    });
                }
            }
        });
        
        chatsListView.getStyleClass().add("chat-list-view");
        
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
                    
                    HBox messageContainer = new HBox();
                    messageContainer.setAlignment(isMyMessage ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
                    messageContainer.setPadding(new Insets(6, 0, 6, 0));
                    
                    VBox messageBubble = new VBox(3);
                    messageBubble.setMaxWidth(500);
                    messageBubble.setAlignment(isMyMessage ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
                    
                    // Message text
                    Label textLabel = new Label(message.getMessageText());
                    textLabel.setWrapText(true);
                    textLabel.setMaxWidth(480);
                    textLabel.setPadding(new Insets(12, 18, 12, 18));
                    textLabel.setFont(Font.font("Segoe UI", 14));
                    textLabel.getStyleClass().add("message-text");
                    
                    // Message time
                    String timeStr = "";
                    if (message.getCreatedAt() != null) {
                        timeStr = message.getCreatedAt().format(TIME_FORMATTER);
                    }
                    Label timeLabel = new Label(timeStr);
                    timeLabel.setFont(Font.font("Segoe UI", 11));
                    
                    // Добавляем CSS классы в зависимости от отправителя
                    if (isMyMessage) {
                        textLabel.getStyleClass().add("message-bubble-mine");
                        timeLabel.getStyleClass().add("message-time-mine");
                        timeLabel.setAlignment(Pos.CENTER_RIGHT);
                    } else {
                        textLabel.getStyleClass().add("message-bubble-theirs");
                        timeLabel.getStyleClass().add("message-time-theirs");
                        timeLabel.setAlignment(Pos.CENTER_LEFT);
                    }
                    
                    messageBubble.getChildren().addAll(textLabel, timeLabel);
                    messageContainer.getChildren().add(messageBubble);
                    
                    setGraphic(messageContainer);
                    setText(null);
                    getStyleClass().add("message-cell");
                }
            }
        });
        
        messagesListView.getStyleClass().add("messages-list-view");
        
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
        
        // CSS классы для элементов ввода
        messageInputArea.getStyleClass().add("message-input-area");
        sendMessageButton.getStyleClass().add("chat-send-button");
        // УДАЛИЛИ: messageInputWrapper.getStyleClass().add("message-input-wrapper");
        chatPartnerNameLabel.getStyleClass().add("chat-header-name");
        chatPartnerInfoLabel.getStyleClass().add("chat-header-info");
        chatArea.getStyleClass().add("chat-area");
        noChatSelectedPane.getStyleClass().add("no-chat-pane");
        
        // Добавляем CSS классы для элементов внутри noChatSelectedPane
        Platform.runLater(() -> {
            Circle noChatIcon = (Circle) ((VBox) noChatSelectedPane.getChildren().get(0)).getChildren().get(0);
            noChatIcon.getStyleClass().add("no-chat-icon");
            
            VBox noChatContent = (VBox) ((VBox) noChatSelectedPane.getChildren().get(0)).getChildren().get(1);
            Label noChatTitle = (Label) noChatContent.getChildren().get(0);
            Label noChatSubtitle = (Label) noChatContent.getChildren().get(1);
            noChatTitle.getStyleClass().add("no-chat-title");
            noChatSubtitle.getStyleClass().add("no-chat-subtitle");
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
    public void onSendButtonHover() {
        sendMessageButton.getStyleClass().add("send-button-hover");
    }

    @FXML  
    public void onSendButtonExit() {
        sendMessageButton.getStyleClass().remove("send-button-hover");
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