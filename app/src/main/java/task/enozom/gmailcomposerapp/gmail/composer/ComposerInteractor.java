package task.enozom.gmailcomposerapp.gmail.composer;


import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import task.enozom.gmailcomposerapp.gmail.composer.interfaces.ComposerInteractorInterface;
import task.enozom.gmailcomposerapp.gmail.composer.interfaces.ComposerPresenterInterface;


/**
 * Created by Mayada on 8/8/2018.
 */

public class ComposerInteractor implements ComposerInteractorInterface {

    private ComposerPresenterInterface composerPresenterInterface;
    private Boolean acceptedFile = false;
    private UploadTask.TaskSnapshot myTaskSnapShot;
    private DatabaseReference mDatabase;
    private StorageReference storageReference;


    public ComposerInteractor(ComposerPresenterInterface composerPresenterInterface) {
        this.composerPresenterInterface = composerPresenterInterface;

    }

    @Override
    public void interactorSaveFileToDatabase(String subjectToSave, String contentToSave, String attachmentURL) {

        mDatabase = FirebaseDatabase.getInstance().getReference("messages");
        MessagePojo uploadedMessage = new MessagePojo(subjectToSave, contentToSave, attachmentURL);
        String uploadIdDatabase = mDatabase.push().getKey();
        mDatabase.child(uploadIdDatabase).setValue(uploadedMessage);
    }

    @Override
    public void interactorUploadFileToFirebaseStorage(Uri filePath, Boolean checkAttachmentType) {

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
                        composerPresenterInterface.presenterResponseTosaveTofirebaseStorage(myTaskSnapShot, acceptedFile);
                        composerPresenterInterface.presenterDismissDialog();
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
