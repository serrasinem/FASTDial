package fastdial.interfaces.service;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.json.JSONException;
import org.json.JSONObject;

import fastdial.clientmodel.APIRequest;


/**
 * DMS Bot Service Interface
 *
 * @author Serra Sinem Tekiroglu (tekiroglu@fbk.eu)
 *
 */

@Path("/bot")
public class Service {

	// logger
	final static Logger log = Logger.getLogger("FastLogger");

	/**
	 * The description of DMS api service between the middleware and the DMS.
	 * 
	 * @author Serra Sinem Tekiroglu (tekiroglu@fbk.eu)
	 */
	private APIServerBot dms = new APIServerBot();

	@POST
	@Consumes("application/json")
	@Produces("application/json")
	public Response getNextUpdate(String updateJson) throws JSONException {
		JSONObject updateObject = new JSONObject(updateJson);
		APIRequest request = new APIRequest(updateObject);
		JSONObject jsonObject = dms.onUpdateReceived(request);
		return Response.status(200).entity(jsonObject.toString()).build();
	}
}
