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
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class Controller {
    private var timer: ScheduledExecutorService? = null

    @FXML
    private lateinit var vidImage: ImageView

    @FXML
    private lateinit var btnStartCamera: Button

    @FXML
    fun initialize() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
        val mat = Mat.eye(3, 3, CvType.CV_8UC1)
        System.out.println("mat = ${mat.dump()}")
    }

    @FXML
    fun startCamera() {
        this.timer?.let {
//            it.execute {
//                Platform.runLater {
//                    // Release and null video
//                    this.video?.release()
//
//                    // Update button text
//                    this.btnStartCamera.text = "Start Camera"
//
//                    // Null timer
//                    this.timer = null
//                }
//            }
        } ?: {
            // Init video
            val video = VideoCapture("C:/Users/info/Desktop/Projects/HackathonPrep/sample.mp4")

            val faceCascade = CascadeClassifier()
            faceCascade.load("C:/Users/info/Desktop/Projects/HackathonPrep/haarcascade_frontalface_default.xml")

            // Every 33 milliseconds, grab a frame and render it
            this.timer = Executors.newSingleThreadScheduledExecutor()
            this.timer?.scheduleAtFixedRate({
                if (video.isOpened == true) {
                    val frame = Mat()
                    video.read(frame)

                    // Convert the frame to grey scale
                    val greyFrame = Mat()
                    Imgproc.cvtColor(frame, greyFrame, Imgproc.COLOR_BGR2GRAY)
                    Imgproc.equalizeHist(greyFrame, greyFrame)

                    // Track faces
                    val faces = MatOfRect()
                    val minFaceSize = greyFrame.rows() * 0.05
                    faceCascade.detectMultiScale(greyFrame, faces, 1.1, 2, 0, Size(minFaceSize, minFaceSize), Size())

                    // Render faces to frame
                    faces.toArray().forEach {
                        Imgproc.rectangle(frame, it.tl(), it.br(), Scalar(0.0, 255.0, 0.0), 3)
                    }

                    val buffer = MatOfByte()
                    Imgcodecs.imencode(".png", frame, buffer)

                    Platform.runLater {
                        this.vidImage.image = Image(ByteArrayInputStream(buffer.toArray()), 1024.0, 769.0, true, true)
                    }
                }
            },0, 33, TimeUnit.MILLISECONDS)

            // Update button text
            this.btnStartCamera.text = "Stop Camera"
        }.invoke()
    }
}