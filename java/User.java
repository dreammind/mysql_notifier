import net.java.ao.Entity;
import net.java.ao.OneToMany;
import net.java.ao.schema.Table;

@Table("users")
public interface User extends Entity {
    public int getId();
    public void setId(int id);

    public String getName();
    public void setName(String name);

    @OneToMany
    public UserAttribute[] getUserAttribute();
}
