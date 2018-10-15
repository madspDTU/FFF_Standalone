package fastOrForcedToFollow;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.TreeMap;


public class Runner {
	public static final double widthOfLastLink = 2;
	public static final double widthOfFirstLinks = 3;
	public static final double lengthOfLinks = 100;
 
	public static final double omega = 1.25;
	public static double T = 3600*16;
	public static double TOnToy = 3600;

	public static double t = 1;
	public static int L = 10000;
	public static Link[] links = new Link[L];
	public static Link sourceLink;
	public static int N = 100000;
	public static int seed = 5355633;
	public static LinkedHashMap<Integer,Link> linksMap = new LinkedHashMap<Integer,Link>();

	public static final boolean useLogisticDistribution = false; //Based on the data from Cowi, this seems reasonable.
	public static final boolean useJohnsonDistribution = true; //Based on the data from Cowi, this seems reasonable.
	public static double JohnsonGamma = -2.745957257392118;
	public static double JohnsonXsi = 3.674350833333333;
	public static double JohnsonDelta = 4.068155531972158;
	public static double JohnsonLambda = 3.494609779450189;


	public static double k = 5d;
	public static double lambda = 6.5d;
	public static final double mu = 6.085984;
	//public static final double s = 0.610593;  // Interestingly, results have more (reasonable) curvature, when using a higher scale...
	public static final double s = 0.610593;  // Interestingly, results have more (reasonable) curvature, when using a higher scale...
	public static double tau = 1d;
	public static final double deadSpace = 0.4;
	//public static final double l = 1.93; //Average length of a bicycle measured in metres + minimum safety distance
	//public static final double l = 1.73; //Average length of a bicycle measured in metres + minimum safety distance
	//public static final double l = -1.970194143353036; //Average length of a bicycle measured in metres + minimum safety distance
	public static final double l = -4.220641337789; //For square root model;
	public static final double lambda_c = 1.73; //Average length of a bicycle according to Andresen (2014);
	

	//public static final double t_safety = 0.72; //Safety time between cyclists according to Andresen.

	public static final double t_safetySqrt =  4.602161217943; 
	//public static final double t_safety =  1.88579462750199101; 
	public static final double t_safety =  0; 
	
	public static final double t_safety2Poly = 0; //Safety time between cyclists according to Andresen 0.
	//public static final double t_safety2Poly = -0.07403800101957327; //Safety time between cyclists according to Andresen 0.
	public static final double capacityMultiplier = 1;  // The totalLaneLength is multiplied with this number;
	public static final double minimumAllowedSpeed = 2;  // Lowest allowed desired speed (lower truncation of distribution);

	public static final boolean useToyNetwork = true;

	public static LinkedList<Cyclist> cyclists= new LinkedList<Cyclist>();

	public static final boolean circuit = false;
	public static final boolean reportSpeeds = true;
	public static final String circuitString = circuit? "Circuit" : "Linear";
	@SuppressWarnings("rawtypes")
	public static final Class<? extends PriorityQueue> priorityQueueClassForLinks = PriorityQueue.class; 
	public static final LinkTransmissionModel ltm = new AdvancedSpatialLTM(); //So far the options are: BasicLTM, SpatialLTM, AdvancedSpatialLTM.

	// The notification-based approach allows the running time to be fully independent on the number of links.
	// ~Linear with number of agents.
	public static final boolean notificationBased = true;
	@SuppressWarnings("unchecked")
	public static HashMap<Integer, Double>[] notificationArray;
	public static PriorityQueue<LinkQObject> shortTermPriorityQueue;
	public static String baseDir = "/zhome/81/e/64390/CykelSmallScale/ToyNetwork";
	public static double tieBreaker = 0;
	public static final boolean waitingTimeCountsOnNextLink = false; // if false, then it counts on the previous link (i.e. spillback)


