package pro.jazzy.paxi.entity;

public class Payment implements RoadEvent {

	int distance;

	float amount;

	public Payment(float amount) {

		this.amount = amount;
	}

	@Override
	public int getDistance() {

		return distance;
	}

	@Override
	public void setDistance(int distance) {

		this.distance = distance;
	}

	public float getAmount() {

		return amount;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(amount);
		result = prime * result + distance;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Payment other = (Payment) obj;
		if (Float.floatToIntBits(amount) != Float.floatToIntBits(other.amount))
			return false;
		if (distance != other.distance)
			return false;
		return true;
	}

}
