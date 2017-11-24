package chat_App;

import com.google.gson.Gson;

public class Message {
	String ID;
	String message;
	String DestinationID;
	
	public Message(String id)
	{
		this.ID = id;
	}
	public void setMessage(String message)
	{
		this.message = message;
	}
	public void setDestination(String did)
	{
		this.DestinationID = did;
	}
	public String toString()
	{
		Gson g = new Gson();
		return g.toJson(this);
	}
	public static Message toObject(String test)
	{
		Gson g = new Gson();
		return g.fromJson(test, Message.class);
	}
}