	//Dronning Louises bro sydgående
	// 3500 cykler mellem 8 og 9 i sydgående retning.
	// Sampl hver af disse med et ankomsttidspunkt U(0,3600)
	// 4 meter bred.
	// 200 meter lang.



	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException, InterruptedException, InstantiationException, IllegalAccessException{

		//	System.out.println("Waiting for <Enter> until CPU Sampling has started");
		//	System.in.read();

		List<Integer> NOCyclists = Arrays.asList(100,200,350,500,750,1000,1300, 1600, 2000, 2500, 3000, 4000, 5000, 6000, 7000, 8000, 9000, 10000,
				12000, 14000, 16000, 18000, 20000, 22500, 25000, 27500, 30000, 35000, 40000, 45000, 50000, 60000, 70000, 80000, 90000, 100000);
		int stepSize = 50;
		for( int itN = 10000; itN >= stepSize; itN -= stepSize){
			//int itN = 2050;{
			//for( int itN = stepSize; itN <= 10000; itN += stepSize){
			N = itN;
			if(circuit){
				N = itN/25;
				if( N > 100){
					N = 100;
				}
			}
			//{	
			t =0 ;
			double startTime = System.currentTimeMillis();
			Random random3 = new Random(seed + 171);
			System.out.println("Start of a simulation with " + N + " cyclists.");

			if(useToyNetwork){
				L = 3;
				links = new Link[L];
				for(int i = 0; i < L; i++){
					if(i == (L-1)){
						links[i] = new Link(i,widthOfLastLink,lengthOfLinks);
					} else {
						links[i] = new Link(i,widthOfFirstLinks,lengthOfLinks);
					}
					linksMap.put(links[i].id, links[i]);
				}
				T = TOnToy;
				if(!baseDir.contains("ToyNetwork")){
					baseDir += "/ToyNetwork";
				}
			} else {
				for(int counter = 0; counter < L; counter++){
					links[counter] = new Link(counter,random3.nextInt(2)+1, random3.nextDouble()*390d + 10d);
				}
			}

			sourceLink = new Link(-1,1,0);
			linksMap.put(sourceLink.id,sourceLink);

			if(notificationBased){
				notificationArray = (HashMap<Integer, Double>[]) Array.newInstance(HashMap.class, (int) T+1);
				for(int i = 0; i < T+1; i++){
					notificationArray[i] = new HashMap<Integer, Double>();
				}
			}

			// Initialising random number generators;
			Random random = new Random(seed);
			Random random2 = new Random(seed+9);
			for(int i = 0; i<100; i++){
				random.nextDouble();
				random2.nextDouble();
			}

			cyclists.clear();
			for(int n = 0; n < N; n++){
				double w = -1;
				while( w < minimumAllowedSpeed){
					double u = random.nextDouble();
					if(useJohnsonDistribution){
						w = JohnsonLambda * Math.sinh( (qNorm(u) - JohnsonGamma) / JohnsonDelta) + JohnsonXsi;
					} else if(useLogisticDistribution){
						w = mu-Math.log(1/u-1)*s ;
					} else {
						//Weibull
						w = lambda*Math.pow((-Math.log(1d-u)),(1d/k));
					}

					Random r = new Random();
					r.nextGaussian();
				}

				double time = 0;
				if(!circuit){
					time = random2.nextDouble()*T;
				} else {
					time = random2.nextDouble()*0.01;
				}
				/*	LinkedList<Link> defaultRoute = new LinkedList();
				for(int i = 0; i < L; i++){
					defaultRoute.addLast(links[i]);
				} */
				int nL = random3.nextInt(290)+10;
				LinkedList<Link> defaultRoute = new LinkedList<Link>();
				if(useToyNetwork){
					for( int i = 0; i < L; i++){
						defaultRoute.addLast(links[i]);
					}
				} else {
					for(int i = 0; i < nL; i++){
						defaultRoute.addLast(links[random.nextInt(L)]);
					}
				}
				Cyclist cyclist = ltm.createCyclist(n, w, defaultRoute, ltm);
				cyclists.add(cyclist);

				sourceLink.outQ.add(new CyclistQObject(time, cyclist));	
				if(notificationBased){
					cyclist.sendNotification(sourceLink.id, time);
				}
			}

			System.out.println("1st part (Initialisation) finished after " + (System.currentTimeMillis()-startTime)/1000d + " seconds.");

			if(notificationBased){
				for( ;t < T; t += tau){
					int i = (int) (t / tau);
					if( Math.round(t) % 3600 == 0 && t > 0){
						System.out.println("   " + ((int) t / 3600) + " hours simulated.");
					}
					shortTermPriorityQueue = (PriorityQueue<LinkQObject>) priorityQueueClassForLinks.newInstance();
					for(Integer linkId : notificationArray[i].keySet()){
						Link link = linksMap.get(linkId);
						// It is fully possible that tReady > tNotificationArray. This could happen if tReady is increased after the notification,
						// e.g. due to the nextLink being fully occupied forcing tReady to be increased to the Q-time of the following link.
						// Can the opposite be true? Yes! initially tReady is not even defined yet, and will only be once congestion occurs.

						// This is a little weird, but since -1 is the null value of Double, an empty set will contain the key -1 but with a null value.
						if(notificationArray[i].get(linkId) != null){
							double maxTime = Math.max(link.tReady, notificationArray[i].get(linkId));
							link.tReady = maxTime;
							linksMap.replace(linkId, link);
							if( maxTime > t && notificationArray.length > i+1){
								notificationArray[i+1].put(linkId, notificationArray[i+1].get(linkId));
							} else {
								// This ensures that the LQO constructed will actually use the correct time (maxTime)
								shortTermPriorityQueue.add(new LinkQObject(maxTime, link.id));
							}
						}
					}
					while(!shortTermPriorityQueue.isEmpty()){
						LinkQObject loq = shortTermPriorityQueue.poll();
						linksMap.get(loq.linkId).handleQOnNotification(loq);
					}
					for(int i_l = 0; i_l < L; i_l++){
						Link link = links[i_l];
						link.densityReport.addLast(link.outQ.size() / (link.length * link.Psi / 1000d) );
					}
				}
			} else {
				/*
				for(;t < T; t += tau){
					sourceLink.handleQ();
					for(Link link : links){
						link.handleQ();
						//if(maxCyclistsPerTau > 0){
						//	link.setSpeedToZeroForStuckCyclists();
						//}
					}
				}
				 */

			}
			System.out.println("2nd part (Mobility Simul) finished after " + (System.currentTimeMillis()-startTime)/1000d + " seconds.");
			if(reportSpeeds){			
				exportCyclistSpeeds(baseDir + "/Cyclists/" + (int) lengthOfLinks, itN);
				exportCyclistCruisingSpeeds(baseDir + "/Cyclists/DesiredSpeeds");


				for(Link link : links){
					link.exportSpeeds(baseDir + "/Links/" + (int) lengthOfLinks);
					link.exportDensities(baseDir + "/Links/" + (int) lengthOfLinks);
					link.exportFlows(baseDir + "/Links/" + (int) lengthOfLinks);
					link.exportSpeedTimes(baseDir + "/Links/" + (int) lengthOfLinks);
					link.exportOutputTimes(baseDir + "/Links/" + (int) lengthOfLinks);
				}
			}


			System.out.println("3rd part (Xporting stuff) finished after " + (System.currentTimeMillis()-startTime)/1000d + " seconds.");
			System.out.println((System.currentTimeMillis()-startTime)/((double) N) +
					" miliseconds per cyclist.\n");

		}
	}


