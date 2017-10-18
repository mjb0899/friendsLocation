package fmf.subscription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import cmm529.coursework.friend.model.Subscription;
import cmm529.coursework.friend.model.SubscriptionRequest;
import cmm529.coursework.friend.model.User;
import fmf.aws.util.DynamoDBUtil;

@Path("/subs")
public class SubscriptionManager {
	
//send a subscription
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response sendSubscription(
			@FormParam("subscriberID") String subscriberID, 
			@FormParam("subscribeTo") String subscribeTo) {
		
		try {
			DynamoDBMapper mapper=DynamoDBUtil.getMapper(Regions.EU_WEST_1);
			User subscriberUser = mapper.load(User.class,subscriberID);
			User subscribeToUser = mapper.load(User.class,subscribeTo);

			if(subscriberUser != null && subscribeToUser != null) {
				long nowMill = System.currentTimeMillis();
				SubscriptionRequest newSub = new SubscriptionRequest(subscriberID, subscribeTo, nowMill);
				mapper.save(newSub);
				}else{
					return Response.
					status(404).
					entity("User Not Found").
					build();
					}	
				return Response.
				status(200).
				build();
			}catch(Exception e){		
				return Response.
				status(404).
				entity("Error").
				build();
		}
	}//end method

//Retrieve Subscription Requests
	@Path("/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Iterable<SubscriptionRequest> retrievesubscription( 
			@PathParam("id") String id){
			DynamoDBMapper mapper=DynamoDBUtil.getMapper(Regions.EU_WEST_1);
			Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
			eav.put(":user", new AttributeValue().withS(id));
			DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
				.withFilterExpression("subscribeTo = :user")
				.withExpressionAttributeValues(eav);

			List<SubscriptionRequest> result=  mapper.scan(SubscriptionRequest.class, scanExpression);
			return result;
			}//end method
	
//Reply to Subscription Requests
	@Path("/reply")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response replyRequests(
			@FormParam("user") String user, 
			@FormParam("reply") Boolean reply,
			@FormParam("requests") String requests) {
		
		try{
			String[] reqArray = requests.split(",");
			DynamoDBMapper mapper=DynamoDBUtil.getMapper(Regions.EU_WEST_1);
			if(reply) {
					for (int i = 0; i < reqArray.length; i++) {
					Subscription record = mapper.load(Subscription.class, reqArray[i]);
					
					if(record != null) { // If the user is already subscribed to someone else
						Set<String> subsctribedTo = record.getSubscribeTo();
						subsctribedTo.add(user);
						record.setSubscribeTo(subsctribedTo);
						mapper.save(record);
						} else {
							Subscription newRecord = new Subscription(reqArray[i], user);
							mapper.save(newRecord);
						}//end if

					// Delete the current record that we've just saved form the Subscription Request table
					SubscriptionRequest currReq = mapper.load(SubscriptionRequest.class, user, reqArray[i]);
					mapper.delete(currReq);
					}//end for
			} else {
			for (int i = 0; i < reqArray.length; i++) {
				SubscriptionRequest currReq = mapper.load(SubscriptionRequest.class, user, reqArray[i]);
				mapper.delete(currReq);
				}//end for
			}//end else
			return Response.
			status(200).
			build();
		}catch(Exception e){
				return Response.
				status(404).
				entity("reply--Error").
				build();
		}//end try-catch

	
	}//end method
	
//Retrieve Subscriptions(Friends)

	@Path("/myfriends")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response mySubscription( 
			@FormParam("id") String id){

		try{
			DynamoDBMapper mapper=DynamoDBUtil.getMapper(Regions.EU_WEST_1);
			Subscription user=mapper.load(Subscription.class,id);	
			Set<String> following = user.getSubscribeTo();
			List<User> allFriends = new ArrayList<User>();
			for(String s:following){
				User friend=mapper.load(User.class,s);
				allFriends.add(friend);

			}	
			return Response.
	               status(200).
	               entity(allFriends).
	               build();
			
		}catch(Exception e){
			return Response.
					status(200).
					entity("No subscriptions").
					build();
		}//end try-catch
	}//end method
	
}//end class
