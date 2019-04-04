package fastOrForcedToFollow;

import java.io.FileWriter;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.Random;

import org.apache.commons.math3.distribution.NormalDistribution;


/**
 * @author mpaulsen
 *
 */
public class Runner {

	/*
	 * Network parameters
	 */
	/**
	 * The width (in metres) of the last link in the link series.
	 */
	public static final double WIDTH_OF_LAST_LINK = 2;


	/**
	 * The width (in metres) of all links but the last link in the link series.
	 */
	public static final double WIDTH_OF_FIRST_LINKS = 3;

	/**
	 * The length of each link in the link series. If non-positive then iterating through (20,50,100,200,500)
	 */
	public static double LENGTH_OF_LINKS = -1;

	/**
	 * The numbers of links in serial used in the simulation.
	 */
	public static final int L = 3;

	/**
	 * A capacity multiplier that can be used to increase the total lane length, see {@link Link#getTotalLaneLength()}.
	 */
	public static final double CAPACITY_MULTIPLIER = 1;  // The totalLaneLength is multiplied with this number;


	/*
	 *  Pseudolane partition parameters
	 */
	/**
	 * Additional width needed to gain another efficient lane. Based on Buch & Greibe (2015).
	 */
	public static final double OMEGA = 1.25; 

	/**
	 * Dead horizontal space on bicycle paths that is unused. Based on Buch & Greibe (2015).
	 */
	public static final double DEAD_SPACE = 0.4;


	/*
	 * Simulation parameters
	 */
	/**
	 * The base directory for storing output.
	 */
	//public static String baseDir = "Z:/git/fastOrForcedToFollow/output/ToyNetwork";
	public static String baseDir = "C:/Users/madsp/git/FFF_Standalone/output/ToyNetwork";

	/**
	 * The span of the simulation period (in seconds).
	 */
	public static final double T = 3600;

	/**
	 * The timestep used in the simulation.
	 */
	public static final double TIMESTEP = 1d;

	/**
	 * Maximum number of cyclist to enter the system during the simulation.
	 */
	public static final int MAX_N = 10000;

	/**
	 * The number of cyclists subtracted from the previous simulation until reaching 0 cyclists.
	 */
	public static final int CYCLIST_STEPSIZE = 50;

	/**
	 * The random seed used for the the population.
	 */
	public static final int SEED = 5355633;
	//As a backup: 5355633;

	/**
	 * Whether or not to report speeds (takes the majority of the time, but results cannot be analysed without).
	 */
	public static final boolean reportSpeeds = true;

	/**
	 * The type of Q used for the priorityQ for links. Can be PriorityQueue.class or FibonacciHeap.class.
	 */
	@SuppressWarnings("rawtypes")
	public static final Class<? extends PriorityQueue> PRIORITY_QUEUE_CLASS_FOR_LINKS = PriorityQueue.class; 


	/*
	 * Desired speed distribution parameters -- General
	 */
	/**
	 * The minimum allowed desired speed (lower bound for truncation).
	 */
	private static final double MINIMUM_ALLOWED_DESIRED_SPEED = 2;  // Lowest allowed desired speed (lower truncation of distribution);


	private static final double DESIRED_SPEED_MULTIPLIER = 1.; // 0.95 
	static final double HEADWAY_MULTIPLIER = 1.; // 1.6  


	static final boolean ASSUMING_HOMOGENEITY = false;
	
	/**
	 * The distribution used for desired speeds. Valid options are (so far) "JohnsonSU", "Logistic".
	 */
	enum SpeedDistribution { JOHNSON_SU, LOGISTIC };
	private static final SpeedDistribution DESIRED_SPEED_DISTRIBUTION = SpeedDistribution.JOHNSON_SU;


	/**
	 * The distribution used for headways.
	 */
	enum HeadwayDistribution { BETA, NORMAL, BOUNDED_NORMAL};
	static final  HeadwayDistribution HEADWAY_DISTRIBUTION = HeadwayDistribution.BETA;

