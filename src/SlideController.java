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
import java.util.Timer;
import java.util.TimerTask;

public class SlideController {
    public Label Red;
    public Label Green;
    public Label Blue;


    @FXML
    Parent root;

    @FXML
    private ImageView imageView;

    @FXML
     private Pane RGB;
    Timer timer = new Timer();


    private final List<Image> images = new ArrayList<>();
    private int currentImageIndex = 0;

    @FXML
    public void handleBtnLoadAction(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select image files");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images",
                "*.png", "*.jpg", "*.gif", "*.tif", "*.bmp"));
        List<File> files = fileChooser.showOpenMultipleDialog(new Stage());

        if (files != null && !files.isEmpty()) {
            files.forEach(file -> images.add(new Image(file.toURI().toString())));
            displayImage();
        }
    }

    private void displayImage() {
        if (!images.isEmpty()) {
            imageView.setImage(images.get(currentImageIndex));
            analyzeAndDisplayImageColors();
        }
    }

    @FXML
    public void handleBtnStart(ActionEvent actionEvent) {
        timer.cancel(); // Вимкнути поточний таймер перед створенням нового
        timer = new Timer();
        slideShow();
    }

    public void slideShow() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    currentImageIndex = (currentImageIndex + 1) % images.size();
                    displayImage();
                });
            }
        }, 0, 5000); // Почати негайно з інтервалом у 5000 мс (5 сек)
    }

    @FXML
    public void handleBtnEnd(ActionEvent actionEvent) {
        timer.cancel(); // Скасувати слайд-шоу
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
        // Обчислення відсоткових значень для кожного кольору
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