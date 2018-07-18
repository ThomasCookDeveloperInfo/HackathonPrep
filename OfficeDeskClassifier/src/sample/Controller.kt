package sample

import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import org.opencv.videoio.VideoCapture
import java.io.ByteArrayInputStream
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class Controller {
    @FXML
    private lateinit var vidImage: ImageView

    @FXML
    private lateinit var btnStartCamera: Button

    @FXML
    fun initialize() {
        // Load the opencv library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
    }

    @FXML
    fun startCamera() {
        // Init video
        val video = VideoCapture("C:/Users/info/Desktop/Projects/HackathonPrep/sample.mp4")

        // Load cascade
        val cascade = CascadeClassifier()
        cascade.load("C:/Users/info/Desktop/Projects/HackathonPrep/haarcascade_profileface.xml")

        // Every 33 milliseconds, grab a frame from video, do cascade classification on it
        // Then render the frame with the classified bounding boxes overlaid on top of it
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
            if (video.isOpened) {
                // Read from the video into a mat object
                val frame = Mat()
                video.read(frame)

                // Convert the frame to grey scale
                val greyFrame = Mat()
                Imgproc.cvtColor(frame, greyFrame, Imgproc.COLOR_BGR2GRAY)
                Imgproc.equalizeHist(greyFrame, greyFrame)

                // Do cascade classification
                val classifiedBoundingBoxes = MatOfRect()
                val minFaceSize = greyFrame.rows() * 0.2
                cascade.detectMultiScale(greyFrame, classifiedBoundingBoxes, 1.01, 4, 0, Size(minFaceSize, minFaceSize), Size())

                // Render classified bounding boxes to frame
                classifiedBoundingBoxes.toArray().forEach {
                    Imgproc.rectangle(frame, it.tl(), it.br(), Scalar(0.0, 255.0, 0.0), 3)
                }

                // Encode the frame to a buffer for rendering
                val buffer = MatOfByte()
                Imgcodecs.imencode(".jpg", frame, buffer)

                // Set the image to the buffer (run later to avoid blocking)
                Platform.runLater {
                    this.vidImage.image = Image(ByteArrayInputStream(buffer.toArray()), 1024.0, 769.0, true, true)
                }
            }
        },0, 33, TimeUnit.MILLISECONDS)

        // Update button text
        this.btnStartCamera.text = "Stop Video"
    }
}