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
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("messages");
    private  StorageReference storageReference;


    public ComposerInteractor(ComposerPresenterInterface composerPresenterInterface) {
        this.composerPresenterInterface = composerPresenterInterface;
    }

    @Override
    public void interactorSaveFileToDatabase(String subjectToSave, String contentToSave, String attachmentURL) {

        MessagePojo uploadedMessage = new MessagePojo(subjectToSave, contentToSave, attachmentURL);
        String uploadIdDatabase = mDatabase.push().getKey();
        mDatabase.child(uploadIdDatabase).setValue(uploadedMessage);
    }

    @Override
    public void interactorDeleteFileFromFirebaseStorage(String attachmentURL) {
        storageReference = FirebaseStorage.getInstance().getReference().child(attachmentURL);
        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                composerPresenterInterface.presenterDeletionFromFirebaseResponse(true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception exception) {
                composerPresenterInterface.presenterDeletionFromFirebaseResponse(false);
            }
        });
    }
    }
