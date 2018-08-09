package task.enozom.gmailcomposerapp.gmail.composer;

import android.net.Uri;

/**
 * Created by Mayada on 8/8/2018.
 */

public interface ComposerInteractorInterface {

    void interactorSaveFileToDatabase(String subjectToSave, String contentToSave, String attachmentURL);

    void interactorUploadFileToFirebaseStorage(Uri filePath, Boolean checkattachmentType);
}
