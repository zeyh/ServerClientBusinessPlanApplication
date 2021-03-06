package Client;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;

import Server.BP_Node;
import Server.Person;
import Server.ServerInterfaceRMI;
import fx.Main;
import fx.model.Model;
import fx.viewPlanPage.PlanViewController;

public class ClientImpl implements  ClientInterfaceRMI , Serializable, Remote  {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ServerInterfaceRMI proxy;
	public Person person;
	public BP_Node business;
	public transient Model model;
	public String currentMessage;
	
	ClientInterfaceRMI clientinter;
	
	
	//Constructor that make the client remote
	public ClientImpl(ServerInterfaceRMI proxy){
		this.proxy = proxy;
		
		try {
			String hello = this.proxy.hello();
			this.clientinter = (ClientInterfaceRMI) UnicastRemoteObject.exportObject(this, 0);
			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	//another way to initiate a client with a model
	public ClientImpl(ServerInterfaceRMI proxy, Model model){
		this.proxy = proxy;
		this.model = model;
		
		try {
			String hello = this.proxy.hello();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	
	//set the model
	public void setModel(Model model) {
		this.model = model;
	}
	
	//being notified after there is a change  | message - "the plan has been revised"
	public String notifyChanges(String message) {
		this.currentMessage = message;
		System.out.println("current message - " + this.currentMessage);
		
//		PlanViewController.alertPop(message);
//		PlanViewController.notifychanges();

		if(this.model!= null) {
			System.out.println("The client has a model "+ this.model);
			this.model.notifyChanges(message);
		}
		return message;

	}
	
	
	//getter for current message
	public String returnNotifyMessage() {
		return this.currentMessage;
	}
	
	
	//setter for current message
	public void setMessage(String message) {
		this.currentMessage = message;
		
	}
	
	//if this client clicked save, signal the server to notify
	public void signalChange() {
		try {
			this.proxy.signalChange(this.business.department+this.business.year);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	//register client themselves in the server
	public void addClient() {
		try {
			
			//System.out.println(this);
			proxy.addClient(this.business.getDepartment()+this.business.getYear(), this);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	//remove the client from pair list in server
	public void removeClient() {
		try {
			proxy.removeClient(this.business.getDepartment()+this.business.getYear(), this);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	// Takes a username and password (to be given by GUI in future)
	// call get person on server with user and pass, set person variable
	public void login(String username, String password) 
	{ 
		try {
			person = proxy.getPerson(username, password);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	// Same as login, but for the business plan
	public void requestBusinessPlan(String department, int year) 
	{
		try {
			business = proxy.getBP_Node(department, year);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	
	// write BP_Node to the disk
	public void writeLocalBP(BP_Node business) {
		XMLEncoder encoder = null;
		try
		{
			encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream("businessPlan.xml")));
		} catch (FileNotFoundException fileNotFound)
		{
			System.out.println("ERROR: While Creating or Opening the File businessPlan.xml");
		}
		encoder.writeObject(business);
		encoder.close();

	}
	
	// Read from disk
	public void readLocalBP() {
		XMLDecoder decoder = null;
		try
		{
			decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream("businessPlan.xml")));
		} catch (FileNotFoundException e)
		{
			System.out.println("ERROR: File businessPlan.xml not found");
		}
		business = (BP_Node) decoder.readObject();
		decoder.close();
	}
	
	// add person to list on server
	public void addPeople(String username, String password, String department, Boolean admin) {
		// Check if person is admin before allowing
		if (person.isAdmin()){
			// makes new person based on parameters
			Person newPerson = new Person(username, password, department, admin); 
			try {
				proxy.addPerson(newPerson);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("You definitely don't have permission to do this.");
		}
	}
		
	 // make a BP_Node with blank business plan
	public void make_BlankBP(int year, String department, Boolean editable) {

		try {
			proxy.make_BlankBP(year,department,editable);
			business = proxy.getBP_Node(department,year);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		writeLocalBP(business);
	}
	
	//clone the business plan
	public void make_CloneBP(int year, String department, Boolean editable, int new_year){

		try {
			proxy.make_CloneBP(year,department,editable,new_year);
			business = proxy.getBP_Node(department,new_year);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		writeLocalBP(business);
	}
	
	//set the businee plan's status
	public void setBPStatus(BP_Node businessPlan, boolean editable){
		try {
			proxy.setEditStatus(businessPlan, editable);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	//upload the business plan to the server
	public void uploadBP(BP_Node businessPlan){
		try {
			proxy.addBP_Node(businessPlan);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	//delete the business plan
	public void deleteBP(BP_Node businessPlan){
		try {
			proxy.deleteBP_Node(businessPlan);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	//get the business plan
	public LinkedList<BP_Node> getBP() {
		try {
			return proxy.getBP();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//get the signuped people in the server
	public LinkedList<Person> getPeople(){
		try {
			return proxy.getPeople();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//get the registry and set up the connection
	public static void main(String[] args){	
		Registry registry;
		try {			
			registry = LocateRegistry.getRegistry("");
			ServerInterfaceRMI server = (ServerInterfaceRMI) registry.lookup("server");
			ClientImpl client = new ClientImpl(server);
			System.err.println("Client ready") ;
			
		} catch (RemoteException e) {
			System.err.println("Client exception: " + e.toString()) ;
			e.printStackTrace();
		} catch (NotBoundException e) {
			System.err.println("Client exception: " + e.toString()) ;
			e.printStackTrace();
		}		
	}

	
	
}