	/*
	 * Desired speed distribution parameters -- Johnson SU   ,  see https://en.wikipedia.org/wiki/Johnson's_SU-distribution
	 */
	/**
	 * One of the parameters for Johnson's SU-distribution, as estimated based on data from COWI.
	 */
	private static final double JOHNSON_GAMMA = -2.745957257392118;

	/**
	 * One of the parameters for Johnson's SU-distribution, as estimated based on data from COWI.
	 */
	private static final double JOHNSON_XSI = 3.674350833333333;

	/**
	 * One of the parameters for Johnson's SU-distribution, as estimated based on data from COWI.
	 */
	private static final double JOHNSON_DELTA = 4.068155531972158;

	/**
	 * One of the parameters for Johnson's SU-distribution, as estimated based on data from COWI.
	 */
	private static final double JOHNSON_LAMBDA = 3.494609779450189;

	/**
	 * The space mean speed [m/s] for desired speed used in certain tests
	 */
	private static final double SPACE_MEAN_SPEED = 6.1042421441361157;




	/*
	 * Desired speed distribution parameters -- Logistic
	 */
	/**
	 * Mean value of the logistic distribution estimated based on data from COWI.
	 */
	public static final double LOGISTIC_MU = 6.085984;

	/**
	 * Scale value of the logistic distribution estimated based on data from COWI.
	 */
	public static final double LOGISTIC_S = 0.610593; 


	/*
	 * Safety distance parameters
	 */
	/**
	 * Constant term in the square root model for safety distance.
	 */
	//ORIGINAL public static final double THETA_0 = -4.220641337789;
	public static final double THETA_0 = -4.233561639232980;
	public static final double BOUNDED_THETA_0 = -4.4096047388353892;
	public static final double BETA_THETA_0 = -4.3570197635081049;

	/**
	 * Square root term in the square root model for safety distance.
	 */
	//ORIGINAL public static final double THETA_1 =  4.602161217943;
	public static final double THETA_1 =  4.601916313111973;
	public static final double BOUNDED_THETA_1 =  4.7412143565284097;
	public static final double BETA_THETA_1 =  4.7128195220959830;

	/**
	 * Constant term in the square root model for standard deviation of safety distance.
	 */
	//ORIGINAL public static final double ZETA_0 =  -4.3975231775567600;
	public static final double ZETA_0 =  -4.397399476679904;
	public static final double BOUNDED_ZETA_0 =  -22.5034590560309766;
	public static final double BETA_ZETA_0 =  -9.6741585958330667;

	/**
	 * Square root term in the square root model for standard deviation of safety distance.
	 */
	//ORIGINAL public static final double ZETA_1 =  3.1095184592753986;
	public static final double ZETA_1 =  3.109430989546536;
	public static final double BOUNDED_ZETA_1 =   15.9123484986733263;
	public static final double BETA_ZETA_1 =   6.8406631453876896;


	public static final double BOUNDED_ETA = 0.4379727849018566;
	public static double rejectionSamplingDenominator = 1.-Math.exp(-BOUNDED_ETA*BOUNDED_ETA/2.);;

	public static final double BETA_ALPHA =   1.8647308191839824;


	/**
	 * Average length of a bicycle according to Andresen et al. (2014),
	 * Basic Driving Dynamics of Cyclists, In: Simulation of Urban Mobility;
	 */
	public static final double LAMBDA_c = 1.73; 



	/*
	 * Required fields (used by other classes) with no input values.
	 */
	/**
	 * The simulation clock.
	 */
	public static double t;

	/**
	 * An array containing all the links.
	 */
	public static Link[] links;

	/** 
	 * The sourceLink where cyclist stay until they enter the system.
	 */
	public static Link sourceLink;

	/**
	 * The current number of cyclists being simulated.
	 */
	public static int N;

	/**
	 * The map containing all links.
	 */
	public static LinkedHashMap<Integer,Link> linksMap; 

	/**
	 * A linked list containing all cyclists.
	 */
	public static LinkedList<Cyclist> cyclists;

	/**
	 * The notificationArray that for every timeslot of the day hold the minimum event time for all links.
	 */
	public static HashMap<Integer, NotificationArrayObject>[] notificationArray;

