package task.enozom.gmailcomposerapp.gmail.composer;

import android.content.Context;
import android.net.Uri;

import com.google.firebase.storage.UploadTask;

/**
 * Created by Mayada on 8/8/2018.
 */

public interface ComposerPresenterInterface {

    void presenterSaveFileToDatabase(String subjectToSave, String contentToSave, String attachmentURL);
    void presenterUploadFileToFirebaseStorage(Uri filePath, Context context, Boolean checkattachmentType);

    void presenterResponseTosaveTofirebaseStorage(UploadTask.TaskSnapshot myTaskSnapShot, Boolean acceptedFile);
}
