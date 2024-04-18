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

// Controller class for managing the slide show of images
public class SlideController {
    // Labels to display the percentage values of red, green, and blue components
    public Label Red;
    public Label Green;
    public Label Blue;
    public Label photoName; // Label for displaying the name of the image

    @FXML
    Parent root; // Root element from the FXML

    @FXML
    private ImageView imageView; // ImageView for displaying the image

    @FXML
    private Pane RGB; // Pane for RGB information

    private final List<File> images = new ArrayList<>(); // List to store loaded image files
    private int currentImageIndex = 0; // Index to track the current image being displayed
    private Thread slideShowThread; // Thread for running the slideshow
    private final AtomicBoolean slideShowRunning = new AtomicBoolean(false); // Atomic flag to control slideshow running state

    // Handle the action of the 'Load' button to load images
    @FXML
    public void handleBtnLoadAction(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select image files");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.gif", "*.tif", "*.bmp"));
        List<File> files = fileChooser.showOpenMultipleDialog(new Stage());

        if (files != null && !files.isEmpty()) {
            images.addAll(files); // Add selected files to the image list
            displayImage(); // Display the first image
        }
    }

    // Display the current image in the ImageView and update relevant information
    private void displayImage() {
        if (!images.isEmpty()) {
            File currentFile = images.get(currentImageIndex);
            Image image = new Image(currentFile.toURI().toString());
            imageView.setImage(image);
            photoName.setText(currentFile.getName()); // Update the file name label
            analyzeAndDisplayImageColors(); // Analyze and display color percentages
        }
    }

    // Start the slideshow on 'Start' button click
    @FXML
    public void handleBtnStart(ActionEvent actionEvent) {
        if (slideShowRunning.get()) return; // Return if the slideshow is already running

        slideShowRunning.set(true);
        slideShowThread = new Thread(this::slideShow);
        slideShowThread.start(); // Start the slideshow in a new thread
    }

    // Slideshow logic to cycle through images every 5 seconds
    public void slideShow() {
        while (slideShowRunning.get()) {
            Platform.runLater(() -> {
                currentImageIndex = (currentImageIndex + 1) % images.size(); // Update index to show next image
                displayImage(); // Display the image at the new index
            });

            try {
                Thread.sleep(5000); // Wait for 5 seconds before showing the next image
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                slideShowRunning.set(false); // Stop slideshow on interruption
            }
        }
    }

    // Stop the slideshow on 'End' button click
    @FXML
    public void handleBtnEnd(ActionEvent actionEvent) {
        if (slideShowRunning.getAndSet(false) && slideShowThread != null) {
            slideShowThread.interrupt(); // Interrupt the slideshow thread
        }
    }

    // Navigate to the previous image on 'Previous' button click
    public void handleBtnPreviousAction(ActionEvent actionEvent) {
        if (!images.isEmpty()) {
            currentImageIndex = (currentImageIndex - 1 + images.size()) % images.size(); // Update index for previous image
            displayImage(); // Display the previous image
        }
    }

    // Navigate to the next image on 'Next' button click
    public void handleBtnNextAction(ActionEvent actionEvent) {
        if (!images.isEmpty()) {
            currentImageIndex = (currentImageIndex + 1) % images.size(); // Update index for next image
            displayImage(); // Display the next image
        }
    }

    // Analyze the image and display the percentage of red, green, and blue colors
    public void analyzeAndDisplayImageColors() {
        if (imageView.getImage() == null) {
            return; // Do nothing if no image is loaded
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

        long total = totalRed + totalGreen + totalBlue; // Total sum of all colors
        double percentRed = total > 0 ? (double) totalRed / total * 100 : 0;
        double percentGreen = total > 0 ? (double) totalGreen / total * 100 : 0;
        double percentBlue = total > 0 ? (double) totalBlue / total * 100 : 0;

        // Update the labels to show calculated percentage values
        Platform.runLater(() -> {
            Red.setText(String.format("%.2f%%", percentRed));
            Green.setText(String.format("%.2f%%", percentGreen));
            Blue.setText(String.format("%.2f%%", percentBlue));
        });
    }
}