	/**
	 * The short term priority queue that for every ??????//TODO
	 */
	public static PriorityQueue<LinkQObject> shortTermPriorityQueue;

	/**
	 * A tie breaker maintained in <code>Runner</code> that is passed on to Q-object in order to ensure correct ordering.
	 */
	public static long tieBreaker = Long.MIN_VALUE;


	public static double samplingScalingBeta;
	public static double BETA_h;




	public static void main(String[] args) throws IOException, InterruptedException, InstantiationException, IllegalAccessException{
		boolean stopAfterOne =  LENGTH_OF_LINKS <= 0 ? false : true;

		System.out.println("Headway distribution is " + HEADWAY_DISTRIBUTION);

		//for(double d : new double[]{20,50,100,200,500}){
			for(double d : new double[]{1,5}){
				
			//for(double d : new double[]{100}){

			if(!stopAfterOne){
				LENGTH_OF_LINKS = d;
			}
			for( N = MAX_N; N >= CYCLIST_STEPSIZE; N -= CYCLIST_STEPSIZE){
				double startTime = System.nanoTime();
				System.out.println("Start of a simulation with " + N + " cyclists and " + L + " links.");
				simulationPreparation();
				System.out.println("1st part (Initialisation) finished after " + (System.nanoTime()-startTime)/1000d + " seconds.");
				simulation();
				System.out.println("2nd part (Mobility Simul) finished after " + (System.nanoTime()-startTime)/1000d + " seconds.");
				double intermediateTime = System.nanoTime();
				if(reportSpeeds){	
					exportSpeeds();
				}
				System.out.println("3rd part (Xporting stuff) finished after " + (System.nanoTime()-startTime)/1000d + " seconds.");
				System.out.format(Locale.US, "%.3f (%.3f excluding export) microseconds per cyclist-link-interaction in total.%n%n", 
						(System.nanoTime()-startTime)/((double) N)/((double) L)/1000d, 
						(intermediateTime-startTime)/((double) N)/((double) L)/1000d);
			}
			if(stopAfterOne){
				break;
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static void generalPreparation() throws InstantiationException, IllegalAccessException{
		t =0;
		notificationArray = (HashMap<Integer, NotificationArrayObject>[]) Array.newInstance(HashMap.class, (int) T+1);
		shortTermPriorityQueue = (PriorityQueue<LinkQObject>) PRIORITY_QUEUE_CLASS_FOR_LINKS.newInstance();
		for(int i = 0; i < T+1; i++){
			notificationArray[i] = new HashMap<Integer, NotificationArrayObject>();
		}
		tieBreaker = Long.MIN_VALUE;

	}

	public static void simulationPreparation() throws InstantiationException, IllegalAccessException{
		generalPreparation();
		networkPreparation();
		populationPreparation();
	}


	/**
	 * Creates the network including a {@link Runner.sourceLink}.
	 * 
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static void networkPreparation() throws InstantiationException, IllegalAccessException{
		links = new Link[L];
		linksMap = new LinkedHashMap<Integer,Link>();
		for(int i = 0; i < L; i++){
			if(i == (L-1)){
				links[i] = new Link(i,WIDTH_OF_LAST_LINK,LENGTH_OF_LINKS);
			} else {
				links[i] = new Link(i,WIDTH_OF_FIRST_LINKS,LENGTH_OF_LINKS);
			}
			linksMap.put(links[i].getId(), links[i]);
		}
		sourceLink = new Link(-1,1,0);
		linksMap.put(sourceLink.getId(),sourceLink);
	}



	/**
	 * Method preparing the cyclist population by assigning a desired speed,
	 * a link transmission model and an initial arrival time to the system
	 * for every cyclist in the population. The desired speed is based on
	 * the distribution chosen in {@link Runner#DESIRED_SPEED_DISTRIBUTION}.
	 * 
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static void populationPreparation() throws InstantiationException, IllegalAccessException{
		Random desiredSpeedRandom = new Random(SEED);
		Random arrivalTimeRandom = new Random(SEED+9);
		Random headwayRandom = new Random(SEED + 341);
		for(int i = 0; i<100; i++){ //Throwing away the first 100 draws;
			desiredSpeedRandom.nextDouble();
			arrivalTimeRandom.nextDouble();	
		}

		NormalDistribution normalDistribution = new NormalDistribution();
		cyclists= new LinkedList<Cyclist>();
		for(int id = 0; id < N; id++){
			double speed = -1;
			while( speed < MINIMUM_ALLOWED_DESIRED_SPEED){
				double u = desiredSpeedRandom.nextDouble();
				switch(DESIRED_SPEED_DISTRIBUTION){
				case JOHNSON_SU:
					speed = JOHNSON_LAMBDA * Math.sinh( (normalDistribution.inverseCumulativeProbability(u) - JOHNSON_GAMMA) / JOHNSON_DELTA) + JOHNSON_XSI;
					break;
				case LOGISTIC:
					speed = LOGISTIC_MU - Math.log(1/u-1) * LOGISTIC_S;
					break;
				default: 
					throw new IllegalArgumentException("Valid distributions are: JohnsonSU, Logistic, Homogeneous");
				}
				if(ASSUMING_HOMOGENEITY){
					speed = SPACE_MEAN_SPEED;
				}
				speed = DESIRED_SPEED_MULTIPLIER * speed;
			}
			double time = arrivalTimeRandom.nextDouble()*T;
			LinkedList<Link> defaultRoute = new LinkedList<Link>(); // At the moment all routes are equal.
			for( int i = 0; i < L; i++){
				defaultRoute.addLast(links[i]);
			}

			double z_c = 0;
			switch(HEADWAY_DISTRIBUTION){
			case BETA:
				while(true){
					z_c = headwayRandom.nextDouble();
					double u = headwayRandom.nextDouble();
					double p = Math.pow(4*z_c*(1-z_c), BETA_ALPHA-1);
					if(u < p){
						break;
					}
				}
				break;
			case BOUNDED_NORMAL:
				while(true){
					z_c = headwayRandom.nextGaussian();
					if(z_c > -BOUNDED_ETA && z_c < BOUNDED_ETA){
						double u = headwayRandom.nextDouble();
						double p = (1. - Math.exp((z_c*z_c - BOUNDED_ETA*BOUNDED_ETA)))/rejectionSamplingDenominator;
						if(u < p){
							break;
						}
					}
				}
				break;
			case NORMAL:
				z_c = headwayRandom.nextGaussian();
				break;
			default:
				throw new IllegalArgumentException("Valid distributions are: Beta, Bounded Normal, Normal or Homogeneous");
			}

			if(ASSUMING_HOMOGENEITY){
				if(HEADWAY_DISTRIBUTION == HeadwayDistribution.BETA){
					z_c = 0.5;
				} else {
					z_c = 0;
				}
			}
			
			Cyclist cyclist = new Cyclist(id, speed, z_c, defaultRoute);
			cyclists.add(cyclist);
			sourceLink.getOutQ().add(new CyclistQObject(time, cyclist));	
			sourceLink.sendNotification(time);
		}
	}


	/**
	 * The method including the actual simulation. 
	 * 
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IOException
	 */
	public static void simulation() throws InstantiationException, IllegalAccessException, IOException{
		for( ;t < T; t += TIMESTEP){
			int timeSlot = (int) (t / TIMESTEP);
			//Initial check to see if the times in the notification array are still valid - correcting them if not, and 
			// adding them to the short term priority queue if they are valid.

			for(int linkId : notificationArray[timeSlot].keySet()){
				Link link = linksMap.get(linkId);
				// It is fully possible that tReady > tNotificationArray. This could happen if tWakeUp is increased after the notification,
				// e.g. due to the nextLink being fully occupied forcing tWakeUp to be increased to the Q-time of the following link.
				// Can the opposite be true? Yes! initially tWakeUp is not even defined yet, and will only be once congestion occurs.

				// If the notification array has a non-null value.
				if(notificationArray[timeSlot].get(linkId) != null){ 
					//The time in the notification array might not be relevant anymore, if the next link "sleeps", i.e. is full.
					double maxTime = Math.max(link.getWakeUpTime(), notificationArray[timeSlot].get(linkId).time);
					link.setWakeUpTime(maxTime);
					//If maxTime still belongs to this time slot, and the notification array has an additional time slot, then
					// replace the current value in this slot by this max value. 
					if( maxTime > t && notificationArray.length > timeSlot+1){
						notificationArray[timeSlot+1].put(linkId, notificationArray[timeSlot+1].get(linkId));
					} else {
						//Otherwise, create an entry in the shortTermPriorityQueue, using the maxTime.
						shortTermPriorityQueue.add(new LinkQObject(maxTime, link.getId(),
								notificationArray[timeSlot].get(linkId).tieBreaker));
					}
				}
			}

			// Keep processing the short term priority queue as long as it has elements.
			while(!shortTermPriorityQueue.isEmpty()){
				LinkQObject loq = shortTermPriorityQueue.poll();
				linksMap.get(loq.getId()).handleQOnNotification(loq.getTime());
			}

			//This step - which takes a lot of time if L is large, can be ommited if not reporting densities. 
			if(Runner.reportSpeeds){
				for(int i_l = 0; i_l < L; i_l++){
					Link link = links[i_l];
					link.getDensityReport().
					addLast(link.getOutQ().size() / (link.getLength() * link.getNumberOfPseudoLanes() / 1000d) );
				}
			}
		}
	}

	/**
	 * Export the speeds of links and cyclists from the simulation using <code>itN</code> cyclists.
	 * 
	 * @param itN The number of cyclists used in the simulation to be reported.
	 * 
	 * @throws IOException
	 */
	public static void exportSpeeds() throws IOException{
		exportCyclistSpeeds(baseDir + "/Cyclists/" + (int) LENGTH_OF_LINKS);
		exportCyclistDesiredSpeeds(baseDir + "/Cyclists/DesiredSpeeds");

		for(Link link : links){
			link.exportSpeeds(baseDir + "/Links/" + (int) LENGTH_OF_LINKS);
			link.exportDensities(baseDir + "/Links/" + (int) LENGTH_OF_LINKS);
			link.exportFlows(baseDir + "/Links/" + (int) LENGTH_OF_LINKS);
			link.exportSpeedTimes(baseDir + "/Links/" + (int) LENGTH_OF_LINKS);
			link.exportOutputTimes(baseDir + "/Links/" + (int) LENGTH_OF_LINKS);
		}
	}



	/**
	 * Exporting desired speeds to subfolders within the baseDir.
	 * 
	 * @param baseDir The base directory into which the output is stored.
	 * 
	 * @throws IOException
	 */
	public static void exportCyclistDesiredSpeeds(String baseDir) throws IOException{
		ToolBox.createFolderIfNeeded(baseDir);
		FileWriter writer = new FileWriter(baseDir + "/CyclistCruisingSpeeds_" 	+ Runner.N + "Persons.csv");
		writer.append("CyclistId;CruisingSpeed\n");
		for(Cyclist cyclist : cyclists){
			writer.append(cyclist.getId() + ";"  + cyclist.getDesiredSpeed() + "\n");
		}
		writer.flush();
		writer.close();
	}

	/**
	 * Exporting cyclist speeds to subfolders within the baseDir.
	 * 
	 * @param baseDir The base directory into which the output is stored.
	 * 
	 * @throws IOException
	 */
	public static void exportCyclistSpeeds(String baseDir) throws IOException{
		ToolBox.createFolderIfNeeded(baseDir);
		FileWriter writer = new FileWriter(baseDir + "/CyclistSpeeds_" + N + "Persons.csv");

		System.out.println(baseDir + "/CyclistSpeeds_" + N + "Persons.csv");
		writer.append("CyclistId;LinkId;Time;Speed\n");
		for(Cyclist cyclist : cyclists){
			for(Double[] reportElement : cyclist.getSpeedReport()){
				if(reportElement[0] == 0 || reportElement[2] > 0){  //On all real links, the speed has to be positive.
					writer.append(cyclist.getId() + ";"  + reportElement[0] + ";" + reportElement[1] + ";" + reportElement[2] + "\n");
				}
			}
		}
		writer.flush();
		writer.close();
	}


}
