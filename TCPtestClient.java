package TCPtest;

import java.awt.Dimension;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

/**
 *
 * @author Will Ransom
 * 
 * NOTE:  This client (run from other computer) is set up to access the server (this computer) at (169.254.17.255:50000)
 * 
 */
public class TCPtestClient {

    public static void main(String[] args) {
         // GUI
        JFrame frame = new JFrame("TCP Messager");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);  // Terminate program on 'X'
        frame.getContentPane().setPreferredSize(new Dimension(400, 750));  // adjust the size of the content pane not the frame to account for border space
        frame.pack();
        JPanel panel = new JPanel();
        panel.setLayout(null);
        frame.getContentPane().add(panel);
        
        JButton sendButton = new JButton("Send");
        sendButton.setBounds(100, 675, 200, 50);
        panel.add(sendButton);

        JTextArea inBox = new JTextArea();
        inBox.setBounds(25, 25, 350, 300);
        inBox.setEditable(false);
        panel.add(inBox);
        
        JTextArea outBox = new JTextArea();
        outBox.setBounds(25, 350, 350, 300);
        panel.add(outBox);
        
        outBox.addKeyListener(new KeyListener(){
            public void keyPressed(KeyEvent e){
                //no action
            }
            public void keyReleased(KeyEvent e){
                if (e.getKeyCode() == KeyEvent.VK_ENTER){
                    sendButton.doClick();
                }
            }
            public void keyTyped(KeyEvent e){
                //no action
            }
        });
        
        frame.setVisible(true);
        
        
        // SET UP CONNECTION AS CLIENT
        SocketAddress serverAdd = new InetSocketAddress("169.254.17.255", 50000);
        SocketAddress clientPort = new InetSocketAddress(50001);

        try{
            SocketChannel client = SocketChannel.open();
            client.socket().bind(clientPort);  // unnecessary step, allows control of port used by client
            client.connect(serverAdd);  //request connection to the server
            System.out.println("Client Connected: " + client.isConnected());
        
            // HANDLE MESSAGES
            Thread clientSendThread = new Thread(){
                public void run(){

                        sendButton.addActionListener(new ActionListener(){  
                            public void actionPerformed(ActionEvent e){  
                                try{
                                    String message = outBox.getText();
                                    outBox.setText("");

                                    ByteBuffer sendMSG = ByteBuffer.allocate(100);
                                    sendMSG.put(message.getBytes());
                                    sendMSG.flip(); //set ByteBuffer position back to 0

                                    int bytesWritten = 0;
                                    while (bytesWritten == 0){
                                        bytesWritten = client.write(sendMSG);
                                        Thread.sleep(100);
                                    }
                                }
                                catch(Exception ex){
                                    System.out.println(ex);
                                    System.exit(0);
                                }
                            }
                        });
                }
            };
            clientSendThread.start();
            
            Thread clientRecvThread = new Thread(){
                public void run(){
                    try{
                        ByteBuffer recvMSG = ByteBuffer.allocate(100);
                        int bytesRead = 0;
                        while(true){
                            bytesRead = client.read(recvMSG);
                            if (bytesRead > 0){
                                bytesRead = 0;
                                inBox.setText(new String(recvMSG.array(), "UTF-8"));
                                recvMSG = ByteBuffer.allocate(100);
                            }
                            Thread.sleep(100);
                        }
                    }
                    catch(Exception ex){
                        System.out.println(ex);
                        System.exit(0);
                    }
                }
            };
            clientRecvThread.start();
        }
        catch(Exception ex){
            System.out.println(ex);
            System.exit(0);
        }
    }
}
