public class Member {
    private final int memberId;
    private String name;
    private String email;
    private final String membershipId;
    private boolean notificationsEnabled;

    public Member(int memberId, String name, String email,
                  String membershipId, boolean notificationsEnabled) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.membershipId = membershipId;
        this.notificationsEnabled = notificationsEnabled;
    }

    public int getMemberId() { return memberId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getMembershipId() { return membershipId; }
    public boolean isNotificationsEnabled() { return notificationsEnabled; }

    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setNotificationsEnabled(boolean v) { this.notificationsEnabled = v; }

    @Override
    public String toString() {
        return String.format("%-5d %-20s %-30s %-15s %-12s",
                memberId, Book.cut(name, 20), Book.cut(email, 30), membershipId,
                notificationsEnabled ? "ON" : "OFF");
    }
}
