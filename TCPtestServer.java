package TCPtest;

import java.awt.Dimension;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;
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
 * NOTE:  To allow client (other computer) to connect to server (this computer), Java SE Platform Binary 
 *          inbound TCP firewall rule was disabled.  Additionally, a new rule was created to allow connection 
 *          at (169.254.17.255:50000) when accessed by the specific client at (169.254.198.155:50001).
 *        Also note that server must be started before client to successfully connect.
 * 
 */
public class TCPtestServer {

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
        
        
        // SET UP CONNECTION AS SERVER
        SocketAddress serverPort = new InetSocketAddress(50000);
        
        try{
            ServerSocketChannel connListener = ServerSocketChannel.open();  //ready server to receive connection requests
            connListener.socket().bind(serverPort);  //specify the port to listen for requests on
            SocketChannel server = connListener.accept();  //accept client's connection request, completing the connection
            System.out.println("Server Connected: " + server.isConnected());
        
            // HANDLE MESSAGES
            Thread serverSendThread = new Thread(){
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
                                        bytesWritten = server.write(sendMSG);
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
            serverSendThread.start();
            
            Thread serverRecvThread = new Thread(){
                public void run(){
                    try{
                        ByteBuffer recvMSG = ByteBuffer.allocate(100);
                        int bytesRead = 0;
                        while(true){
                            bytesRead = server.read(recvMSG);
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
            serverRecvThread.start();
        }
        catch(Exception ex){
            System.out.println(ex);
            System.exit(0);
        }
    }
}
