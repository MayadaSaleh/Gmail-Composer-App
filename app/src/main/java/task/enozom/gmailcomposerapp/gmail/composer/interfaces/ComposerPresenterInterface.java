package task.enozom.gmailcomposerapp.gmail.composer.interfaces;

/**
 * Created by Mayada on 8/8/2018.
 */

public interface ComposerPresenterInterface {

    void presenterSaveFileToDatabase(String subjectToSave, String contentToSave, String attachmentURL);
    void presenterDeleteFileFromFirebaseStorage(String attachmentURL, Boolean checkAttachmentType);
    void presenterDeletionFromFirebaseResponse(Boolean checkDeletion);

}
