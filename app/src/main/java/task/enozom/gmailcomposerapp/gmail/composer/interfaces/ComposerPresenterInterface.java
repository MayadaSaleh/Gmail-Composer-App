package task.enozom.gmailcomposerapp.gmail.composer.interfaces;

import android.content.Context;
import android.net.Uri;

import com.google.firebase.storage.UploadTask;

/**
 * Created by Mayada on 8/8/2018.
 */

public interface ComposerPresenterInterface {

    void presenterSaveFileToDatabase(String subjectToSave, String contentToSave, String attachmentURL);
    void presenterDeleteFileFromFirebaseStorage(String attachmentURL);
    void presenterDeletionFromFirebaseResponse(Boolean checkDeletion);

}
