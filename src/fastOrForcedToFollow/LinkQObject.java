package fastOrForcedToFollow;

public class LinkQObject implements Comparable<LinkQObject>{

	public double time;
	public int linkId;
	public double tieBreaker;

	LinkQObject(double time, int linkId){
		this.time = time;
		this.linkId = linkId;
		this.tieBreaker = Runner.tieBreaker;
		Runner.tieBreaker = Math.nextUp(Runner.tieBreaker);
	}

	public int compareTo(LinkQObject other){
		if(this.time != other.time){
			return Double.compare(this.time, other.time);
		} else {
			return Double.compare(this.tieBreaker, other.tieBreaker);
		}
	}

	public int compare(LinkQObject cqo1, LinkQObject cqo2){
		if(cqo1.time != cqo2.time){
			return Double.compare(cqo1.time, cqo2.time);
		} else {
			return Double.compare(cqo1.tieBreaker, cqo2.tieBreaker);
		}
	}

}
