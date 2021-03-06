package task.enozom.gmailcomposerapp.gmail.composer.interfaces;

import com.google.firebase.storage.UploadTask;

/**
 * Created by Mayada on 8/8/2018.
 */

public interface ComposerViewInterface {

    void initView();

    void viewResponseTosaveTofirebaseStorage(UploadTask.TaskSnapshot myTaskSnapShot, Boolean acceptedFile);

    void dismissProgressBar();

    void showUploadingPercentage(UploadTask.TaskSnapshot taskSnapshot);

    void viewResponseDeletionFromFirebaseStorage(Boolean checkDeletion);
}
