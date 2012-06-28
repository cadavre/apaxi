
package pro.jazzy.paxi.entity;

public class Payment implements RoadEvent {

    static long idCounter = 0;

    long id;

    int distance;

    float amount;

    private static Payment emptyInstance = new Payment(0);

    public Payment(float amount) {

        this.id = idCounter;
        idCounter++;
        this.amount = amount;
    }

    public long getId() {

        return id;
    }

    private void setId(long id) {

        this.id = id;
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

    static public Payment getInstance(long id) {

        emptyInstance.setId(id);
        return emptyInstance;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
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
        if (id != other.id)
            return false;
        return true;
    }

}
