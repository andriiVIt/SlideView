import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class SlideController {
    public Label Red;
    public Label Green;
    public Label Blue;
    public Label photoName; // Лейбл для відображення імені зображення

    @FXML
    Parent root;

    @FXML
    private ImageView imageView;

    @FXML
    private Pane RGB;

    private final List<File> images = new ArrayList<>(); // Змінили тип зберігання на List<File>
    private int currentImageIndex = 0;
    private Thread slideShowThread;
    private final AtomicBoolean slideShowRunning = new AtomicBoolean(false);

    @FXML
    public void handleBtnLoadAction(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select image files");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images",
                "*.png", "*.jpg", "*.gif", "*.tif", "*.bmp"));
        List<File> files = fileChooser.showOpenMultipleDialog(new Stage());

        if (files != null && !files.isEmpty()) {
            images.addAll(files); // Додавання файлів безпосередньо до списку
            displayImage();
        }
    }

    private void displayImage() {
        if (!images.isEmpty()) {
            File currentFile = images.get(currentImageIndex);
            Image image = new Image(currentFile.toURI().toString());
            imageView.setImage(image);
            photoName.setText(currentFile.getName()); // Оновлюємо ім'я файлу
            analyzeAndDisplayImageColors();
        }
    }

    @FXML
    public void handleBtnStart(ActionEvent actionEvent) {
        if (slideShowRunning.get()) return; // Слайд-шоу вже запущено

        slideShowRunning.set(true);
        slideShowThread = new Thread(this::slideShow);
        slideShowThread.start();
    }

    public void slideShow() {
        while (slideShowRunning.get()) {
            Platform.runLater(() -> {
                currentImageIndex = (currentImageIndex + 1) % images.size();
                displayImage();
            });

            try {
                Thread.sleep(5000); // Задержка між слайдами
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                slideShowRunning.set(false);
            }
        }
    }

    @FXML
    public void handleBtnEnd(ActionEvent actionEvent) {
        if (slideShowRunning.getAndSet(false) && slideShowThread != null) {
            slideShowThread.interrupt(); // Зупиняємо слайд-шоу
        }
    }

    public void handleBtnPreviousAction(ActionEvent actionEvent) {
        if (!images.isEmpty()) {
            currentImageIndex = (currentImageIndex - 1 + images.size()) % images.size();
            displayImage();
        }
    }

    public void handleBtnNextAction(ActionEvent actionEvent) {
        if (!images.isEmpty()) {
            currentImageIndex = (currentImageIndex + 1) % images.size();
            displayImage();
        }
    }

    public void analyzeAndDisplayImageColors() {
        if (imageView.getImage() == null) {
            return; // Якщо зображення не завантажено, не робимо нічого
        }

        Image image = imageView.getImage();
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        long totalRed = 0;
        long totalGreen = 0;
        long totalBlue = 0;

        PixelReader pixelReader = image.getPixelReader();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);
                totalRed += color.getRed() * 255;
                totalGreen += color.getGreen() * 255;
                totalBlue += color.getBlue() * 255;
            }
        }

        long total = totalRed + totalGreen + totalBlue; // Загальна сума всіх кольорів
        double percentRed = total > 0 ? (double) totalRed / total * 100 : 0;
        double percentGreen = total > 0 ? (double) totalGreen / total * 100 : 0;
        double percentBlue = total > 0 ? (double) totalBlue / total * 100 : 0;

        // Відображення обчислених відсоткових значень у лейблах
        Platform.runLater(() -> {
            Red.setText(String.format("%.2f%%", percentRed));
            Green.setText(String.format("%.2f%%", percentGreen));
            Blue.setText(String.format("%.2f%%", percentBlue));
        });
    }

}