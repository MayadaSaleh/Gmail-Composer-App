package task.enozom.gmailcomposerapp.gmail.composer;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import task.enozom.gmailcomposerapp.R;

/**
 * Created by Mayada on 8/8/2018.
 */

public class ComposerInteractor implements ComposerInteractorInterface{

    private ComposerPresenterInterface composerPresenterInterface;


    public ComposerInteractor(ComposerPresenterInterface composerPresenterInterface) {
        this.composerPresenterInterface = composerPresenterInterface;

    }

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("messages");
    private StorageReference storageReference= FirebaseStorage.getInstance().getReference();
    private Boolean acceptedFile = false;
    private UploadTask.TaskSnapshot myTaskSnapShot;



    @Override
    public void interactorSaveFileToDatabase(String subjectToSave, String contentToSave, String attachmentURL) {
        MessagePojo uploadedMessage = new MessagePojo(subjectToSave, contentToSave, attachmentURL);

        String uploadId = mDatabase.push().getKey();
        mDatabase.child(uploadId).setValue(uploadedMessage);

    }

    @Override
    public void interactorUploadFileToFirebaseStorage(Uri filePath, Boolean checkattachmentType) {

        StorageReference sRef;
        if (checkattachmentType == true){
            sRef = storageReference.child("messages/video.mp4");
        }else{
            sRef = storageReference.child("messages/image.jpg");
        }
        sRef.putFile(filePath)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        composerPresenterInterface.presenterDismissDialog();
                        myTaskSnapShot = taskSnapshot;
                        acceptedFile= true;

                        composerPresenterInterface.presenterResponseTosaveTofirebaseStorage(myTaskSnapShot,acceptedFile);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        composerPresenterInterface.presenterDismissDialog();
                        Log.i("failure", "failure " + exception.getMessage());
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        //displaying the upload progress
                       composerPresenterInterface.presenterUploadingProgress(taskSnapshot);
                    }
                });

    }
}
