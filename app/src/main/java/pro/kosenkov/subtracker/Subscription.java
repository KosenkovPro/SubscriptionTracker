package pro.kosenkov.subtracker;

public class Subscription {
    private long id;
    private String name;
    private long startDate;
    private long trialEndDate;
    private double price;
    private String billingCycle; // "monthly" or "yearly"

    public Subscription() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getStartDate() { return startDate; }
    public void setStartDate(long startDate) { this.startDate = startDate; }

    public long getTrialEndDate() { return trialEndDate; }
    public void setTrialEndDate(long trialEndDate) { this.trialEndDate = trialEndDate; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getBillingCycle() { return billingCycle; }
    public void setBillingCycle(String billingCycle) { this.billingCycle = billingCycle; }

    public int getDaysRemaining() {
        long diff = trialEndDate - System.currentTimeMillis();
        if (diff <= 0) return 0;
        return (int) (diff / (1000L * 60 * 60 * 24));
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= trialEndDate;
    }

    public String getPriceLabel() {
        String cycle = "yearly".equals(billingCycle) ? "/год" : "/мес";
        return String.format("%.0f ₽%s", price, cycle);
    }
}
