package fmf.users;

import java.util.Set;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import cmm529.coursework.friend.model.Location;
import cmm529.coursework.friend.model.Subscription;
import cmm529.coursework.friend.model.SubscriptionRequest;
import cmm529.coursework.friend.model.User;

import fmf.aws.util.DynamoDBUtil;

@Path("/user")
public class UserManager {
	
//retrieve the user
	 @Path("/{id}")
	 @GET
	 @Produces(MediaType.APPLICATION_JSON)
	 public User getUser(@PathParam("id") String id) {

	   try {
		   DynamoDBMapper mapper = DynamoDBUtil.getMapper(Regions.EU_WEST_1);
		   User user = mapper.load(User.class, id);
		   return user;
	   		} catch (Exception e) {

	   			return null;
	   		}

	  } //end method
	
	
//update user location
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	public Response SaveUserLocation(
			@FormParam("lon") double lon, 
			@FormParam("lat") double lat,
			@FormParam("user") String id) {
		
		if(id==null) {
			return Response.
					status(404).
					entity("Error").
					build();
						
		}
		try{
			Location newLocation = new Location(lon, lat);
			DynamoDBMapper mapper=DynamoDBUtil.getMapper(Regions.EU_WEST_1);
			User user = mapper.load(User.class,id);
			user.setLocation(newLocation);
			mapper.save(user);
			return Response.
			status(200).
			build();
			}catch(Exception e){
			return Response.
					status(404).
					entity("Error").
					build();
			}//end try-catch
		
		
		
		}//end method
	

	

	
	
	
	
}//end class
