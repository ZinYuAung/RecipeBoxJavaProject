package com.recipebox.App.component;

import com.recipebox.App.entity.MealPlanItem;
import com.recipebox.App.entity.Recipe;
import com.recipebox.App.service.MealPlanService;
import com.recipebox.App.service.RecipeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RecipeBoxBot extends TelegramLongPollingBot {

    private final String botUsername;
    private final RecipeService recipeService;
    private final MealPlanService mealPlanService;

    // Track state for multi-step conversations
    private enum BmiState { NONE, AWAITING_HEIGHT, AWAITING_WEIGHT }

    // Store user session data in-memory (chatId -> state/data)
    private final Map<Long, BmiState> userBmiState = new ConcurrentHashMap<>();
    private final Map<Long, Double> userHeights = new ConcurrentHashMap<>();

    public RecipeBoxBot(@Value("${telegram.bot.token}") String botToken,
                        @Value("${telegram.bot.username}") String botUsername,
                        RecipeService recipeService,
                        MealPlanService mealPlanService) {
        super(botToken);
        this.botUsername = botUsername;
        this.recipeService = recipeService;
        this.mealPlanService = mealPlanService;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        // 1. Handle regular text messages
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText().trim();
            long chatId = update.getMessage().getChatId();

            BmiState currentState = userBmiState.getOrDefault(chatId, BmiState.NONE);

            // Handle multi-step input first
            if (currentState == BmiState.AWAITING_HEIGHT) {
                handleHeightInput(chatId, messageText);
                return;
            } else if (currentState == BmiState.AWAITING_WEIGHT) {
                handleWeightInput(chatId, messageText);
                return;
            }

            // Command Handling
            if (messageText.equalsIgnoreCase("/start")) {
                sendWelcomeMessageWithButtons(chatId);
            } else if (messageText.equalsIgnoreCase("/today")) {
                handleTodayMealPlan(chatId);
            } else if (messageText.equalsIgnoreCase("/random")) {
                handleRandomRecipe(chatId);
            } else {
                sendResponse(chatId, "❓ Tap a button below to get started!", buildMainMenuKeyboard());
            }
        }
        // 2. Handle button clicks
        else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            String callbackQueryId = update.getCallbackQuery().getId();

            if (callbackData.equals("CMD_TODAY_PLAN")) {
                handleTodayMealPlan(chatId);
            } else if (callbackData.equals("CMD_RANDOM_RECIPE")) {
                handleRandomRecipe(chatId);
            } else if (callbackData.equals("CMD_START_BMI")) {
                startBmiStepProcess(chatId);
            }
        }
    }
    

    // --- Interactive Multi-Step BMI Logic ---

    private void startBmiStepProcess(long chatId) {
        userBmiState.put(chatId, BmiState.AWAITING_HEIGHT);
        
        // ForceReply puts a direct text reply box in Telegram
        SendMessage message = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text("📐 *Step 1/2: Enter your height in cm*\n\nExample: `170`")
                .parseMode("Markdown")
                .replyMarkup(new ForceReplyKeyboard(true)) 
                .build();

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleHeightInput(long chatId, String text) {
        try {
            double heightCm = Double.parseDouble(text);
            if (heightCm < 50 || heightCm > 250) {
                sendResponse(chatId, "⚠️ Please enter a realistic height between 50 and 250 cm.");
                return;
            }

            userHeights.put(chatId, heightCm);
            userBmiState.put(chatId, BmiState.AWAITING_WEIGHT);

            SendMessage message = SendMessage.builder()
                    .chatId(String.valueOf(chatId))
                    .text("⚖️ *Step 2/2: Enter your weight in kg*\n\nExample: `68`")
                    .parseMode("Markdown")
                    .replyMarkup(new ForceReplyKeyboard(true))
                    .build();

            execute(message);

        } catch (NumberFormatException e) {
            sendResponse(chatId, "⚠️ Invalid number! Please type a number for your height (e.g. `175`).");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleWeightInput(long chatId, String text) {
        try {
            double weightKg = Double.parseDouble(text);
            if (weightKg < 20 || weightKg > 300) {
                sendResponse(chatId, "⚠️ Please enter a realistic weight between 20 and 300 kg.");
                return;
            }

            double heightCm = userHeights.getOrDefault(chatId, 170.0);
            double heightM = heightCm / 100.0;
            double bmi = weightKg / (heightM * heightM);
            String category = getBMICategory(bmi);

            // Reset user state
            userBmiState.remove(chatId);
            userHeights.remove(chatId);

            String resultText = String.format(
                    "📊 *Your BMI Assessment Result:*\n\n" +
                    "• *Height:* %.1f cm\n" +
                    "• *Weight:* %.1f kg\n" +
                    "• *BMI Score:* *%.1f*\n" +
                    "• *Category:* *%s*\n\n" +
                    "💡 *Next step:* Click *Today's Plan* below to view recommended meals!",
                    heightCm, weightKg, bmi, category
            );

            sendResponse(chatId, resultText, buildMainMenuKeyboard());

        } catch (NumberFormatException e) {
            sendResponse(chatId, "⚠️ Invalid number! Please type a number for your weight (e.g. `68`).");
        }
    }

    // --- Keyboards & Handlers ---

    private InlineKeyboardMarkup buildMainMenuKeyboard() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton btnToday = new InlineKeyboardButton("📅 Today's Plan");
        btnToday.setCallbackData("CMD_TODAY_PLAN");

        InlineKeyboardButton btnRandom = new InlineKeyboardButton("🎲 Random Recipe");
        btnRandom.setCallbackData("CMD_RANDOM_RECIPE");

        row1.add(btnToday);
        row1.add(btnRandom);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton btnBmi = new InlineKeyboardButton("⚖️ Calculate BMI Step-by-Step");
        btnBmi.setCallbackData("CMD_START_BMI");
        row2.add(btnBmi);

        rows.add(row1);
        rows.add(row2);
        markup.setKeyboard(rows);

        return markup;
    }

    private void sendWelcomeMessageWithButtons(long chatId) {
        sendResponse(chatId, "🥗 *Welcome to RecipeBox Bot!*\n\nTap a button below to quickly access meal suggestions or calculate your daily metrics.", buildMainMenuKeyboard());
    }

    private void handleTodayMealPlan(long chatId) {
        List<MealPlanItem> plan = mealPlanService.generate7DayMealPlan("Normal", Collections.emptyList());
        if (plan == null || plan.isEmpty()) {
            sendResponse(chatId, "🍱 No suitable recipes found in the database.", buildMainMenuKeyboard());
            return;
        }

        StringBuilder sb = new StringBuilder("📅 *Today's Recommended Meals:*\n\n");
        plan.stream()
        .filter(item -> item.getDayNumber() == 1)
        .forEach(item -> {
            Recipe recipe = item.getRecipe();
            
            // 1. Append Main Title Line
            sb.append("• *").append(item.getMealType()).append("*: ")
              .append(recipe.getName())
              .append(" (🔥 ").append(recipe.getCalories()).append(" kcal)\n");

            // 2. Append Description safely if present
            if (recipe.getDescription() != null && !recipe.getDescription().isBlank()) {
                // Replace line breaks with indented line breaks so paragraphs stay aligned
                String formattedDesc = recipe.getDescription().trim()
                                             .replace("\n", "\n  "); 
                
                sb.append("  _").append(formattedDesc).append("_\n");
            }
            
            sb.append("\n"); // Extra spacing between meals
        });

        sendResponse(chatId, sb.toString(), buildMainMenuKeyboard());
    }
 
    
    private void handleRandomRecipe(long chatId) {
        List<Recipe> recipes = recipeService.getActiveRecipes();
        if (recipes == null || recipes.isEmpty()) {
            sendResponse(chatId, "🍲 No active recipes available right now.", buildMainMenuKeyboard());
            return;
        }

        Recipe randomRecipe = recipes.get((int) (Math.random() * recipes.size()));

        String message = String.format(
                "🎲 *Featured Recipe:*\n\n" +
                "📖 *Name:* %s\n" +
                "🏷️ *Type:* %s\n" +
                "⏱️ *Cooking Time:* %d mins\n" +
                "🔥 *Calories:* %d kcal",
                randomRecipe.getName(),
                randomRecipe.getMealType(),
                randomRecipe.getCookingTimeMin(),
                randomRecipe.getCalories()
        );
        // 2. Append Description safely if present
        if (randomRecipe.getDescription() != null && !randomRecipe.getDescription().isBlank()) {
            // Replace line breaks with indented line breaks so paragraphs stay aligned
            String formattedDesc = randomRecipe.getDescription().trim()
                                         .replace("\n", "\n  "); 
            
            message = message + "  _" + formattedDesc + "_\n";
        }
        
        sendResponse(chatId, message, buildMainMenuKeyboard());
    }

    private void acknowledgeCallback(String callbackQueryId) {
        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(callbackQueryId);
        try {
            execute(answer);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private String getBMICategory(double bmi) {
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25.0) return "Normal";
        if (bmi < 30.0) return "Overweight";
        return "Obese";
    }

    public void sendResponse(long chatId, String text) {
        sendResponse(chatId, text, null);
    }

    public void sendResponse(long chatId, String text, InlineKeyboardMarkup keyboard) {
        SendMessage message = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(text)
                .parseMode("Markdown")
                .replyMarkup(keyboard)
                .build();
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}