import javax.crypto.Cipher;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import static java.lang.Float.parseFloat;


public class ServerPControl extends JFrame {

    private JPanel mainPanel;
    private JLabel ipValue;
    private JLabel portValue;
    public static final int SERVER_PORT = 5657;
    public static final String PC_IP;

    static {
        try {
            PC_IP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private static final byte[] buffer = new byte[2048];
    private static final String CONNECTION_SUCCESS = "connection success";
    private static final String CONNECTION_MSG = "connection: Hello server, can you read me?";
    private static final String DISCONNECTION = "connection: disconnection";
    public static final String INVALID_MSG_FROM_CLIENT = "Error: got invalid message from client";
    private static final String CIPHER_MODE = "RSA/ECB/PKCS1Padding";
    private static String connectionSuccessAndPublicKey, cmd;//cmd = what type of function need to use
    private static DatagramSocket datagramSocket;
    public static boolean isPrevPointTouch = true, isPrevPointTwoFingersTouch = true, didClientLogin = false;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private static String privateKeyString;
    static int distancePrevPoints, distanceNewPoints, scroll, moveLittle, differenceX, differenceY, bigDifference;
    static float distanceX0, distanceX1, distanceY0, distanceY1, distanceY, distanceX, prevTouchPointX, prevTouchPointY, prevTouchPoint0ZoomX, prevTouchPoint0ZoomY, prevTouchPoint1ZoomX, prevTouchPoint1ZoomY;

    public ServerPControl() {
        setContentPane(mainPanel);
        setTitle("PControl");
        setSize(450, 300);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
        portValue.setText(String.valueOf(SERVER_PORT));
        ipValue.setText(PC_IP);
    }

    public static void main(String[] args) throws Exception {
        ServerPControl rsa = new ServerPControl();
        DatagramSocket datagramSocket = new DatagramSocket(SERVER_PORT);
        System.out.println("Your port is: " + SERVER_PORT);
        System.out.println("Your ip is: " + InetAddress.getLocalHost().getHostAddress());
        System.out.println("Server is running up...");
        while (true) {
            try {
//                receive
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(datagramPacket);
                InetAddress inetAddress = datagramPacket.getAddress();
                int port = datagramPacket.getPort();
                String encryptedMessageFromClient = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
//                doing something with the message from client
                if (encryptedMessageFromClient.equals(CONNECTION_MSG)) {
                    System.out.println("connectionSuccess = " + CONNECTION_SUCCESS);
                    rsa.generateKeys();
                    System.out.println("publicKey = " + rsa.getPublicKey());
                    System.out.println("privateKey = " + rsa.getPrivateKey());
                    privateKeyString = rsa.getPrivateKey();
                    connectionSuccessAndPublicKey = CONNECTION_SUCCESS + ":" + rsa.getPublicKey();
                    datagramPacket = new DatagramPacket(connectionSuccessAndPublicKey.getBytes(), 0, connectionSuccessAndPublicKey.length(), inetAddress, port);
                    didClientLogin = true;
                    datagramSocket.send(datagramPacket);
                } else if (encryptedMessageFromClient.equals(DISCONNECTION))
                    didClientLogin = false;
                else if (didClientLogin) {
                    String decryptedMessage = rsa.decrypt(encryptedMessageFromClient);
                    System.out.println("Decrypted message:\n " + decryptedMessage);
                    checkTheMessageFromClient(decryptedMessage);
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;

            } catch (AWTException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public static void checkTheMessageFromClient(String messageFromClient) throws AWTException {
        cmd = convertMessageFromClientToCmd(messageFromClient);
        try {
            if (cmd.equals("click")) {
                pressLeftCLickOrRightClick(messageFromClient);
            } else if (cmd.equals("msg from keyboard")) {
                pressOnKeyboard(messageFromClient);
            } else if (cmd.equals("Press Key")) {
                pressEnterOrDeleteKey(messageFromClient);
            } else if (cmd.equals("twoFingers")) {
                convertTwoFingersStringToTwoPoints(messageFromClient);
            } else if (cmd.equals("move mouse")) {
                convertMouseStringToPoint(messageFromClient);
            } else if (cmd.equals(INVALID_MSG_FROM_CLIENT)) {
                System.out.println(INVALID_MSG_FROM_CLIENT);
            }
        } catch (
                Exception e) {
            e.printStackTrace();
        }
    }

    public static String convertMessageFromClientToCmd(String messageFromClient) {
        try {
            String[] msgFromClient = messageFromClient.split(":", 2);
            System.out.println("cmd=" + msgFromClient[0] + " other=" + msgFromClient[1]);
            return msgFromClient[0];
        } catch (Exception e) {
            return INVALID_MSG_FROM_CLIENT;
        }
    }

    public static void pressEnterOrDeleteKey(String enterOrDeleteKey) throws AWTException {
        Robot robot = new Robot();
        if (enterOrDeleteKey.contains("Delete")) {
            robot.keyPress(KeyEvent.VK_BACK_SPACE);
            robot.delay(300);
            robot.keyRelease(KeyEvent.VK_BACK_SPACE);
        } else if (enterOrDeleteKey.contains("Enter")) {
            robot.keyPress(KeyEvent.VK_ENTER);
            robot.delay(300);
            robot.keyRelease(KeyEvent.VK_ENTER);
        }
    }

    public static void pressOnKeyboard(String messageFromClientKeyboard) throws AWTException {
        String text = splitMsgAndTakeTheFirst(messageFromClientKeyboard);
        StringSelection strSelection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(strSelection, null);
        Robot robot = new Robot();
        robot.delay(300);
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);
    }

    public static void pressLeftCLickOrRightClick(String click) throws AWTException {
        Robot robot = new Robot();
        if (click.contains("Left") && click.contains("First")) {//left click pressed first time
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

        } else if (click.contains("Left") && click.contains("Press")) {//left click press (after first)
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);

        } else if (click.contains("Left") && click.contains("Release")) {//left click release
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

        } else if (click.contains("Right") && click.contains("First")) {//left click pressed first time
            robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);

        } else if (click.contains("Right") && click.contains("Press")) {//right click press (after first)
            robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);

        } else if (click.contains("Right") && click.contains("Release")) {//Right click release
            robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
        } else {
            System.out.println("Error: can't define right or left click");
        }
    }

    public static void moveMouse(float x, float y, boolean isFingerOnScreen) throws AWTException {
        Robot robot = new Robot();
        Point currentPoint = getMouseLocation();
        if (isPrevPointTouch && isFingerOnScreen) {//need 2 points from the client to difference
            prevTouchPointX = x;
            prevTouchPointY = y;
            isPrevPointTouch = false;
        } else if (isFingerOnScreen) {
            Point differencePoint = differenceMouseLocation(prevTouchPointX, prevTouchPointY, x, y);
            robot.mouseMove(currentPoint.x + differencePoint.x, currentPoint.y + differencePoint.y);
            isPrevPointTouch = true;
        }
    }

    public static void twoFingersOnTouchPad(float x0, float y0, float x1, float y1, boolean isFingerOnScreen) throws AWTException {
        if (isPrevPointTwoFingersTouch && isFingerOnScreen) {//need 2 points from the client to difference
            prevTouchPoint0ZoomX = x0;
            prevTouchPoint0ZoomY = y0;
            prevTouchPoint1ZoomX = x1;
            prevTouchPoint1ZoomY = y1;
            isPrevPointTwoFingersTouch = false;
        } else if (!isPrevPointTwoFingersTouch && isFingerOnScreen) {
            defineWhatTwoFingersDid(prevTouchPoint0ZoomX, prevTouchPoint0ZoomY, prevTouchPoint1ZoomX, prevTouchPoint1ZoomY, x0, y0, x1, y1);
            isPrevPointTwoFingersTouch = true;
        }
    }


    public static void defineWhatTwoFingersDid(float prevTouchPoint0X, float prevTouchPoint0Y, float prevTouchPoint1X, float prevTouchPoint1Y, float x0, float y0, float x1, float y1) throws AWTException {
        Robot robot = new Robot();
        moveLittle = 5;//if the finger move a little on the screen.
        distancePrevPoints = (int) Math.abs(Math.sqrt(Math.pow(prevTouchPoint0X - prevTouchPoint1X, 2) + Math.pow((prevTouchPoint0Y - prevTouchPoint1Y), 2)));
        distanceNewPoints = (int) Math.abs(Math.sqrt(Math.pow(x0 - x1, 2) + Math.pow((y0 - y1), 2)));
        distanceX0 = (x0 - prevTouchPoint0X);
        distanceY0 = (y0 - prevTouchPoint0Y);
        distanceX1 = (x1 - prevTouchPoint1X);
        distanceY1 = (y1 - prevTouchPoint1Y);
        scroll = Math.abs(distanceNewPoints - distancePrevPoints);

        if (scroll >= moveLittle) {
            //both distance + or - is zoom. if only one its scroll
            System.out.println(scroll);
            //zoom
            System.out.println("zoom in or out");
            if (distanceNewPoints > distancePrevPoints) {
                zoomInOrOut(-1 * scroll);
                System.out.println("zoom in");
                robot.keyRelease(KeyEvent.VK_CONTROL);
            } else {
                zoomInOrOut(scroll);
                System.out.println("zoom out");
                robot.keyRelease(KeyEvent.VK_CONTROL);
            }
        } else {
            System.out.println("scroll");
            distanceY = (int) Math.abs((distanceY0 + distanceY1) / 2);// the distance that need to move
            distanceX = (int) Math.abs((distanceX0 + distanceX1) / 2);// the distance that need to move
            if (Math.abs(distanceX0) < moveLittle && Math.abs(distanceX1) < moveLittle && distanceY0 > moveLittle && distanceY0 > moveLittle) {
                System.out.println("scroll_down");
                scrollUpOrDown(distanceY);

            } else if (Math.abs(distanceX0) < moveLittle && Math.abs(distanceX1) < moveLittle && distanceY0 < moveLittle && distanceY0 < moveLittle) {
                System.out.println("scroll_up");
                scrollUpOrDown(-1 * distanceY);

            } else if (Math.abs(distanceY0) < moveLittle && Math.abs(distanceY1) < moveLittle && distanceX0 > moveLittle && distanceX0 > moveLittle) {
                System.out.println("scroll right");
                scrollLeftOrRight(distanceX);
                robot.keyRelease(KeyEvent.VK_SHIFT);

            } else if (Math.abs(distanceY0) < moveLittle && Math.abs(distanceY1) < moveLittle && distanceX0 < moveLittle && distanceX0 < moveLittle) {
                System.out.println("scroll left");
                scrollLeftOrRight(-1 * distanceX);
                robot.keyRelease(KeyEvent.VK_SHIFT);
            }
        }
    }

    public static void zoomInOrOut(int distance) throws AWTException {
        Robot robot = new Robot();
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.mouseWheel(distance);
        robot.keyRelease(KeyEvent.VK_CONTROL);
    }

    public static void scrollUpOrDown(float distance) throws AWTException {
        Robot robot = new Robot();
        robot.mouseWheel((int) (distance));
    }

    public static void scrollLeftOrRight(float distance) throws AWTException {
        Robot robot = new Robot();
        robot.keyPress(KeyEvent.VK_SHIFT);
        robot.mouseWheel((int) (distance));
        robot.keyRelease(KeyEvent.VK_SHIFT);
    }

    public static Point differenceMouseLocation(float prevX, float prevY, float clientX, float clientY) {
        bigDifference = 100;//if the fingers cords going crazy.
        differenceX = (int) Math.abs(Math.sqrt(Math.pow(prevX - clientX, 2)));
        differenceY = (int) Math.abs(Math.sqrt(Math.pow(prevY - clientY, 2)));
        if ((int) (prevX - clientX) > 0) //calc if we need to move to left or right [(0,0) is top left of screen]
            differenceX *= -1;
        if ((int) (prevY - clientY) > 0)//calc if we need to move to up or down [(0,0) is top left of screen]
            differenceY *= -1;
        if (Math.abs(differenceX) > bigDifference)//fix tp issue
            differenceX = 0;
        if (Math.abs(differenceY) > bigDifference)// fix tp issue
            differenceY = 0;
        return new Point(differenceX, differenceY);
    }

    public static Point getMouseLocation() {
        return new Point((int) MouseInfo.getPointerInfo().getLocation().getX(), (int) MouseInfo.getPointerInfo().getLocation().getY());
    }

    public static String splitMsgAndTakeTheFirst(String msgFromClient) {// use this instead the above function that I made earlier // add Exception for each one of the split
        //for typing msg from client -> return only the msg that the user wrote
        String[] msgFromClientKeyboard = msgFromClient.split(":", 2);
        return msgFromClientKeyboard[1];
    }

    public static void convertMouseStringToPoint(String messagePoint) throws AWTException {//done
        String[] msgFromClient = messagePoint.split(":", 4);
        moveMouse(parseFloat(msgFromClient[1]), parseFloat(msgFromClient[2]), Boolean.parseBoolean(msgFromClient[3]));
    }

    public static void convertTwoFingersStringToTwoPoints(String messagePoint) throws AWTException {//done
        String[] msgFromClient = messagePoint.split(":", 6);
        twoFingersOnTouchPad(parseFloat(msgFromClient[1]), parseFloat(msgFromClient[2]), parseFloat(msgFromClient[3]), parseFloat(msgFromClient[4]), Boolean.parseBoolean(msgFromClient[5]));
    }

    public void generateKeys() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(1024);
            KeyPair pair = generator.generateKeyPair();
            privateKey = pair.getPrivate();
            publicKey = pair.getPublic();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getPublicKey() {
        return encode(publicKey.getEncoded());
    }

    public String getPrivateKey() {
        return encode(privateKey.getEncoded());
    }

    private String encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    public String decrypt(String encryptedMessage) throws Exception {
        byte[] encryptedBytes = decode(encryptedMessage);
        Cipher cipher = Cipher.getInstance(CIPHER_MODE);//cipher type
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedMessage = cipher.doFinal(encryptedBytes);
        return new String(decryptedMessage, "UTF8");
    }

    private byte[] decode(String data) {
        return Base64.getDecoder().decode(data);
    }
}
