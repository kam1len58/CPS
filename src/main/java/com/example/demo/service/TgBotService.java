package com.example.demo.service;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import jakarta.annotation.PostConstruct;

@Component
public class TgBotService extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(TgBotService.class);

    @Value("${bot.username}")
    private String botUsername;

    @Value("${bot.token}")
    private String botToken;

    @Value("${bot.adminChatId}")
    private Long adminChatId;

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @PostConstruct
    public void init() {
        logger.info("🚀 Telegram bot for time tracking system @{} successfully created!", botUsername);
        logger.info("🔑 Bot token: {}", botToken != null ? "set" : "missing");
        logger.info("👨‍💼 Admin chat ID: {}", adminChatId);
    }

    // Register bot after full Spring context initialization
    @EventListener({ ContextRefreshedEvent.class })
    public void initBot() {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(this);
            logger.info("✅ Telegram bot for time tracking system successfully registered!");
            logger.info("📊 System ready to send project time tracking notifications");
        } catch (TelegramApiException e) {
            logger.error("❌ Error registering Telegram bot for time tracking system: {}", e.getMessage(), e);
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update == null || !update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        String takenMessage = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        logger.info("📨 Received message '{}' from chat ID {} in time tracking system", takenMessage, chatId);

        if ("/start".equals(takenMessage)) {
            sendMessage(chatId.toString(),
                    "👋 Hello! I'm the bot for Project Time Tracking System.\n\n" +
                            "📊 My main tasks:\n" +
                            "• Send notifications about time spent on tasks\n" +
                            "• Generate project reports\n" +
                            "• Notify about work status updates\n" +
                            "• Send system technical logs\n\n" +
                            "📋 To see available commands, send '/help'");

        } else if ("/help".equals(takenMessage)) {
            sendMessage(chatId.toString(),
                    "📋 Time Tracking System Commands:\n\n" +
                            "'/help' - Show this command list\n" +
                            "'/chat_id' - Show this chat ID\n" +
                            "'/send_logs_to_admin' - Send system logs to administrator\n\n" +
                            "⏰ System automatically sends:\n" +
                            "• Daily time reports\n" +
                            "• Task completion notifications\n" +
                            "• Time overrun warnings");

        } else if ("/chat_id".equals(takenMessage)) {
            sendMessage(chatId.toString(),
                    "🆔 Your chat ID: " + chatId.toString() +
                            "\n\nSave this ID for notification setup in time tracking system");

        } else if ("/send_logs_to_admin".equals(takenMessage)) {
            sendLogsToAdmin(chatId.toString());
        } else {
            sendMessage(chatId.toString(),
                    "⚠️ Unknown command.\n" +
                            "Use /help to see available commands.\n\n" +
                            "⏰ Time tracking system will automatically notify you about important project events.");
        }
    }

    public void sendMessage(String chatId, String text) {
        SendMessage newMessage = new SendMessage();
        newMessage.setChatId(chatId);
        newMessage.setText(text);
        try {
            execute(newMessage);
            logger.info("✅ Message sent to chat {} in time tracking system", chatId);
        } catch (Exception e) {
            logger.warn("⚠️ Error sending message in time tracking system: {}", e.getMessage(), e);
        }
    }

    public void sendLogsToAdmin(String reasonChatId) {
        try {
            File logFile = new File("logs/system-time-tracking.log");

            // Check different possible log file names
            if (!logFile.exists()) {
                logFile = new File("logs/application.log");
            }
            if (!logFile.exists()) {
                logFile = new File("logs/spring.log");
            }
            if (!logFile.exists()) {
                logFile = new File("logs/smart-home-syst.log"); // fallback
            }

            if (!logFile.exists()) {
                logger.warn("📁 Time tracking system log file not found");
                sendMessage(adminChatId.toString(),
                        "⚠️ Time tracking system log file not found. Check logging settings.");
                sendMessage(reasonChatId,
                        "📁 Time tracking system log file temporarily unavailable. Please try again later.");
                return;
            }

            SendDocument currentLogs = new SendDocument();
            currentLogs.setChatId(adminChatId.toString());
            currentLogs.setCaption("📊 Project Time Tracking System Logs");
            currentLogs.setDocument(new InputFile(logFile, "time-tracking-system-logs.log"));

            execute(currentLogs);
            sendMessage(reasonChatId, "✅ Time tracking system logs sent to administrator. Thank you for reporting!");
            logger.info("📤 Time tracking system log file successfully sent to administrator");

        } catch (Exception e) {
            logger.warn("⚠️ Error sending time tracking system log file: {}", e.getMessage(), e);
            sendMessage(adminChatId.toString(), "❌ Error sending time tracking system logs: " + e.getMessage());
        }
    }

    // Additional method for time tracking notifications
    public void sendTimeNotification(String chatId, String projectName, String taskName, String timeSpent,
            String totalTime) {
        String message = String.format(
                "⏰ Time Tracking Notification:\n" +
                        "────────────────────\n" +
                        "📁 Project: %s\n" +
                        "📝 Task: %s\n" +
                        "⏱️ Time spent: %s\n" +
                        "📈 Total time: %s\n" +
                        "────────────────────\n" +
                        "✅ Task recorded in system",
                projectName, taskName, timeSpent, totalTime);

        sendMessage(chatId, message);
        logger.info("⏱️ Time notification sent for project '{}', task '{}'", projectName, taskName);
    }

    // Method for daily reports
    public void sendDailyReport(String chatId, String date, String totalProjects, String totalTime,
            String completedTasks) {
        String message = String.format(
                "📊 Daily Time Tracking Report\n" +
                        "═══════════════════════\n" +
                        "📅 Date: %s\n" +
                        "📁 Projects: %s\n" +
                        "⏱️ Total time: %s\n" +
                        "✅ Completed tasks: %s\n" +
                        "═══════════════════════\n" +
                        "📈 Statistics saved in system",
                date, totalProjects, totalTime, completedTasks);

        sendMessage(chatId, message);
        logger.info("📅 Daily report sent for {}", date);
    }

    // Method for project completion notification
    public void sendProjectCompletionNotification(String chatId, String projectName, String completionDate,
            String totalTime) {
        String message = String.format(
                "🎉 Project Completed!\n" +
                        "═══════════════════════\n" +
                        "📁 Project: %s\n" +
                        "📅 Completion date: %s\n" +
                        "⏱️ Total project time: %s\n" +
                        "═══════════════════════\n" +
                        "✅ Project archived in time tracking system",
                projectName, completionDate, totalTime);

        sendMessage(chatId, message);
        logger.info("🎉 Project completion notification sent for project '{}'", projectName);
    }

    // Method for warning about time overrun
    public void sendTimeOverrunWarning(String chatId, String projectName, String taskName, String allocatedTime,
            String actualTime) {
        String message = String.format(
                "⚠️ Time Overrun Warning!\n" +
                        "═══════════════════════\n" +
                        "📁 Project: %s\n" +
                        "📝 Task: %s\n" +
                        "⏱️ Allocated time: %s\n" +
                        "⏱️ Actual time: %s\n" +
                        "═══════════════════════\n" +
                        "📊 Please review task requirements",
                projectName, taskName, allocatedTime, actualTime);

        sendMessage(chatId, message);
        logger.info("⚠️ Time overrun warning sent for project '{}', task '{}'", projectName, taskName);
    }
}