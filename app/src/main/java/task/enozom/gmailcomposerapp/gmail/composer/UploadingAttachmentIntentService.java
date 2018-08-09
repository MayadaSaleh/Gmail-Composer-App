package task.enozom.gmailcomposerapp.gmail.composer;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import task.enozom.gmailcomposerapp.gmail.composer.interfaces.ComposerViewInterface;

public class UploadingAttachmentIntentService extends IntentService {

    private StorageReference storageReference;
    private UploadTask.TaskSnapshot myTaskSnapShot;
    private Boolean acceptedFile = false;

    public UploadingAttachmentIntentService() {
        super("UploadingAttachmentIntentService");
    }

    ComposerViewInterface composerViewInterface;

    public UploadingAttachmentIntentService(ComposerViewInterface composerViewInterface) {
        super("UploadingAttachmentIntentService");
        this.composerViewInterface = composerViewInterface;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
       //  MainActivity mainActivity = new MainActivity();
        if (intent != null) {
             Uri filePath = Uri.parse(intent.getExtras().getString("filepath"));
           Boolean checkAttachmentType = intent.getBooleanExtra("checkAttachmentType",false);

            storageReference = FirebaseStorage.getInstance().getReference(filePath.getLastPathSegment());
            StorageReference sRef;
            if (checkAttachmentType == true) {
                sRef = storageReference.child("video.mp4");
            } else {
                sRef = storageReference.child("image.jpg");
            }
            sRef.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            myTaskSnapShot = taskSnapshot;
                            acceptedFile = true;

                            composerViewInterface.viewResponseTosaveTofirebaseStorage(myTaskSnapShot, acceptedFile);
                            composerViewInterface.dismissProgressBar();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            composerViewInterface.dismissProgressBar();
                            Log.i("failure", "failure " + exception.getMessage());
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //displaying the upload progress
                            composerViewInterface.showUploadingPercentage(taskSnapshot);
                        }
                    });

        }
    }
}
