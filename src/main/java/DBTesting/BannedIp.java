package DBTesting;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "banned_ips")
public class BannedIp {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String ip;
}