	public static void exportCyclistCruisingSpeeds(String baseDir) throws IOException{
		FileWriter writer = new FileWriter(baseDir + "/CyclistCruisingSpeeds_" + Runner.ltm.getClass().getName() + "_" 
				+ Runner.N + "Persons_" + Runner.circuitString + ".csv");
		writer.append("CyclistId;CruisingSpeed\n");
		for(Cyclist cyclist : cyclists){
			writer.append(cyclist.id + ";"  + cyclist.desiredSpeed + "\n");
		}
		writer.flush();
		writer.close();
	}

	public static void exportCyclistSpeeds(String baseDir, int itN) throws IOException{
		FileWriter writer = new FileWriter(baseDir + "/CyclistSpeeds_" + Runner.ltm.getClass().getName() + "_" 
				+ itN + "Persons_" + Runner.circuitString + ".csv");
		
		System.out.println(baseDir + "/CyclistSpeeds_" + Runner.ltm.getClass().getName() + "_" 
				+ itN + "Persons_" + Runner.circuitString + ".csv");
		writer.append("CyclistId;LinkId;Time;Speed\n");
		for(Cyclist cyclist : cyclists){
			for(Double[] reportElement : cyclist.speedReport){
				if(reportElement[0] == 0 || reportElement[2] > 0){  //On all real links, the speed has to be positive.
					writer.append(cyclist.id + ";"  + reportElement[0] + ";" + reportElement[1] + ";" + reportElement[2] + "\n");
				}
			}
		}
		writer.flush();
		writer.close();
	}

	public static double qNorm(double u){
		double a1 = -3.969683028665376e+01;
		double a2 =  2.209460984245205e+02;
		double a3 = -2.759285104469687e+02;
		double a4 =  1.383577518672690e+02;
		double a5 = -3.066479806614716e+01;
		double a6 =  2.506628277459239e+00;

		double b1 = -5.447609879822406e+01;
		double b2 =  1.615858368580409e+02;
		double b3 = -1.556989798598866e+02;
		double b4 =  6.680131188771972e+01;
		double b5 = -1.328068155288572e+01;

		double c1 = -7.784894002430293e-03;
		double c2 = -3.223964580411365e-01;
		double c3 = -2.400758277161838e+00;
		double c4 = -2.549732539343734e+00;
		double c5 =  4.374664141464968e+00;
		double c6 =  2.938163982698783e+00;

		double d1 =  7.784695709041462e-03;
		double d2 =  3.224671290700398e-01;
		double d3 =  2.445134137142996e+00;
		double d4 =  3.754408661907416e+00;

		double p_low  = 0.02425;
		double p_high = 1 - p_low;

		if(u < p_low){
			double q = Math.sqrt(-2*Math.log(u));
			return (((((c1*q+c2)*q+c3)*q+c4)*q+c5)*q+c6) /
					((((d1*q+d2)*q+d3)*q+d4)*q+1);
		}

		if(u > p_high){
			double q = Math.sqrt(-2*Math.log(1-u));
			return -(((((c1*q+c2)*q+c3)*q+c4)*q+c5)*q+c6) /
					((((d1*q+d2)*q+d3)*q+d4)*q+1);
		}

		double q = u - 0.5;
		double r = q * q;
		return (((((a1*r+a2)*r+a3)*r+a4)*r+a5)*r+a6)*q /
				(((((b1*r+b2)*r+b3)*r+b4)*r+b5)*r+1);


	}
}