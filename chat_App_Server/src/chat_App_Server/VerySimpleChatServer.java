package chat_App_Server;

import java.io.*;
import java.net.*;
import java.util.*;
import com.google.gson.Gson;

import com.Message.*;


public class VerySimpleChatServer
{
    @SuppressWarnings("rawtypes")
	ArrayList clientOutputStreams;
    HashMap<String, Socket> onlineClients;
    public VerySimpleChatServer()
    {
    	this.onlineClients = new HashMap<String, Socket>();
    }
    public class ClientHandler implements Runnable {
        BufferedReader reader;
        Socket sock;
        Message clientMessage;

        public ClientHandler(Socket clientSOcket) {
            try {
                sock = clientSOcket;
                InputStreamReader isReader = new InputStreamReader(sock.getInputStream());
                reader = new BufferedReader(isReader);

            } catch (Exception ex) { ex.printStackTrace(); }
        }
        public Socket getsock()
        {
        	return this.sock;
        }
        public void run() {
            String message;
            try {
                while ((message = reader.readLine()) != null) {
                    System.out.println("read " + message);
                    clientMessage = Message.toObject(message);
                    System.out.println(clientMessage.getType());
                    processMessage(this, clientMessage);
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        }
    }

    public static void main(String[] args) {
        new VerySimpleChatServer().go();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public void go() {
        clientOutputStreams = new ArrayList();
        try {
            @SuppressWarnings("resource")
			ServerSocket serverSock = new ServerSocket(5000);
            while(true) {
                Socket clientSocket = serverSock.accept();
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
                clientOutputStreams.add(writer);
                Thread t = new Thread(new ClientHandler(clientSocket));
                t.start();
                System.out.println("got a connection");
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    @SuppressWarnings("rawtypes")
	public void tellEveryone(String message) {
        Iterator it = clientOutputStreams.iterator();
        while (it.hasNext()) {
            try {
                PrintWriter writer = (PrintWriter) it.next();
                writer.println(message);
                writer.flush();
            } catch (Exception ex) { ex.printStackTrace(); }
        }
    }
    private void processMessage(ClientHandler client, Message clientMessage)
    {
    	//System.out.println(client.getsock().toString());
    	if(clientMessage.getType() == Type.START)
    	{
    		onlineClients.put(clientMessage.getID(), client.getsock());
    	}
    	else if(clientMessage.getType() == Type.GET_ONLINE_CLIENTS)
    	{
    		Gson g = new Gson();
    		try {
				PrintWriter writer = new PrintWriter(client.getsock().getOutputStream());
				//System.out.println(onlineClients.toString());
				//System.out.println(onlineClients.keySet());
				//writer.println(g.toJson(onlineClients));
				clientMessage.setMessage(g.toJson(onlineClients.keySet()));
				clientMessage.setType(Type.CLIENTS);
				writer.println(clientMessage.toString());
				writer.flush();
				System.out.println("Done...");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	else if(clientMessage.getType() == Type.END)
    	{
    		onlineClients.remove(clientMessage.getID());
    	}
    	else
    	{
    		try {
    			String reciever = clientMessage.getDestination();
				PrintWriter writer = new PrintWriter(onlineClients.get(reciever).getOutputStream());
				clientMessage.setType(Type.NORMAL);
				writer.println(clientMessage.toString());
				writer.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
}
