import net.java.ao.Entity;
import net.java.ao.OneToOne;
import net.java.ao.schema.Table;

@Table("event_queues")
public interface EventQueue extends Entity {
    public UserAttribute getUserAttribute();
    public void setUserAttribute(UserAttribute attr);
}
