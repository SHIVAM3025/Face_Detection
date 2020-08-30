package in.app.newproject;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.util.List;

import dmax.dialog.SpotsDialog;
import in.app.newproject.Helper.GraphicOverlay;
import in.app.newproject.Helper.RectOverlay;

public class MainActivity extends AppCompatActivity {

    private Button detect_button;
    CameraView cameraView;
    GraphicOverlay graphicOverlay;
    AlertDialog alertDialog;

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.stop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        detect_button = findViewById(R.id.detect_button);
        cameraView = findViewById(R.id.camera_view);
        graphicOverlay = findViewById(R.id.graphic_overlay);

        alertDialog = new SpotsDialog.Builder().setContext(this)
                .setMessage("Please Wait")
                .setCancelable(false)
                .build();

        detect_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                cameraView.start();
                cameraView.captureImage();
                graphicOverlay.clear();

            }
        });

        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                alertDialog.show();

                Bitmap bitmap = cameraKitImage.getBitmap();
                bitmap = Bitmap.createScaledBitmap(bitmap, cameraView.getWidth(), cameraView.getHeight(), false);
                cameraView.stop();

                runFaceDetector(bitmap);

            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });


    }

    private void runFaceDetector(Bitmap bitmap) {

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionFaceDetectorOptions options = new FirebaseVisionFaceDetectorOptions.Builder().build();

        FirebaseVisionFaceDetector faceDetector = FirebaseVision.getInstance().getVisionFaceDetector(options);

        faceDetector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {

                        processFaceResult(firebaseVisionFaces);

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void processFaceResult(List<FirebaseVisionFace> firebaseVisionFaces) {

        int count = 0;
        for(FirebaseVisionFace face :firebaseVisionFaces){
            Rect bounds = face.getBoundingBox();

            RectOverlay overlay = new RectOverlay(graphicOverlay ,bounds);
            graphicOverlay.add(overlay);

        }

        alertDialog.dismiss();

        Toast.makeText(this, "Detected Faces", Toast.LENGTH_SHORT).show();

    }
}