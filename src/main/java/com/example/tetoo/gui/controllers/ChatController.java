package com.example.tetoo.gui.controllers;

import com.example.tetoo.model.Chat;
import com.example.tetoo.model.Message;
import com.example.tetoo.model.UserProfile;
import com.example.tetoo.service.SupabaseService;
import com.example.tetoo.service.ChatService; // Исправленный импорт
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
import javafx.scene.control.ListView; // Явный импорт

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    @FXML private StackPane mainChatPane;
    @FXML private HBox chatMainContainer;
    
    private ChatService chatService;
    private boolean isLoadingMessages = false;

    // Services and State
    private SupabaseService supabaseService;
    private Chat selectedChat;
    private ScheduledExecutorService scheduler;
    private LocalDateTime lastPollTime = LocalDateTime.now().minusMinutes(5);
    
    // Constants
    private static final DateTimeFormatter TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("dd MMM, HH:mm");
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("ChatController initialized with ASYNC loading");
        
        // Initialize services
        supabaseService = new SupabaseService();
        chatService = new ChatService(supabaseService); // Добавляем ChatService
        
        // Setup UI
        setupChatUI();
        
        // Load initial data if user is logged in
        if (SessionManager.getInstance().isLoggedIn()) {
            loadUserChatsAsync(); // Меняем на async версию
        }
        
        // Start OPTIMIZED auto-updates
        startOptimizedPolling();
        
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
                        if (time.toLocalDate().equals(LocalDateTime.now().toLocalDate())) {
                            timeStr = time.format(TIME_FORMATTER);
                        } else {
                            timeStr = time.format(DATE_FORMATTER);
                        }
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
                    lastPollTime = LocalDateTime.now(); // Сбрасываем время для активного чата
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
    
    // НОВЫЙ: Асинхронная загрузка чатов
    private void loadUserChatsAsync() {
        Integer currentUserId = SessionManager.getInstance().getCurrentUserId();
        if (currentUserId != null) {
            System.out.println("ASYNC: Loading chats for user: " + currentUserId);
            
            chatService.getUserChatsAsync(currentUserId)
                .thenAccept(chats -> {
                    Platform.runLater(() -> {
                        chatsListView.getItems().setAll(chats);
                        System.out.println("ASYNC: Loaded " + chats.size() + " chats");
                        
                        if (!chats.isEmpty() && selectedChat == null) {
                            chatsListView.getSelectionModel().selectFirst();
                        }
                    });
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        System.out.println("ASYNC ERROR loading chats: " + e.getMessage());
                        // Fallback к синхронному методу
                        loadUserChats();
                    });
                    return null;
                });
        }
    }
    
    private void loadChatMessages(Chat chat) {
        if (chat == null || isLoadingMessages) {
            return; // Уже грузим или null
        }
        
        Integer currentUserId = SessionManager.getInstance().getCurrentUserId();
        Integer otherUserId = chat.getOtherUser().getUserId();
        
        System.out.println("ASYNC: Loading messages with user: " + otherUserId);
        
        // 1. МГНОВЕННО обновляем заголовок (не ждем загрузки сообщений)
        chatPartnerNameLabel.setText(chat.getOtherUser().getFullName());
        chatPartnerInfoLabel.setText(chat.getOtherUser().getLocation());
        
        // 2. ПОКАЗЫВАЕМ что грузим (убираем лаг "ничего не происходит")
        showMessagesLoading();
        isLoadingMessages = true;
        
        // 3. Запускаем загрузку В ФОНЕ (не блокируем UI)
        chatService.getChatHistoryAsync(currentUserId, otherUserId)
            .thenAccept(messages -> {
                // Этот код выполнится когда сообщения загрузятся
                Platform.runLater(() -> {
                    System.out.println("ASYNC: Loaded " + messages.size() + " messages");
                    
                    // 4. Обновляем UI когда готово
                    messagesListView.getItems().setAll(messages);
                    
                    // 5. Прокручиваем к последнему сообщению
                    if (!messages.isEmpty()) {
                        messagesListView.scrollTo(messages.size() - 1);
                    }
                    
                    // 6. Помечаем как прочитанные
                    supabaseService.markMessagesAsRead(currentUserId, otherUserId);
                    
                    // 7. Обновляем счетчик непрочитанных
                    chat.resetUnreadCount();
                    refreshChatsList();
                    
                    // 8. Скрываем индикатор загрузки
                    hideMessagesLoading();
                    isLoadingMessages = false;
                });
            })
            .exceptionally(e -> {
                // Обработка ошибок
                Platform.runLater(() -> {
                    System.out.println("ASYNC ERROR: " + e.getMessage());
                    hideMessagesLoading();
                    isLoadingMessages = false;
                    showErrorMessage("Failed to load messages: " + e.getMessage());
                });
                return null;
            });
    }
    
    // Методы для показа/скрытия индикатора загрузки
    private void showMessagesLoading() {
        Platform.runLater(() -> {
            // Простой индикатор - очищаем список и показываем текст
            messagesListView.getItems().clear();
            
            // Создаем placeholder с индикатором
            VBox loadingBox = new VBox(10);
            loadingBox.setAlignment(Pos.CENTER);
            loadingBox.setPadding(new Insets(50, 0, 50, 0));
            
            ProgressIndicator progress = new ProgressIndicator();
            progress.setPrefSize(40, 40);
            progress.getStyleClass().add("messages-loading-indicator");
            
            Label loadingLabel = new Label("Loading messages...");
            loadingLabel.getStyleClass().add("loading-label");
            
            loadingBox.getChildren().addAll(progress, loadingLabel);
            messagesListView.setPlaceholder(loadingBox);
        });
    }
    
    private void hideMessagesLoading() {
        Platform.runLater(() -> {
            // Возвращаем стандартный placeholder
            Label noMessagesLabel = new Label("No messages yet");
            noMessagesLabel.getStyleClass().add("no-messages-label");
            messagesListView.setPlaceholder(noMessagesLabel);
        });
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
            Integer receiverId = selectedChat.getOtherUser().getUserId();
            
            System.out.println("ASYNC: Sending message to user " + receiverId);
            
            // 1. Мгновенно показываем сообщение локально
            Message localMessage = createLocalMessage(currentUserId, receiverId, messageText);
            addSingleMessageToUI(localMessage);
            
            // 2. Очищаем поле ввода
            messageInputArea.clear();
            
            // 3. Отправляем в фоне
            chatService.sendMessageAsync(currentUserId, receiverId, messageText)
                .thenAccept(success -> {
                    Platform.runLater(() -> {
                        if (success) {
                            System.out.println("ASYNC: Message sent successfully");
                            // Обновляем список чатов
                            updateLastMessageInChatList(localMessage);
                        } else {
                            System.out.println("ASYNC: Failed to send message");
                            showErrorMessage("Failed to send message");
                        }
                    });
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        System.out.println("ASYNC ERROR sending message: " + e.getMessage());
                        showErrorMessage("Error sending message");
                    });
                    return null;
                });
        }
    }
    
    private Message createLocalMessage(Integer senderId, Integer receiverId, String text) {
        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setMessageText(text);
        message.setCreatedAt(LocalDateTime.now());
        message.setIsRead(false);
        return message;
    }
    
    private void addSingleMessageToUI(Message message) {
        // Проверяем, нет ли уже такого сообщения в списке
        for (Message existing : messagesListView.getItems()) {
            if (existing.getId() != null && existing.getId().equals(message.getId())) {
                return; // Сообщение уже есть
            }
        }
        
        messagesListView.getItems().add(message);
        messagesListView.scrollTo(messagesListView.getItems().size() - 1);
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
        Platform.runLater(() -> {
            chatsListView.refresh();
        });
    }
    
    private void showErrorMessage(String message) {
        System.err.println("Chat Error: " + message);
        
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Chat Error");
            alert.setHeaderText("Error");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    private void startOptimizedPolling() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
        
        scheduler = Executors.newScheduledThreadPool(2); // Два потока для разных задач
        
        System.out.println("Starting OPTIMIZED polling...");
        
        // 1. БЫСТРЫЙ ПОЛЛИНГ: Для активного чата (каждые 3 секунды)
        scheduler.scheduleAtFixedRate(() -> {
            try {
                Platform.runLater(() -> {
                    if (SessionManager.getInstance().isLoggedIn() && selectedChat != null) {
                        checkForNewMessagesInCurrentChat();
                    }
                });
            } catch (Exception e) {
                System.out.println("Error in active chat polling: " + e.getMessage());
            }
        }, 0, 3, TimeUnit.SECONDS);
        
        // 2. МЕДЛЕННЫЙ ПОЛЛИНГ: Для списка чатов (каждые 30 секунд)
        scheduler.scheduleAtFixedRate(() -> {
            try {
                Platform.runLater(() -> {
                    if (SessionManager.getInstance().isLoggedIn()) {
                        refreshChatsListOnly();
                    }
                });
            } catch (Exception e) {
                System.out.println("Error in chat list polling: " + e.getMessage());
            }
        }, 10, 30, TimeUnit.SECONDS);
        
        System.out.println("Optimized polling started: Active chat=3s, Chat list=30s");
    }

    private void checkForNewMessagesInCurrentChat() {
        if (selectedChat == null) return;
        
        Integer currentUserId = SessionManager.getInstance().getCurrentUserId();
        Integer otherUserId = selectedChat.getOtherUser().getUserId();
        
        // Получаем время последнего сообщения в текущем чате
        LocalDateTime lastMessageTime = getLastMessageTimeInCurrentChat();
        
        // Загружаем только НОВЫЕ сообщения (после последнего опроса)
        LocalDateTime sinceTime = lastPollTime.isBefore(lastMessageTime) ? 
                                 lastMessageTime : lastPollTime;
        
        List<Message> newMessages = supabaseService.getNewMessagesSince(
            currentUserId, 
            otherUserId, 
            sinceTime
        );
        
        if (!newMessages.isEmpty()) {
            System.out.println("Found " + newMessages.size() + " new messages in active chat");
            addNewMessagesToUI(newMessages);
            
            // Обновляем последнее сообщение в списке чатов
            updateLastMessageInChatList(newMessages.get(newMessages.size() - 1));
            
            // Обновляем время последнего опроса
            lastPollTime = LocalDateTime.now();
        }
    }

    private LocalDateTime getLastMessageTimeInCurrentChat() {
        if (messagesListView.getItems().isEmpty()) {
            return LocalDateTime.now().minusDays(1);
        }
        
        // Возвращаем время последнего сообщения в списке
        Message lastMessage = messagesListView.getItems().get(messagesListView.getItems().size() - 1);
        return lastMessage.getCreatedAt() != null ? 
               lastMessage.getCreatedAt() : 
               LocalDateTime.now().minusHours(1);
    }

    private void addNewMessagesToUI(List<Message> newMessages) {
        for (Message message : newMessages) {
            // Проверяем, нет ли уже такого сообщения
            boolean exists = false;
            for (Message existing : messagesListView.getItems()) {
                if (existing.getId() != null && existing.getId().equals(message.getId())) {
                    exists = true;
                    break;
                }
            }
            
            if (!exists) {
                messagesListView.getItems().add(message);
            }
        }
        
        // Прокручиваем к последнему сообщению
        if (!newMessages.isEmpty()) {
            messagesListView.scrollTo(messagesListView.getItems().size() - 1);
            
            // Помечаем как прочитанные если сообщения от собеседника
            Integer currentUserId = SessionManager.getInstance().getCurrentUserId();
            Message lastNewMessage = newMessages.get(newMessages.size() - 1);
            
            if (lastNewMessage.getSenderId() != null && 
                !lastNewMessage.getSenderId().equals(currentUserId) && 
                !lastNewMessage.getIsRead()) {
                
                supabaseService.markMessagesAsRead(currentUserId, lastNewMessage.getSenderId());
                
                // Обновляем счетчик непрочитанных
                selectedChat.resetUnreadCount();
                refreshChatsList();
            }
        }
    }

    private void updateLastMessageInChatList(Message lastMessage) {
        Platform.runLater(() -> {
            for (Chat chat : chatsListView.getItems()) {
                Integer chatOtherUserId = chat.getOtherUser().getUserId();
                Integer chatUserId = chat.getUser1Id().equals(SessionManager.getInstance().getCurrentUserId()) ? 
                                   chat.getUser2Id() : chat.getUser1Id();
                
                if (chatOtherUserId != null && chatOtherUserId.equals(lastMessage.getSenderId()) ||
                    chatOtherUserId != null && chatOtherUserId.equals(lastMessage.getReceiverId()) ||
                    chatUserId != null && chatUserId.equals(lastMessage.getSenderId()) ||
                    chatUserId != null && chatUserId.equals(lastMessage.getReceiverId())) {
                    
                    chat.setLastMessage(lastMessage);
                    
                    // Если сообщение от собеседника и не прочитано, увеличиваем счетчик
                    Integer currentUserId = SessionManager.getInstance().getCurrentUserId();
                    if (lastMessage.getSenderId() != null && 
                        !lastMessage.getSenderId().equals(currentUserId) && 
                        !lastMessage.getIsRead()) {
                        
                        if (chat.getUnreadCount() == null) {
                            chat.setUnreadCount(1);
                        } else {
                            chat.setUnreadCount(chat.getUnreadCount() + 1);
                        }
                    }
                    break;
                }
            }
            refreshChatsList();
        });
    }

    private void refreshChatsListOnly() {
        System.out.println("Refreshing chats list (light refresh)...");
        
        Integer currentUserId = SessionManager.getInstance().getCurrentUserId();
        if (currentUserId != null) {
            try {
                // Загружаем только ОСНОВНУЮ информацию о чатах
                List<Chat> updatedChats = supabaseService.getChatsLight(currentUserId);
                
                Platform.runLater(() -> {
                    // Обновляем существующие чаты, сохраняя selection
                    Chat previouslySelected = selectedChat;
                    
                    if (!updatedChats.isEmpty()) {
                        // Сохраняем непрочитанные счетчики из текущего списка
                        Map<Integer, Integer> unreadCounts = new HashMap<>();
                        for (Chat chat : chatsListView.getItems()) {
                            Integer otherUserId = chat.getOtherUser().getUserId();
                            if (otherUserId != null && chat.getUnreadCount() != null) {
                                unreadCounts.put(otherUserId, chat.getUnreadCount());
                            }
                        }
                        
                        // Обновляем список
                        chatsListView.getItems().setAll(updatedChats);
                        
                        // Восстанавливаем счетчики
                        for (Chat chat : chatsListView.getItems()) {
                            Integer otherUserId = chat.getOtherUser().getUserId();
                            if (otherUserId != null && unreadCounts.containsKey(otherUserId)) {
                                chat.setUnreadCount(unreadCounts.get(otherUserId));
                            }
                        }
                        
                        // Восстанавливаем selection
                        if (previouslySelected != null && previouslySelected.getOtherUser() != null) {
                            Integer selectedUserId = previouslySelected.getOtherUser().getUserId();
                            for (int i = 0; i < chatsListView.getItems().size(); i++) {
                                Chat chat = chatsListView.getItems().get(i);
                                if (chat.getOtherUser() != null && 
                                    chat.getOtherUser().getUserId() != null &&
                                    chat.getOtherUser().getUserId().equals(selectedUserId)) {
                                    chatsListView.getSelectionModel().select(i);
                                    selectedChat = chat;
                                    break;
                                }
                            }
                        }
                        
                        chatsListView.refresh();
                        System.out.println("Chat list refreshed: " + updatedChats.size() + " chats");
                    }
                });
            } catch (Exception e) {
                System.out.println("Error refreshing chat list: " + e.getMessage());
            }
        }
    }
    
    // Cleanup method
    public void shutdown() {
        System.out.println("ChatController shutdown initiated...");
        
        if (chatService != null) {
            chatService.shutdown();
        }
        
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
            System.out.println("Scheduler shutdown complete");
        }
        
        // Очищаем кэш профилей
        if (supabaseService != null) {
            supabaseService.clearProfileCache();
        }
        
        System.out.println("ChatController shutdown completed");
    }
    
    // Метод для принудительного обновления чатов
    public void forceRefreshChats() {
        if (SessionManager.getInstance().isLoggedIn()) {
            loadUserChatsAsync(); // Используем async версию
        }
    }
    
    // Геттер для selectedChat (может понадобиться извне)
    public Chat getSelectedChat() {
        return selectedChat;
    }
}