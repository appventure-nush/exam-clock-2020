# Exam Clock

## Basic functionality completed!

### How to use!
1. Run the attached jar file with **Java 8**
   - You should see a sample exam that is meant to test the program
2. You can add exams via "Add" button
3. You can start all exams via "Start All" and vice versa.
4. You can customise the display in "Settings"

### Web Panel
1. Open settings
2. Go to "Connection" section
3. Enter the url from "Panel IP" into your device
4. Click on "QR Code for Key" button, and take a photo of the QR code
5. When in the panel, click on "Upload QR Code Image" and upload the QR code
6. You should see detailed information of the key popping up automatically
   - Note that if you are using chrome, this may not work coz chrome disables crypto.subtle on `http` networks
   - In the future the web panel may be hosted else where to ensure `https` connection and that fake web panels are not sent to the user
7. Click "Verify me"
8. If all goes well, you now have access to the panel's full functionality

### [Releases](https://github.com/appventure-nush/exam-clock-2020/releases/tag/v1.0-beta)