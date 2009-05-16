import net.java.ao.Entity;
import net.java.ao.OneToOne;
import net.java.ao.schema.Table;

@Table("user_attributes")
public interface UserAttribute extends Entity {
    public int getId();
    public void setId(int id);

    public String getK();
    public void setK(String k);

    public String getV();
    public void setV(String v);

    public User getUser();
    public void setUser(User user);
}
