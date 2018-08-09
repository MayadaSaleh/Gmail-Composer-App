# Gmail-Composer-App
Message Composer is a simple Android app using java language like the one in Gmail app

### The app is consisting of two views
1. Splash Screen
A screen that consists of a logo to be displayed for 3
seconds then it moves to the main view.
2. Compose Message
Display two text boxes for the user to enter the message and the
subject
> First : After clicking on the attach button a popup will appear (with animation)
that contains 3 buttons for Gallery, Camera and Video as shown in figure.
The attached file will be uploaded and show a loading indicator to the user while
uploading the file and validate that the file does not exceed 5 MB.
Technical Assumptions:
a. Just one image or video can be selected per a message.
b. The selected attachment file should be uploaded to firebase storage with
name “image.jpg” or “video.mp4” .
> Second: After clicking on the send button the message is saved on
 firebase database reference on a node called “messages” with sub nodes of
a- messageSubject
b- messageContent
c- attachmentUrl
