package chat_App;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.*;
import java.awt.event.*;


public class SimpleChatClient
{
    JTextArea incoming;
    JTextField outgoing;
    JList online_clients;
    JLabel online_clients_label;
    private DefaultListModel listbuilder;
    BufferedReader reader;
    PrintWriter writer;
    Socket sock;
    Message clientMessage;
    String IpAdress;
    int portNumber;

    public SimpleChatClient(String id, String ip, int port) {
    	clientMessage = new Message(id);
    	this.IpAdress = ip;
    	this.portNumber = port;
    }

    public void go(String User) {
        JFrame frame = new JFrame(User);
        JPanel mainPanel = new JPanel();
        online_clients_label = new JLabel("Online: ");
        listbuilder = new DefaultListModel();
    	online_clients = new JList(listbuilder);
    	listbuilder.addElement("irfan");
    	listbuilder.addElement("Ameen");
    	online_clients.setLayoutOrientation(JList.VERTICAL);
    	online_clients.addListSelectionListener(new ResponseToList());
    	JScrollPane listScroller = new JScrollPane(online_clients);
    	listScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    	listScroller.setPreferredSize(new Dimension(100, 80));
    	online_clients.setVisibleRowCount(3);
        incoming = new JTextArea(15, 50);
        incoming.setLineWrap(true);
        incoming.setWrapStyleWord(true);
        incoming.setEditable(false);
        JScrollPane qScroller = new JScrollPane(incoming);
        qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        outgoing = new JTextField(20);
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(new SendButtonListener());
        mainPanel.add(qScroller);
        mainPanel.add(outgoing);
        mainPanel.add(sendButton);
        mainPanel.add(online_clients_label);
        mainPanel.add(listScroller);
        frame.getContentPane().add(BorderLayout.CENTER, mainPanel);
        setUpNetworking();

        //Thread to process the incoming data
        Thread readerThread = new Thread(new IncomingReader());
        readerThread.start();
        frame.setSize(650, 500);
        frame.setVisible(true);

    }

    private void setUpNetworking() {
        try {
            sock = new Socket(this.IpAdress, this.portNumber);
            InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
            reader = new BufferedReader(streamReader);
            writer = new PrintWriter(sock.getOutputStream());
            System.out.println("networking established");
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
    }

    public class SendButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent ev) {
            try {
            	clientMessage.setMessage(outgoing.getText());

                writer.println(clientMessage.toString());
                writer.flush();

            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
            outgoing.setText("");
            outgoing.requestFocus();
        }
    }

    public static void main(String[] args) {
        new SimpleChatClient(args[0], args[1], Integer.parseInt(args[2])).go(args[0]);
    }

    class IncomingReader implements Runnable {
        public void run() {
            String message;
            try {
                while ((message = reader.readLine()) != null) {
                    System.out.println("client read " + message);
                    Message recievedMessage = Message.toObject(message);
                    incoming.append(recievedMessage.fullMessage() + "\n");
                }
            } catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
    }
    class ResponseToList implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent request)
		{
			if(request.getValueIsAdjusting() == true)
				clientMessage.setDestination(listbuilder.get(request.getFirstIndex()).toString());
		}

    }
}
