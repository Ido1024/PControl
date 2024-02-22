PControl
PControl is a Java application designed for controlling your PC using UDP client-server communication. This application enables users to send commands from a remote device to their PC over a local network, facilitating various remote actions or tasks.
Technologies Used
Android Studio: Development environment for Android applications.
Java IntelliJ: Integrated development environment for Java programming.
Android Phone (Android 9 and above): Target platform for the mobile application.
Key Algorithms in the Project
RSA Encryption:
RSA encryption was utilized as it is one of the most secure encryption methods. Initially, a public key and private key were generated on the server. The public key was then sent to the client. The client encrypted messages using the public key and sent the encrypted message to the server. On the server, the encryption was decrypted using the private key, and actions were performed accordingly.
Mouse Movement:
When the user presses with one finger on the touchpad, a red dot is created that follows the finger, and the dot's position is sent to the server. After two points sent by the client, the server calculates the distance needed to move the mouse from the current position using a mathematical formula.
Left/Right Click:
When the user presses the button, the application measures time, up to 150 ms. If within 150 ms, the user does not release the button, the server receives a message that the user wants to perform a long press. When the user releases, the server receives a message to release the click. Otherwise, the server receives a message to click and release the button. Additionally, there's a class gestures dedicated to this functionality.
Sending Message to Computer:
When the user enters a message to send via voice or keyboard input and presses the send button, the server receives the message and types it on the computer.
Button Presses (Enter, Delete):
When the user presses the Enter or Delete buttons, the client sends a message to the server to press the respective buttons. This is done using the Robot class, which simulates button presses and releases.
Server Connection:
When the user enters the IP and port received from the server and clicks the Login button, the client attempts to send a message to the server and receive a response to verify that the connection works. If successful, the application prints "Connection successful" on the screen; otherwise, the server prints "Connection failed."
Disconnecting from the Server:
When the user clicks the Disconnect button, the client sends a message to the server to disconnect, terminating the connection between the server and the client.
Two-Finger Touchpad Functions:
When the user presses with two fingers on the touchpad, the client sends the coordinates of both fingers to the server. The server waits for two more points to calculate the distance between them and determine the action to perform. If both fingers move downwards, scroll down; upwards, scroll up; leftwards, scroll left; rightwards, scroll right. Similar to other functionalities, there's a dedicated class gestures for this feature.
