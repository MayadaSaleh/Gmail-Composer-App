package task.enozom.gmailcomposerapp.gmail.composer.interfaces;

/**
 * Created by Mayada on 8/8/2018.
 */

public interface ComposerInteractorInterface {

    void interactorSaveFileToDatabase(String subjectToSave, String contentToSave, String attachmentURL);

    void interactorDeleteFileFromFirebaseStorage(String attachmentURL, Boolean checkAttachmentType);
}
