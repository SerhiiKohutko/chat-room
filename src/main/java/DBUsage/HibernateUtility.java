package DBUsage;

import lombok.Getter;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtility {
    @Getter
    private static SessionFactory sessionFactory;

    static {
        try {

            sessionFactory = new Configuration().configure("hibernate.cfg.xml")
                    .addAnnotatedClass(BannedIp.class)
                    .addAnnotatedClass(BannedName.class)
                    .buildSessionFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void shutdown() {
        getSessionFactory().close();
    }
}
