import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ServerHealthCheck {
	List<Character> listToStoreResponse = new ArrayList<Character>();
	static  String url= "http://localhost:12345";

	//Currently I am  using Main thead, but this can be done by creating a new thread.
	public static void main(String args[]) throws IOException {

		ServerHealthCheck object = new ServerHealthCheck();
		object.waitMethod();
 
	}

	private synchronized void waitMethod() throws IOException,ConnectException{

		while (true) {
			System.out.println("Demon thread running ==> " + Calendar.getInstance().getTime());

			try {
				checkServerIsUp();

			//using URL from java.net
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");

			int responseCode = con.getResponseCode();
			System.out.println("GET Response Code :: " + responseCode);

			//variables to store counts of success and failure
			int count_Res_Succ=0, count_Res_Fail=0, count_total_Resp=0;
			Double succ_pert=0.0;

			//Logic to calculate success percent using an arraylist
			if (responseCode == HttpURLConnection.HTTP_OK) { // success
				listToStoreResponse.add('S');
				BufferedReader in = new BufferedReader(new InputStreamReader(
						con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				// print result
				System.out.println(response.toString());
			} else { //failure
				listToStoreResponse.add('F');
				System.out.println("Processing Failed");
			}

			count_Res_Succ = (int) listToStoreResponse.stream().filter(s->s=='S').count();
			count_Res_Fail = (int) listToStoreResponse.stream().filter(s->s=='F').count();
			succ_pert = Double.valueOf(count_Res_Succ)/Double.valueOf(listToStoreResponse.size())*100;
			System.out.println("succ: "+ count_Res_Succ + " fail: "+ count_Res_Fail+ "  success percent: "+ succ_pert+"%");

			if(listToStoreResponse.size()==30){//checking health in every 5 mins(30 pings)
				listToStoreResponse.clear();
			}
			}
			catch(ConnectException e)
			{
				// return;
			}

			try {
				this.wait(10000); //currently thread is running every 10 sec. So 6 times a min.
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
		}

	}

	private void checkServerIsUp() throws ConnectException,IOException {
		Socket socket = null;
		String server = "localhost";
		int port = 12345;

		try {
			socket = new Socket(server, port);

			if(socket.isConnected()) {
				System.out.println("Service is up on port " + port + ".");
			}
		} catch (IOException e) {
			System.out.println("Service is down on port " + port + "!");
		} finally {
			if(socket != null && socket.isConnected()) {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}