package DBUsage;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "banned_names")
@Data
public class BannedName {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String bannedName;

    @Override
    public String toString() {
        return bannedName;
    }
}
