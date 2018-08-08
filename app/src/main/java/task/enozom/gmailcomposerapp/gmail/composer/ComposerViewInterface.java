package task.enozom.gmailcomposerapp.gmail.composer;

import com.google.firebase.storage.UploadTask;

/**
 * Created by Mayada on 8/8/2018.
 */

public interface ComposerViewInterface {

    void initView();

    void viewResponseTosaveTofirebaseStorage(UploadTask.TaskSnapshot myTaskSnapShot, Boolean acceptedFile);
}
